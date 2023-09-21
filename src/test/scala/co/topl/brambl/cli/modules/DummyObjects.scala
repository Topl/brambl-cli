package co.topl.brambl.cli.modules
import co.topl.brambl.codecs.AddressCodecs
import co.topl.brambl.models.TransactionId
import co.topl.brambl.models.TransactionOutputAddress
import co.topl.brambl.models.box.Value
import co.topl.brambl.models.transaction.UnspentTransactionOutput
import co.topl.brambl.models.transaction.SpentTransactionOutput
import co.topl.genus.services.Txo
import com.google.protobuf.ByteString
import quivr.models.Int128
import co.topl.consensus.models.BlockId
import co.topl.node.models.BlockBody
import co.topl.brambl.models.box.Attestation
import co.topl.brambl.models.Datum
import co.topl.brambl.dataApi.GenusQueryAlgebra
import co.topl.brambl.models.LockAddress
import co.topl.genus.services.TxoState
import cats.Monad
trait DummyObjects {

  lazy val transactionId01 = TransactionId(
    ByteString.copyFrom(
      Array.fill[Byte](32)(0)
    )
  )

  lazy val lockAddress01 = AddressCodecs
    .decodeAddress(
      "ptetP7jshHVrEKqDRdKAZtuybPZoMWTKKM2ngaJ7L5iZnxP5BprDB3hGJEFr"
    )
    .toOption
    .get

  lazy val lvlValue01 = Value(
    Value.Value.Lvl(
      Value.LVL(
        Int128(ByteString.copyFrom(BigInt(100L).toByteArray))
      )
    )
  )

  lazy val transactionOutputAddress01 = TransactionOutputAddress(
    lockAddress01.network,
    lockAddress01.ledger,
    1,
    transactionId01
  )

  lazy val txo01 = Txo(
    UnspentTransactionOutput(
      lockAddress01,
      lvlValue01
    ),
    co.topl.genus.services.TxoState.UNSPENT,
    transactionOutputAddress01
  )

  lazy val blockId01 = BlockId(
    ByteString.copyFrom(
      Array.fill[Byte](32)(0)
    )
  )

  lazy val blockBody01 = BlockBody(
    Seq(transactionId01)
  )

  lazy val stxo01 = SpentTransactionOutput(
    transactionOutputAddress01,
    Attestation(Attestation.Value.Empty),
    lvlValue01
  )

  lazy val utxo01 = UnspentTransactionOutput(
    lockAddress01,
    lvlValue01
  )

  lazy val iotransaction01 = co.topl.brambl.models.transaction
    .IoTransaction(
      Some(transactionId01),
      Seq(stxo01),
      Seq(utxo01),
      Datum.IoTransaction.defaultInstance
    )

  def makeGenusQueryAlgebraMockWithAddress[F[_]: Monad] =
    new GenusQueryAlgebra[F] {

      override def queryUtxo(
          fromAddress: LockAddress,
          txoState: TxoState
      ): F[Seq[Txo]] = {
        Monad[F].pure(
          Seq(txo01)
        )
      }
    }

}
