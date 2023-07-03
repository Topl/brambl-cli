package co.topl.brambl.cli.controllers

import cats.effect.IO
import co.topl.brambl.cli.BramblCliValidatedParams
import cats.effect.kernel.Resource
import io.grpc.ManagedChannel
import co.topl.brambl.dataApi.BifrostQueryAlgebra
import co.topl.brambl.cli.views.BlockDisplayOps
import co.topl.brambl.utils.Encoding
import co.topl.consensus.models.BlockId
import com.google.protobuf.ByteString
import co.topl.brambl.models.TransactionId

class BifrostQueryController(channelResource: Resource[IO, ManagedChannel]) {
  def blockByHeight(
      params: BramblCliValidatedParams
  ): IO[String] = {
    BifrostQueryAlgebra
      .make[IO](channelResource)
      .blockByHeight(
        params.height
      )
      .map { someResult =>
        someResult match {
          case Some(((blockId, _, ioTransactions))) =>
            BlockDisplayOps.display(blockId, ioTransactions)
          case None =>
            "No blocks found at that block id"
        }
      }
  }

  def blockById(
      params: BramblCliValidatedParams
  ): IO[String] = BifrostQueryAlgebra
    .make[IO](channelResource)
    .blockById(
      Encoding
        .decodeFromBase58(params.blockId.get)
        .map(x => BlockId(ByteString.copyFrom(x)))
        .toOption // validation should ensure that this is a Some
        .get
    )
    .map { someResult =>
      someResult match {
        case Some(((blockId, _, ioTransactions))) =>
          BlockDisplayOps.display(blockId, ioTransactions)
        case None =>
          "No blocks found at that block id"
      }
    }

  def fetchTransaction(params: BramblCliValidatedParams): IO[String] =
    BifrostQueryAlgebra
      .make[IO](channelResource)
      .fetchTransaction(
        Encoding
          .decodeFromBase58(params.transactionId.get)
          .map(x => TransactionId(ByteString.copyFrom(x)))
          .toOption // validation should ensure that this is a Some
          .get
      )
      .map { someResult =>
        someResult match {
          case Some(ioTransaction) =>
            BlockDisplayOps.display(ioTransaction)
          case None =>
            s"No transaction found with id ${params.transactionId.get}"
        }
      }

}
