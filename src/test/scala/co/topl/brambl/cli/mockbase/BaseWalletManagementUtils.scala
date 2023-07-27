package co.topl.brambl.cli.mockbase

import co.topl.brambl.cli.impl.WalletManagementUtils
import cats.effect.kernel.Sync
import co.topl.crypto.encryption.VaultStore
import quivr.models.KeyPair

class BaseWalletManagementUtils[F[_]: Sync]
    extends WalletManagementUtils[F](null, null) {
  override def loadKeys(keyfile: String, password: String): F[KeyPair] = ???

  override def readInputFile(
      inputFile: String
  ): F[VaultStore[F]] = ???
}
