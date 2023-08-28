package co.topl.brambl.cli.validation

import cats.data.Validated
import cats.data.ValidatedNel

trait CommonValidationModule {


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

}
