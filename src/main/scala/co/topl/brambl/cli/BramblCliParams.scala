package co.topl.brambl.cli

import co.topl.client.Provider

object NetworkParamName extends Enumeration {
  type NetworkParamName = Value

  val main, valhalla, `private` = Value
}

object BramblCliMode extends Enumeration {
  type BramblCliMode = Value

  val wallet, transaction = Value
}

object BramblCliSubCmd extends Enumeration {
  type BramblCliSubCmd = Value

  val create, sign, broadcast, balance = Value
}

object TokenType extends Enumeration {
  type TokenType = Value

  val poly = Value
}

final case class BramblCliParams(
    mode: String = "",
    subcmd: String = "",
    networkType: String = "",
    someNetworkUri: Option[String] = None,
    someOutputFile: Option[String] = None,
    someApiKey: Option[String] = None,
    someKeyfile: Option[String] = None,
    somePassword: Option[String] = None,
    someToken: Option[String] = None,
    fromAddresses: Seq[String] = Nil,
    toAddresses: Map[String, Int] = Map(),
    changeAddress: String = "",
    someInputFile: Option[String] = None,
    fee: Int = 0
)
final case class BramblCliValidatedParams(
    mode: BramblCliMode.Value,
    subcmd: BramblCliSubCmd.Value,
    provider: Provider,
    password: String,
    someTokenType: Option[TokenType.Value],
    someOutputFile: Option[String],
    someInputFile: Option[String] = None,
    someKeyfile: Option[String],
    fromAddresses: Seq[String],
    toAddresses: List[(String, Int)],
    changeAddress: String,
    fee: Int = 0
)
