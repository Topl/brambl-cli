package co.topl.brambl.cli

import cats.effect.{ExitCode, IO}
import munit.CatsEffectSuite

import java.nio.file.{Files, Path, Paths}
import scala.concurrent.duration.Duration

import cats.effect.kernel.{Resource, Sync}

import java.io.FileInputStream

class WalletRecoveryTest
    extends CatsEffectSuite
    with WalletConstants
    with CommonTxOperations {

  val tmpDirectory = FunFixture[Path](
    setup = { _ =>
      val tmpDir = Paths.get(TMP_DIR).toFile()
      if (tmpDir.exists()) {
        Paths.get(TMP_DIR).toFile().listFiles().map(_.delete()).mkString("\n")
        Files.deleteIfExists(Paths.get(TMP_DIR))
      }
      Files.createDirectory(Paths.get(TMP_DIR))
    },
    teardown = { _ => () }
  )

  override val munitTimeout = Duration(180, "s")

  tmpDirectory.test("Initialize wallet and Move funds from genesis") { _ =>
    import scala.concurrent.duration._
    assertIO(
      for {
        _ <- createWallet().run(walletContext)
        _ <- IO.asyncForIO.timeout(
          (for {
            _ <- IO.println("Querying genesis to start")
            queryRes <- queryAccount("nofellowship", "genesis", Some(1))
              .run(walletContext)
            _ <- IO.sleep(5.seconds)
          } yield queryRes)
            .iterateUntil(_ == ExitCode.Success),
          240.seconds
        )
        next_address <- walletController(WALLET).currentaddress("self", "default", None)
        _ <- IO.println(s"Next address is $next_address")
        _ <- IO.println("Moving funds from genesis")
        _ <- assertIO(
          createSimpleTransactionToAddress(
            "nofellowship",
            "genesis",
            Some(1),
            Some("nofellowship"),
            Some("genesis"),
            Some(1),
            next_address.get,
            BASE_AMOUNT,
            BASE_FEE,
            WALLET_FIRST_TX_RAW,
            TokenType.lvl,
            None,
            None
          ).run(walletContext),
          ExitCode.Success
        )
        _ <- assertIO(
          proveSimpleTransaction(
            WALLET_FIRST_TX_RAW,
            WALLET_FIRST_TX_PROVED
          ).run(walletContext),
          ExitCode.Success
        )
        _ <- assertIO(
          broadcastSimpleTx(WALLET_FIRST_TX_PROVED),
          ExitCode.Success
        )
        _ <- IO.println("Query Account")
        res <- IO.asyncForIO.timeout(
          (for {
            queryRes <- queryAccount("self", "default").run(walletContext)
            _ <- IO.sleep(5.seconds)
          } yield queryRes)
            .iterateUntil(_ == ExitCode.Success),
          240.seconds
        )
      } yield res,
      ExitCode.Success
    )
  }

  private def extractMnemonic(mnemonicFile: String): IO[String] = Resource
    .make(
      Sync[IO]
        .delay(
          new FileInputStream(mnemonicFile)
        )
    )(fis => Sync[IO].delay(fis.close()))
    .use { fis =>
      Sync[IO].blocking(new String(fis.readAllBytes))
    }

  test("Recover wallet key and Spend Existing Funds") {
    import scala.concurrent.duration._
    assertIO(
      for {
        _ <- IO.println("Recover wallet key")
        mnemonic <- extractMnemonic(WALLET_MNEMONIC)
        _ <- IO(Files.deleteIfExists(Paths.get(WALLET)))
        _ <- assertIO(
          recoverWallet(mnemonic).run(
            walletContext.copy(keyFile = WALLET_MAIN_KEY_RECOVERED)
          ),
          ExitCode.Success
        )
        next_address <- walletController(WALLET).currentaddress("self", "default", None)
        _ <- IO.println(s"Next address is $next_address")
        _ <- IO.println("Spend funds (500 LVLs) using new key")
        _ <- assertIO(
          createSimpleTransactionToAddress(
            "self",
            "default",
            None,
            None,
            None,
            None,
            next_address.get,
            500,
            BASE_FEE,
            WALLET_SECOND_TX_RAW,
            TokenType.lvl,
            None,
            None
          ).run(walletContext.copy(keyFile = WALLET_MAIN_KEY_RECOVERED)),
          ExitCode.Success
        )
        _ <- assertIO(
          proveSimpleTransaction(
            WALLET_SECOND_TX_RAW,
            WALLET_SECOND_TX_PROVED
          ).run(walletContext.copy(keyFile = WALLET_MAIN_KEY_RECOVERED)),
          ExitCode.Success
        )
        _ <- assertIO(
          broadcastSimpleTx(WALLET_SECOND_TX_PROVED),
          ExitCode.Success
        )
        _ <- IO.println("Query account")
        res <- IO.asyncForIO.timeout(
          (for {
            queryRes <- queryAccount("self", "default").run(
              walletContext.copy(keyFile = WALLET_MAIN_KEY_RECOVERED)
            )
            _ <- IO.sleep(5.seconds)
          } yield queryRes)
            .iterateUntil(_ == ExitCode.Success),
          240.seconds
        )
      } yield res,
      ExitCode.Success
    )
  }

}
