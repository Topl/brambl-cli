package co.topl.brambl.cli.modules

import cats.effect.IO
import co.topl.brambl.cli.BramblCliParams
import co.topl.brambl.cli.BramblCliSubCmd
import co.topl.brambl.cli.controllers.TxController
import co.topl.brambl.constants.NetworkConstants

trait TxModeModule extends TxParserAlgebraModule with TransactionAlgebraModule {

  def txModeSubcmds(
      validateParams: BramblCliParams
  ): IO[Either[String, String]] = {
    validateParams.subcmd match {
      case BramblCliSubCmd.invalid =>
        IO.pure(Left("A subcommand needs to be specified"))
      case BramblCliSubCmd.broadcast =>
        new TxController(
          txParserAlgebra(
            validateParams.network.networkId,
            NetworkConstants.MAIN_LEDGER_ID
          ),
          transactionOps(
            validateParams.walletFile,
            validateParams.host,
            validateParams.bifrostPort,
            validateParams.secureConnection
          )
        ).broadcastSimpleTransactionFromParams(validateParams.someInputFile.get)
      case BramblCliSubCmd.prove =>
        new TxController(
          txParserAlgebra(
            validateParams.network.networkId,
            NetworkConstants.MAIN_LEDGER_ID
          ),
          transactionOps(
            validateParams.walletFile,
            validateParams.host,
            validateParams.bifrostPort,
            validateParams.secureConnection
          )
        ).proveSimpleTransactionFromParams(
          validateParams.someInputFile.get,
          validateParams.someKeyFile.get,
          validateParams.password,
          validateParams.someOutputFile.get
        )
      case BramblCliSubCmd.inspect =>
        new TxController(
          txParserAlgebra(
            validateParams.network.networkId,
            NetworkConstants.MAIN_LEDGER_ID
          ),
          transactionOps(
            validateParams.walletFile,
            validateParams.host,
            validateParams.bifrostPort,
            validateParams.secureConnection
          )
        ).inspectTransaction(validateParams.someInputFile.get)
      case BramblCliSubCmd.create =>
        new TxController(
          txParserAlgebra(
            validateParams.network.networkId,
            NetworkConstants.MAIN_LEDGER_ID
          ),
          transactionOps(
            validateParams.walletFile,
            validateParams.host,
            validateParams.bifrostPort,
            validateParams.secureConnection
          )
        )
          .createComplexTransaction(
            validateParams.someInputFile.get,
            validateParams.someOutputFile.get
          )
    }
  }

}
