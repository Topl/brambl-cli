package co.topl.brambl.cli.validation

import cats.data.Validated
import cats.data.ValidatedNel
import co.topl.brambl.cli.BramblCliParams

import scala.util.Try

trait KeyValidationModule {

  self: CommonValidationModule =>

  def validateCoordinates(paramConfig: BramblCliParams) = {
    import cats.implicits._
    if (paramConfig.coordinates.length == 3)
      paramConfig.coordinates
        .map({ x =>
          Try(x.toInt).toOption match {
            case Some(_) => Validated.validNel(x)
            case None =>
              Validated.invalidNel(
                s"Invalid coordinate: $x. Coordinates must be integers."
              )
          }
        })
        .sequence
    else
      Validated.invalidNel(
        s"Invalid coordinates. Three coordinates are necessary."
      )
  }

  def validateKeyDeriveParams(
      paramConfig: BramblCliParams
  ): ValidatedNel[String, BramblCliParams] = {
    import cats.implicits._
    List(
      validatePassword(paramConfig.password),
      validateNoPassphrase(paramConfig.somePassphrase),
      validateOutputFileRequired(paramConfig.someOutputFile),
      validateInputFileRequired(paramConfig.someInputFile),
      validateCoordinates(paramConfig).map(_ => paramConfig)
    ).sequence.map(_ => paramConfig)
  }

  def validateInputFileRequired(
      someInputFile: Option[String]
  ): ValidatedNel[String, Option[String]] = {
    if (someInputFile.isEmpty) {
      Validated.invalidNel(
        "Input file is required for this command"
      )
    } else {
      Validated.validNel(someInputFile)
    }
  }

  def validateOutputFileRequired(
      someOutputFile: Option[String]
  ): ValidatedNel[String, Option[String]] = {
    if (someOutputFile.isEmpty) {
      Validated.invalidNel(
        "Output file is required for this command"
      )
    } else {
      Validated.validNel(someOutputFile)
    }
  }

  def validateKeyGenerationParams(
      paramConfig: BramblCliParams
  ): ValidatedNel[String, BramblCliParams] = {
    import cats.implicits._
    List(
      validatePassword(paramConfig.password),
      validatePassphrase(paramConfig.somePassphrase)
    ).sequence.map(_ => paramConfig)
  }

}
