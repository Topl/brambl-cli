package co.topl.brambl.cli.modules

import cats.effect.IO
import co.topl.brambl.cli.controllers.FellowshipsController
import co.topl.brambl.servicekit.{FellowshipStorageApi, WalletStateResource}
import co.topl.brambl.cli.BramblCliSubCmd
import co.topl.brambl.cli.BramblCliParams
import scopt.OParser
import co.topl.brambl.cli.BramblCliParamsParserModule

trait FellowshipsModeModule extends WalletStateResource {
  def fellowshipsModeSubcmds(
      validateParams: BramblCliParams
  ): IO[Either[String, String]] = {
    val fellowshipStorageAlgebra = FellowshipStorageApi.make[IO](
      walletResource(validateParams.walletFile)
    )
    validateParams.subcmd match {
      case BramblCliSubCmd.invalid =>
        IO.pure(
          Left(
            OParser.usage(
              BramblCliParamsParserModule.fellowshipsMode
            ) + "\nA subcommand needs to be specified"
          )
        )
      case BramblCliSubCmd.add =>
        new FellowshipsController(fellowshipStorageAlgebra)
          .addFellowship(validateParams.fellowshipName)
      case BramblCliSubCmd.list =>
        new FellowshipsController(fellowshipStorageAlgebra)
          .listFellowships()
    }
  }
}
