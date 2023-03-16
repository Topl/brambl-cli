package co.topl.brambl.cli.validation

import akka.http.scaladsl.model.Uri
import cats.data.ValidatedNel
import co.topl.brambl.cli.BramblCliMode
import co.topl.brambl.cli.BramblCliParams
import co.topl.brambl.cli.BramblCliSubCmd
import co.topl.brambl.cli.BramblCliValidatedParams
import co.topl.brambl.cli.NetworkParamName
import co.topl.client.Provider
import co.topl.utils.NetworkType

object BramblCliParamsValidatorModule
    extends CommonValidationModule
    with WalletValidationModule
    with TransactionValidationModule {


  def buildNetwork(
      uri: Uri,
      networkType: NetworkParamName.Value,
      someApiKey: Option[String]
  ): Provider = {
    networkType match {
      case NetworkParamName.main =>
        new Provider.ToplMainNet(uri, someApiKey.getOrElse(""))
      case NetworkParamName.valhalla =>
        new Provider.ValhallaTestNet(uri, someApiKey.getOrElse(""))
      case NetworkParamName.`private` =>
        new Provider.PrivateTestNet(uri, someApiKey.getOrElse(""))
    }
  }



  def validateParams(
      paramConfig: BramblCliParams
  ): ValidatedNel[String, BramblCliValidatedParams] = {
    import cats.implicits._
    (
      validateMode(paramConfig.mode).andThen(mode =>
        validateSubCmd(mode, paramConfig.subcmd).map((mode, _))
      ),
      (
        validateToplNetworkUri(
          paramConfig.someNetworkUri.getOrElse("http://127.0.0.1:9085")
        ),
        validateNetworkType(paramConfig.networkType)
      ).mapN((uri, networkType) =>
        buildNetwork(
          uri,
          networkType,
          paramConfig.someApiKey
        )
      )
    ).mapN((modeAndSubCmd, provider) => {
      val (mode, subcmd) = modeAndSubCmd
      implicit val networkPrefix: NetworkType.NetworkPrefix =
        provider.networkPrefix
      modeAndSubCmd match {
        case (BramblCliMode.transaction, BramblCliSubCmd.create) =>
          validatTransactionCreate(paramConfig).mapN(
            (_, token, fromAddresses, toAddresses, changeAddress, fee) =>
              BramblCliValidatedParams(
                mode,
                subcmd,
                provider,
                "",
                Some(token),
                paramConfig.someOutputFile,
                None,
                None,
                fromAddresses,
                toAddresses,
                changeAddress,
                fee
              )
          )
        case (BramblCliMode.transaction, BramblCliSubCmd.broadcast) =>
          validateTransactionBroadcast(paramConfig).map { someInputFile =>
            BramblCliValidatedParams(
              mode,
              subcmd,
              provider,
              "",
              None,
              None,
              someInputFile,
              None,
              Nil,
              Nil,
              "",
              0
            )
          }
        case (BramblCliMode.wallet, BramblCliSubCmd.balance) =>
          validateWalletBalance(paramConfig).map { fromAddresses =>
            BramblCliValidatedParams(
              mode,
              subcmd,
              provider,
              "",
              None,
              None,
              None,
              None,
              fromAddresses,
              Nil,
              "",
              0
            )
          }
        case (BramblCliMode.wallet, BramblCliSubCmd.sign) =>
          validateWalletSign(paramConfig).mapN((_, password, tokenType) =>
            BramblCliValidatedParams(
              mode,
              subcmd,
              provider,
              password,
              Some(tokenType),
              paramConfig.someOutputFile,
              paramConfig.someInputFile,
              paramConfig.someKeyfile,
              Nil,
              Nil,
              "",
              0
            )
          )
        case (BramblCliMode.wallet, BramblCliSubCmd.create) =>
          validateWalletCreate(paramConfig).map { password =>
            BramblCliValidatedParams(
              mode,
              subcmd,
              provider,
              password,
              None,
              paramConfig.someOutputFile,
              None,
              None,
              Nil,
              Nil,
              "",
              0
            )
          }
      }
    }).andThen(x => x)
  }

}
