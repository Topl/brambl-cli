package co.topl.brambl.cli.modules

import co.topl.brambl.cli.controllers.SimpleMintingController
import co.topl.brambl.cli.BramblCliSubCmd
import co.topl.brambl.cli.BramblCliParams
import cats.effect.IO
import co.topl.brambl.cli.impl.GroupPolicyParserModule
import co.topl.brambl.constants.NetworkConstants

trait SimpleMintingModeModule
    extends GroupPolicyParserModule
    with SimpleMintingAlgebraModule {

  def simpleMingingSubcmds(
      validateParams: BramblCliParams
  ): IO[Either[String, String]] = validateParams.subcmd match {
    case BramblCliSubCmd.create =>
      new SimpleMintingController(
        groupPolicyParserAlgebra(validateParams.network.networkId),
        simpleMintingAlgebra(
          validateParams.walletFile,
          validateParams.network.networkId,
          NetworkConstants.MAIN_LEDGER_ID,
          validateParams.host,
          validateParams.bifrostPort
        )
      ).createSimpleGroupMintingTransactionFromParams(
        validateParams.someInputFile.get,
        validateParams.someKeyFile.get,
        validateParams.password,
        validateParams.fromParty,
        validateParams.fromContract,
        validateParams.someFromState,
        validateParams.amount,
        validateParams.fee,
        validateParams.someOutputFile.get
      )
  }
}
