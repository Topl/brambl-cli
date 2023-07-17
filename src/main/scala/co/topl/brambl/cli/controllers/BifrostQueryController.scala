package co.topl.brambl.cli.controllers

import cats.Functor
import co.topl.brambl.cli.views.BlockDisplayOps
import co.topl.brambl.dataApi.BifrostQueryAlgebra
import co.topl.brambl.models.TransactionId
import co.topl.brambl.utils.Encoding
import co.topl.consensus.models.BlockId
import com.google.protobuf.ByteString

class BifrostQueryController[F[_]: Functor](
    bifrostQueryAlgebra: BifrostQueryAlgebra[F]
) {
  def blockByHeight(
      height: Long
  ): F[Either[String, String]] = {
    import cats.implicits._
    bifrostQueryAlgebra
      .blockByHeight(
        height
      )
      .map { someResult =>
        someResult match {
          case Some(((blockId, _, ioTransactions))) =>
            Right(BlockDisplayOps.display(blockId, ioTransactions))
          case None =>
            Left("No blocks found at that height")
        }
      }
  }

  def blockById(
      pBlockId: String
  ): F[Either[String, String]] = {
    import cats.implicits._
    bifrostQueryAlgebra
      .blockById(
        Encoding
          .decodeFromBase58(pBlockId)
          .map(x => BlockId(ByteString.copyFrom(x)))
          .toOption // validation should ensure that this is a Some
          .get
      )
      .map { someResult =>
        someResult match {
          case Some(((blockId, _, ioTransactions))) =>
            Right(BlockDisplayOps.display(blockId, ioTransactions))
          case None =>
            Left("No blocks found at that block id")
        }
      }
  }

  def fetchTransaction(transactionId: String): F[Either[String, String]] = {
    import cats.implicits._
    bifrostQueryAlgebra
      .fetchTransaction(
        Encoding
          .decodeFromBase58(transactionId)
          .map(x => TransactionId(ByteString.copyFrom(x)))
          .toOption // validation should ensure that this is a Some
          .get
      )
      .map { someResult =>
        someResult match {
          case Some(ioTransaction) =>
            Right(BlockDisplayOps.display(ioTransaction))
          case None =>
            Left(s"No transaction found with id ${transactionId}")
        }
      }
  }

}
