package co.topl.brambl.cli.impl

import cats.arrow.FunctionK
import cats.data.Kleisli
import cats.data.OptionT
import cats.effect.kernel.Resource
import cats.effect.kernel.Sync
import cats.free.Free
import co.topl.brambl.models.TransactionId
import co.topl.brambl.models.transaction.IoTransaction
import co.topl.consensus.models.BlockId
import co.topl.node.models.BlockBody
import co.topl.node.services.FetchBlockBodyReq
import co.topl.node.services.FetchBlockIdAtHeightReq
import co.topl.node.services.FetchTransactionReq
import co.topl.node.services.NodeRpcGrpc
import io.grpc.ManagedChannel

trait BifrostQueryAlgebra[F[_]] {

  def blockByHeight(
      height: Long
  ): F[Option[(BlockId, BlockBody, Seq[IoTransaction])]]

  def blockById(
      blockId: BlockId
  ): F[Option[(BlockId, BlockBody, Seq[IoTransaction])]]

  def fetchTransaction(txId: TransactionId): F[Option[IoTransaction]]

}

object BifrostQueryAlgebra {

  sealed trait BifrostQueryADT[A]

  case class FetchBlockBody(blockId: BlockId)
      extends BifrostQueryADT[Option[BlockBody]]

  case class FetchTransaction(txId: TransactionId)
      extends BifrostQueryADT[Option[IoTransaction]]

  case class BlockByHeight(height: Long)
      extends BifrostQueryADT[Option[BlockId]]

  type BifrostQueryADTMonad[A] = Free[BifrostQueryADT, A]

  def fetchBlockBodyF(
      blockId: BlockId
  ): BifrostQueryADTMonad[Option[BlockBody]] =
    Free.liftF(FetchBlockBody(blockId))

  def fetchTransactionF(
      txId: TransactionId
  ): BifrostQueryADTMonad[Option[IoTransaction]] =
    Free.liftF(FetchTransaction(txId))

  def blockByHeightF(height: Long): BifrostQueryADTMonad[Option[BlockId]] =
    Free.liftF(BlockByHeight(height))

  def interpretADT[A, F[_]: Sync](
      channelResource: Resource[F, ManagedChannel],
      computation: BifrostQueryADTMonad[A]
  ) = {
    type ChannelContextKlesli[A] =
      Kleisli[F, NodeRpcGrpc.NodeRpcBlockingStub, A]
    val kleisliComputation = computation.foldMap[ChannelContextKlesli](
      new FunctionK[BifrostQueryADT, ChannelContextKlesli] {

        override def apply[A](fa: BifrostQueryADT[A]): ChannelContextKlesli[A] =
          fa match {
            case FetchBlockBody(blockId) =>
              Kleisli(blockingStub =>
                Sync[F].blocking(
                  blockingStub
                    .fetchBlockBody(
                      FetchBlockBodyReq(blockId)
                    )
                    .asInstanceOf[A]
                )
              )
            case FetchTransaction(txId) =>
              Kleisli(blockingStub =>
                Sync[F].blocking(
                  blockingStub
                    .fetchTransaction(
                      FetchTransactionReq(txId)
                    )
                    .asInstanceOf[A]
                )
              )
            case BlockByHeight(height) =>
              Kleisli(blockingStub =>
                Sync[F].blocking(
                  blockingStub
                    .fetchBlockIdAtHeight(
                      FetchBlockIdAtHeightReq(height)
                    )
                    .asInstanceOf[A]
                )
              )
          }

      }
    )
    (for {
      channel <- channelResource
    } yield channel).use { channel =>
      kleisliComputation.run(NodeRpcGrpc.blockingStub(channel))
    }
  }

  def make[F[_]: Sync](channelResource: Resource[F, ManagedChannel]) =
    new BifrostQueryAlgebra[F] {

      override def blockById(
          blockId: BlockId
      ): F[Option[(BlockId, BlockBody, Seq[IoTransaction])]] = {
        import cats.implicits._
        interpretADT(
          channelResource,
          (for {
            blockBody <- OptionT(fetchBlockBodyF(blockId))
            transactions <- blockBody.transactionIds
              .map(txId => OptionT(fetchTransactionF(txId)))
              .sequence
          } yield (blockId, blockBody, transactions)).value
        )
      }

      override def fetchTransaction(
          txId: TransactionId
      ): F[Option[IoTransaction]] = {
        interpretADT(channelResource, fetchTransactionF(txId))
      }

      def blockByHeight(height: Long) = {
        import cats.implicits._
        interpretADT(
          channelResource,
          (for {
            blockId <- OptionT(blockByHeightF(height))
            blockBody <- OptionT(fetchBlockBodyF(blockId))
            transactions <- blockBody.transactionIds
              .map(txId => OptionT(fetchTransactionF(txId)))
              .sequence
          } yield (blockId, blockBody, transactions)).value
        )
      }
    }
}
