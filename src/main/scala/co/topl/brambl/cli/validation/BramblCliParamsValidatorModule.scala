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
        )
      )
      .map((modeAndSubCmd) => {
        modeAndSubCmd match {
          case (BramblCliMode.key, BramblCliSubCmd.generate) =>
            BramblCliValidatedParams(
              mode = BramblCliMode.key,
              subcmd = BramblCliSubCmd.generate
            ).validNel
        }
      })
      .andThen(x => x)
  }

}
