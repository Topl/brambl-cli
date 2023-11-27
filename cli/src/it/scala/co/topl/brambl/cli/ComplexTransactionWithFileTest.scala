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
        _ <- IO.println("Create a wallet for alice")
        _ <- createWallet().run(aliceContext)
        _ <- IO.println("Created a wallet for alice")
        _ <- IO.asyncForIO.timeout(
          (for {
            _ <- IO.println("Querying genesis to start")
            queryRes <- queryAccount("nofellowship", "genesis", Some(1))
              .run(aliceContext)
            _ <- IO.sleep(5.seconds)
          } yield queryRes)
            .iterateUntil(_ == ExitCode.Success),
          60.seconds
        )
        ALICE_TO_ADDRESS <- walletController(ALICE_WALLET).currentaddress("self", "default", None)
        genesisAddress <- walletController(ALICE_WALLET)
          .currentaddress("nofellowship", "genesis", Some(1))
        utxos <- genusQueryAlgebra
          .queryUtxo(
            decodeAddress(genesisAddress.get).toOption.get
          )
          .map(_.filter(_.transactionOutput.value.value.isLvl))
        _ <- IO.println(s"Alice's address is $ALICE_TO_ADDRESS")
        genesisUtxoAddresses = utxos.map { utxo =>
          (
            Encoding.encodeToBase58(
              utxo.outputAddress.id.value.toByteArray
            ) + "#" + utxo.outputAddress.index.toString,
            BigInt(
              utxo.transactionOutput.value.value.lvl.get.quantity.value
                .toByteArray()
            ).toLong
          )
        }
        genesisAmount = utxos.foldLeft(0L) { (acc, utxo) =>
          acc + BigInt(
            utxo.transactionOutput.value.value.lvl.get.quantity.value
              .toByteArray()
          ).toLong
        }
        _ <- IO.println("Moving funds from genesis to alice")
        _ <- createComplexTxFileFromGenesisToAlice(
          ALICE_FIRST_COMPLEX_TX,
          genesisUtxoAddresses.toList,
          genesisAmount,
          genesisAmount - BASE_AMOUNT,
          BASE_FEE,
          genesisAddress.get,
          ALICE_TO_ADDRESS.get
        )
        _ <- assertIO(
          createComplexTransactionToAddress(
            ALICE_FIRST_COMPLEX_TX,
            ALICE_FIRST_COMPLEX_TX_RAW
          ).run(aliceContext),
          ExitCode.Success
        )
        _ <- assertIO(
          proveSimpleTransaction(
            ALICE_FIRST_COMPLEX_TX_RAW,
            ALICE_FIRST_COMPLEX_TX_PROVED
          ).run(aliceContext),
          ExitCode.Success
        )
        _ <- assertIO(
          broadcastSimpleTx(ALICE_FIRST_COMPLEX_TX_PROVED),
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

  test("Move funds from alice to shared or and and account with complex tx") {
    import scala.concurrent.duration._
    assertIO(
      for {
        _ <- IO.println("Create a wallet for bob")
        _ <- assertIO(createWallet().run(bobContext), ExitCode.Success)
        _ <- IO.println("Add bob to alice's wallet")
        _ <- assertIO(
          addFellowshipToWallet("alice_bob_0").run(aliceContext),
          ExitCode.Success
        )
        _ <- IO.println("Add alice to bob's wallet")
        _ <- assertIO(
          addFellowshipToWallet("alice_bob_0").run(bobContext),
          ExitCode.Success
        )
        _ <- IO.println("Add a template to alice's wallet")
        _ <- assertIO(
          addTemplateToWallet(
            "or_sign",
            "threshold(1, sign(0) or sign(1))"
          ).run(aliceContext),
          ExitCode.Success
        )
        _ <- assertIO(
          addTemplateToWallet(
            "and_sign",
            "threshold(1, sign(0) and sign(1))"
          ).run(aliceContext),
          ExitCode.Success
        )
        _ <- IO.println("Add a template to bob's wallet")
        _ <- assertIO(
          addTemplateToWallet(
            "or_sign",
            "threshold(1, sign(1) or sign(0))"
          ).run(bobContext),
          ExitCode.Success
        )
        _ <- assertIO(
          addTemplateToWallet(
            "and_sign",
            "threshold(1, sign(1) and sign(0))"
          ).run(bobContext),
          ExitCode.Success
        )
        _ <- IO.println("Exporting VK from alice's wallet")
        _ <- assertIO(
          exportVk("alice_bob_0", "or_sign", ALICE_COMPLEX_VK_OR).run(
            aliceContext
          ),
          ExitCode.Success
        )
        _ <- IO.println("Exporting VK from bob's wallet")
        _ <- assertIO(
          exportVk("alice_bob_0", "or_sign", BOB_COMPLEX_VK_OR).run(bobContext),
          ExitCode.Success
        )
        _ <- IO.println("Importing or VK to alice's wallet")
        _ <- assertIO(
          importVk("alice_bob_0", "or_sign", ALICE_COMPLEX_VK_OR).run(
            bobContext
          ),
          ExitCode.Success
        )
        _ <- IO.println("Importing or VK to bob's wallet")
        _ <- assertIO(
          importVk("alice_bob_0", "or_sign", BOB_COMPLEX_VK_OR).run(
            aliceContext
          ),
          ExitCode.Success
        )
        _ <- IO.println("Exporting and VK from alice's wallet")
        _ <- assertIO(
          exportVk("alice_bob_0", "and_sign", ALICE_COMPLEX_VK_AND).run(
            aliceContext
          ),
          ExitCode.Success
        )
        _ <- IO.println("Exporting and VK from bob's wallet")
        _ <- assertIO(
          exportVk("alice_bob_0", "and_sign", BOB_COMPLEX_VK_AND).run(
            bobContext
          ),
          ExitCode.Success
        )
        _ <- IO.println("Importing and VK to alice's wallet")
        _ <- assertIO(
          importVk("alice_bob_0", "and_sign", ALICE_COMPLEX_VK_AND).run(
            bobContext
          ),
          ExitCode.Success
        )
        _ <- IO.println("Importing VK to bob's wallet")
        _ <- assertIO(
          importVk("alice_bob_0", "and_sign", BOB_COMPLEX_VK_AND).run(
            aliceContext
          ),
          ExitCode.Success
        )
        aliceAddress <- walletController(ALICE_WALLET)
          .currentaddress("self", "default", Some(1))
        utxos <- genusQueryAlgebra
          .queryUtxo(
            decodeAddress(aliceAddress.get).toOption.get
          )
          .map(_.filter(_.transactionOutput.value.value.isLvl))
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
        _ <- assertIO(
          proveSimpleTransaction(
            ALICE_SECOND_COMPLEX_TX_RAW,
            ALICE_SECOND_COMPLEX_TX_PROVED
          ).run(aliceContext),
          ExitCode.Success
        )
        _ <- assertIO(
          broadcastSimpleTx(ALICE_SECOND_COMPLEX_TX_PROVED),
          ExitCode.Success
        )
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

  test("Move funds from shared or and and account with complex tx") {
    import scala.concurrent.duration._
    assertIO(
      for {
        bobAddress <- walletController(BOB_WALLET)
          .currentaddress("self", "default", Some(1))
        andAddress <- walletController(ALICE_WALLET)
          .currentaddress("alice_bob_0", "and_sign", Some(1))
        orAddress <- walletController(ALICE_WALLET)
          .currentaddress("alice_bob_0", "or_sign", Some(1))
        utxosAnd <- genusQueryAlgebra
          .queryUtxo(
            decodeAddress(andAddress.get).toOption.get
          )
          .map(_.filter(_.transactionOutput.value.value.isLvl))
        utxosOr <- genusQueryAlgebra
          .queryUtxo(
            decodeAddress(orAddress.get).toOption.get
          )
          .map(_.filter(_.transactionOutput.value.value.isLvl))
        orUtxo = Encoding.encodeToBase58(
          utxosOr.head.outputAddress.id.value.toByteArray
        ) + "#" + utxosOr.head.outputAddress.index.toString
        andUtxo = Encoding.encodeToBase58(
          utxosAnd.head.outputAddress.id.value.toByteArray
        ) + "#" + utxosAnd.head.outputAddress.index.toString
        _ <- assertIO(
          exportFinalVk("alice_bob_0", "and_sign", 1, ALICE_AND_VK).run(
            aliceContext
          ),
          ExitCode.Success
        )
        _ <- assertIO(
          exportFinalVk("alice_bob_0", "and_sign", 1, BOB_AND_VK).run(
            bobContext
          ),
          ExitCode.Success
        )
        _ <- assertIO(
          exportFinalVk("alice_bob_0", "or_sign", 1, ALICE_OR_VK).run(
            aliceContext
          ),
          ExitCode.Success
        )
        aliceAndKey <- Resource
          .make(IO(Source.fromFile(ALICE_AND_VK)))(f => IO(f.close()))
          .use { file =>
            IO(
              file.getLines().mkString
            )
          }
        bobAndKey <- Resource
          .make(IO(Source.fromFile(BOB_AND_VK)))(f => IO(f.close()))
          .use { file =>
            IO(
              file.getLines().mkString
            )
          }
        aliceOrKey <- Resource
          .make(IO(Source.fromFile(ALICE_OR_VK)))(f => IO(f.close()))
          .use { file =>
            IO(
              file.getLines().mkString
            )
          }
        _ <- createSharedTemplatesToBob(
          ALICE_THIRD_COMPLEX_TX,
          andUtxo,
          orUtxo,
          aliceAndKey,
          bobAndKey,
          aliceOrKey,
          1000,
          1000,
          bobAddress.get
        )
        _ <- IO.println("Moving funds (1000) from shared accounts to bob")
        _ <- assertIO(
          createComplexTransactionToAddress(
            ALICE_THIRD_COMPLEX_TX,
            ALICE_THIRD_COMPLEX_TX_RAW
          ).run(aliceContext),
          ExitCode.Success
        )
        _ <- assertIO(
          proveSimpleTransaction(
            ALICE_THIRD_COMPLEX_TX_RAW,
            ALICE_THIRD_COMPLEX_TX_PROVED
          ).run(aliceContext),
          ExitCode.Success
        )
        _ <- assertIO(
          proveSimpleTransaction(
            ALICE_THIRD_COMPLEX_TX_PROVED,
            ALICE_THIRD_COMPLEX_TX_PROVED_BY_BOTH
          ).run(bobContext),
          ExitCode.Success
        )
        _ <- assertIO(
          broadcastSimpleTx(ALICE_THIRD_COMPLEX_TX_PROVED_BY_BOTH),
          ExitCode.Success
        )
        _ <- IO.println(
          "Check bob wallet, expected 2000 LVLs"
        )
        res <- IO.asyncForIO.timeout(
          (for {
            _ <- IO.println("Querying bob's shared or account")
            queryRes <- queryAccount("self", "default").run(bobContext)
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
