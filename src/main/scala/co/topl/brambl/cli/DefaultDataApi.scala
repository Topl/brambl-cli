package co.topl.brambl.cli

import cats.effect.kernel.Resource
import cats.effect.kernel.Sync
import co.topl.brambl.dataApi.DataApi
import co.topl.brambl.models.Indices
import co.topl.brambl.models.LockAddress
import co.topl.brambl.models.TransactionOutputAddress
import co.topl.brambl.models.box.Lock
import co.topl.brambl.models.transaction.UnspentTransactionOutput
import co.topl.crypto.encryption.VaultStore
import quivr.models.Preimage
import co.topl.brambl.cli.impl.WalletStateAlgebra

import java.io.PrintWriter
import scala.io.Source
import quivr.models.Proposition

case class DefaultDataApi[F[_]: Sync](walletStateAlgebra: WalletStateAlgebra[F])
    extends DataApi[F] {

  override def getLockByLockAddress(
      address: LockAddress
  ): F[Either[DataApi.DataApiException, Lock]] = ???

  override def getUtxoByTxoAddress(
      address: TransactionOutputAddress
  ): F[Either[DataApi.DataApiException, UnspentTransactionOutput]] = ???

  override def getPreimage(
      digestProposition: Proposition.Digest
  ): F[Either[DataApi.DataApiException, Preimage]] = ???

  override def getIndices(
      signatureProposition: Proposition.DigitalSignature
  ): F[Either[DataApi.DataApiException, Indices]] = {
    import cats.implicits._
    walletStateAlgebra
      .getIndicesBySignature(signatureProposition)
      .map(x =>
        Either.cond(
          x.isDefined,
          x.get,
          NoIndicesFound(new IllegalStateException("No indices found"))
        )
      )
  }

  override def updateMainKeyVaultStore(
      mainKeyVaultStore: VaultStore[F],
      name: String
  ): F[Either[DataApi.DataApiException, Unit]] = ???

  override def deleteMainKeyVaultStore(
      name: String
  ): F[Either[DataApi.DataApiException, Unit]] = ???

  case class NoIndicesFound(t: Throwable)
      extends DataApi.DataApiException(null, t)
  case class DecodeVaultStoreException(msg: String, t: Throwable)
      extends DataApi.DataApiException(msg, t)

  override def saveMainKeyVaultStore(
      mainKeyVaultStore: VaultStore[F],
      name: String
  ): F[Either[DataApi.DataApiException, Unit]] =
    Resource
      .make(Sync[F].delay(new PrintWriter(name)))(file =>
        Sync[F].delay(file.close())
      )
      .use { file =>
        import co.topl.crypto.encryption.VaultStore.Codecs._
        import io.circe.syntax._
        import cats.implicits._
        for {
          res <- Sync[F].blocking(file.write(mainKeyVaultStore.asJson.noSpaces))
        } yield res.asRight[DataApi.DataApiException]
      }

  override def getMainKeyVaultStore(
      name: String
  ): F[Either[DataApi.DataApiException, VaultStore[F]]] = Resource
    .make(Sync[F].delay(Source.fromFile(name))) { file =>
      Sync[F].delay(file.close())
    }
    .use { file =>
      import co.topl.crypto.encryption.VaultStore.Codecs._
      import io.circe.parser.decode
      import cats.implicits._
      for {
        inputString <- Sync[F].blocking(file.getLines().mkString("\n"))
        res <- Sync[F].delay(
          decode[VaultStore[F]](inputString)
            .leftMap(e => DecodeVaultStoreException("Invalid JSON", e))
        )
      } yield res
    }

}
