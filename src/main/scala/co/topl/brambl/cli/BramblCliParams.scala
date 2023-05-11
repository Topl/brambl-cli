package co.topl.brambl.cli

import co.topl.brambl.models.LockAddress
import co.topl.brambl.constants.NetworkConstants

object BramblCliMode extends Enumeration {
  type BramblCliMode = Value

  val wallet, utxo, simpletransaction = Value
}

object BramblCliSubCmd extends Enumeration {
  type BramblCliSubCmd = Value

  val init, query, create = Value
}

sealed abstract class NetworkIdentifiers(
    val i: Int,
    val name: String,
    val networkId: Int
) {
  override def toString: String = name
}

case object NetworkIdentifiers {

  def values = Set(Mainnet, Testnet, Privatenet)

  def fromString(s: String): Option[NetworkIdentifiers] = {
    s match {
      case "mainnet"    => Some(Mainnet)
      case "testnet"    => Some(Testnet)
      case "private" => Some(Privatenet)
      case _            => None
    }
  }
}

case object Mainnet
    extends NetworkIdentifiers(0, "mainnet", NetworkConstants.MAIN_NETWORK_ID)
case object Testnet
    extends NetworkIdentifiers(1, "testnet", NetworkConstants.TEST_NETWORK_ID)
case object Privatenet
    extends NetworkIdentifiers(
      2,
      "private",
      NetworkConstants.PRIVATE_NETWORK_ID
    )

object TokenType extends Enumeration {
  type TokenType = Value

  val lvl = Value
}

final case class BramblCliParams(
    mode: String = "",
    subcmd: String = "",
    password: String = "",
    host: String = "",
    port: Int = 0,
    network: String = "",
    someWalletFile: Option[String] = None,
    toAddress: Option[String] = None,
    amount: Long = 0L,
    someFromParty: Option[String] = None,
    someFromContract: Option[String] = None,
    someFromState: Option[String] = None,
    somePassphrase: Option[String] = None,
    someInputFile: Option[String] = None,
    someOutputFile: Option[String] = None
)
final case class BramblCliValidatedParams(
    mode: BramblCliMode.Value,
    subcmd: BramblCliSubCmd.Value,
    network: NetworkIdentifiers,
    host: String = "",
    port: Int = 0,
    walletFile: String = "",
    password: String,
    fromParty: String,
    fromContract: String,
    someFromState: Option[Int],
    toAddress: Option[LockAddress],
    amount: Long,
    somePassphrase: Option[String],
    someInputFile: Option[String] = None,
    someOutputFile: Option[String]
)
