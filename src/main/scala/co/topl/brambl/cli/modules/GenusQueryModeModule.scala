package co.topl.brambl.cli.modules

import cats.effect.IO
import co.topl.brambl.cli.BramblCliValidatedParams
import co.topl.brambl.cli.controllers.GenusQueryController
import co.topl.brambl.dataApi.GenusQueryAlgebra
import co.topl.brambl.cli.BramblCliSubCmd

trait GenusQueryModeModule
    extends WalletStateAlgebraModule
    with ChannelResourceModule {

  def genusQuerySubcmd(
      validateParams: BramblCliValidatedParams
  ): IO[String] = validateParams.subcmd match {
    case BramblCliSubCmd.utxobyaddress =>
      new GenusQueryController(
        walletStateAlgebra(
          validateParams.walletFile,
          validateParams.network.networkId
        ),
        GenusQueryAlgebra
          .make[IO](
            channelResource(
              validateParams.host,
              validateParams.bifrostPort
            )
          )
      ).queryUtxoFromParams(
        validateParams.fromParty,
        validateParams.fromContract,
        validateParams.someFromState
      )
  }

}
