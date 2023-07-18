package co.topl.brambl.cli.impl

import cats.effect.Resource
import cats.effect.kernel.Sync
import co.topl.brambl.dataApi.WalletStateAlgebra
import co.topl.brambl.wallet.WalletApi
import co.topl.crypto.encryption.VaultStore
import quivr.models.KeyPair

import java.io.FileOutputStream

trait WalletAlgebra[F[_]] {

  def createWalletFromParams(
      password: String,
      somePassphrase: Option[String],
      someOutputFile: Option[String],
      someMnemonicFile: Option[String]
  ): F[Unit]

  def recoverKeysFromParams(
    mnemonic: IndexedSeq[String],
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

    private def recoverWalletKey(
      mnemonic: IndexedSeq[String],
      password: String,
      somePassphrase: Option[String]
    ) = walletApi
      .importWallet(
        mnemonic,
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

    private def saveMnemonic(
      mnemonic: IndexedSeq[String],
      mnemonicFile: String
                          ) = {
      Resource
        .make(
          Sync[F]
            .delay(
              new FileOutputStream(mnemonicFile)
            )
        )(fos => Sync[F].delay(fos.close()))
        .use { fos =>
          Sync[F].delay(fos.write(mnemonic.mkString(",").getBytes))
        }
    }

    def createWalletFromParams(
        password: String,
        somePassphrase: Option[String],
        someOutputFile: Option[String],
        someMnemonicFile: Option[String]
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
        _ <- someMnemonicFile
          .map { mnemonicFile =>
            saveMnemonic(
              wallet.mnemonic,
              mnemonicFile
            )
          }
          .getOrElse {
            Sync[F].delay(
              println(wallet.mnemonic.mkString(","))
            )
          }
        derivedKey <- walletApi.deriveChildKeysPartial(keyPair, 1, 1)
        _ <- walletStateApi.initWalletState(derivedKey.vk)
      } yield ()

    }
    def recoverKeysFromParams(
      mnemonic: IndexedSeq[String],
      password: String,
      somePassphrase: Option[String],
      someOutputFile: Option[String]
    ) = {
      import io.circe.syntax._
      import co.topl.crypto.encryption.VaultStore.Codecs._

      for {
        wallet <- recoverWalletKey(mnemonic, password, somePassphrase)
        _ <- someOutputFile
          .map { outputFile =>
            saveWallet(
              wallet,
              outputFile
            )
          }
          .getOrElse {
            Sync[F].delay(
              println(new String(wallet.asJson.noSpaces))
            )
          }
      } yield ()
    }
  }

}
