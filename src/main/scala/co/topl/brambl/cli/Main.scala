package co.topl.brambl.cli

import akka.actor.ActorSystem
import cats.data.Validated
import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import co.topl.attestation.keyManagement.KeyRing
import co.topl.attestation.keyManagement.KeyfileCurve25519
import co.topl.attestation.keyManagement.KeyfileCurve25519Companion
import co.topl.attestation.keyManagement.PrivateKeyCurve25519
import scopt.OParser
import com.typesafe.config.ConfigFactory

object Main
    extends IOApp
    with BramblCliParamsParserModule
    with BramblCliParamsValidatorModule {

  override def run(args: List[String]): IO[ExitCode] = {
    val conf = ConfigFactory.load()
    implicit val actorSystem = ActorSystem("CliActorSystem", conf)
    OParser.parse(paramParser, args, BramblCliParams()) match {
      case Some(params) =>
        val op = validateParams(params) match {
          case Validated.Valid(validatedParams) =>
            implicit val ec = actorSystem.dispatcher
            val keyRing: KeyRing[PrivateKeyCurve25519, KeyfileCurve25519] =
              KeyRing.empty[PrivateKeyCurve25519, KeyfileCurve25519]()(
                validatedParams.provider.networkPrefix,
                PrivateKeyCurve25519.secretGenerator,
                KeyfileCurve25519Companion
              )
            val interpreter =
              BramblCliInterpreter.makeIO(validatedParams.provider, keyRing)
            (validatedParams.mode, validatedParams.subcmd) match {
              case (BramblCliMode.wallet, BramblCliSubCmd.sign) =>
                // after validation, we know that someTokenType is defined
                validatedParams.someTokenType.get match {
                  case TokenType.poly =>
                    interpreter.signTransferPoly(
                      validatedParams.someKeyfile.get,
                      validatedParams.password,
                      validatedParams.someOutputFile,
                      validatedParams.someInputFile
                    )
                }
              case (BramblCliMode.transaction, BramblCliSubCmd.broadcast) =>
                interpreter.broadcastPolyTransfer(validatedParams.someInputFile)
              case (BramblCliMode.wallet, BramblCliSubCmd.balance) =>
                interpreter.balancePolys(
                  validatedParams.fromAddresses,
                  validatedParams.someOutputFile
                )
              case (BramblCliMode.wallet, BramblCliSubCmd.create) =>
                interpreter.createWallet(
                  validatedParams.password,
                  validatedParams.someOutputFile
                )
              case (BramblCliMode.transaction, BramblCliSubCmd.create) =>
                interpreter.createUnsignedPolyTransfer(
                  validatedParams.someOutputFile,
                  validatedParams.fromAddresses,
                  validatedParams.toAddresses,
                  validatedParams.changeAddress,
                  validatedParams.fee
                )
              case _ =>
                IO.println("Unknown mode")
            }
          case Validated.Invalid(errors) =>
            IO.println("Invalid params") *> IO.println(
              errors.toList.mkString(", ")
            ) *> IO.print(OParser.usage(paramParser))
        }
        for {
          _ <- op
          _ <- IO.fromFuture(IO(actorSystem.terminate()))
        } yield ExitCode.Success
      case _ =>
        IO.pure(ExitCode.Error)
    }
  }

}
