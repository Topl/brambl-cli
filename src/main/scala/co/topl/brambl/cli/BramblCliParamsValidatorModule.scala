package co.topl.brambl.cli

import akka.http.scaladsl.model.Uri
import cats.data.Validated
import cats.data.ValidatedNel
import co.topl.client.Provider
import scala.util.Try

object BramblCliMode extends Enumeration {
  type BramblCliMode = Value

  val wallet, transaction = Value
}

object BramblCliSubCmd extends Enumeration {
  type BramblCliSubCmd = Value

  val create, sign, broadcast = Value
}

trait BramblCliParamsValidatorModule {

  object NetworkParamName extends Enumeration {
    type NetworkParamName = Value

    val main, valhalla, `private` = Value
  }

  def validateMode(mode: String) = {
    Try(BramblCliMode.withName(mode)).toOption match {
      case Some(mode) => Validated.validNel(mode)
      case None =>
        Validated.invalidNel(
          "Invalid mode. Valid values are " + BramblCliMode.values.mkString(
            ", "
          )
        )
    }
  }

  def validateSubCmd(
      mode: BramblCliMode.BramblCliMode,
      subcmd: String
  ): ValidatedNel[String, BramblCliSubCmd.Value] = {
    mode match {
      case BramblCliMode.wallet =>
        Try(BramblCliSubCmd.withName(subcmd)).toOption match {
          case Some(subcmd) =>
            val validSubCmds =
              List(BramblCliSubCmd.create, BramblCliSubCmd.sign)
            if (validSubCmds.contains(subcmd))
              Validated.validNel(subcmd)
            else
              Validated.invalidNel(
                "Invalid subcommand. Valid values are " + validSubCmds.mkString(
                  ", "
                )
              )
          case None =>
            Validated.invalidNel(
              "Invalid subcommand. Valid values are " + BramblCliSubCmd.values
                .mkString(", ")
            )
        }
      case BramblCliMode.transaction =>
        Try(BramblCliSubCmd.withName(subcmd)).toOption match {
          case Some(subcmd) =>
            val validSubCmds =
              List(BramblCliSubCmd.create, BramblCliSubCmd.sign)
            if (validSubCmds.contains(subcmd))
              Validated.validNel(subcmd)
            else
              Validated.invalidNel(
                "Invalid subcommand. Valid values are " + validSubCmds.mkString(
                  ", "
                )
              )
          case None =>
            Validated.invalidNel(
              "Invalid subcommand. Valid values are " + BramblCliSubCmd.values
                .mkString(", ")
            )
        }
    }
  }

  def validateNetworkType(networkType: String) = {
    Try(NetworkParamName.withName(networkType)).toOption match {
      case Some(networkType) => Validated.validNel(networkType)
      case None =>
        Validated.invalidNel(
          "Invalid network type. Valid values are main, valhalla, and private"
        )
    }
  }

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
  def validateToplNetworkUri(networkUri: String): ValidatedNel[String, Uri] = {
    val invalidUriMessage = "Invalid Topl network URI"
    try {
      Uri(networkUri) match {
        case uri if uri.isAbsolute => Validated.validNel(uri)
        case _                     => Validated.invalidNel(invalidUriMessage)
      }
    } catch {
      case _: Throwable => Validated.invalidNel(invalidUriMessage)
    }
  }

  def validateWalletCreate(paramConfig: BramblCliParams) = {
    (paramConfig.somePassword, paramConfig.someOutputFile) match {
      case (Some(password), Some(outputFile)) =>
        Validated.validNel((password, outputFile))
      case (None, Some(_)) =>
        Validated.invalidNel("Password is required for wallet creation")
      case (Some(_), None) =>
        Validated.invalidNel("Output file is required for wallet creation")
      case (None, None) =>
        Validated.invalidNel(
          "Password and output file are required for wallet creation"
        )
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
        validateToplNetworkUri(paramConfig.someNetworkUri.getOrElse("http://127.0.0.1:9085")),
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
      modeAndSubCmd match {
        case (BramblCliMode.wallet, BramblCliSubCmd.create) =>
          validateWalletCreate(paramConfig).map { passwordAndFile =>
            val (password, outputFile) = passwordAndFile
            BramblCliValidatedParams(
              mode,
              subcmd,
              provider,
              password,
              outputFile,
              None
            )
          }
      }
    }).andThen(x => x)
  }

}
