package co.topl.brambl.cli.impl

import co.topl.brambl.cli.BramblCliValidatedParams
import cats.effect.IO
import co.topl.brambl.dataApi.DataApi
import co.topl.brambl.wallet.WalletApi
import co.topl.brambl.models.Indices
import cats.effect.kernel.Resource
import java.io.FileOutputStream
import cats.data.EitherT

trait WalletOps {

  val dataApi: DataApi[IO]

  val walletApi: WalletApi[IO]

  val walletStateApi: WalletStateApi[IO]

  def liftF[A, B <: Throwable](io: IO[Either[B, A]]) = EitherT(io)

  def createWalletFromParams(params: BramblCliValidatedParams) = {
    import io.circe.syntax._
    import co.topl.crypto.encryption.VaultStore.Codecs._
    for {
      walletEither <-
        walletApi.createNewWallet(
          params.password.getBytes(),
          params.somePassphrase
        )
      wallet <- IO.fromEither(walletEither)
      _ <- params.someOutputFile
        .map { outputFile =>
          for {
            saveEither <- walletApi.saveWallet(
              wallet.mainKeyVaultStore,
              outputFile
            )
            _ <- IO.fromEither(saveEither)
          } yield ()
        }
        .getOrElse {
          IO.println(new String(wallet.mainKeyVaultStore.asJson.noSpaces))
        }
      _ <- walletStateApi.initWalletState()
    } yield ()
  }

  def readInputFile(someInputFile: Option[String]) = {
    someInputFile match {
      case Some(inputFile) =>
        dataApi.getMainKeyVaultStore(inputFile).map(_.left.map(_.getCause()))
      case None =>
        IO.raiseError(new Throwable("No input file (should not happen)"))
    }
  }

  def loadKeysFromParam(params: BramblCliValidatedParams) = {
    for {
      walletEither <- readInputFile(params.someInputFile)
      wallet <- IO.fromEither(walletEither)
      keyPair <- walletApi
        .extractMainKey(wallet, params.password.getBytes())
        .map(
          _.left.map(_ => new Throwable("No input file (should not happen)"))
        )
    } yield keyPair
  }

  def deriveChildKeyFromParams(
      params: BramblCliValidatedParams
  ) = {
    for {
      kpEither <- loadKeysFromParam(params)
      kp <- IO.fromEither(kpEither)
      derivedKeyPair <-
        walletApi.deriveChildKeys(
          kp,
          new Indices(
            params.coordinates(0).toInt,
            params.coordinates(1).toInt,
            params.coordinates(2).toInt
          )
        )
      _ <- Resource
        .make(IO(new FileOutputStream(params.someOutputFile.get)))(fos =>
          IO(fos.close())
        )
        .use(fos => IO(derivedKeyPair.writeTo(fos)))
    } yield derivedKeyPair
  }

}
