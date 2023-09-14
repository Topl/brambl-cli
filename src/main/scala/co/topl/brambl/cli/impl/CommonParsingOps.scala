package co.topl.brambl.cli.impl

import co.topl.brambl.codecs.AddressCodecs
import co.topl.brambl.constants.NetworkConstants
import co.topl.brambl.models.TransactionId
import co.topl.brambl.models.TransactionOutputAddress
import co.topl.brambl.models.box.Value
import co.topl.brambl.models.transaction.UnspentTransactionOutput
import co.topl.brambl.utils.Encoding
import com.google.protobuf.ByteString
import quivr.models.Int128

import scala.util.Try

object CommonParsingOps {

  import cats.implicits._

  def parseUnspentTransactionOutput(
      lockAddressString: String,
      value: Long
  ): Either[CommonParserError, UnspentTransactionOutput] =
    for {
      lockAddress <-
        AddressCodecs
          .decodeAddress(lockAddressString)
          .leftMap(_ =>
            InvalidAddress(
              "Invalid address for unspent transaction output: " + lockAddressString
            ): CommonParserError
          )
    } yield UnspentTransactionOutput(
      lockAddress,
      Value(
        Value.Value.Lvl(
          Value.LVL(
            Int128(ByteString.copyFrom(BigInt(value).toByteArray))
          )
        )
      )
    )

  def parseTransactionOuputAddress(
      networkId: Int,
      address: String
  ) = for {
    sp <- Right(address.split("#"))
    idx <- Try(sp(1).toInt).toEither.leftMap(_ =>
        InvalidAddress("Invalid index for address: " + address)
      )
    txIdByteArray <- Encoding
        .decodeFromBase58(sp(0))
        .leftMap(_ => InvalidAddress("Invalid transaction id for: " + sp(0)))
    txId <- Right(
        TransactionId(
          ByteString.copyFrom(
            txIdByteArray
          )
        )
      )
  } yield TransactionOutputAddress(
    networkId,
    NetworkConstants.MAIN_LEDGER_ID,
    idx,
    txId
  )
}
