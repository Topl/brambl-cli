package co.topl.brambl.cli.validation

import cats.data.Validated
import cats.data.ValidatedNel

trait CommonValidationModule {

  def validatePort(port: Int) = {
    import cats.implicits._
    if (port > 0 && port < 65536) {
      port.validNel
    } else {
      "Port must be between 0 and 65536".invalidNel
    }
  }

  def validateHost(host: String) = {
    import cats.implicits._
    if (host.nonEmpty) {
      host.validNel
    } else {
      "Host must be non-empty".invalidNel
    }
  }

  def validateOutputfile(
      someOutputFile: Option[String],
      required: Boolean
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
      case None =>
        if (required) Validated.invalidNel("Output file is required")
        else Validated.validNel(None)
    }
  }
  def validateWalletFile(
      someWalletFile: Option[String]
  ): ValidatedNel[String, String] = {
    someWalletFile match {
      case Some(walletFile) =>
        if (walletFile.trim().length >= 0) {
          Validated.validNel(walletFile)
        } else {
          Validated.invalidNel(
            "Wallet file is mandatory"
          )
        }
      case None =>
        Validated.invalidNel("Wallet file is mandatory")
    }
  }

  def validateInputFile(
      fileType: String,
      someInputFile: Option[String],
      required: Boolean
  ): ValidatedNel[String, Option[String]] = {
    someInputFile match {
      case Some(inputFile) =>
        if (inputFile.trim().length >= 0) {
          // check if the file exists
          if (new java.io.File(inputFile).exists) {
            Validated.validNel(Some(inputFile))
          } else {
            Validated.invalidNel(
              fileType + " does not exist"
            )
          }
        } else {
          Validated.invalidNel(
            fileType + " must not be empty"
          )
        }
      case None =>
        if (required) Validated.invalidNel(fileType + " is required")
        else Validated.validNel(None)
    }
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

  def validateMnemonic(
      mnemonic: Seq[String]
  ): ValidatedNel[String, Seq[String]] =
    if (List(12, 15, 18, 21, 24).contains(mnemonic.length))
      Validated.validNel(mnemonic)
    else Validated.invalidNel(
      "Mnemonic must be 12, 15, 18, 21 or 24 words"
    )
}
