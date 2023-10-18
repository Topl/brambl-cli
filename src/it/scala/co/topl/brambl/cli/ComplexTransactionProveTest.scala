package co.topl.brambl.cli

import cats.effect.ExitCode
import cats.effect.IO
import munit.CatsEffectSuite

import scala.concurrent.duration.Duration

class ComplexTransactionProveTest
    extends CatsEffectSuite
    with CommonFunctions
    with AliceConstants
    with CommonTxOperations
    with IntegrationTearDown
    with BobConstants {

  override val munitTimeout = Duration(180, "s")

  tmpDirectory.test("Move funds from genesis to alice") { _ =>
    assertIO(
      moveFundsFromGenesisToAlice(),
      ExitCode.Success
    )
  }

  test("Move funds from alice to shared or account") {
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

  test("Move funds from alice to shared and account") {
    import scala.concurrent.duration._
    assertIO(
      for {
        _ <- IO.println("Add a contract to alice's wallet")
        _ <- assertIO(
          addContractToWallet(
            "and_sign",
            "threshold(1, sign(0) and sign(1))"
          ).run(aliceContext),
          ExitCode.Success
        )
        _ <- IO.println("Add a contract to bob's wallet")
        _ <- assertIO(
          addContractToWallet(
            "and_sign",
            "threshold(1, sign(1) and sign(0))"
          ).run(bobContext),
          ExitCode.Success
        )
        _ <- IO.println("Exporting VK from alice's wallet")
        _ <- assertIO(
          exportVk("alice_bob_0", "and_sign", ALICE_VK_AND).run(aliceContext),
          ExitCode.Success
        )
        _ <- IO.println("Exporting VK from bob's wallet")
        _ <- assertIO(
          exportVk("alice_bob_0", "and_sign", BOB_VK_AND).run(bobContext),
          ExitCode.Success
        )
        _ <- IO.println("Importing VK to alice's wallet")
        _ <- assertIO(
          importVk("alice_bob_0", "and_sign", ALICE_VK_AND).run(bobContext),
          ExitCode.Success
        )
        _ <- IO.println("Importing VK to bob's wallet")
        _ <- assertIO(
          importVk("alice_bob_0", "and_sign", BOB_VK_AND).run(aliceContext),
          ExitCode.Success
        )
        _ <- IO.println(
          "Moving funds (700 LVLs) from alice to shared and account"
        )
        _ <- assertIO(
          createSimpleTransactionToCartesianIdx(
            "self",
            "default",
            None,
            "alice_bob_0",
            "and_sign",
            700,
            BASE_FEE,
            ALICE_THIRD_TX_RAW,
            TokenType.lvl,
            None,
            None
          ).run(aliceContext),
          ExitCode.Success
        )
        _ <- assertIO(
          proveSimpleTransaction(
            ALICE_THIRD_TX_RAW,
            ALICE_THIRD_TX_PROVED
          ).run(aliceContext),
          ExitCode.Success
        )
        _ <- assertIO(
          broadcastSimpleTx(ALICE_THIRD_TX_PROVED),
          ExitCode.Success
        )
        _ <- IO.println(
          "Check shared and account for from alice's wallet, expected 700 LVLs"
        )
        res <- IO.asyncForIO.timeout(
          (for {
            _ <- IO.println("Querying alice's and shared account")
            queryRes <- queryAccount("alice_bob_0", "and_sign").run(
              aliceContext
            )
            _ <- IO.sleep(5.seconds)
          } yield queryRes)
            .iterateUntil(_ == ExitCode.Success),
          60.seconds
        )
      } yield res,
      ExitCode.Success
    )
  }

  test("Move funds from shared or account to bob") {
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
            ALICE_TO_ADDRESS.toOption.get,
            200,
            BASE_FEE,
            BOB_SECOND_TX_RAW,
            TokenType.lvl,
            None,
            None
          ).run(bobContext),
          ExitCode.Success
        )
        _ <- assertIO(
          proveSimpleTransaction(
            BOB_SECOND_TX_RAW,
            BOB_SECOND_TX_PROVED
          ).run(bobContext),
          ExitCode.Success
        )
        _ <- assertIO(
          broadcastSimpleTx(BOB_SECOND_TX_PROVED),
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
        _ <- syncWallet("alice_bob_0", "or_sign").run(aliceContext)
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

  test("Move funds from shared and account to bob") {
    import scala.concurrent.duration._
    assertIO(
      for {
        sharedAddressForAlice <- walletController(ALICE_WALLET)
          .currentaddress("alice_bob_0", "and_sign", None)
        sharedAddressForBob <- walletController(BOB_WALLET)
          .currentaddress("alice_bob_0", "and_sign", None)
        _ <- IO.println("Address for Alice: " + sharedAddressForAlice)
        _ <- IO.println("Address for Bob: " + sharedAddressForBob)
        _ <- IO.println(
          "Moving funds (100 LVLs) from shared and account to alice"
        )
        ALICE_TO_ADDRESS <- walletController(ALICE_WALLET).currentaddress()
        _ <- IO.println(s"Alice's address is $ALICE_TO_ADDRESS")
        _ <- assertIO(
          createSimpleTransactionToAddress(
            "alice_bob_0",
            "and_sign",
            None,
            ALICE_TO_ADDRESS.toOption.get,
            100,
            BASE_FEE,
            BOB_THIRD_TX_RAW,
            TokenType.lvl,
            None,
            None
          ).run(bobContext),
          ExitCode.Success
        )
        _ <- IO.println("Proving by Alice")
        _ <- assertIO(
          proveSimpleTransaction(
            BOB_THIRD_TX_RAW,
            BOB_THIRD_TX_PROVED
          ).run(aliceContext),
          ExitCode.Success
        )
        _ <- IO.println("Proving by Bob")
        _ <- assertIO(
          proveSimpleTransaction(
            BOB_THIRD_TX_PROVED,
            ALICE_FOURTH_TX_PROVED
          ).run(bobContext),
          ExitCode.Success
        )
        _ <- assertIO(
          broadcastSimpleTx(ALICE_FOURTH_TX_PROVED),
          ExitCode.Success
        )
        _ <- IO.println(
          "Check shared and account for from bob's wallet, expected 600 LVLs"
        )
        _ <- IO.asyncForIO.timeout(
          (for {
            _ <- IO.println("Querying bob's and shared account")
            queryRes <- queryAccount("alice_bob_0", "and_sign").run(bobContext)
            _ <- IO.sleep(5.seconds)
          } yield queryRes)
            .iterateUntil(_ == ExitCode.Success),
          60.seconds
        )
        _ <- IO.println("Sync alice's and account")
        _ <- syncWallet("alice_bob_0", "and_sign").run(aliceContext)
        res <- IO.asyncForIO.timeout(
          (for {
            _ <- IO.println("Querying alice's and shared account")
            queryRes <- queryAccount("alice_bob_0", "and_sign").run(
              aliceContext
            )
            _ <- IO.sleep(5.seconds)
          } yield queryRes)
            .iterateUntil(_ == ExitCode.Success),
          60.seconds
        )
      } yield res,
      ExitCode.Success
    )
  }

  test("Send Alice Change back to HeightLock") {
    assertIO(
      tearDown(aliceContext),
      ExitCode.Success
    )
  }
}
