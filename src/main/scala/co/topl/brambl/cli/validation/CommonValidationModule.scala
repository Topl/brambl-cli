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

  def checkValidSubCmd(
      mode: BramblCliMode.BramblCliMode,
      subcmd: String,
      validSubCmds: Set[BramblCliSubCmd.Value]
  ) = {
    Try(BramblCliSubCmd.withName(subcmd)).toOption match {
      case Some(subcmd) =>
        if (validSubCmds.contains(subcmd)) {
          Validated.validNel(subcmd)
        } else {
          Validated.invalidNel(
            s"Invalid $mode for utxo mode. Valid values are " + validSubCmds
              .mkString(
                ", "
              )
          )
        }
      case None =>
        Validated.invalidNel(
          "Invalid subcmd. Valid values are " + BramblCliSubCmd.values.mkString(
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
      case BramblCliMode.utxo =>
        checkValidSubCmd(mode, subcmd, Set(BramblCliSubCmd.query))
      case BramblCliMode.wallet =>
        checkValidSubCmd(
          mode,
          subcmd,
          Set(BramblCliSubCmd.init)
        )
    }
  }

  def validatePassword(password: String) = {
    if (password.trim().length > 0) {
      Validated.validNel(password)
    } else {
      Validated.invalidNel(
        "Password must not be empty"
      )
    }
  }

  def validateOutputfile(
      someOutputFile: Option[String]
  ): ValidatedNel[String, Option[String]] = {
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

  def validateInputFile(
      someInputFile: Option[String]
  ): ValidatedNel[String, Option[String]] = {
    someInputFile match {
      case Some(inputFile) =>
        if (inputFile.trim().length >= 0) {
          // check if the file exists
          if (new java.io.File(inputFile).exists) {
            Validated.validNel(Some(inputFile))
          } else {
            Validated.invalidNel(
              "Input file does not exist"
            )
          }
        } else {
          Validated.invalidNel(
            "Input file must not be empty"
          )
        }
      case None => Validated.validNel(None)
    }
  }

  def validateNoPassphrase(somePassphrase: Option[String]) =
    somePassphrase match {
      case Some(_) =>
        Validated.invalidNel(
          "Passphrase must not be specified"
        )
      case None => Validated.validNel(None)
    }

  def validatePassphrase(
      somePassphrase: Option[String]
  ): ValidatedNel[String, Option[String]] = {
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
