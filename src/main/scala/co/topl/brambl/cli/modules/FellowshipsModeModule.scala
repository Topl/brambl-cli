package co.topl.brambl.cli.modules

import cats.effect.IO
import co.topl.brambl.cli.controllers.FellowshipsController
import co.topl.brambl.servicekit.{PartyStorageApi, WalletStateResource}
import co.topl.brambl.cli.BramblCliSubCmd
import co.topl.brambl.cli.BramblCliParams

trait FellowshipsModeModule extends WalletStateResource {
  def fellowshipsModeSubcmds(
      validateParams: BramblCliParams
  ): IO[Either[String, String]] = {
    val fellowshipStorageAlgebra = PartyStorageApi.make[IO](
      walletResource(validateParams.walletFile)
    )
    validateParams.subcmd match {
      case BramblCliSubCmd.invalid =>
        IO.pure(Left("A subcommand needs to be specified"))
      case BramblCliSubCmd.add =>
        new FellowshipsController(fellowshipStorageAlgebra)
          .addParty(validateParams.fellowshipName)
      case BramblCliSubCmd.list =>
        new FellowshipsController(fellowshipStorageAlgebra)
          .listFellowships()
    }
  }
}
