package co.topl.brambl.cli.modules

import cats.effect.IO
import co.topl.brambl.cli.BramblCliParams
import co.topl.brambl.cli.BramblCliSubCmd
import co.topl.brambl.cli.controllers.WalletController
import co.topl.brambl.dataApi.GenusQueryAlgebra

trait WalletModeModule
    extends WalletStateAlgebraModule
    with WalletManagementUtilsModule
    with WalletApiModule
    with WalletAlgebraModule
    with TransactionBuilderApiModule
    with ChannelResourceModule {

  def walletModeSubcmds(
      validateParams: BramblCliParams
  ): IO[Either[String, String]] = {
    val walletController = new WalletController(
      walletStateAlgebra(
        validateParams.walletFile
      ),
      walletManagementUtils,
      walletApi,
      walletAlgebra(
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
    )
    validateParams.subcmd match {
      case BramblCliSubCmd.balance =>
        walletController.getBalance(
          validateParams.fromAddress,
          if (validateParams.fromAddress.isEmpty) Some(validateParams.fromFellowship) else None,
          if (validateParams.fromAddress.isEmpty) Some(validateParams.fromContract) else None,
          validateParams.someFromState
        )
      case BramblCliSubCmd.invalid =>
        IO.pure(Left("A subcommand needs to be specified"))
      case BramblCliSubCmd.exportvk =>
        validateParams.someFromState
          .map(x =>
            walletController.exportFinalVk(
              validateParams.someKeyFile.get,
              validateParams.password,
              validateParams.someOutputFile.get,
              validateParams.fellowshipName,
              validateParams.contractName,
              x
            )
          )
          .getOrElse(
            walletController.exportVk(
              validateParams.someKeyFile.get,
              validateParams.password,
              validateParams.someOutputFile.get,
              validateParams.fellowshipName,
              validateParams.contractName
            )
          )
      case BramblCliSubCmd.importvks =>
        walletController.importVk(
          validateParams.network.networkId,
          validateParams.inputVks,
          validateParams.someKeyFile.get,
          validateParams.password,
          validateParams.contractName,
          validateParams.fellowshipName
        )
      case BramblCliSubCmd.init =>
        walletController.createWalletFromParams(validateParams)
      case BramblCliSubCmd.recoverkeys =>
        walletController.recoverKeysFromParams(validateParams)
      case BramblCliSubCmd.sync =>
        walletController.sync(
          validateParams.network.networkId,
          validateParams.contractName,
          validateParams.fellowshipName
        )
      case BramblCliSubCmd.currentaddress =>
        walletController.currentaddress(
          validateParams
        )
    }
  }
}
