package co.topl.brambl.cli

import cats.effect.kernel.Resource
import cats.effect.kernel.Sync
import co.topl.brambl.dataApi.WalletKeyApiAlgebra
import co.topl.crypto.encryption.VaultStore

import java.io.PrintWriter
import scala.io.Source

case class DefaultWalletKeyApi[F[_]: Sync]() extends WalletKeyApiAlgebra[F] {

  override def updateMainKeyVaultStore(
      mainKeyVaultStore: VaultStore[F],
      name: String
  ): F[Either[WalletKeyApiAlgebra.WalletKeyException, Unit]] = ???

  override def deleteMainKeyVaultStore(
      name: String
  ): F[Either[WalletKeyApiAlgebra.WalletKeyException, Unit]] = ???

  case class DecodeVaultStoreException(msg: String, t: Throwable)
      extends WalletKeyApiAlgebra.WalletKeyException(msg, t)

  override def saveMainKeyVaultStore(
      mainKeyVaultStore: VaultStore[F],
      name: String
  ): F[Either[WalletKeyApiAlgebra.WalletKeyException, Unit]] =
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
        } yield res.asRight[WalletKeyApiAlgebra.WalletKeyException]
      }

  override def getMainKeyVaultStore(
      name: String
  ): F[Either[WalletKeyApiAlgebra.WalletKeyException, VaultStore[F]]] = Resource
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
