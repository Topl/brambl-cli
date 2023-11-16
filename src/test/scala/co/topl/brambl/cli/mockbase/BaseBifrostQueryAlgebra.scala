package co.topl.brambl.cli.mockbase

import co.topl.brambl.dataApi.BifrostQueryAlgebra
import co.topl.brambl.models.TransactionId
import co.topl.brambl.models.transaction.IoTransaction
import co.topl.consensus.models.BlockId
import co.topl.node.models.BlockBody

abstract class BaseBifrostQueryAlgebra[F[_]]
    extends BifrostQueryAlgebra[F] {

  override def blockByHeight(
      height: Long
  ): F[Option[(BlockId, BlockBody, Seq[IoTransaction])]] = ???

  override def blockById(
      blockId: BlockId
  ): F[Option[(BlockId, BlockBody, Seq[IoTransaction])]] = ???

  override def fetchTransaction(
      txId: TransactionId
  ): F[Option[IoTransaction]] = ???

  override def broadcastTransaction(tx: IoTransaction): F[TransactionId] = ???
}
