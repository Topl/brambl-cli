package co.topl.brambl.cli.validation

import cats.data.ValidatedNel
import co.topl.brambl.cli.BramblCliMode
import co.topl.brambl.cli.BramblCliParams
import co.topl.brambl.cli.BramblCliSubCmd
import co.topl.brambl.cli.BramblCliValidatedParams
import co.topl.brambl.cli.NetworkIdentifiers
import co.topl.brambl.cli.Privatenet
import co.topl.brambl.codecs.AddressCodecs

object BramblCliParamsValidatorModule
    extends SimpleTransactionValidationModule {

  def validateSpecificParams(
      mode: BramblCliMode.Value,
      subcmd: BramblCliSubCmd.Value,
      paramConfig: BramblCliParams
  ) = {
    (mode, subcmd) match {
      case (BramblCliMode.wallet, BramblCliSubCmd.currentaddress) =>
        import cats.implicits._
        (mode, subcmd).validNel
      case (BramblCliMode.simpletransaction, BramblCliSubCmd.create) =>
        validateSimpleTransactionCreateParams(paramConfig).map(_ =>
          (mode, subcmd)
        )
      case (BramblCliMode.invalid, BramblCliSubCmd.invalid) =>
        import cats.implicits._
        "Invalid mode and subcmd".invalidNel
      case (BramblCliMode.invalid, _) =>
        import cats.implicits._
        "Invalid mode".invalidNel
      case (_, BramblCliSubCmd.invalid) =>
        import cats.implicits._
        "Invalid subcmd".invalidNel
      case (_, _) =>
        import cats.implicits._
        (mode, subcmd).validNel
    }
  }

  def validateParams(
      paramConfig: BramblCliParams
  ): ValidatedNel[String, BramblCliValidatedParams] = {
    import cats.implicits._
    validateSpecificParams(
      paramConfig.mode,
      paramConfig.subcmd,
      paramConfig
    ).map(
      (
        modeAndSubCmd
      ) => {
        BramblCliValidatedParams(
          mode = modeAndSubCmd._1,
          subcmd = modeAndSubCmd._2,
          tokenType = paramConfig.tokenType,
          network = NetworkIdentifiers
            .fromString(paramConfig.network) // this was validated before
            .getOrElse(Privatenet),
          password = paramConfig.password,
          walletFile = paramConfig.someWalletFile.getOrElse(""),
          toAddress = paramConfig.toAddress.map(x =>
            AddressCodecs
              .decodeAddress(x)
              .toOption
              .get
          ), // this was validated before
          someToParty = paramConfig.someToParty,
          someToContract = paramConfig.someToContract,
          partyName = paramConfig.partyName,
          contractName = paramConfig.contractName,
          lockTemplate = paramConfig.lockTemplate,
          inputVks = paramConfig.inputVks.map(new java.io.File(_)),
          fromParty = paramConfig.someFromParty.getOrElse("self"),
          fromContract = paramConfig.someFromContract.getOrElse("default"),
          someFromState = paramConfig.someFromState.map(_.toInt),
          bifrostPort = paramConfig.bifrostPort,
          host = paramConfig.host,
          amount = paramConfig.amount,
          height = paramConfig.height,
          blockId = paramConfig.blockId,
          transactionId = paramConfig.transactionId,
          someKeyFile = paramConfig.someKeyFile,
          somePassphrase = paramConfig.somePassphrase,
          someOutputFile = paramConfig.someOutputFile,
          someInputFile = paramConfig.someInputFile,
          mnemonic = paramConfig.mnemonic.toIndexedSeq,
          someMnemonicFile = paramConfig.someMnemonicFile
        ).validNel
      }
    ).andThen(x => x)
  }

}
