package co.topl.brambl.cli.impl

import cats.effect.kernel.Sync
import co.topl.brambl.dataApi.WalletKeyApiAlgebra
import co.topl.brambl.wallet.WalletApi
import co.topl.crypto.encryption.VaultStore
import quivr.models.KeyPair

class WalletManagementUtils[F[_]: Sync](
    walletApi: WalletApi[F],
    dataApi: WalletKeyApiAlgebra[F]
) {

  def loadKeys(keyfile: String, password: String) = {
    import cats.implicits._
    for {
      wallet <- readInputFile(keyfile)
      keyPair <-
        walletApi
          .extractMainKey(wallet, password.getBytes())
          .flatMap(
            _.fold(
              _ =>
                Sync[F].raiseError[KeyPair](
                  new Throwable("No input file (should not happen)")
                ),
              Sync[F].point(_)
            )
          )
    } yield keyPair
  }

  def readInputFile(
      inputFile: String
  ): F[VaultStore[F]] = {
    import cats.implicits._
    dataApi
      .getMainKeyVaultStore(inputFile)
      .flatMap(
        _.fold(
          x =>
            Sync[F].raiseError[VaultStore[F]](
              new Throwable("Error reading input file: " + x)
            ),
          Sync[F].point(_)
        )
      )

  }

}
