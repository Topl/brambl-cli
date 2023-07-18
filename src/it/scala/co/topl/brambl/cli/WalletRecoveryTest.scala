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
      Files.createDirectory(Paths.get("./tmp"))
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
            queryRes <- queryAccount("noparty", "genesis", Some(1))
              .run(walletContext)
            _ <- IO.sleep(5.seconds)
          } yield queryRes)
            .iterateUntil(_ == ExitCode.Success),
          60.seconds
        )
        next_address <- walletController(WALLET).currentaddress()
        _ <- IO.println(s"Next address is $next_address")
        _ <- IO.println("Moving funds from genesis")
        _ <- assertIO(
          createSimpleTransactionToAddress(
            "noparty",
            "genesis",
            Some(1),
            next_address.toOption.get,
            BASE_AMOUNT,
            WALLET_FIRST_TX_RAW
          ).run(walletContext),
          ExitCode.Success
        )
        _ <- IO.sleep(5.seconds)
        _ <- assertIO(
          proveSimpleTransaction(
            "noparty",
            "genesis",
            Some(1),
            WALLET_FIRST_TX_RAW,
            WALLET_FIRST_TX_PROVED
          ).run(walletContext),
          ExitCode.Success
        )
        _ <- IO.sleep(5.seconds)
        _ <- assertIO(
          broadcastSimpleTx(WALLET_FIRST_TX_PROVED, WALLET),
          ExitCode.Success
        )
        _ <- IO.println("Query Account")
        res <- IO.asyncForIO.timeout(
          (for {
            queryRes <- queryAccount("self", "default").run(walletContext)
            _ <- IO.sleep(5.seconds)
          } yield queryRes)
            .iterateUntil(_ == ExitCode.Success),
          60.seconds
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
        _ <- assertIO(recoverWallet(mnemonic).run(walletContext.copy(keyFile = WALLET_MAIN_KEY_RECOVERED)), ExitCode.Success)
        next_address <- walletController(WALLET).currentaddress()
        _ <- IO.println(s"Next address is $next_address")
        _ <- IO.println("Spend funds (500 LVLs) using new key")
        _ <- assertIO(
          createSimpleTransactionToAddress(
            "self",
            "default",
            None,
            next_address.toOption.get,
            500,
            WALLET_SECOND_TX_RAW
          ).run(walletContext.copy(keyFile = WALLET_MAIN_KEY_RECOVERED)),
          ExitCode.Success
        )
        _ <- IO.sleep(5.seconds)
        _ <- assertIO(
          proveSimpleTransaction(
            "self",
            "default",
            None,
            WALLET_SECOND_TX_RAW,
            WALLET_SECOND_TX_PROVED
          ).run(walletContext.copy(keyFile = WALLET_MAIN_KEY_RECOVERED)),
          ExitCode.Success
        )
        _ <- IO.sleep(5.seconds)
        _ <- assertIO(
          broadcastSimpleTx(WALLET_SECOND_TX_PROVED, WALLET),
          ExitCode.Success
        )
        _ <- IO.sleep(5.seconds)
        _ <- IO.println("Query account")
        res <- IO.asyncForIO.timeout(
          (for {
            queryRes <- queryAccount("self", "default").run(walletContext.copy(keyFile = WALLET_MAIN_KEY_RECOVERED))
            _ <- IO.sleep(5.seconds)
          } yield queryRes)
            .iterateUntil(_ == ExitCode.Success),
          60.seconds
        )
      } yield res,
      ExitCode.Success
    )
  }

}