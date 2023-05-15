package co.topl.brambl.cli.impl

import cats.effect.kernel.Resource
import cats.effect.kernel.Sync
import co.topl.brambl.models.LockAddress
import co.topl.genus.services.QueryByAddressRequest
import co.topl.genus.services.TransactionServiceGrpc
import co.topl.genus.services.Txo
import co.topl.genus.services.TxoState
import io.grpc.ManagedChannel

trait UtxoAlgebra[F[_]] {

  def queryUtxo(fromAddress: LockAddress): F[Seq[Txo]]

}

object UtxoAlgebra {

  def make[F[_]: Sync](channelResource: Resource[F, ManagedChannel]) =
    new UtxoAlgebra[F] {

      def queryUtxo(fromAddress: LockAddress): F[Seq[Txo]] = {
        import cats.implicits._
        (for {
          channel <- channelResource
        } yield channel).use { channel =>
          for {
            blockingStub <- Sync[F].point(
              TransactionServiceGrpc.blockingStub(channel)
            )
            response <- Sync[F].blocking(
              blockingStub
                .getTxosByAddress(
                  QueryByAddressRequest(fromAddress, None, TxoState.UNSPENT)
                )
            )
          } yield response.txos
        }
      }
    }
}
