package co.topl.brambl.cli

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import co.topl.brambl.cli.modules.BifrostQueryModeModule
import co.topl.brambl.cli.modules.ContractModeModule
import co.topl.brambl.cli.modules.GenusQueryModeModule
import co.topl.brambl.cli.modules.PartiesModeModule
import co.topl.brambl.cli.modules.SimpleTransactionModeModule
import co.topl.brambl.cli.modules.TxModeModule
import co.topl.brambl.cli.modules.WalletModeModule
import scopt.OParser
import co.topl.brambl.cli.modules.SimpleMintingModeModule

object Main
    extends IOApp
    with GenusQueryModeModule
    with BifrostQueryModeModule
    with ContractModeModule
    with PartiesModeModule
    with WalletModeModule
    with SimpleTransactionModeModule
    with TxModeModule
    with SimpleMintingModeModule {

  import BramblCliParamsParserModule._

  override def run(args: List[String]): IO[ExitCode] = {
    OParser.parse(paramParser, args, BramblCliParams()) match {
      case Some(params) =>
        val op: IO[Either[String, String]] =
          params.mode match {
            case BramblCliMode.tx =>
              txModeSubcmds(params)
            case BramblCliMode.contracts =>
              contractModeSubcmds(params)
            case BramblCliMode.parties =>
              partiesModeSubcmds(params)
            case BramblCliMode.wallet =>
              walletModeSubcmds(params)
            case BramblCliMode.simpletransaction =>
              simpleTransactionSubcmds(params)
            case BramblCliMode.simpleminting =>
              simpleMintingSubcmds(params)
            case BramblCliMode.genusquery =>
              genusQuerySubcmd(params)
            case BramblCliMode.bifrostquery =>
              bifrostQuerySubcmd(params)
            case _ =>
              IO.pure(Left("Invalid mode"))
          }
        import cats.implicits._
        for {
          output <- op
          res <- output.fold(
            x => IO.consoleForIO.errorln(x).map(_ => ExitCode.Error),
            x => IO.consoleForIO.println(x).map(_ => ExitCode.Success)
          )
        } yield res
      case _ =>
        IO.pure(ExitCode.Error)
    }
  }

}
