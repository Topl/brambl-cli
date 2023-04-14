package co.topl.brambl.cli

import co.topl.brambl.dataApi.DataApi
import cats.effect.kernel.Sync
import quivr.models.Preimage
import co.topl.crypto.encryption.VaultStore
import co.topl.brambl.models.Indices
import co.topl.brambl.routines.signatures.Signing
import quivr.models.KeyPair
import co.topl.brambl.models.TransactionOutputAddress
import co.topl.brambl.models.transaction.UnspentTransactionOutput
import co.topl.brambl.models.LockAddress
import co.topl.brambl.models.box.Lock
import cats.effect.kernel.Resource
import java.io.PrintWriter
import scala.io.Source


case class DefaultDataApi[F[_]: Sync]() extends DataApi[F] {

  case class DecodeVaultStoreException(msg: String, t: Throwable) extends DataApi.DataApiException(msg, t)


  override def getIndicesByTxoAddress(
      address: TransactionOutputAddress
  ): Option[Indices] = ???

  override def getUtxoByTxoAddress(
      address: TransactionOutputAddress
  ): Option[UnspentTransactionOutput] = ???

  override def getLockByLockAddress(address: LockAddress): Option[Lock] = ???

  override def getPreimage(idx: Indices): Option[Preimage] = ???

  override def getKeyPair(idx: Indices, routine: Signing): Option[KeyPair] = ???

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
  ): F[Either[DataApi.DataApiException, VaultStore[F]]] = Resource.make(Sync[F].delay(Source.fromFile(name))) { file =>
    Sync[F].delay(file.close())
  }.use { file =>
    import co.topl.crypto.encryption.VaultStore.Codecs._
    import io.circe.parser.decode
    import cats.implicits._
    for {
      inputString <- Sync[F].blocking(file.getLines().mkString("\n"))
      res <- Sync[F].delay(decode[VaultStore[F]](inputString).leftMap(e => DecodeVaultStoreException("Invalid JSON", e)))
    } yield res
  }

}
