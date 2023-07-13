package co.topl.brambl.cli

import cats.effect.ExitCode
import cats.effect.IO
import munit.CatsEffectSuite

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import scala.concurrent.duration.Duration

class ComplexTransactionProveTest
    extends CatsEffectSuite
    with AliceConstants
    with CommonTxOperations
    with BobConstants {

  val tmpDirectory = FunFixture[Path](
    setup = { _ =>
      Paths.get(TMP_DIR).toFile().listFiles().map(_.delete()).mkString("\n")
      Files.deleteIfExists(Paths.get(TMP_DIR))
      Files.createDirectory(Paths.get("./tmp"))
    },
    teardown = { _ => () }
  )

  override val munitTimeout = Duration(120, "s")

  tmpDirectory.test(
    "Move funds from genesis to alice"
  ) { _ =>
    import scala.concurrent.duration._
    assertIO(
      for {
        _ <- createWallet().run(aliceContext)
        ALICE_TO_ADDRESS <- walletController(ALICE_WALLET).currentaddress()
        _ <- IO.println(s"Alice's address is $ALICE_TO_ADDRESS")
        _ <- IO.println("Moving funds from genesis to alice")
        _ <- assertIO(
          createSimpleTransactionToAddress(
            "noparty",
            "genesis",
            Some(1),
            ALICE_TO_ADDRESS,
            BASE_AMOUNT,
            ALICE_FIRST_TX_RAW
          ).run(aliceContext),
          ExitCode.Success
        )
        _ <- IO.sleep(5.seconds)
        _ <- assertIO(
          proveSimpleTransaction(
            "noparty",
            "genesis",
            Some(1),
            ALICE_FIRST_TX_RAW,
            ALICE_FIRST_TX_PROVED
          ).run(aliceContext),
          ExitCode.Success
        )
        _ <- IO.sleep(5.seconds)
        _ <- assertIO(
          broadcastSimpleTx(ALICE_FIRST_TX_PROVED, ALICE_WALLET),
          ExitCode.Success
        )
        _ <- IO.println("Check alice's address (is contained in the change)")
        res <- IO.asyncForIO.timeout(
          (for {
            queryRes <- queryAccount("self", "default").run(aliceContext)
            _ <- IO.sleep(5.seconds)
          } yield queryRes)
            .iterateUntil(_ == ExitCode.Success),
          60.seconds
        )
      } yield res,
      ExitCode.Success
    )
  }

  test("Move funds from alice to shared account") {
    import scala.concurrent.duration._
    assertIO(
      for {
        _ <- IO.println("Create a wallet for bob")
        _ <- assertIO(createWallet().run(bobContext), ExitCode.Success)
        _ <- IO.println("Add bob to alice's wallet")
        _ <- assertIO(
          addPartyToWallet("alice_bob_0").run(aliceContext),
          ExitCode.Success
        )
        _ <- IO.println("Add alice to bob's wallet")
        _ <- assertIO(
          addPartyToWallet("alice_bob_0").run(bobContext),
          ExitCode.Success
        )
        _ <- IO.println("Add a contract to alice's wallet")
        _ <- assertIO(
          addContractToWallet(
            "or_sign",
            "threshold(1, sign(0) or sign(1))"
          ).run(aliceContext),
          ExitCode.Success
        )
        _ <- IO.println("Add a contract to bob's wallet")
        _ <- assertIO(
          addContractToWallet(
            "or_sign",
            "threshold(1, sign(1) or sign(0))"
          ).run(bobContext),
          ExitCode.Success
        )
        _ <- IO.println("Exporting VK from alice's wallet")
        _ <- assertIO(
          exportVk("alice_bob_0", "or_sign", ALICE_VK).run(aliceContext),
          ExitCode.Success
        )
        _ <- IO.println("Exporting VK from bob's wallet")
        _ <- assertIO(
          exportVk("alice_bob_0", "or_sign", BOB_VK).run(bobContext),
          ExitCode.Success
        )
        _ <- IO.println("Importing VK to alice's wallet")
        _ <- assertIO(
          importVk("alice_bob_0", "or_sign", ALICE_VK).run(bobContext),
          ExitCode.Success
        )
        _ <- IO.sleep(5.seconds)
        _ <- IO.println("Importing VK to bob's wallet")
        _ <- assertIO(
          importVk("alice_bob_0", "or_sign", BOB_VK).run(aliceContext),
          ExitCode.Success
        )
        _ <- IO.println("Moving funds (500 LVLs) from alice to shared account")
        _ <- assertIO(
          createSimpleTransactionToCartesianIdx(
            "self",
            "default",
            None,
            "alice_bob_0",
            "or_sign",
            500,
            ALICE_SECOND_TX_RAW
          ).run(aliceContext),
          ExitCode.Success
        )
        _ <- IO.sleep(5.seconds)
        _ <- assertIO(
          proveSimpleTransaction(
            "self",
            "default",
            None,
            ALICE_SECOND_TX_RAW,
            ALICE_SECOND_TX_PROVED
          ).run(aliceContext),
          ExitCode.Success
        )
        _ <- IO.sleep(5.seconds)
        _ <- assertIO(
          broadcastSimpleTx(ALICE_SECOND_TX_PROVED, ALICE_WALLET),
          ExitCode.Success
        )
        _ <- IO.sleep(5.seconds)
        _ <- IO.println(
          "Check shared account for from alice's wallet, expected 500 LVLs"
        )
        res <- IO.asyncForIO.timeout(
          (for {
            _ <- IO.println("Querying alice's shared account")
            queryRes <- queryAccount("alice_bob_0", "or_sign").run(aliceContext)
            _ <- IO.sleep(5.seconds)
          } yield queryRes)
            .iterateUntil(_ == ExitCode.Success),
          60.seconds
        )
      } yield res,
      ExitCode.Success
    )
  }

  test("Move funds from shared account to bob") {
    import scala.concurrent.duration._
    assertIO(
      for {
        sharedAddressForAlice <- walletController(ALICE_WALLET)
          .currentaddress("alice_bob_0", "or_sign", None)
        sharedAddressForBob <- walletController(BOB_WALLET)
          .currentaddress("alice_bob_0", "or_sign", None)
        _ <- IO.println("Address for Alice: " + sharedAddressForAlice)
        _ <- IO.println("Address for Bob: " + sharedAddressForBob)
        _ <- IO.println("Moving funds (200 LVLs) from shared account to alice")
        ALICE_TO_ADDRESS <- walletController(ALICE_WALLET).currentaddress()
        _ <- IO.println(s"Alice's address is $ALICE_TO_ADDRESS")
        _ <- assertIO(
          createSimpleTransactionToAddress(
            "alice_bob_0",
            "or_sign",
            None,
            ALICE_TO_ADDRESS,
            200,
            BOB_SECOND_TX_RAW
          ).run(bobContext),
          ExitCode.Success
        )
        _ <- IO.sleep(5.seconds)
        _ <- assertIO(
          proveSimpleTransaction(
            "alice_bob_0",
            "or_sign",
            None,
            BOB_SECOND_TX_RAW,
            BOB_SECOND_TX_PROVED
          ).run(bobContext),
          ExitCode.Success
        )
        _ <- IO.sleep(5.seconds)
        _ <- assertIO(
          broadcastSimpleTx(BOB_SECOND_TX_PROVED, BOB_WALLET),
          ExitCode.Success
        )
        _ <- IO.println(
          "Check shared account for from bob's wallet, expected 300 LVLs"
        )
        _ <- IO.asyncForIO.timeout(
          (for {
            _ <- IO.println("Querying bob's shared account")
            queryRes <- queryAccount("alice_bob_0", "or_sign").run(bobContext)
            _ <- IO.sleep(5.seconds)
          } yield queryRes)
            .iterateUntil(_ == ExitCode.Success),
          60.seconds
        )
        _ <- IO.println("Sync alice's account")
        _ <- walletController(ALICE_WALLET)
          .sync("alice_bob_0", "or_sign")
        res <- IO.asyncForIO.timeout(
          (for {
            _ <- IO.println("Querying alice's shared account")
            queryRes <- queryAccount("alice_bob_0", "or_sign").run(aliceContext)
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
