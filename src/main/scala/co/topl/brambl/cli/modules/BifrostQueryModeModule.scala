package co.topl.brambl.cli.modules

import cats.effect.IO
import co.topl.brambl.cli.controllers.BifrostQueryController
import co.topl.brambl.cli.modules.ChannelResourceModule
import co.topl.brambl.cli.BramblCliSubCmd
import co.topl.brambl.dataApi.BifrostQueryAlgebra
import co.topl.brambl.cli.BramblCliParams

trait BifrostQueryModeModule extends ChannelResourceModule {

  def bifrostQuerySubcmd(
      validateParams: BramblCliParams
  ): IO[Either[String, String]] = {
    val bifrostQueryAlgebra = BifrostQueryAlgebra.make[IO](
      channelResource(
        validateParams.host,
        validateParams.bifrostPort
      )
    )
    validateParams.subcmd match {
      case BramblCliSubCmd.blockbyheight =>
        new BifrostQueryController(
          bifrostQueryAlgebra
        ).blockByHeight(validateParams.height)
      case BramblCliSubCmd.blockbyid =>
        new BifrostQueryController(
          bifrostQueryAlgebra
        ).blockById(validateParams.blockId)
      case BramblCliSubCmd.transactionbyid =>
        new BifrostQueryController(
          bifrostQueryAlgebra
        ).fetchTransaction(validateParams.transactionId)
    }
  }

}
