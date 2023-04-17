package co.topl.brambl.cli

import cats.data.EitherT
import cats.data.Validated
import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import co.topl.brambl.cli.validation.BramblCliParamsValidatorModule
import co.topl.brambl.models.Indices
import co.topl.brambl.wallet.WalletApi
import co.topl.crypto.encryption.VaultStore.Codecs._
import io.circe.syntax._
import scopt.OParser

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
                deriveChildKeyFromParams(validateParams)
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
