package co.topl.brambl.cli.validation

import cats.data.ValidatedNel
import co.topl.brambl.cli.BramblCliValidatedParams
import co.topl.brambl.cli.BramblCliParams
import co.topl.brambl.cli.BramblCliMode
import co.topl.brambl.cli.BramblCliSubCmd

object BramblCliParamsValidatorModule
    extends CommonValidationModule
    with WalletValidationModule {

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
            ???
        }
      })
      .andThen(x => x)
  }

}
