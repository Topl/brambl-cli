package co.topl.brambl.cli.modules

import co.topl.brambl.cli.BramblCliValidatedParams
import co.topl.brambl.cli.controllers.WalletController
import co.topl.brambl.cli.BramblCliSubCmd
import co.topl.brambl.constants.NetworkConstants

trait WalletModeModule
    extends WalletStateAlgebraModule
    with WalletManagementUtilsModule
    with WalletApiModule
    with WalletAlgebraModule
    with TransactionBuilderApiModule {

  def walletModeSubcmds(
      validateParams: BramblCliValidatedParams
  ) = {
    val walletController = new WalletController(
      transactionBuilderApi(
        validateParams.network.networkId,
        NetworkConstants.MAIN_LEDGER_ID
      ),
      walletStateAlgebra(
        validateParams.walletFile,
        validateParams.network.networkId
      ),
      walletManagementUtils,
      walletApi,
      walletAlgebra(validateParams.walletFile, validateParams.network.networkId)
    )
    validateParams.subcmd match {
      case BramblCliSubCmd.exportvk =>
        walletController.exportVk(validateParams)
      case BramblCliSubCmd.importvks =>
        walletController.importVk(
          validateParams.inputVks,
          validateParams.someKeyFile.get,
          validateParams.password,
          validateParams.contractName,
          validateParams.partyName
        )
      case BramblCliSubCmd.init =>
        walletController.createWalletFromParams(validateParams)
      case BramblCliSubCmd.currentaddress =>
        walletController.currentaddress()
    }
  }
}
