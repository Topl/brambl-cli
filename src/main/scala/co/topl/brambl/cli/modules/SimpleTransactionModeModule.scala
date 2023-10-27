package co.topl.brambl.cli.modules

import cats.effect.IO
import co.topl.brambl.cli.BramblCliSubCmd
import co.topl.brambl.cli.controllers.SimpleTransactionController
import co.topl.brambl.cli.BramblCliParams

trait SimpleTransactionModeModule
    extends SimpleTransactionAlgebraModule
    with WalletStateAlgebraModule {

  def simpleTransactionSubcmds(
      validateParams: BramblCliParams
  ): IO[Either[String, String]] = validateParams.subcmd match {
    case BramblCliSubCmd.invalid =>
      IO.pure(Left("Invalid subcommand"))
    case BramblCliSubCmd.create =>
      new SimpleTransactionController(
        walletStateAlgebra(
          validateParams.walletFile
        ),
        simplTransactionOps(
          validateParams.walletFile,
          validateParams.network.networkId,
          validateParams.host,
          validateParams.bifrostPort,
          validateParams.secureConnection
        )
      ).createSimpleTransactionFromParams(
        validateParams.someKeyFile.get,
        validateParams.password,
        (
          validateParams.fromFellowship,
          validateParams.fromTemplate,
          validateParams.someFromState
        ),
        (
          validateParams.someChangeFellowship,
          validateParams.someChangeTemplate,
          validateParams.someChangeState
        ),
        validateParams.toAddress,
        validateParams.someToFellowship,
        validateParams.someToTemplate,
        validateParams.amount,
        validateParams.fee,
        validateParams.someOutputFile.get,
        validateParams.tokenType,
        validateParams.someGroupId,
        validateParams.someSeriesId
      )
  }
}
