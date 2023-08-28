package co.topl.brambl.cli.modules

import co.topl.brambl.cli.controllers.TxController
import cats.effect.IO
import co.topl.brambl.cli.BramblCliSubCmd
import co.topl.brambl.constants.NetworkConstants
import co.topl.brambl.cli.BramblCliParams

trait TxModeModule extends TxParserAlgebraModule {

  def txModeSubcmds(
      validateParams: BramblCliParams
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
