package co.topl.brambl.cli.validation

import scala.util.Try
import cats.data.Validated
import co.topl.brambl.cli.TokenType
import cats.data.ValidatedNel
import akka.http.scaladsl.model.Uri
import java.nio.file.Files
import java.nio.file.Paths
import co.topl.brambl.cli.BramblCliMode
import co.topl.brambl.cli.BramblCliSubCmd
import co.topl.brambl.cli.NetworkParamName

trait CommonValidationModule {

  
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
              List(
                BramblCliSubCmd.create,
                BramblCliSubCmd.sign,
                BramblCliSubCmd.balance
              )
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
              List(BramblCliSubCmd.create, BramblCliSubCmd.broadcast)
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


  protected def validateFileExists(fileTag: String, fileName: String) = {
    Files.exists(Paths.get(fileName)) match {
      case true => Validated.validNel(fileName)
      case false =>
        Validated.invalidNel(
          s"$fileTag does not exist"
        )
    }
  }
  protected def validateToplNetworkUri(networkUri: String): ValidatedNel[String, Uri] = {
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

  protected def validateTokenType(token: String) = {
    Try(TokenType.withName(token)).toOption match {
      case Some(token) => Validated.validNel(token)
      case None =>
        Validated.invalidNel(
          "Invalid token type. Valid values are poly"
        )
    }
  }
}
