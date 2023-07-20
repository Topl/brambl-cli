package co.topl.brambl.cli

import cats.effect.IO
import co.topl.brambl.cli.controllers.WalletController
import co.topl.brambl.cli.modules.ChannelResourceModule
import co.topl.brambl.cli.modules.TransactionBuilderApiModule
import co.topl.brambl.cli.modules.WalletAlgebraModule
import co.topl.brambl.cli.modules.WalletManagementUtilsModule
import co.topl.brambl.cli.modules.WalletStateAlgebraModule
import co.topl.brambl.constants.NetworkConstants
import co.topl.brambl.dataApi.GenusQueryAlgebra
import cats.data.Kleisli
import cats.effect.ExitCode
import co.topl.brambl.models.LockAddress

trait CommonTxOperations
    extends TransactionBuilderApiModule
    with WalletStateAlgebraModule
    with WalletManagementUtilsModule
    with WalletAlgebraModule
    with ChannelResourceModule
    with BaseConstants {

  def syncWallet(
      contractName: String,
      partyName: String
  ) =
    Kleisli[IO, WalletKeyConfig, ExitCode]((c: WalletKeyConfig) =>
      Main.run(
        List(
          "wallet",
          "sync",
          "--contract-name",
          contractName,
          "--party-name",
          partyName,
          "--walletdb",
          c.walletFile,
          "-n",
          "private",
          "-h",
          HOST,
          "--bifrost-port",
          s"$BIFROST_PORT",
          "--keyfile",
          c.keyFile,
          "-w",
          c.password
        )
      )
    )

  def proveSimpleTransaction(
      fromParty: String,
      fromContract: String,
      someFromState: Option[Int],
      inputTx: String,
      outputFile: String
  ) = Kleisli[IO, WalletKeyConfig, ExitCode]((c: WalletKeyConfig) =>
    Main.run(
      List(
        "simpletransaction",
        "prove",
        "--from-party",
        fromParty,
        "--from-contract",
        fromContract,
        "-w",
        c.password,
        "--keyfile",
        c.keyFile,
        "-n",
        "private",
        "-i",
        inputTx,
        "-o",
        outputFile,
        "--walletdb",
        c.walletFile
      ) ++ someFromState
        .map(s => List("--from-state", s.toString()))
        .getOrElse(List.empty)
    )
  )

  def createSimpleTransactionToCartesianIdx(
      fromParty: String,
      fromContract: String,
      someFromState: Option[Int],
      toParty: String,
      toContract: String,
      amount: Int,
      outputFile: String
  ) =
    Kleisli[IO, WalletKeyConfig, ExitCode]((c: WalletKeyConfig) =>
      Main.run(
        List(
          "simpletransaction",
          "create",
          "--from-party",
          fromParty, // "alice_bob_0",
          "--from-contract",
          fromContract, // "or_sign",
          "--to-party",
          toParty,
          "--to-contract",
          toContract,
          "-w",
          c.password,
          "--bifrost-port",
          s"$BIFROST_PORT",
          "-o",
          outputFile, // BOB_SECOND_TX_RAW,
          "-n",
          "private",
          "-a",
          amount.toString(),
          "-h",
          HOST,
          "--keyfile",
          c.keyFile,
          "--walletdb",
          c.walletFile
        ) ++ someFromState
          .map(s => List("--from-state", s.toString()))
          .getOrElse(List.empty)
      )
    )
  def createSimpleTransactionToAddress(
      fromParty: String,
      fromContract: String,
      someFromState: Option[Int],
      aliceAddress: String,
      amount: Int,
      outputFile: String
  ) =
    Kleisli[IO, WalletKeyConfig, ExitCode]((c: WalletKeyConfig) =>
      Main.run(
        List(
          "simpletransaction",
          "create",
          "--from-party",
          fromParty, // "alice_bob_0",
          "--from-contract",
          fromContract, // "or_sign",
          "-t",
          aliceAddress,
          "-w",
          c.password,
          "--bifrost-port",
          s"$BIFROST_PORT",
          "-o",
          outputFile, // BOB_SECOND_TX_RAW,
          "-n",
          "private",
          "-a",
          amount.toString(),
          "-h",
          HOST,
          "--keyfile",
          c.keyFile,
          "--walletdb",
          c.walletFile
        ) ++ someFromState
          .map(s => List("--from-state", s.toString()))
          .getOrElse(List.empty)
      )
    )

  def queryAccount(
      partyName: String,
      contractName: String,
      someFromState: Option[Int] = None
  ) =
    Kleisli[IO, WalletKeyConfig, ExitCode]((c: WalletKeyConfig) =>
      Main.run(
        List(
          "genus-query",
          "utxo-by-address",
          "--from-party",
          partyName,
          "--from-contract",
          contractName,
          "-n",
          "private",
          "-h",
          HOST,
          "--bifrost-port",
          s"$BIFROST_PORT",
          "--walletdb",
          c.walletFile
        ) ++ someFromState
          .map(s => List("--from-state", s.toString()))
          .getOrElse(List.empty)
      )
    )

  def exportVk(partyName: String, contractName: String, vkFile: String) =
    Kleisli[IO, WalletKeyConfig, ExitCode]((c: WalletKeyConfig) =>
      Main.run(
        List(
          "wallet",
          "export-vk",
          "-w",
          c.password,
          "-o",
          vkFile,
          "-n",
          "private",
          "--walletdb",
          c.walletFile,
          "--party-name",
          partyName,
          "--contract-name",
          contractName,
          "--keyfile",
          c.keyFile
        )
      )
    )

  def importVk(partyName: String, contractName: String, vkFile: String) =
    Kleisli[IO, WalletKeyConfig, ExitCode]((c: WalletKeyConfig) =>
      Main.run(
        List(
          "wallet",
          "import-vks",
          "-n",
          "private",
          "--input-vks",
          vkFile,
          "--party-name",
          partyName,
          "--contract-name",
          contractName,
          "--walletdb",
          c.walletFile,
          "--keyfile",
          c.keyFile,
          "-w",
          c.password
        )
      )
    )

  def addPartyToWallet(partyName: String) =
    Kleisli[IO, WalletKeyConfig, ExitCode]((c: WalletKeyConfig) =>
      Main.run(
        List(
          "parties",
          "add",
          "--party-name",
          partyName,
          "--walletdb",
          c.walletFile,
          "-n",
          "private"
        )
      )
    )

  def addContractToWallet(
      contractName: String,
      contractTemplate: String
  ) =
    Kleisli[IO, WalletKeyConfig, ExitCode]((c: WalletKeyConfig) =>
      Main.run(
        List(
          "contracts",
          "add",
          "--walletdb",
          c.walletFile,
          "-n",
          "private",
          "--contract-name",
          contractName,
          "--contract-template",
          contractTemplate
        )
      )
    )

  def createWallet() =
    Kleisli[IO, WalletKeyConfig, ExitCode]((c: WalletKeyConfig) =>
      Main.run(
        List(
          "wallet",
          "init",
          "-w",
          c.password,
          "-n",
          "private",
          "-o",
          c.keyFile,
          "--walletdb",
          c.walletFile,
          "--mnemonicfile",
          c.mnemonicFile
        )
      )
    )
  def recoverWallet(mnemonic: String) =
    Kleisli[IO, WalletKeyConfig, ExitCode]((c: WalletKeyConfig) =>
      Main.run(
        List(
          "wallet",
          "recover-keys",
          "-w",
          c.password,
          "-n",
          "private",
          "-o",
          c.keyFile,
          "--walletdb",
          c.walletFile,
          "--mnemonic",
          mnemonic
        )
      )
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
          HOST,
          BIFROST_PORT
        )
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
      HOST,
      "--bifrost-port",
      s"$BIFROST_PORT",
      "--walletdb",
      wallet
    )
  )
}
