package co.topl.brambl.cli

import cats.data.Validated
import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import co.topl.brambl.cli.validation.BramblCliParamsValidatorModule
import scopt.OParser
import co.topl.brambl.cli.modules.WalletModeModule
import co.topl.brambl.cli.modules.SimpleTransactionModeModule
import co.topl.brambl.cli.modules.GenusQueryModeModule
import co.topl.brambl.cli.modules.PartiesModeModule
import co.topl.brambl.cli.modules.ContractModeModule
import co.topl.brambl.cli.modules.BifrostQueryModeModule
import co.topl.brambl.cli.modules.TxModeModule

object Main
    extends IOApp
    with GenusQueryModeModule
    with BifrostQueryModeModule
    with ContractModeModule
    with PartiesModeModule
    with WalletModeModule
    with SimpleTransactionModeModule
    with TxModeModule {

  import BramblCliParamsValidatorModule._

  import BramblCliParamsParserModule._

  override def run(args: List[String]): IO[ExitCode] = {
    OParser.parse(paramParser, args, BramblCliParams()) match {
      case Some(params) =>
        val op: IO[Either[String, String]] = validateParams(params) match {
          case Validated.Valid(validateParams) =>
            validateParams.mode match {
              case BramblCliMode.tx =>
                txModeSubcmds(validateParams)
              case BramblCliMode.contracts =>
                contractModeSubcmds(validateParams)
              case BramblCliMode.parties =>
                partiesModeSubcmds(validateParams)
              case BramblCliMode.wallet =>
                walletModeSubcmds(validateParams)
              case BramblCliMode.simpletransaction =>
                simpleTransactionSubcmds(validateParams)
              case BramblCliMode.genusquery =>
                genusQuerySubcmd(validateParams)
              case BramblCliMode.bifrostquery =>
                bifrostQuerySubcmd(validateParams)
            }
          case Validated.Invalid(errors) =>
            IO(
              "Invalid params\n" +
                errors.toList.mkString(", ")
            ).map(Left(_))
        }
        import cats.implicits._
        for {
          output <- op
          res <- output.fold(
            x => IO.consoleForIO.errorln(x).map(_ => (ExitCode.Error)),
            x => IO.consoleForIO.println(x).map(_ => ExitCode.Success)
          )
        } yield res
      case _ =>
        IO.pure(ExitCode.Error)
    }
  }

}
