package co.topl.brambl.cli.impl

import co.topl.crypto.encryption.VaultStore
import cats.effect.kernel.Sync
import co.topl.brambl.dataApi.WalletKeyApiAlgebra
import co.topl.brambl.cli.BramblCliValidatedParams
import co.topl.brambl.wallet.WalletApi
import quivr.models.KeyPair

class WalletManagementUtils[F[_]: Sync](
    walletApi: WalletApi[F],
    dataApi: WalletKeyApiAlgebra[F]
) {

  def loadKeysFromParam(params: BramblCliValidatedParams) = {
    import cats.implicits._
    for {
      wallet <- readInputFile(params.someKeyFile)
      keyPair <-
        walletApi
          .extractMainKey(wallet, params.password.getBytes())
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
      someInputFile: Option[String]
  ): F[VaultStore[F]] = {
    someInputFile match {
      case Some(inputFile) =>
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

      case None =>
        Sync[F].raiseError(
          (new Throwable("No input file (should not happen)"))
        )
    }
  }

}
