package co.topl.brambl.cli

import cats.data.Kleisli
import cats.effect.ExitCode
import cats.effect.IO
import co.topl.brambl.cli.controllers.WalletController
import co.topl.brambl.cli.modules.ChannelResourceModule
import co.topl.brambl.cli.modules.TransactionBuilderApiModule
import co.topl.brambl.cli.modules.WalletAlgebraModule
import co.topl.brambl.cli.modules.WalletManagementUtilsModule
import co.topl.brambl.cli.modules.WalletStateAlgebraModule
import co.topl.brambl.dataApi.GenusQueryAlgebra

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
      inputTx: String,
      outputFile: String
  ) = Kleisli[IO, WalletKeyConfig, ExitCode]((c: WalletKeyConfig) =>
    Main.run(
      List(
        "simpletransaction",
        "prove",
        "-w",
        c.password,
        "--keyfile",
        c.keyFile,
        "-i",
        inputTx,
        "-o",
        outputFile,
        "--walletdb",
        c.walletFile
      )
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

  def createComplexTransactionToAddress(
      inputFile: String,
      outputFile: String
  ) =
    Kleisli[IO, WalletKeyConfig, ExitCode](_ =>
      Main.run(
        List(
          "tx",
          "create",
          "-i",
          inputFile,
          "--bifrost-port",
          "9084",
          "-o",
          outputFile, // BOB_SECOND_TX_RAW,
          "-n",
          "private",
          "-h",
          "localhost"
        )
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
          "-h",
          HOST,
          "--bifrost-port",
          s"$BIFROST_PORT",
          "--walletdb",
          c.walletFile,
          "--token",
          "lvl"
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

  def exportFinalVk(
      partyName: String,
      contractName: String,
      state: Int,
      vkFile: String
  ) =
    Kleisli[IO, WalletKeyConfig, ExitCode]((c: WalletKeyConfig) =>
      Main.run(
        List(
          "wallet",
          "export-vk",
          "-w",
          c.password,
          "-o",
          vkFile,
          "--walletdb",
          c.walletFile,
          "--party-name",
          partyName,
          "--contract-name",
          contractName,
          "--state",
          state.toString(),
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
          c.password,
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
          c.walletFile
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
          "--newwalletdb",
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
          "--newwalletdb",
          c.walletFile,
          "--mnemonic",
          mnemonic
        )
      )
    )

  val genusQueryAlgebra = GenusQueryAlgebra
    .make[IO](
      channelResource(
        HOST,
        BIFROST_PORT
      )
    )

  def walletController(walletFile: String) = new WalletController(
    walletStateAlgebra(
      walletFile
    ),
    walletManagementUtils,
    walletApi,
    walletAlgebra(walletFile),
    genusQueryAlgebra
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
      s"$BIFROST_PORT"
    )
  )
}
