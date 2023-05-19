package co.topl.brambl.cli

import co.topl.brambl.models.LockAddress
import co.topl.brambl.constants.NetworkConstants
import co.topl.consensus.models.BlockId

object BramblCliMode extends Enumeration {
  type BramblCliMode = Value

  val wallet, genusquery, bifrostquery, simpletransaction = Value
}

object BramblCliSubCmd extends Enumeration {
  type BramblCliSubCmd = Value

  val init, utxobyaddress, blockbyheight, blockbyid, transactionbyid, create,
      prove, broadcast = Value
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
      case "mainnet" => Some(Mainnet)
      case "testnet" => Some(Testnet)
      case "private" => Some(Privatenet)
      case _         => None
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
    genusPort: Int = 0,
    bifrostPort: Int = 0,
    network: String = "",
    someWalletFile: Option[String] = None,
    toAddress: Option[String] = None,
    amount: Long = 0L,
    height: Long = 0L,
    blockId: Option[String] = None,
    transactionId: Option[String] = None,
    someFromParty: Option[String] = None,
    someFromContract: Option[String] = None,
    someFromState: Option[String] = None,
    somePassphrase: Option[String] = None,
    someKeyFile: Option[String] = None,
    someInputFile: Option[String] = None,
    someOutputFile: Option[String] = None
)
final case class BramblCliValidatedParams(
    mode: BramblCliMode.Value,
    subcmd: BramblCliSubCmd.Value,
    network: NetworkIdentifiers,
    host: String = "",
    genusPort: Int = 0,
    bifrostPort: Int = 0,
    walletFile: String = "",
    password: String,
    fromParty: String,
    fromContract: String,
    height: Long,
    blockId: Option[String],
    transactionId: Option[String] = None,
    someFromState: Option[Int],
    toAddress: Option[LockAddress],
    amount: Long,
    somePassphrase: Option[String],
    someKeyFile: Option[String] = None,
    someInputFile: Option[String] = None,
    someOutputFile: Option[String]
)
