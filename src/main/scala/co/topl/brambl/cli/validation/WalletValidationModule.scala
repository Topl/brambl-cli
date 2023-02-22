package co.topl.brambl.cli.validation

import cats.data.Validated
import co.topl.brambl.cli.BramblCliParams

trait WalletValidationModule {

  self: CommonValidationModule =>

  def validateWalletCreate(paramConfig: BramblCliParams) = {
    (paramConfig.somePassword) match {
      case Some(password) =>
        Validated.validNel(password)
      case None =>
        Validated.invalidNel(
          "Password is required for wallet creation"
        )
    }
  }


  def validateKeyfile(someKeyfile: Option[String]) = {
    someKeyfile match {
      case Some(keyfile) =>
        validateFileExists("Keyfile", keyfile)
      case None =>
        Validated.invalidNel(
          "Keyfile is required"
        )
    }
  }

  def validatePassword(somePassword: Option[String]) = {
    somePassword match {
      case Some(password) => Validated.validNel(password)
      case None =>
        Validated.invalidNel(
          "Password is required"
        )
    }
  }

  def validateWalletSign(paramConfig: BramblCliParams) = {
    (
      validateKeyfile(paramConfig.someKeyfile),
      validatePassword(paramConfig.somePassword),
      validateTokenType(paramConfig.someToken.getOrElse(""))
    )
  }
}
