package co.topl.brambl.cli.validation

import cats.data.Validated
import cats.data.ValidatedNel
import co.topl.brambl.utils.Encoding

trait WalletValidationModule {

  def validateInputKey(
      someInputFile: Option[String],
      required: Boolean
  ): ValidatedNel[String, Option[String]] = {
    import cats.implicits._
    someInputFile match {
      case Some(inputFile) =>
        if (inputFile.trim().length >= 0) {
          // check if the file exists
          if (new java.io.File(inputFile).exists) {
            Encoding.decodeFromBase58(
              scala.io.Source.fromFile(inputFile).getLines().mkString("")
            ) match {
              case Left(_) =>
                "Invalid key file".invalidNel
              case Right(_) =>
                Some(inputFile).validNel
            }
          } else {
            Validated.invalidNel(
              inputFile + " does not exist"
            )
          }
        } else {
          Validated.invalidNel(
            inputFile + " must not be empty"
          )
        }
      case None =>
        if (required) Validated.invalidNel("Key file is required")
        else Validated.validNel(None)
    }
  }




}
