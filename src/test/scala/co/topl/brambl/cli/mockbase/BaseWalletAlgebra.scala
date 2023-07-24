package co.topl.brambl.cli.mockbase

import co.topl.brambl.cli.impl.WalletAlgebra

class BaseWalletAlgebra[F[_]] extends WalletAlgebra[F] {

  override def createWalletFromParams(
      password: String,
      somePassphrase: Option[String],
      someOutputFile: Option[String]
  ): F[Unit] = ???

}
