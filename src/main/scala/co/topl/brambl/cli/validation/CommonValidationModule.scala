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
        Try(BramblCliSubCmd.withName(subcmd)).toOption match {
          case Some(subcmd) => Validated.validNel(subcmd)
          case None =>
            Validated.invalidNel(
              "Invalid subcmd. Valid values are " + BramblCliSubCmd.values
                .mkString(
                  ", "
                )
            )
        }
    }
  }

  def validatePassword(password: String) = {
    if (password.trim().length >= 0) {
      Validated.validNel(password)
    } else {
      Validated.invalidNel(
        "Password must not be empty"
      )
    }
  }

  def validateOutputfile(someOutputFile: Option[String]): ValidatedNel[String, Option[String]] = {
    someOutputFile match {
      case Some(outputFile) =>
        if (outputFile.trim().length >= 0) {
          Validated.validNel(Some(outputFile))
        } else {
          Validated.invalidNel(
            "Output file must not be empty"
          )
        }
      case None => Validated.validNel(None)
    }
  }

  def validatePassphrase(somePassphrase: Option[String]): ValidatedNel[String, Option[String]] = {
    somePassphrase match {
      case Some(passphrase) =>
        if (passphrase.trim().length >= 0) {
          Validated.validNel(Some(passphrase))
        } else {
          Validated.invalidNel(
            "Passphrase must not be empty"
          )
        }
      case None => Validated.validNel(None)
    }
  }

}
