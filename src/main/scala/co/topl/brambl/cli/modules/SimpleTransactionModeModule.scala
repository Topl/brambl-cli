package co.topl.brambl.cli.modules

import cats.effect.IO
import co.topl.brambl.cli.BramblCliSubCmd
import co.topl.brambl.cli.BramblCliValidatedParams
import co.topl.brambl.cli.controllers.SimpleTransactionController

trait SimpleTransactionModeModule
    extends WalletResourceModule
    with ChannelResourceModule {

  def simpleTransactionSubcmds(
      validateParams: BramblCliValidatedParams
  ): IO[String] = validateParams.subcmd match {
    case BramblCliSubCmd.broadcast =>
      new SimpleTransactionController(
        walletResource(validateParams.walletFile),
        channelResource(
          validateParams.host,
          validateParams.bifrostPort
        )
      ).broadcastSimpleTransactionFromParams(validateParams)
    case BramblCliSubCmd.prove =>
      new SimpleTransactionController(
        walletResource(validateParams.walletFile),
        channelResource(
          validateParams.host,
          validateParams.bifrostPort
        )
      ).proveSimpleTransactionFromParams(validateParams)
    case BramblCliSubCmd.create =>
      new SimpleTransactionController(
        walletResource(validateParams.walletFile),
        channelResource(
          validateParams.host,
          validateParams.bifrostPort
        )
      ).createSimpleTransactionFromParams(validateParams)
  }
}
