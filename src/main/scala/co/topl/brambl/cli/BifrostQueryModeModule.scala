package co.topl.brambl.cli

import cats.effect.IO
import co.topl.brambl.cli.BramblCliValidatedParams
import co.topl.brambl.cli.controllers.BifrostQueryController

trait BifrostQueryModeModule extends ChannelResourceModule {

  def bifrostQuerySubcmd(
      validateParams: BramblCliValidatedParams
  ): IO[String] = validateParams.subcmd match {
    case BramblCliSubCmd.blockbyheight =>
      new BifrostQueryController(
        channelResource(
          validateParams.host,
          validateParams.bifrostPort
        )
      ).blockByHeight(validateParams)
    case BramblCliSubCmd.blockbyid =>
      new BifrostQueryController(
        channelResource(
          validateParams.host,
          validateParams.bifrostPort
        )
      ).blockById(validateParams)
    case BramblCliSubCmd.transactionbyid =>
      new BifrostQueryController(
        channelResource(
          validateParams.host,
          validateParams.bifrostPort
        )
      ).fetchTransaction(validateParams)
  }

}
