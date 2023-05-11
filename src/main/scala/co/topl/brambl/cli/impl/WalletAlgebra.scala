package co.topl.brambl.cli.impl

import cats.effect.kernel.Sync
import co.topl.brambl.cli.BramblCliValidatedParams
import co.topl.brambl.wallet.WalletApi
import quivr.models.KeyPair
import co.topl.brambl.models.Indices
import co.topl.crypto.encryption.VaultStore

trait WalletAlgebra[F[_]] {

  def createWalletFromParams(params: BramblCliValidatedParams): F[Unit]

}

object WalletAlgebra {
  def make[F[_]: Sync](
      walletApi: WalletApi[F],
      walletStateApi: WalletStateAlgebra[F]
  ) = new WalletAlgebra[F] {
    import cats.implicits._

    private def createNewWallet(params: BramblCliValidatedParams) = walletApi
      .createNewWallet(
        params.password.getBytes(),
        params.somePassphrase
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

    def createWalletFromParams(params: BramblCliValidatedParams) = {
      import io.circe.syntax._
      import co.topl.crypto.encryption.VaultStore.Codecs._

      for {
        wallet <- createNewWallet(params)
        keyPair <- extractMainKey(wallet.mainKeyVaultStore, params.password)
        _ <- params.someOutputFile
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
        derivedKey <- walletApi.deriveChildKeys(
          keyPair,
          new Indices(
            1,
            1,
            1
          )
        )
        _ <- walletStateApi.initWalletState(derivedKey.vk)
      } yield ()

    }
  }

}
