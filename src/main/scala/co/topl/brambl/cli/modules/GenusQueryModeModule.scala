package co.topl.brambl.cli.modules

import cats.effect.IO
import co.topl.brambl.cli.controllers.GenusQueryController
import co.topl.brambl.dataApi.GenusQueryAlgebra
import co.topl.brambl.cli.BramblCliSubCmd
import co.topl.brambl.cli.BramblCliParams

trait GenusQueryModeModule
    extends WalletStateAlgebraModule
    with ChannelResourceModule {

  def genusQuerySubcmd(
      validateParams: BramblCliParams
  ): IO[Either[String, String]] = validateParams.subcmd match {
    case BramblCliSubCmd.invalid =>
      IO.pure(Left("A subcommand needs to be specified"))
    case BramblCliSubCmd.utxobyaddress =>
      new GenusQueryController(
        walletStateAlgebra(
          validateParams.walletFile
        ),
        GenusQueryAlgebra
          .make[IO](
            channelResource(
              validateParams.host,
              validateParams.bifrostPort,
              validateParams.secureConnection
            )
          )
      ).queryUtxoFromParams(
        validateParams.fromAddress,
        validateParams.fromFellowship,
        validateParams.fromContract,
        validateParams.someFromState,
        validateParams.tokenType
      )
  }

}
