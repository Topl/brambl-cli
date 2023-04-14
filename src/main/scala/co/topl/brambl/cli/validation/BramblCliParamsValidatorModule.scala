package co.topl.brambl.cli.validation

import cats.data.ValidatedNel
import co.topl.brambl.cli.BramblCliValidatedParams
import co.topl.brambl.cli.BramblCliParams
import co.topl.brambl.cli.BramblCliMode
import co.topl.brambl.cli.BramblCliSubCmd

import co.topl.brambl.cli.validation.KeyValidationModule

object BramblCliParamsValidatorModule
    extends CommonValidationModule
    with KeyValidationModule {

  def validateParams(
      paramConfig: BramblCliParams
  ): ValidatedNel[String, BramblCliValidatedParams] = {
    import cats.implicits._
    (
      validateMode(paramConfig.mode)
        .andThen(mode =>
          validateSubCmd(mode, paramConfig.subcmd).map((mode, _))
        ),
      validatePassword(paramConfig.password),
      validatePassphrase(paramConfig.somePassphrase),
      validateOutputfile(paramConfig.someOutputFile),
      validateInputFile(paramConfig.someInputFile)
    )
      .mapN(
        (
            modeAndSubCmd,
            password,
            somePassphrase,
            someOutputFile,
            someInputFile
        ) => {
          modeAndSubCmd match {
            case (BramblCliMode.key, BramblCliSubCmd.generate) =>
              BramblCliValidatedParams(
                mode = BramblCliMode.key,
                subcmd = BramblCliSubCmd.generate,
                password = password,
                somePassphrase = somePassphrase,
                someOutputFile = someOutputFile,
                someInputFile = someInputFile
              ).validNel
            case (BramblCliMode.key, BramblCliSubCmd.derive) =>
              BramblCliValidatedParams(
                mode = BramblCliMode.key,
                subcmd = BramblCliSubCmd.derive,
                password = password,
                coordinates = paramConfig.coordinates,
                somePassphrase = somePassphrase,
                someOutputFile = someOutputFile,
                someInputFile = someInputFile
              ).validNel
          }
        }
      )
      .andThen(x => x)
  }

}
