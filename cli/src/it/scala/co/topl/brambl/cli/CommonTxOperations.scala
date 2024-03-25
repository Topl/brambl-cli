package co.topl.brambl.cli

import cats.data.Kleisli
import cats.effect.ExitCode
import cats.effect.IO
import co.topl.brambl.cli.controllers.WalletController
import co.topl.brambl.cli.modules.TransactionBuilderApiModule
import co.topl.brambl.cli.modules.WalletAlgebraModule
import co.topl.brambl.cli.modules.WalletManagementUtilsModule
import co.topl.brambl.cli.modules.WalletStateAlgebraModule
import co.topl.brambl.dataApi.{GenusQueryAlgebra, RpcChannelResource}

trait CommonTxOperations
    extends TransactionBuilderApiModule
    with WalletStateAlgebraModule
    with WalletManagementUtilsModule
    with WalletAlgebraModule
    with RpcChannelResource
    with BaseConstants {

  def syncWallet(
      fellowshipName: String,
      templateName: String
  ) =
    Kleisli[IO, WalletKeyConfig, ExitCode]((c: WalletKeyConfig) =>
      Main.run(
        List(
          "wallet",
          "sync",
          "--template-name",
          templateName,
          "--fellowship-name",
          fellowshipName,
          "--walletdb",
          c.walletFile,
          "-n",
          "private",
          "-h",
          HOST,
          "--port",
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
        "tx",
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
      fromFellowship: String,
      fromTemplate: String,
      someFromInteraction: Option[Int],
      someChangeFellowship: Option[String],
      someChangeTemplate: Option[String],
      someChangeInteraction: Option[Int],
      toFellowship: String,
      toTemplate: String,
      amount: Int,
      fee: Int,
      outputFile: String,
      token: TokenType.Value,
      someGroupId: Option[String],
      someSeriesId: Option[String]
  ) =
    Kleisli[IO, WalletKeyConfig, ExitCode]((c: WalletKeyConfig) =>
      Main.run(
        List(
          "simple-transaction",
          "create",
          "--from-fellowship",
          fromFellowship, // "alice_bob_0",
          "--from-template",
          fromTemplate, // "or_sign",
          "--to-fellowship",
          toFellowship,
          "--to-template",
          toTemplate,
          "-w",
          c.password,
          "--port",
          s"$BIFROST_PORT",
          "-o",
          outputFile, // BOB_SECOND_TX_RAW,
          "-n",
          "private",
          "-a",
          amount.toString(),
          "-h",
          HOST,
          "--fee",
          fee.toString(),
          "--keyfile",
          c.keyFile,
          "--walletdb",
          c.walletFile,
          "--transfer-token",
          token.toString()
        ) ++ someFromInteraction
          .map(s => List("--from-interaction", s.toString()))
          .getOrElse(List.empty)
          ++ someGroupId
            .map(s => List("--group-id", s.toString()))
            .getOrElse(List.empty)
          ++ someSeriesId
            .map(s => List("--series-id", s.toString()))
            .getOrElse(List.empty)
          ++ someChangeFellowship
            .map(s => List("--change-fellowship", s.toString()))
            .getOrElse(List.empty)
          ++ someChangeTemplate
            .map(s => List("--change-template", s.toString()))
            .getOrElse(List.empty)
          ++ someChangeInteraction
            .map(s => List("--change-interaction", s.toString()))
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
          "--port",
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
      fromFellowship: String,
      fromTemplate: String,
      someFromInteraction: Option[Int],
      someChangeFellowship: Option[String],
      someChangeTemplate: Option[String],
      someChangeInteraction: Option[Int],
      aliceAddress: String,
      amount: Int,
      fee: Int,
      outputFile: String,
      token: TokenType.Value,
      someGroupId: Option[String],
      someSeriesId: Option[String],
      secure: Boolean = false
  ) =
    Kleisli[IO, WalletKeyConfig, ExitCode] { (c: WalletKeyConfig) =>
      Main.run(
        List(
          "simple-transaction",
          "create",
          "--from-fellowship",
          fromFellowship, // "alice_bob_0",
          "--from-template",
          fromTemplate, // "or_sign",
          "-t",
          aliceAddress,
          "-w",
          c.password,
          "--port",
          s"$BIFROST_PORT",
          "-o",
          outputFile, // BOB_SECOND_TX_RAW,
          "-a",
          amount.toString(),
          "--fee",
          fee.toString(),
          "-h",
          HOST,
          "-n",
          "private",
          "--keyfile",
          c.keyFile,
          "--walletdb",
          c.walletFile,
          "--transfer-token",
          token.toString()
        ) ++ someFromInteraction
          .map(s => List("--from-interaction", s.toString()))
          .getOrElse(List.empty)
          ++ someGroupId
            .map(s => List("--group-id", s.toString()))
            .getOrElse(List.empty)
          ++ someSeriesId
            .map(s => List("--series-id", s.toString()))
            .getOrElse(List.empty)
          ++ someChangeFellowship
            .map(s => List("--change-fellowship", s.toString()))
            .getOrElse(List.empty)
          ++ someChangeTemplate
            .map(s => List("--change-template", s.toString()))
            .getOrElse(List.empty)
          ++ someChangeInteraction
            .map(s => List("--change-interaction", s.toString()))
            .getOrElse(List.empty)
          ++ (if (secure) List("--secure", "true") else List.empty)
      )
    }

  def createSimpleGroupMintingTransaction(
      fromFellowship: String,
      fromTemplate: String,
      someFromInteraction: Option[Int],
      amount: Long,
      fee: Long,
      groupPolicy: String,
      outputFile: String,
      secure: Boolean = false
  ) =
    Kleisli[IO, WalletKeyConfig, ExitCode]((c: WalletKeyConfig) =>
      Main.run(
        List(
          "simple-minting",
          "create",
          "--from-fellowship",
          fromFellowship,
          "--from-template",
          fromTemplate,
          "-h",
          HOST,
          "--port",
          s"$BIFROST_PORT",
          "-n",
          "private",
          "--keyfile",
          c.keyFile,
          "-w",
          c.password,
          "-o",
          outputFile,
          "-i",
          groupPolicy,
          "--mint-amount",
          amount.toString(),
          "--fee",
          fee.toString(),
          "--walletdb",
          c.walletFile,
          "--mint-token",
          "group"
        ) ++ someFromInteraction
          .map(s => List("--from-interaction", s.toString()))
          .getOrElse(List.empty)
          ++ (if (secure) List("--secure", "true") else List.empty)
      )
    )
  def createSimpleSeriesMintingTransaction(
      fromFellowship: String,
      fromTemplate: String,
      someFromInteraction: Option[Int],
      amount: Long,
      fee: Long,
      seriesPolicy: String,
      outputFile: String,
      secure: Boolean = false
  ) =
    Kleisli[IO, WalletKeyConfig, ExitCode]((c: WalletKeyConfig) =>
      Main.run(
        List(
          "simple-minting",
          "create",
          "--from-fellowship",
          fromFellowship,
          "--from-template",
          fromTemplate,
          "-h",
          HOST,
          "--port",
          s"$BIFROST_PORT",
          "-n",
          "private",
          "--keyfile",
          c.keyFile,
          "-w",
          c.password,
          "-o",
          outputFile,
          "-i",
          seriesPolicy,
          "--mint-amount",
          amount.toString(),
          "--fee",
          fee.toString(),
          "--walletdb",
          c.walletFile,
          "--mint-token",
          "series"
        ) ++ someFromInteraction
          .map(s => List("--from-interaction", s.toString()))
          .getOrElse(List.empty)
          ++ (if (secure) List("--secure", "true") else List.empty)
      )
    )
  def createSimpleAssetMintingTransaction(
      fromFellowship: String,
      fromTemplate: String,
      someFromInteraction: Option[Int],
      fee: Long,
      assetMintingStatement: String,
      outputFile: String,
      ephemeralMetadata: String,
      secure: Boolean = false
  ) =
    Kleisli[IO, WalletKeyConfig, ExitCode]((c: WalletKeyConfig) =>
      Main.run(
        List(
          "simple-minting",
          "create",
          "--from-fellowship",
          fromFellowship,
          "--from-template",
          fromTemplate,
          "-h",
          HOST,
          "--port",
          s"$BIFROST_PORT",
          "-n",
          "private",
          "--keyfile",
          c.keyFile,
          "-w",
          c.password,
          "-o",
          outputFile,
          "-i",
          assetMintingStatement,
          "--fee",
          fee.toString(),
          "--walletdb",
          c.walletFile,
          "--mint-token",
          "asset",
          "--commitment",
          "3e8fd1ed52e0c8107f3265da13a42b323a492d334b6da23b0f1ef279b988a225",
          "--ephemeralMetadata",
          ephemeralMetadata
        ) ++ someFromInteraction
          .map(s => List("--from-interaction", s.toString()))
          .getOrElse(List.empty)
          ++ (if (secure) List("--secure", "true") else List.empty)
      )
    )

  def queryAccount(
      fellowshipName: String,
      templateName: String,
      someFromInteraction: Option[Int] = None,
      secure: Boolean = false
  ) =
    Kleisli[IO, WalletKeyConfig, ExitCode]((c: WalletKeyConfig) =>
      Main.run(
        List(
          "genus-query",
          "utxo-by-address",
          "--from-fellowship",
          fellowshipName,
          "--from-template",
          templateName,
          "-h",
          HOST,
          "--port",
          s"$BIFROST_PORT",
          "--walletdb",
          c.walletFile,
          "--token",
          "lvl"
        ) ++ someFromInteraction
          .map(s => List("--from-interaction", s.toString()))
          .getOrElse(List.empty)
          ++ (if (secure) List("--secure", "true") else List.empty)
      )
    )
  def queryAccountAllTokens(
      fellowshipName: String,
      templateName: String,
      someFromInteraction: Option[Int] = None
  ) =
    Kleisli[IO, WalletKeyConfig, ExitCode]((c: WalletKeyConfig) =>
      Main.run(
        List(
          "genus-query",
          "utxo-by-address",
          "--from-fellowship",
          fellowshipName,
          "--from-template",
          templateName,
          "-h",
          HOST,
          "--port",
          s"$BIFROST_PORT",
          "--walletdb",
          c.walletFile,
          "--token",
          "all"
        ) ++ someFromInteraction
          .map(s => List("--from-interaction", s.toString()))
          .getOrElse(List.empty)
      )
    )
  def queryAccountGroupTokens(
      fellowshipName: String,
      templateName: String,
      someFromInteraction: Option[Int] = None,
      secure: Boolean = false
  ) =
    Kleisli[IO, WalletKeyConfig, ExitCode]((c: WalletKeyConfig) =>
      Main.run(
        List(
          "genus-query",
          "utxo-by-address",
          "--from-fellowship",
          fellowshipName,
          "--from-template",
          templateName,
          "-h",
          HOST,
          "--port",
          s"$BIFROST_PORT",
          "--walletdb",
          c.walletFile,
          "--token",
          "group"
        ) ++ someFromInteraction
          .map(s => List("--from-interaction", s.toString()))
          .getOrElse(List.empty)
          ++ (if (secure) List("--secure", "true") else List.empty)
      )
    )
  def queryAccountSeriesTokens(
      fellowshipName: String,
      templateName: String,
      someFromInteraction: Option[Int] = None,
      secure: Boolean = false
  ) =
    Kleisli[IO, WalletKeyConfig, ExitCode]((c: WalletKeyConfig) =>
      Main.run(
        List(
          "genus-query",
          "utxo-by-address",
          "--from-fellowship",
          fellowshipName,
          "--from-template",
          templateName,
          "-h",
          HOST,
          "--port",
          s"$BIFROST_PORT",
          "--walletdb",
          c.walletFile,
          "--token",
          "series"
        ) ++ someFromInteraction
          .map(s => List("--from-interaction", s.toString()))
          .getOrElse(List.empty)
          ++ (if (secure) List("--secure", "true") else List.empty)
      )
    )
  def queryAccountAssetTokens(
      fellowshipName: String,
      templateName: String,
      someFromInteraction: Option[Int] = None,
      secure: Boolean = false
  ) =
    Kleisli[IO, WalletKeyConfig, ExitCode]((c: WalletKeyConfig) =>
      Main.run(
        List(
          "genus-query",
          "utxo-by-address",
          "--from-fellowship",
          fellowshipName,
          "--from-template",
          templateName,
          "-h",
          HOST,
          "--port",
          s"$BIFROST_PORT",
          "--walletdb",
          c.walletFile,
          "--token",
          "asset"
        ) ++ someFromInteraction
          .map(s => List("--from-interaction", s.toString()))
          .getOrElse(List.empty)
          ++ (if (secure) List("--secure", "true") else List.empty)
      )
    )

  def exportVk(fellowshipName: String, templateName: String, vkFile: String) =
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
          "--fellowship-name",
          fellowshipName,
          "--template-name",
          templateName,
          "--keyfile",
          c.keyFile
        )
      )
    )

  def exportFinalVk(
      fellowshipName: String,
      templateName: String,
      interaction: Int,
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
          "--fellowship-name",
          fellowshipName,
          "--template-name",
          templateName,
          "--interaction",
          interaction.toString(),
          "--keyfile",
          c.keyFile
        )
      )
    )

  def importVk(fellowshipName: String, templateName: String, vkFile: String) =
    Kleisli[IO, WalletKeyConfig, ExitCode]((c: WalletKeyConfig) =>
      Main.run(
        List(
          "wallet",
          "import-vks",
          "--input-vks",
          vkFile,
          "--fellowship-name",
          fellowshipName,
          "--template-name",
          templateName,
          "--walletdb",
          c.walletFile,
          "--keyfile",
          c.keyFile,
          "-w",
          c.password
        )
      )
    )

  def addFellowshipToWallet(fellowshipName: String) =
    Kleisli[IO, WalletKeyConfig, ExitCode]((c: WalletKeyConfig) =>
      Main.run(
        List(
          "fellowships",
          "add",
          "--fellowship-name",
          fellowshipName,
          "--walletdb",
          c.walletFile
        )
      )
    )

  def addTemplateToWallet(
      templateName: String,
      templateTemplate: String
  ) =
    Kleisli[IO, WalletKeyConfig, ExitCode]((c: WalletKeyConfig) =>
      Main.run(
        List(
          "templates",
          "add",
          "--walletdb",
          c.walletFile,
          "--template-name",
          templateName,
          "--lock-template",
          templateTemplate
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

  def addTemplate(templateName: String, template: String) =
    Kleisli[IO, WalletKeyConfig, ExitCode]((c: WalletKeyConfig) =>
      Main.run(
        List(
          "templates",
          "add",
          "--walletdb",
          c.walletFile,
          "--template-name",
          templateName,
          "--lock-template",
          template
        )
      )
    )
  def addSecret(secret: String, digestAlgorithm: String) =
    Kleisli[IO, WalletKeyConfig, ExitCode]((c: WalletKeyConfig) =>
      Main.run(
        List(
          "wallet",
          "add-secret",
          "--walletdb",
          c.walletFile,
          "--secret",
          secret,
          "--digest",
          digestAlgorithm
        )
      )
    )
  def getPreimage(digestText: String, digestAlgorithm: String) =
    Kleisli[IO, WalletKeyConfig, ExitCode]((c: WalletKeyConfig) =>
      Main.run(
        List(
          "wallet",
          "get-preimage",
          "--walletdb",
          c.walletFile,
          "--digest-text",
          digestText,
          "--digest",
          digestAlgorithm
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
        BIFROST_PORT,
        false
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

  def broadcastSimpleTx(provedTx: String, secure: Boolean = false) = Main.run(
    List(
      "tx",
      "broadcast",
      "-n",
      "private",
      "-i",
      provedTx,
      "-h",
      HOST,
      "--port",
      s"$BIFROST_PORT"
    ) ++ (if (secure) List("--secure", "true") else List.empty)
  )
}
