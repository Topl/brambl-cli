package co.topl.brambl.cli.impl

import cats.effect.kernel.Resource
import cats.effect.kernel.Sync
import co.topl.brambl.models.LockAddress
import co.topl.genus.services.QueryByLockAddressRequest
import co.topl.genus.services.TransactionServiceGrpc
import co.topl.genus.services.Txo
import co.topl.genus.services.TxoState
import io.grpc.ManagedChannel

trait GenusQueryAlgebra[F[_]] {

  def queryUtxo(fromAddress: LockAddress): F[Seq[Txo]]

}

object GenusQueryAlgebra {

  def make[F[_]: Sync](channelResource: Resource[F, ManagedChannel]) =
    new GenusQueryAlgebra[F] {

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
                .getTxosByLockAddress(
                  QueryByLockAddressRequest(fromAddress, None, TxoState.UNSPENT)
                )
            )
          } yield response.txos
        }
      }
    }
}
