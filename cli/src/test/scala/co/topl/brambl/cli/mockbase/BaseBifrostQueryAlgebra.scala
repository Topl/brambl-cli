package co.topl.brambl.cli.mockbase

import co.topl.brambl.dataApi.BifrostQueryAlgebra
import co.topl.brambl.models.TransactionId
import co.topl.brambl.models.transaction.IoTransaction
import co.topl.consensus.models.BlockId
import co.topl.node.models.BlockBody
import co.topl.consensus.models.BlockHeader
import co.topl.node.services.SynchronizationTraversalRes

abstract class BaseBifrostQueryAlgebra[F[_]] extends BifrostQueryAlgebra[F] {

  override def synchronizationTraversal()
      : F[Iterator[SynchronizationTraversalRes]] = ???

  override def makeBlock(nbOfBlocks: Int): F[Unit] = ???

  override def blockByHeight(
      height: Long
  ): F[Option[(BlockId, BlockHeader, BlockBody, Seq[IoTransaction])]] = ???

  override def blockById(
      blockId: BlockId
  ): F[Option[(BlockId, BlockHeader, BlockBody, Seq[IoTransaction])]] = ???

  override def blockByDepth(
      depth: Long
  ): F[Option[(BlockId, BlockHeader, BlockBody, Seq[IoTransaction])]] = ???

  override def fetchTransaction(
      txId: TransactionId
  ): F[Option[IoTransaction]] = ???

  override def broadcastTransaction(tx: IoTransaction): F[TransactionId] = ???
}
