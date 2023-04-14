package co.topl.brambl.cli

import cats.data.Validated
import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import co.topl.brambl.cli.validation.BramblCliParamsValidatorModule
import co.topl.brambl.wallet.WalletApi
import scopt.OParser
import cats.data.EitherT
import io.circe.syntax._
import co.topl.crypto.encryption.VaultStore.Codecs._
import co.topl.crypto.encryption.VaultStore
import cats.Monad
import co.topl.crypto.signing.{KeyPair => CryptoKeyPair}
import co.topl.crypto.signing.ExtendedEd25519
import quivr.models.KeyPair
import scala.util.Try
import co.topl.crypto.signing.Ed25519
import co.topl.crypto.generation.Bip32Indexes
import quivr.models.SigningKey
import com.google.protobuf.ByteString
import quivr.models.VerificationKey
import co.topl.crypto.encryption.kdf.Kdf
import co.topl.crypto.encryption.cipher.Cipher
import co.topl.crypto.encryption.kdf.SCrypt
import co.topl.crypto.encryption.cipher.Aes
import co.topl.crypto.encryption.Mac

object Main extends IOApp {

  import BramblCliParamsValidatorModule._

  import BramblCliParamsParserModule._

  val dataApi = new DefaultDataApi[IO]()

  val walletApi = WalletApi.make(dataApi)

  def createWalletFromParams(params: BramblCliValidatedParams) = {
    (for {
      wallet <- EitherT(
        walletApi.createNewWallet(
          params.password.getBytes(),
          params.somePassphrase
        )
      )
      _ <- params.someOutputFile
        .map { outputFile =>
          EitherT(
            walletApi.saveWallet(
              wallet.mainKeyVaultStore,
              outputFile
            )
          )
        }
        .getOrElse {
          EitherT.liftF(
            IO.println(new String(wallet.mainKeyVaultStore.asJson.noSpaces))
          )
        }
    } yield ()).value
  }

  def decodeWallet[F[_]: Monad](vaultStore: VaultStore[F], password: String) = {
    for {
      decoded <- EitherT(
        VaultStore.decodeCipher[F](vaultStore, password.getBytes())
      ).leftMap(x => new Throwable("Error" + x))
    } yield decoded
  }

  def pbKeyPairToCryotoKeyPair(
      pbKeyPair: KeyPair
  ): CryptoKeyPair[ExtendedEd25519.SecretKey, ExtendedEd25519.PublicKey] = {
    CryptoKeyPair(
      ExtendedEd25519.SecretKey(
        pbKeyPair.sk.sk.extendedEd25519.get.leftKey.toByteArray(),
        pbKeyPair.sk.sk.extendedEd25519.get.rightKey.toByteArray(),
        pbKeyPair.sk.sk.extendedEd25519.get.chainCode.toByteArray()
      ),
      ExtendedEd25519.PublicKey(
        Ed25519.PublicKey(
          pbKeyPair.vk.vk.extendedEd25519.get.vk.value.toByteArray()
        ),
        pbKeyPair.vk.vk.extendedEd25519.get.chainCode.toByteArray()
      )
    )
  }

  def loadKeysFromParam(params: BramblCliValidatedParams) = {
    (for {
      wallet <- EitherT(
        params.someInputFile
          .map(inputFile => dataApi.getMainKeyVaultStore(inputFile))
          .getOrElse(
            IO(Left(new Throwable("No input file (should not happen)")))
          )
      )
      decoded <- decodeWallet(wallet, params.password)
      pbKeyPair <- EitherT(IO(Try(KeyPair.parseFrom(decoded)).toEither))
      cryptoKeyPair <- EitherT(
        IO(Try(pbKeyPairToCryotoKeyPair(pbKeyPair)).toEither)
      )
    } yield cryptoKeyPair).value
  }

  def deriveChildKeyFromKeyPair[F[_]: Monad](
      kp: CryptoKeyPair[ExtendedEd25519.SecretKey, ExtendedEd25519.PublicKey],
      x: Long,
      y: Long,
      z: Long
  )(implicit instance: ExtendedEd25519) = {
    import cats.implicits._
    for {
      xCoordinate <- Monad[F].pure(Bip32Indexes.HardenedIndex(x))
      yCoordinate <- Monad[F].pure(Bip32Indexes.SoftIndex(y))
      zCoordinate <- Monad[F].pure(Bip32Indexes.SoftIndex(z))
    } yield instance.deriveKeyPairFromChildPath(
      kp.signingKey,
      List(xCoordinate, yCoordinate, zCoordinate)
    )
  }

  def cryptoToPbKeyPair(
      keyPair: CryptoKeyPair[
        ExtendedEd25519.SecretKey,
        ExtendedEd25519.PublicKey
      ]
  ): KeyPair = {
    val skCrypto = keyPair.signingKey
    val sk = SigningKey.ExtendedEd25519Sk(
      ByteString.copyFrom(skCrypto.leftKey),
      ByteString.copyFrom(skCrypto.rightKey),
      ByteString.copyFrom(skCrypto.chainCode)
    )
    val vkCrypto = keyPair.verificationKey
    val vk = VerificationKey.ExtendedEd25519Vk(
      VerificationKey.Ed25519Vk(ByteString.copyFrom(vkCrypto.vk.bytes)),
      ByteString.copyFrom(vkCrypto.chainCode)
    )
    KeyPair(
      VerificationKey(VerificationKey.Vk.ExtendedEd25519(vk)),
      SigningKey(SigningKey.Sk.ExtendedEd25519(sk))
    )
  }

  val kdf: Kdf[IO] = SCrypt.make[IO](SCrypt.SCryptParams(SCrypt.generateSalt))
  val cipher: Cipher[IO] = Aes.make[IO](Aes.AesParams(Aes.generateIv))

  def buildMainKeyVaultStore(
      mainKey: Array[Byte],
      password: Array[Byte]
  ): IO[VaultStore[IO]] = for {
    derivedKey <- kdf.deriveKey(password)
    cipherText <- cipher.encrypt(mainKey, derivedKey)
    mac = Mac.make(derivedKey, cipherText).value
  } yield VaultStore[IO](kdf, cipher, cipherText, mac)

  def deriveChildKeyFromParams(
      params: BramblCliValidatedParams
  )(implicit instance: ExtendedEd25519) = {
    for {
      kpEither <- loadKeysFromParam(params)
      kp <- IO.fromEither(kpEither)
      derivedKeyPair <-
        deriveChildKeyFromKeyPair[IO](
          kp,
          params.coordinates(0).toLong,
          params.coordinates(1).toLong,
          params.coordinates(2).toLong
        )
      _ <- IO.fromEither(ExtendedEd25519.validate(derivedKeyPair.signingKey).left.map(x => new Throwable(x.toString())))
      pbKeyPair <- IO(cryptoToPbKeyPair(derivedKeyPair).toByteArray)
      vaultStore <- buildMainKeyVaultStore(
        pbKeyPair,
        params.password.getBytes()
      )
      _ <- params.someOutputFile
        .map(f => walletApi.saveWallet(vaultStore, f))
        .getOrElse(IO.println(vaultStore.asJson.noSpaces))
    } yield derivedKeyPair
  }
  override def run(args: List[String]): IO[ExitCode] = {
    OParser.parse(paramParser, args, BramblCliParams()) match {
      case Some(params) =>
        val op = validateParams(params) match {
          case Validated.Valid(validateParams) =>
            (validateParams.mode, validateParams.subcmd) match {
              case (BramblCliMode.key, BramblCliSubCmd.generate) =>
                createWalletFromParams(validateParams).map(_ => ())
              case (BramblCliMode.key, BramblCliSubCmd.derive) =>
                deriveChildKeyFromParams(validateParams)(new ExtendedEd25519).map(_ => ())
            }
          case Validated.Invalid(errors) =>
            IO.println("Invalid params") *> IO.println(
              errors.toList.mkString(", ")
            ) *> IO.print(OParser.usage(paramParser))
        }
        for {
          _ <- op
        } yield ExitCode.Success
      case _ =>
        IO.pure(ExitCode.Error)
    }
  }

}
