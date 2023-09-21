package co.topl.brambl.cli.mockbase

import co.topl.brambl.cli.impl.WalletAlgebra

class BaseWalletAlgebra[F[_]] extends WalletAlgebra[F] {

  override def createWalletFromParams(
      networkId: Int,
      ledgerId: Int,
      password: String,
      somePassphrase: Option[String],
      someOutputFile: Option[String],
      someMnemonicFile: Option[String]
  ): F[Unit] = ???

  def recoverKeysFromParams(
      mnemonic: IndexedSeq[String],
      password: String,
      networkId: Int,
      ledgerId: Int,
      somePassphrase: Option[String],
      someOutputFile: Option[String]
  ): F[Unit] = ???

}
