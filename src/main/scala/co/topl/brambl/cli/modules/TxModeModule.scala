package co.topl.brambl.cli.modules

import co.topl.brambl.cli.BramblCliValidatedParams
import co.topl.brambl.cli.controllers.TxController
import cats.effect.IO
import co.topl.brambl.cli.BramblCliSubCmd
import co.topl.brambl.constants.NetworkConstants

trait TxModeModule extends TxParserAlgebraModule {

  def txModeSubcmds(
      validateParams: BramblCliValidatedParams
  ): IO[Either[String, String]] = {
    validateParams.subcmd match {
      case BramblCliSubCmd.create =>
        new TxController(
          txParserAlgebra(
            validateParams.network.networkId,
            NetworkConstants.MAIN_LEDGER_ID
          )
        )
          .createComplexTransaction(
            validateParams.someInputFile.get,
            validateParams.someOutputFile.get
          )
    }
  }

}
