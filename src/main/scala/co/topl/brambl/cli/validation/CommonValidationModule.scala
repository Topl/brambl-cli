package co.topl.brambl.cli.validation

import scala.util.Try
import cats.data.Validated
import cats.data.ValidatedNel
import co.topl.brambl.cli.BramblCliMode
import co.topl.brambl.cli.BramblCliSubCmd

trait CommonValidationModule {

  
  def validateMode(mode: String) = {
    Try(BramblCliMode.withName(mode)).toOption match {
      case Some(mode) => Validated.validNel(mode)
      case None =>
        Validated.invalidNel(
          "Invalid mode. Valid values are " + BramblCliMode.values.mkString(
            ", "
          )
        )
    }
  }

  def validateSubCmd(
      mode: BramblCliMode.BramblCliMode,
      subcmd: String
  ): ValidatedNel[String, BramblCliSubCmd.Value] = {
    mode match {
      case BramblCliMode.key =>
          ???
    }
  }



}
