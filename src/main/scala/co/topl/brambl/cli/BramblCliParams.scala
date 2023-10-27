package co.topl.brambl.cli

import co.topl.brambl.constants.NetworkConstants
import co.topl.brambl.models.LockAddress

import java.io.File
import scala.collection.immutable.IndexedSeq
import co.topl.brambl.models.GroupId
import co.topl.brambl.models.SeriesId

object BramblCliMode extends Enumeration {
  type BramblCliMode = Value

  val invalid, wallet, genusquery, bifrostquery, simpletransaction,
      simpleminting, fellowships, contracts, tx =
    Value
}

object BramblCliSubCmd extends Enumeration {
  type BramblCliSubCmd = Value

  val invalid, init, recoverkeys, utxobyaddress, blockbyheight, blockbyid,
      transactionbyid, create, prove, broadcast, currentaddress, list, add,
      inspect, exportvk, importvks, sync, balance = Value
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
case object InvalidNet
    extends NetworkIdentifiers(
      -1,
      "invalid",
      NetworkConstants.PRIVATE_NETWORK_ID
    )

object TokenType extends Enumeration {
  type TokenType = Value

  val all, lvl, topl, asset, group, series = Value
}

final case class BramblCliParams(
    mode: BramblCliMode.Value = BramblCliMode.invalid,
    subcmd: BramblCliSubCmd.Value = BramblCliSubCmd.invalid,
    tokenType: TokenType.Value = TokenType.all,
    network: NetworkIdentifiers = InvalidNet,
    fellowshipName: String = "",
    contractName: String = "",
    lockTemplate: String = "",
    inputVks: Seq[File] = Seq(),
    host: String = "",
    bifrostPort: Int = 0,
    walletFile: String = "",
    password: String = "",
    fromFellowship: String = "",
    fromContract: String = "",
    fromAddress: Option[String] = None,
    height: Long = -1,
    blockId: String = "",
    transactionId: String = "",
    someFromState: Option[Int] = None,
    someChangeFellowship: Option[String] = None,
    someChangeContract: Option[String] = None,
    someChangeState: Option[Int] = None,
    toAddress: Option[LockAddress] = None,
    someToFellowship: Option[String] = None,
    someToContract: Option[String] = None,
    amount: Long = -1,
    fee: Long = -1,
    somePassphrase: Option[String] = None,
    someKeyFile: Option[String] = None,
    someInputFile: Option[String] = None,
    someCommitment: Option[String] = None,
    ephemeralMetadata: Option[File] = None,
    someOutputFile: Option[String] = None,
    mnemonic: Seq[String] = IndexedSeq(),
    someMnemonicFile: Option[String] = None,
    somePolicyFile: Option[File] = None,
    someGroupId: Option[GroupId] = None,
    someSeriesId: Option[SeriesId] = None,
    secureConnection: Boolean = false
)
