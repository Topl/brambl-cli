package co.topl.brambl.cli

import munit.CatsEffectSuite
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import cats.effect.ExitCode
import co.topl.brambl.cli.controllers.WalletController
import co.topl.brambl.cli.modules.WalletStateAlgebraModule
import co.topl.brambl.cli.modules.WalletManagementUtilsModule
import co.topl.brambl.cli.modules.WalletApiModule
import co.topl.brambl.cli.modules.WalletAlgebraModule
import co.topl.brambl.cli.modules.TransactionBuilderApiModule
import co.topl.brambl.constants.NetworkConstants
import cats.effect.IO
import scala.concurrent.duration.Duration
import co.topl.brambl.dataApi.GenusQueryAlgebra
import co.topl.brambl.cli.modules.ChannelResourceModule

class ComplexTransactionProveTest
    extends CatsEffectSuite
    with WalletStateAlgebraModule
    with WalletManagementUtilsModule
    with WalletApiModule
    with WalletAlgebraModule
    with TransactionBuilderApiModule
    with ChannelResourceModule {

  val TMP_DIR = "./tmp"
  val ALICE_WALLET = s"$TMP_DIR/alice_wallet.db"
  val ALICE_MAIN_KEY = s"$TMP_DIR/alice_mainkey.json"
  val ALICE_PASSWORD = "test"
  val ALICE_FIRST_TX_RAW = s"$TMP_DIR/alice_first_tx.pbuf"
  val ALICE_FIRST_TX_PROVED = s"$TMP_DIR/alice_first_tx_proved.pbuf"
  val BASE_AMOUNT = "1000"

  val tmpDirectory = FunFixture[Path](
    setup = { _ =>
      // Files.deleteIfExists(Paths.get("./tmp/" + ALICE_WALLET))
      // Files.deleteIfExists(Paths.get("./tmp/" + ALICE_MAIN_KEY))
      // Files.deleteIfExists(Paths.get("./tmp/" + ALICE_FIRST_TX_RAW))
      // Files.deleteIfExists(Paths.get("./tmp/" + ALICE_FIRST_TX_PROVED))
      Paths.get(TMP_DIR).toFile().listFiles().map(_.delete()).mkString("\n")
      Files.deleteIfExists(Paths.get(TMP_DIR))
      Files.createDirectory(Paths.get("./tmp"))
    },
    teardown = { _ =>
      // file.toFile().listFiles().foreach(_.delete())
    }
  )

  def walletController(walletFile: String) = new WalletController(
    transactionBuilderApi(
      NetworkConstants.PRIVATE_NETWORK_ID,
      NetworkConstants.MAIN_LEDGER_ID
    ),
    walletStateAlgebra(
      walletFile,
      NetworkConstants.PRIVATE_NETWORK_ID
    ),
    walletManagementUtils,
    walletApi,
    walletAlgebra(walletFile, NetworkConstants.PRIVATE_NETWORK_ID),
    GenusQueryAlgebra
      .make[IO](
        channelResource(
          "localhost",
          9084
        )
      )
  )

  def createAliceWallet() = Main.run(
    List(
      "wallet",
      "init",
      "-w",
      ALICE_PASSWORD,
      "-n",
      "private",
      "-o",
      ALICE_MAIN_KEY,
      "--walletdb",
      ALICE_WALLET
    )
  )

  def createSimpleTransactionFromGenesis(toAddress: String) = Main.run(
    List(
      "simpletransaction",
      "create",
      "--from-party",
      "noparty",
      "--from-contract",
      "genesis",
      "--from-state",
      "1",
      "-t",
      toAddress,
      "-w",
      ALICE_PASSWORD,
      "--bifrost-port",
      "9084",
      "-o",
      ALICE_FIRST_TX_RAW,
      "-n",
      "private",
      "-a",
      BASE_AMOUNT,
      "-h",
      "localhost",
      "--keyfile",
      ALICE_MAIN_KEY,
      "--walletdb",
      ALICE_WALLET
    )
  )

  val proveSimpleTransactionFromGenesis = Main.run(
    List(
      "simpletransaction",
      "prove",
      "--from-party",
      "noparty",
      "--from-contract",
      "genesis",
      "--from-state",
      "1",
      "-w",
      ALICE_PASSWORD,
      "--keyfile",
      ALICE_MAIN_KEY,
      "-n",
      "private",
      "-i",
      ALICE_FIRST_TX_RAW,
      "-o",
      ALICE_FIRST_TX_PROVED,
      "--walletdb",
      ALICE_WALLET
    )
  )

  def broadcastSimpleTx(provedTx: String, wallet: String) = Main.run(
    List(
      "simpletransaction",
      "broadcast",
      "-n",
      "private",
      "-i",
      provedTx,
      "-h",
      "localhost",
      "--bifrost-port",
      "9084",
      "--walletdb",
      wallet
    )
  )

  val BOB_WALLET = s"$TMP_DIR/bob_wallet.db"
  val BOB_MAIN_KEY = s"$TMP_DIR/bob_mainkey.json"
  val BOB_PASSWORD = "test"
  val BOB_FIRST_TX_RAW = s"$TMP_DIR/bob_first_tx.pbuf"
  val BOB_FIRST_TX_PROVED = s"$TMP_DIR/bob_first_tx_proved.pbuf"

  def createBobWallet() = Main.run(
    List(
      "wallet",
      "init",
      "-w",
      BOB_PASSWORD,
      "-n",
      "private",
      "-o",
      BOB_MAIN_KEY,
      "--walletdb",
      BOB_WALLET
    )
  )

  def addPartyToAliceWallet() = Main.run(
    List(
      "parties",
      "add",
      "--party-name",
      "alice_bob_0",
      "--walletdb",
      ALICE_WALLET,
      "-n",
      "private"
    )
  )

  def addPartyToBobWallet() = Main.run(
    List(
      "parties",
      "add",
      "--party-name",
      "alice_bob_0",
      "--walletdb",
      BOB_WALLET,
      "-n",
      "private"
    )
  )

  def addContractToAliceWallet() = Main.run(
    List(
      "contracts",
      "add",
      "--walletdb",
      ALICE_WALLET,
      "-n",
      "private",
      "--contract-name",
      "or_sign",
      "--contract-template",
      "threshold(1, sign(0) or sign(1))"
    )
  )

  def addContractToBobWallet() = Main.run(
    List(
      "contracts",
      "add",
      "--walletdb",
      BOB_WALLET,
      "-n",
      "private",
      "--contract-name",
      "or_sign",
      "--contract-template",
      "threshold(1, sign(1) or sign(0))"
    )
  )

  val ALICE_VK = s"$TMP_DIR/alice_vk.json"

  val BOB_VK = s"$TMP_DIR/bob_vk.json"

  def exportVkFromAlice() = Main.run(
    List(
      "wallet",
      "export-vk",
      "-w",
      ALICE_PASSWORD,
      "-o",
      ALICE_VK,
      "-n",
      "private",
      "--walletdb",
      ALICE_WALLET,
      "--party-name",
      "alice_bob_0",
      "--contract-name",
      "or_sign",
      "--keyfile",
      ALICE_MAIN_KEY
    )
  )

  def exportVkFromBob() = Main.run(
    List(
      "wallet",
      "export-vk",
      "-w",
      BOB_PASSWORD,
      "-o",
      BOB_VK,
      "-n",
      "private",
      "--walletdb",
      BOB_WALLET,
      "--party-name",
      "alice_bob_0",
      "--contract-name",
      "or_sign",
      "--keyfile",
      BOB_MAIN_KEY
    )
  )

  def importVkToALiceWallet() = Main.run(
    List(
      "wallet",
      "import-vks",
      "-n",
      "private",
      "--input-vks",
      BOB_VK,
      "--party-name",
      "alice_bob_0",
      "--contract-name",
      "or_sign",
      "--walletdb",
      ALICE_WALLET,
      "--keyfile",
      ALICE_MAIN_KEY,
      "-w",
      ALICE_PASSWORD
    )
  )
  def importVkToBobWallet() = Main.run(
    List(
      "wallet",
      "import-vks",
      "-n",
      "private",
      "--input-vks",
      ALICE_VK,
      "--party-name",
      "alice_bob_0",
      "--contract-name",
      "or_sign",
      "--walletdb",
      BOB_WALLET,
      "--keyfile",
      BOB_MAIN_KEY,
      "-w",
      BOB_PASSWORD
    )
  )

  val ALICE_SECOND_TX_RAW = s"$TMP_DIR/alice_second_tx.pbuf"

  val ALICE_SECOND_TX_PROVED = s"$TMP_DIR/alice_second_tx_proved.pbuf"

  val BOB_SECOND_TX_RAW = s"$TMP_DIR/bob_second_tx.pbuf"

  val BOB_SECOND_TX_PROVED = s"$TMP_DIR/bob_second_tx_proved.pbuf"

  def createSimpleTransactionFromSharedAccountToAlice(aliceAddress: String) =
    Main.run(
      List(
        "simpletransaction",
        "create",
        "--from-party",
        "alice_bob_0",
        "--from-contract",
        "or_sign",
        "-t",
        aliceAddress,
        "-w",
        BOB_PASSWORD,
        "--bifrost-port",
        "9084",
        "-o",
        BOB_SECOND_TX_RAW,
        "-n",
        "private",
        "-a",
        "200",
        "-h",
        "localhost",
        "--keyfile",
        BOB_MAIN_KEY,
        "--walletdb",
        BOB_WALLET
      )
    )

  def proveSimpleTransactionFromSharedAccountToAlice() = Main.run(
    List(
      "simpletransaction",
      "prove",
      "--from-party",
      "alice_bob_0",
      "--from-contract",
      "or_sign",
      "-w",
      BOB_PASSWORD,
      "--keyfile",
      BOB_MAIN_KEY,
      "-n",
      "private",
      "-i",
      BOB_SECOND_TX_RAW,
      "-o",
      BOB_SECOND_TX_PROVED,
      "--walletdb",
      BOB_WALLET
    )
  )

  def createSimpleTransactionFromAliceToSharedAccount() = Main.run(
    List(
      "simpletransaction",
      "create",
      "--from-party",
      "self",
      "--from-contract",
      "default",
      "--to-party",
      "alice_bob_0",
      "--to-contract",
      "or_sign",
      "-w",
      ALICE_PASSWORD,
      "--bifrost-port",
      "9084",
      "-o",
      ALICE_SECOND_TX_RAW,
      "-n",
      "private",
      "-a",
      "500",
      "-h",
      "localhost",
      "--keyfile",
      ALICE_MAIN_KEY,
      "--walletdb",
      ALICE_WALLET
    )
  )

  def proveSimpleTransactionFromAliceToSharedAccount() = Main.run(
    List(
      "simpletransaction",
      "prove",
      "--from-party",
      "self",
      "--from-contract",
      "default",
      "-w",
      ALICE_PASSWORD,
      "--keyfile",
      ALICE_MAIN_KEY,
      "-n",
      "private",
      "-i",
      ALICE_SECOND_TX_RAW,
      "-o",
      ALICE_SECOND_TX_PROVED,
      "--walletdb",
      ALICE_WALLET
    )
  )

  def queryAliceAccount() = Main.run(
    List(
      "genus-query",
      "utxo-by-address",
      "--from-party",
      "self",
      "--from-contract",
      "default",
      "-n",
      "private",
      "-h",
      "localhost",
      "--bifrost-port",
      "9084",
      "--walletdb",
      ALICE_WALLET
    )
  )
  def querySharedAccountForAlice() = Main.run(
    List(
      "genus-query",
      "utxo-by-address",
      "--from-party",
      "alice_bob_0",
      "--from-contract",
      "or_sign",
      "-n",
      "private",
      "-h",
      "localhost",
      "--bifrost-port",
      "9084",
      "--walletdb",
      ALICE_WALLET
    )
  )
  def querySharedAccountForBob() = Main.run(
    List(
      "genus-query",
      "utxo-by-address",
      "--from-party",
      "alice_bob_0",
      "--from-contract",
      "or_sign",
      "-n",
      "private",
      "-h",
      "localhost",
      "--bifrost-port",
      "9084",
      "--walletdb",
      BOB_WALLET
    )
  )

  override val munitTimeout = Duration(120, "s")

  tmpDirectory.test(
    "Move funds from genesis to alice"
  ) { _ =>
    import scala.concurrent.duration._
    assertIO(
      for {
        res <- createAliceWallet()
        ALICE_TO_ADDRESS <- walletController(ALICE_WALLET).currentaddress()
        _ <- IO.println(s"Alice's address is $ALICE_TO_ADDRESS")
        _ <- IO.println("Moving funds from genesis to alice")
        _ <- createSimpleTransactionFromGenesis(ALICE_TO_ADDRESS)
        _ <- IO.sleep(5.seconds)
        _ <- proveSimpleTransactionFromGenesis
        _ <- IO.sleep(5.seconds)
        _ <- broadcastSimpleTx(ALICE_FIRST_TX_PROVED, ALICE_WALLET)
        _ <- IO.println("Check alice's address (is contained in the change)")
        _ <- (for {
          _ <- queryAliceAccount()
          _ <- IO.sleep(5.second)
        } yield ()).replicateA(5)
      } yield res,
      ExitCode.Success
    )
  }

  test("Move funds from alice to shared account") {
    import scala.concurrent.duration._
    assertIO(
      for {
        _ <- IO.println("Create a wallet for bob")
        res <- createBobWallet()
        _ <- IO.println("Add bob to alice's wallet")
        _ <- addPartyToAliceWallet()
        _ <- IO.println("Add alice to bob's wallet")
        _ <- addPartyToBobWallet()
        _ <- IO.println("Add a contract to alice's wallet")
        _ <- addContractToAliceWallet()
        _ <- IO.println("Add a contract to bob's wallet")
        _ <- addContractToBobWallet()
        _ <- IO.println("Exporting VK from alice's wallet")
        _ <- exportVkFromAlice()
        _ <- IO.println("Exporting VK from bob's wallet")
        _ <- exportVkFromBob()
        _ <- IO.println("Importing VK to alice's wallet")
        _ <- importVkToALiceWallet()
        _ <- IO.sleep(5.seconds)
        _ <- IO.println("Importing VK to bob's wallet")
        _ <- importVkToBobWallet()
        _ <- IO.println("Moving funds (500 LVLs) from alice to shared account")
        _ <- createSimpleTransactionFromAliceToSharedAccount()
        _ <- IO.sleep(5.seconds)
        _ <- proveSimpleTransactionFromAliceToSharedAccount()
        _ <- IO.sleep(5.seconds)
        _ <- broadcastSimpleTx(ALICE_SECOND_TX_PROVED, ALICE_WALLET)
        _ <- IO.sleep(5.seconds)
        _ <- IO.println(
          "Check shared account for from alice's wallet, expected 500 LVLs"
        )
        _ <- (for {
          _ <- IO.println("Querying alice's shared account")
          _ <- querySharedAccountForAlice()
          _ <- IO.sleep(5.second)
        } yield ()).replicateA(5)
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
        res <- createSimpleTransactionFromSharedAccountToAlice(ALICE_TO_ADDRESS)
        _ <- IO.sleep(5.seconds)
        _ <- proveSimpleTransactionFromSharedAccountToAlice()
        _ <- IO.sleep(5.seconds)
        _ <- broadcastSimpleTx(BOB_SECOND_TX_PROVED, BOB_WALLET)
        _ <- IO.println(
          "Check shared account for from bob's wallet, expected 300 LVLs"
        )
        _ <- (for {
          _ <- querySharedAccountForBob()
          _ <- IO.sleep(5.second)
        } yield res).replicateA(5)
        _ <- IO.println("Sync alice's account")
        _ <- walletController(ALICE_WALLET)
          .sync(ALICE_MAIN_KEY, ALICE_PASSWORD, "alice_bob_0", "or_sign")
        _ <- (for {
          _ <- querySharedAccountForAlice()
          _ <- IO.sleep(5.second)
        } yield res).replicateA(5)
      } yield res,
      ExitCode.Success
    )
  }

}
