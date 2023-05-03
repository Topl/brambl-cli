package co.topl.brambl.cli.validation

import cats.data.ValidatedNel
import co.topl.brambl.cli.BramblCliMode
import co.topl.brambl.cli.BramblCliParams
import co.topl.brambl.cli.BramblCliSubCmd
import co.topl.brambl.cli.BramblCliValidatedParams
import co.topl.brambl.cli.validation.WalletValidationModule

object BramblCliParamsValidatorModule
    extends CommonValidationModule
    with WalletValidationModule {

  def validateSpecificParams(
      mode: BramblCliMode.Value,
      subcmd: BramblCliSubCmd.Value,
      paramConfig: BramblCliParams
  ) = {
    (mode, subcmd) match {
      case (BramblCliMode.wallet, BramblCliSubCmd.init) =>
        validateKeyGenerationParams(paramConfig).map(_ => (mode, subcmd))
      case (BramblCliMode.utxo, BramblCliSubCmd.query) =>
        import cats.implicits._
        (mode, subcmd).validNel
    }
  }

  def validateParams(
      paramConfig: BramblCliParams
  ): ValidatedNel[String, BramblCliValidatedParams] = {
    import cats.implicits._
    (
      validateMode(paramConfig.mode)
        .andThen(mode =>
          validateSubCmd(mode, paramConfig.subcmd).map((mode, _))
        )
        .andThen(modeAndSubCmd =>
          validateSpecificParams(
            modeAndSubCmd._1,
            modeAndSubCmd._2,
            paramConfig
          )
        ),
      validateOutputfile(paramConfig.someOutputFile),
      validateInputFile(paramConfig.someInputFile)
    )
      .mapN(
        (
            modeAndSubCmd,
            someOutputFile,
            someInputFile
        ) => {
          BramblCliValidatedParams(
            mode = modeAndSubCmd._1,
            subcmd = modeAndSubCmd._2,
            password = paramConfig.password,
            somePassphrase = paramConfig.somePassphrase,
            someOutputFile = someOutputFile,
            someInputFile = someInputFile,
            coordinates = paramConfig.coordinates
          ).validNel
        }
      )
      .andThen(x => x)
  }

}
