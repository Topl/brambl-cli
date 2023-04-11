package co.topl.brambl.cli

import cats.data.Validated
import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import scopt.OParser
import co.topl.brambl.cli.validation.BramblCliParamsValidatorModule

object Main
    extends IOApp {

  import BramblCliParamsValidatorModule._

  import BramblCliParamsParserModule._

  override def run(args: List[String]): IO[ExitCode] = {
    OParser.parse(paramParser, args, BramblCliParams()) match {
      case Some(params) =>
        val op = validateParams(params) match {
          case Validated.Valid(validatedParams) =>
            IO.unit
          case Validated.Invalid(errors) =>
            IO.println("Invalid params") *> IO.println(
              errors.toList.mkString(", ")
            ) *> IO.print(OParser.usage(paramParser))
        }
        for {
          _ <- op
        } yield ExitCode.Success
      case _ =>
        IO.pure(ExitCode.Error)
    }
  }

}
