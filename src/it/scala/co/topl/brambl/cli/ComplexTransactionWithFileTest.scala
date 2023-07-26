package co.topl.brambl.cli

import cats.effect.ExitCode
import cats.effect.IO
import co.topl.brambl.codecs.AddressCodecs.decodeAddress
import co.topl.brambl.utils.Encoding
import munit.CatsEffectSuite

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import scala.concurrent.duration.Duration
import cats.effect.kernel.Resource
import scala.io.Source

class ComplexTransactionWithFileTest
    extends CatsEffectSuite
    with CommonTxOperations
    with AliceConstants
    with BobConstants
    with IntegrationTearDown
    with ComplexTransactionTemplates {

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

  tmpDirectory.test("Move funds from genesis to alice with complex tx") { _ =>
    import scala.concurrent.duration._
    assertIO(
      for {
        _ <- createWallet().run(aliceContext)
        _ <- IO.asyncForIO.timeout(
          (for {
            _ <- IO.println("Querying genesis to start")
            queryRes <- queryAccount("noparty", "genesis", Some(1))
              .run(aliceContext)
            _ <- IO.sleep(5.seconds)
          } yield queryRes)
            .iterateUntil(_ == ExitCode.Success),
          60.seconds
        )
        ALICE_TO_ADDRESS <- walletController(ALICE_WALLET).currentaddress()
        genesisAddress <- walletController(ALICE_WALLET)
          .currentaddress("noparty", "genesis", Some(1))
        utxos <- genusQueryAlgebra.queryUtxo(
          decodeAddress(genesisAddress.get).toOption.get
        )
        _ <- IO.println(s"Alice's address is $ALICE_TO_ADDRESS")
        genesisUtxoAddress = Encoding.encodeToBase58(
          utxos.head.outputAddress.id.value.toByteArray
        ) + "#" + utxos.head.outputAddress.index.toString
        genesisAmount = BigInt(
          utxos.head.transactionOutput.value.value.lvl.get.quantity.value
            .toByteArray()
        ).toLong
        _ <- IO.println(s"GENESIS_UTXO_ADDRESS is $genesisUtxoAddress")
        _ <- IO.println("Moving funds from genesis to alice")
        _ <- createComplexTxFileFromGenesisToAlice(
          ALICE_FIRST_COMPLEX_TX,
          genesisUtxoAddress,
          genesisAmount,
          ALICE_TO_ADDRESS.toOption.get
        )
        _ <- assertIO(
          createComplexTransactionToAddress(
            ALICE_FIRST_COMPLEX_TX,
            ALICE_FIRST_COMPLEX_TX_RAW
          ).run(aliceContext),
          ExitCode.Success
        )
        _ <- IO.sleep(5.seconds)
        _ <- assertIO(
          proveSimpleTransaction(
            "noparty",
            "genesis",
            Some(1),
            ALICE_FIRST_COMPLEX_TX_RAW,
            ALICE_FIRST_COMPLEX_TX_PROVED
          ).run(aliceContext),
          ExitCode.Success
        )
        _ <- IO.sleep(5.seconds)
        _ <- assertIO(
          broadcastSimpleTx(ALICE_FIRST_COMPLEX_TX_PROVED, ALICE_WALLET),
          ExitCode.Success
        )
        _ <- IO.println("Check alice's address")
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

  test("Move funds from alice to shared or account with complex tx") {
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
            "or_sign",
            "threshold(1, sign(1) or sign(0))"
          ).run(bobContext),
          ExitCode.Success
        )
        _ <- assertIO(
          addContractToWallet(
            "and_sign",
            "threshold(1, sign(1) and sign(0))"
          ).run(bobContext),
          ExitCode.Success
        )
        _ <- IO.println("Exporting VK from alice's wallet")
        _ <- assertIO(
          exportVk("alice_bob_0", "or_sign", ALICE_COMPLEX_VK_OR).run(aliceContext),
          ExitCode.Success
        )
        _ <- IO.println("Exporting VK from bob's wallet")
        _ <- assertIO(
          exportVk("alice_bob_0", "or_sign", BOB_COMPLEX_VK_OR).run(bobContext),
          ExitCode.Success
        )
        _ <- IO.println("Importing or VK to alice's wallet")
        _ <- assertIO(
          importVk("alice_bob_0", "or_sign", ALICE_COMPLEX_VK_OR).run(bobContext),
          ExitCode.Success
        )
        _ <- IO.sleep(5.seconds)
        _ <- IO.println("Importing or VK to bob's wallet")
        _ <- assertIO(
          importVk("alice_bob_0", "or_sign", BOB_COMPLEX_VK_OR).run(aliceContext),
          ExitCode.Success
        )
        _ <- IO.println("Exporting and VK from alice's wallet")
        _ <- assertIO(
          exportVk("alice_bob_0", "and_sign", ALICE_COMPLEX_VK_AND).run(aliceContext),
          ExitCode.Success
        )
        _ <- IO.println("Exporting and VK from bob's wallet")
        _ <- assertIO(
          exportVk("alice_bob_0", "and_sign", BOB_COMPLEX_VK_AND).run(bobContext),
          ExitCode.Success
        )
        _ <- IO.println("Importing and VK to alice's wallet")
        _ <- assertIO(
          importVk("alice_bob_0", "and_sign", ALICE_COMPLEX_VK_AND).run(bobContext),
          ExitCode.Success
        )
        _ <- IO.sleep(5.seconds)
        _ <- IO.println("Importing VK to bob's wallet")
        _ <- assertIO(
          importVk("alice_bob_0", "and_sign", BOB_COMPLEX_VK_AND).run(aliceContext),
          ExitCode.Success
        )
        aliceAddress <- walletController(ALICE_WALLET)
          .currentaddress("self", "default", Some(1))
        utxos <- genusQueryAlgebra.queryUtxo(
          decodeAddress(aliceAddress.get).toOption.get
        )
        aliceChangeAddress <- walletController(ALICE_WALLET)
          .currentaddress("self", "default", Some(1))
        addressAliceBobOr <- walletController(ALICE_WALLET)
          .currentaddress("alice_bob_0", "or_sign", Some(1))
        addressAliceBobAnd <- walletController(ALICE_WALLET)
          .currentaddress("alice_bob_0", "and_sign", Some(1))
        aliceUtxoAddress = Encoding.encodeToBase58(
          utxos.head.outputAddress.id.value.toByteArray
        ) + "#" + utxos.head.outputAddress.index.toString
        aliceAmount = BigInt(
          utxos.head.transactionOutput.value.value.lvl.get.quantity.value
            .toByteArray()
        ).toLong
        _ <- assertIO(
          exportFinalVk("self", "default", 1, ALICE_FINAL_VK).run(
            aliceContext
          ),
          ExitCode.Success
        )
        aliceKey <- Resource
          .make(IO(Source.fromFile(ALICE_FINAL_VK)))(f => IO(f.close()))
          .use { file =>
            IO(
              file.getLines().mkString
            )
          }
        _ <- IO.println(s"aliceUtxoAddress is $aliceUtxoAddress")
        _ <- createComplexTxFileFromAliceToAliceBobAndOr(
          ALICE_SECOND_COMPLEX_TX,
          aliceUtxoAddress,
          aliceKey,
          aliceAmount,
          aliceChangeAddress.get,
          addressAliceBobOr.get,
          addressAliceBobAnd.get
        )
        _ <- IO.println("Moving funds (500 LVLs) from alice to shared account")
        _ <- assertIO(
          createComplexTransactionToAddress(
            ALICE_SECOND_COMPLEX_TX,
            ALICE_SECOND_COMPLEX_TX_RAW
          ).run(aliceContext),
          ExitCode.Success
        )
        _ <- IO.sleep(5.seconds)
        _ <- assertIO(
          proveSimpleTransaction(
            "self",
            "default",
            Some(1),
            ALICE_SECOND_COMPLEX_TX_RAW,
            ALICE_SECOND_COMPLEX_TX_PROVED
          ).run(aliceContext),
          ExitCode.Success
        )
        _ <- IO.sleep(5.seconds)
        _ <- assertIO(
          broadcastSimpleTx(ALICE_SECOND_COMPLEX_TX_PROVED, ALICE_WALLET),
          ExitCode.Success
        )
        _ <- IO.sleep(5.seconds)
        _ <- IO.println(
          "Check shared accounts for from alice's wallet, expected 1000 in each LVLs"
        )
        _ <- IO.asyncForIO.timeout(
          (for {
            _ <- IO.println("Querying alice's shared or account")
            queryRes <- queryAccount("alice_bob_0", "or_sign").run(aliceContext)
            _ <- IO.sleep(5.seconds)
          } yield queryRes)
            .iterateUntil(_ == ExitCode.Success),
          60.seconds
        )
        res <- IO.asyncForIO.timeout(
          (for {
            _ <- IO.println("Querying alice's shared and account")
            queryRes <- queryAccount("alice_bob_0", "and_sign").run(aliceContext)
            _ <- IO.sleep(5.seconds)
          } yield queryRes)
            .iterateUntil(_ == ExitCode.Success),
          60.seconds
        )
      } yield res,
      ExitCode.Success
    )
  }

  test("Send Wallet Change back to HeightLock") {
    assertIO(
      tearDown(aliceContext),
      ExitCode.Success
    )
  }

}
