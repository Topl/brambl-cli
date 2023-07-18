package co.topl.brambl.cli.modules

import co.topl.brambl.cli.BramblCliValidatedParams
import co.topl.brambl.cli.controllers.WalletController
import co.topl.brambl.cli.BramblCliSubCmd
import co.topl.brambl.constants.NetworkConstants
import co.topl.brambl.dataApi.GenusQueryAlgebra
import cats.effect.IO

trait WalletModeModule
    extends WalletStateAlgebraModule
    with WalletManagementUtilsModule
    with WalletApiModule
    with WalletAlgebraModule
    with TransactionBuilderApiModule
    with ChannelResourceModule {

  def walletModeSubcmds(
      validateParams: BramblCliValidatedParams
  ): IO[Either[String, String]] = {
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
      walletAlgebra(
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
      case BramblCliSubCmd.recoverkeys =>
        walletController.recoverKeysFromParams(validateParams)
      case BramblCliSubCmd.sync =>
        walletController.sync(
          validateParams.contractName,
          validateParams.partyName
        )
      case BramblCliSubCmd.currentaddress =>
        walletController.currentaddress()
    }
  }
}
