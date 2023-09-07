package co.topl.brambl.cli.modules

import cats.effect.IO
import co.topl.brambl.cli.controllers.PartiesController
import co.topl.brambl.servicekit.{PartyStorageApi, WalletStateResource}
import co.topl.brambl.cli.BramblCliSubCmd
import co.topl.brambl.cli.BramblCliParams

trait PartiesModeModule extends WalletStateResource {
  def partiesModeSubcmds(
      validateParams: BramblCliParams
  ): IO[Either[String, String]] = {
    val partyStorageAlgebra = PartyStorageApi.make[IO](
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
