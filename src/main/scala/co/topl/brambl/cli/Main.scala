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

object Main extends IOApp {

  import BramblCliParamsValidatorModule._

  import BramblCliParamsParserModule._

  val dataApi = new DefaultDataApi[IO]()

  val walletApi = WalletApi.make(dataApi)

  override def run(args: List[String]): IO[ExitCode] = {
    OParser.parse(paramParser, args, BramblCliParams()) match {
      case Some(params) =>
        val op = validateParams(params) match {
          case Validated.Valid(validateParams) =>
            (for {
              wallet <- EitherT(
                walletApi.createNewWallet(
                  validateParams.password.getBytes(),
                  validateParams.somePassphrase
                )
              )
              _ <- validateParams.someOutputFile
                .map { outputFile =>
                  EitherT(
                    walletApi.saveWallet(
                      wallet.mainKeyVaultStore,
                      outputFile
                    )
                  )
                }
                .getOrElse {
                  EitherT.liftF(IO.println(new String(wallet.mainKeyVaultStore.asJson.noSpaces)))
                }
            } yield ()).value
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
