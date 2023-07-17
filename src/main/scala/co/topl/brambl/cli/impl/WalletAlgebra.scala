package co.topl.brambl.cli.impl

import cats.effect.kernel.Sync
import co.topl.brambl.dataApi.WalletStateAlgebra
import co.topl.brambl.wallet.WalletApi
import co.topl.crypto.encryption.VaultStore
import quivr.models.KeyPair

trait WalletAlgebra[F[_]] {

  def createWalletFromParams(
      password: String,
      somePassphrase: Option[String],
      someOutputFile: Option[String]
  ): F[Unit]

}

object WalletAlgebra {
  def make[F[_]: Sync](
      walletApi: WalletApi[F],
      walletStateApi: WalletStateAlgebra[F]
  ) = new WalletAlgebra[F] {
    import cats.implicits._

    private def createNewWallet(
        password: String,
        somePassphrase: Option[String]
    ) = walletApi
      .createNewWallet(
        password.getBytes(),
        somePassphrase
      )
      .map(_.fold(throw _, identity))

    private def extractMainKey(
        wallet: VaultStore[F],
        password: String
    ): F[KeyPair] =
      walletApi
        .extractMainKey(
          wallet,
          password.getBytes()
        )
        .flatMap(
          _.fold(
            _ =>
              Sync[F].raiseError[KeyPair](
                new Throwable("No input file (should not happen)")
              ),
            Sync[F].point(_)
          )
        )

    private def saveWallet(
        wallet: VaultStore[F],
        outputFile: String
    ) = {
      walletApi
        .saveWallet(
          wallet,
          outputFile
        )
        .map(_.fold(throw _, identity))
    }

    def createWalletFromParams(
        password: String,
        somePassphrase: Option[String],
        someOutputFile: Option[String]
    ) = {
      import io.circe.syntax._
      import co.topl.crypto.encryption.VaultStore.Codecs._

      for {
        wallet <- createNewWallet(password, somePassphrase)
        keyPair <- extractMainKey(wallet.mainKeyVaultStore, password)
        _ <- someOutputFile
          .map { outputFile =>
            saveWallet(
              wallet.mainKeyVaultStore,
              outputFile
            )
          }
          .getOrElse {
            Sync[F].delay(
              println(new String(wallet.mainKeyVaultStore.asJson.noSpaces))
            )
          }
        derivedKey <- walletApi.deriveChildKeysPartial(keyPair, 1, 1)
        _ <- walletStateApi.initWalletState(derivedKey.vk)
      } yield ()

    }
  }

}
