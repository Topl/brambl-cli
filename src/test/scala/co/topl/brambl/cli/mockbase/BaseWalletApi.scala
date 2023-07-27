package co.topl.brambl.cli.mockbase

import co.topl.brambl.models.Indices
import co.topl.brambl.wallet.WalletApi
import co.topl.crypto.encryption.VaultStore
import co.topl.crypto.generation.mnemonic.MnemonicSize
import quivr.models.KeyPair
import quivr.models.VerificationKey

class BaseWalletApi[F[_]] extends WalletApi[F] {

  override def saveWallet(
      vaultStore: VaultStore[F],
      name: String
  ): F[Either[WalletApi.WalletApiFailure, Unit]] = ???

  override def loadWallet(
      name: String
  ): F[Either[WalletApi.WalletApiFailure, VaultStore[F]]] = ???

  override def updateWallet(
      newWallet: VaultStore[F],
      name: String
  ): F[Either[WalletApi.WalletApiFailure, Unit]] = ???

  override def deleteWallet(
      name: String
  ): F[Either[WalletApi.WalletApiFailure, Unit]] = ???

  override def buildMainKeyVaultStore(
      mainKey: Array[Byte],
      password: Array[Byte]
  ): F[VaultStore[F]] = ???

  override def createNewWallet(
      password: Array[Byte],
      passphrase: Option[String],
      mLen: MnemonicSize
  ): F[Either[WalletApi.WalletApiFailure, WalletApi.NewWalletResult[F]]] = ???

  override def extractMainKey(
      vaultStore: VaultStore[F],
      password: Array[Byte]
  ): F[Either[WalletApi.WalletApiFailure, KeyPair]] = ???

  override def deriveChildKeys(keyPair: KeyPair, idx: Indices): F[KeyPair] = ???

  override def deriveChildKeysPartial(
      keyPair: KeyPair,
      xParty: Int,
      yContract: Int
  ): F[KeyPair] = ???

  override def deriveChildVerificationKey(
      vk: VerificationKey,
      idx: Int
  ): F[VerificationKey] = ???

  override def importWallet(
      mnemonic: IndexedSeq[String],
      password: Array[Byte],
      passphrase: Option[String]
  ): F[Either[WalletApi.WalletApiFailure, VaultStore[F]]] = ???

  override def recoverWallet(
      mainKeyVaultStore: VaultStore[F],
      password: Array[Byte],
      name: String,
      snapshot: Option[Any]
  ): F[Either[WalletApi.WalletApiFailure, Unit]] = ???

}
