package co.topl.brambl.cli.modules

import co.topl.brambl.cli.BramblCliValidatedParams
import co.topl.brambl.cli.controllers.WalletController
import co.topl.brambl.cli.BramblCliSubCmd

trait WalletModeModule extends WalletResourceModule {

  def walletModeSubcmds(
      validateParams: BramblCliValidatedParams
  ) = {
    val walletController = new WalletController(
      walletResource(validateParams.walletFile)
    )
    validateParams.subcmd match {
      case BramblCliSubCmd.exportvk =>
        walletController.exportVk(validateParams)
      case BramblCliSubCmd.importvks =>
        walletController.importVk(validateParams)
      case BramblCliSubCmd.init =>
        walletController.createWalletFromParams(validateParams)
      case BramblCliSubCmd.currentaddress =>
        walletController.currentaddress(validateParams)
    }
  }
}
