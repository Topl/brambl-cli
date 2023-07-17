package co.topl.brambl.cli.modules

import cats.effect.IO
import co.topl.brambl.cli.BramblCliSubCmd
import co.topl.brambl.cli.BramblCliValidatedParams
import co.topl.brambl.cli.controllers.SimpleTransactionController

trait SimpleTransactionModeModule
    extends SimpleTransactionAlgebraModule
    with WalletStateAlgebraModule {

  def simpleTransactionSubcmds(
      validateParams: BramblCliValidatedParams
  ): IO[Either[String, String]] = validateParams.subcmd match {
    case BramblCliSubCmd.broadcast =>
      new SimpleTransactionController(
        walletStateAlgebra(
          validateParams.walletFile,
          validateParams.network.networkId
        ),
        simplTransactionOps(
          validateParams.walletFile,
          validateParams.network.networkId,
          validateParams.host,
          validateParams.bifrostPort
        )
      ).broadcastSimpleTransactionFromParams(validateParams.someInputFile.get)
    case BramblCliSubCmd.prove =>
      new SimpleTransactionController(
        walletStateAlgebra(
          validateParams.walletFile,
          validateParams.network.networkId
        ),
        simplTransactionOps(
          validateParams.walletFile,
          validateParams.network.networkId,
          validateParams.host,
          validateParams.bifrostPort
        )
      ).proveSimpleTransactionFromParams(
        validateParams.fromParty,
        validateParams.fromContract,
        validateParams.someFromState,
        validateParams.someInputFile.get,
        validateParams.someKeyFile.get,
        validateParams.password,
        validateParams.someOutputFile.get
      )
    case BramblCliSubCmd.create =>
      new SimpleTransactionController(
        walletStateAlgebra(
          validateParams.walletFile,
          validateParams.network.networkId
        ),
        simplTransactionOps(
          validateParams.walletFile,
          validateParams.network.networkId,
          validateParams.host,
          validateParams.bifrostPort
        )
      ).createSimpleTransactionFromParams(
        validateParams.someKeyFile.get,
        validateParams.password,
        validateParams.fromParty,
        validateParams.fromContract,
        validateParams.someFromState,
        validateParams.toAddress,
        validateParams.someToParty,
        validateParams.someToContract,
        validateParams.amount,
        validateParams.someOutputFile.get
      )
  }
}
