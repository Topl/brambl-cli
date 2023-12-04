package co.topl.brambl.cli.modules

import cats.effect.IO
import co.topl.brambl.cli.BramblCliParams
import co.topl.brambl.cli.BramblCliSubCmd
import co.topl.brambl.cli.controllers.WalletController
import co.topl.brambl.dataApi.{GenusQueryAlgebra, RpcChannelResource}
import scopt.OParser
import co.topl.brambl.cli.BramblCliParamsParserModule

trait WalletModeModule
    extends WalletStateAlgebraModule
    with WalletManagementUtilsModule
    with WalletApiModule
    with WalletAlgebraModule
    with TransactionBuilderApiModule
    with RpcChannelResource {

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
          if (validateParams.fromAddress.isEmpty)
            Some(validateParams.fromFellowship)
          else None,
          if (validateParams.fromAddress.isEmpty)
            Some(validateParams.fromTemplate)
          else None,
          validateParams.someFromInteraction
        )
      case BramblCliSubCmd.invalid =>
        IO.pure(
          Left(
            OParser.usage(
              BramblCliParamsParserModule.walletMode
            ) + "\nA subcommand needs to be specified"
          )
        )
      case BramblCliSubCmd.exportvk =>
        validateParams.someFromInteraction
          .map(x =>
            walletController.exportFinalVk(
              validateParams.someKeyFile.get,
              validateParams.password,
              validateParams.someOutputFile.get,
              validateParams.fellowshipName,
              validateParams.templateName,
              x
            )
          )
          .getOrElse(
            walletController.exportVk(
              validateParams.someKeyFile.get,
              validateParams.password,
              validateParams.someOutputFile.get,
              validateParams.fellowshipName,
              validateParams.templateName
            )
          )
      case BramblCliSubCmd.importvks =>
        walletController.importVk(
          validateParams.network.networkId,
          validateParams.inputVks,
          validateParams.someKeyFile.get,
          validateParams.password,
          validateParams.templateName,
          validateParams.fellowshipName
        )
      case BramblCliSubCmd.listinteraction =>
        walletController.listInteractions(
          validateParams.fellowshipName,
          validateParams.templateName
        )
      case BramblCliSubCmd.init =>
        walletController.createWalletFromParams(validateParams)
      case BramblCliSubCmd.recoverkeys =>
        walletController.recoverKeysFromParams(validateParams)
      case BramblCliSubCmd.setinteraction =>
        walletController.setCurrentInteraction(
          validateParams.fromFellowship,
          validateParams.fromTemplate,
          validateParams.someFromInteraction.get
        )
      case BramblCliSubCmd.sync =>
        walletController.sync(
          validateParams.network.networkId,
          validateParams.fellowshipName,
          validateParams.templateName
        )
      case BramblCliSubCmd.currentaddress =>
        walletController.currentaddress(
          validateParams
        )
    }
  }
}
