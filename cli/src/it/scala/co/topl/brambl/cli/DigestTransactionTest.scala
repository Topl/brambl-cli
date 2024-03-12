package co.topl.brambl.cli

import cats.effect.ExitCode
import cats.effect.IO
import munit.CatsEffectSuite

import scala.concurrent.duration.Duration
import java.nio.file.Files
import java.nio.file.Paths

class DigestTransactionTest
    extends CatsEffectSuite
    with CommonFunctions
    with AliceConstants
    with CommonTxOperations
    with BobConstants {
  override val munitTimeout = Duration(180, "s")

  tmpDirectory.test("Move funds from genesis to alice") { _ =>
    assertIO(
      moveFundsFromGenesisToAlice(),
      ExitCode.Success
    )
  }

  test("Move funds from alice to digest locked account") {
    import scala.concurrent.duration._
    assertIO(
      for {
        _ <- IO.println("Create a wallet for bob")
        _ <- assertIO(createWallet().run(bobContext), ExitCode.Success)
        _ <- IO.println("Add bob's digest fellowship to bob's wallet")
        _ <- assertIO(
          addFellowshipToWallet("bob_digest_fellowship").run(bobContext),
          ExitCode.Success
        )
        _ <- IO.println("Add a template to bob's wallet")
        _ <- assertIO(
          addTemplateToWallet(
            "digest_template",
            "threshold(1, sign(0) and sha256(b39f7e1305cd9107ed9af824fcb0729ce9888bbb7f219cc0b6731332105675dc))"
          ).run(bobContext),
          ExitCode.Success
        )
        _ <- IO.println("Importing VK to bob's wallet")
        _ <- IO(Files.createFile(Paths.get(EMPTY_VK)))
        _ <- assertIO(
          importVk("bob_digest_fellowship", "digest_template", EMPTY_VK).run(
            bobContext
          ),
          ExitCode.Success
        )
        _ <- IO(Files.delete(Paths.get(EMPTY_VK)))
        BOB_DIGEST_ADDRESS <- walletController(BOB_WALLET)
          .currentaddress("bob_digest_fellowship", "digest_template", None)
        _ <- IO.println("Bob's digest address: " + BOB_DIGEST_ADDRESS)
        _ <- IO.println("Moving funds (500 LVLs) from alice to bob digest")
        _ <- assertIO(
          createSimpleTransactionToAddress(
            "self",
            "default",
            None,
            None,
            None,
            None,
            BOB_DIGEST_ADDRESS.get,
            600,
            BASE_FEE,
            ALICE_SECOND_TX_RAW,
            TokenType.lvl,
            None,
            None
          ).run(aliceContext),
          ExitCode.Success
        )
        _ <- assertIO(
          proveSimpleTransaction(
            ALICE_SECOND_TX_RAW,
            ALICE_SECOND_TX_PROVED
          ).run(aliceContext),
          ExitCode.Success
        )
        _ <- assertIO(
          broadcastSimpleTx(ALICE_SECOND_TX_PROVED),
          ExitCode.Success
        )
        _ <- IO.println(
          "Check digest account from bob's wallet, lvl tokens"
        )
        res <- IO.asyncForIO.timeout(
          (for {
            _ <- IO.println("Querying bob's digest")
            queryRes <- queryAccount("bob_digest_fellowship", "digest_template").run(bobContext)
            _ <- IO.sleep(5.seconds)
          } yield queryRes)
            .iterateUntil(_ == ExitCode.Success),
          120.seconds
        )
      } yield res,
      ExitCode.Success
    )
  }
  test("Move funds from digest locked account to bob's normal account") {
    import scala.concurrent.duration._
    assertIO(
      for {
        _ <- assertIO(
          addSecret("topl-secret", "sha256").run(bobContext),
          ExitCode.Success
        )
        _ <- assertIO(
          createSimpleTransactionToCartesianIdx(
            "bob_digest_fellowship",
            "digest_template",
            None,
            None,
            None,
            None,
            "self",
            "default",
            500,
            BASE_FEE,
            BOB_FIRST_TX_RAW,
            TokenType.lvl,
            None,
            None
          ).run(bobContext),
          ExitCode.Success
        )
        _ <- assertIO(
          proveSimpleTransaction(
            BOB_FIRST_TX_RAW,
            BOB_FIRST_TX_PROVED
          ).run(bobContext),
          ExitCode.Success
        )
        _ <- assertIO(
          broadcastSimpleTx(BOB_FIRST_TX_PROVED),
          ExitCode.Success
        )
        _ <- IO.println(
          "Check digest account from bob's wallet, lvl tokens"
        )
        res <- IO.asyncForIO.timeout(
          (for {
            _ <- IO.println("Querying bob's address")
            queryRes <- queryAccount("self", "default").run(bobContext)
            _ <- IO.sleep(5.seconds)
          } yield queryRes)
            .iterateUntil(_ == ExitCode.Success),
          120.seconds
        )
      } yield res,
      ExitCode.Success
    )
  }

}
