package co.topl.brambl.cli.modules

import cats.effect.IO
import co.topl.brambl.cli.BramblCliValidatedParams
import co.topl.brambl.cli.controllers.PartiesController
import co.topl.brambl.cli.impl.PartyStorageAlgebra
import co.topl.brambl.cli.BramblCliSubCmd

trait PartiesModeModule extends WalletResourceModule {
  def partiesModeSubcmds(
      validateParams: BramblCliValidatedParams
  ) = {
    val partyStorageAlgebra = PartyStorageAlgebra.make[IO](
      walletResource(validateParams.walletFile)
    )
    validateParams.subcmd match {
      case BramblCliSubCmd.add =>
        new PartiesController(partyStorageAlgebra)
          .addParty(validateParams.partyName)
      case BramblCliSubCmd.list =>
        new PartiesController(partyStorageAlgebra)
          .listParties()
    }
  }
}
