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

object Main
    extends IOApp
    with BramblCliParamsParserModule
    with BramblCliParamsValidatorModule {

  override def run(args: List[String]): IO[ExitCode] = {
    OParser.parse(paramParser, args, BramblCliParams()) match {
      case Some(params) =>
        val op = validateParams(params) match {
          case Validated.Valid(validatedParams) =>
            (validatedParams.mode, validatedParams.subcmd) match {
              case (BramblCliMode.wallet, BramblCliSubCmd.create) =>
                implicit val actorSystem = ActorSystem()
                implicit val ec = actorSystem.dispatcher
                val keyRing: KeyRing[PrivateKeyCurve25519, KeyfileCurve25519] =
                  KeyRing.empty[PrivateKeyCurve25519, KeyfileCurve25519]()(
                    validatedParams.provider.networkPrefix,
                    PrivateKeyCurve25519.secretGenerator,
                    KeyfileCurve25519Companion
                  )

                val interpreter =
                  BramblCliInterpreter.makeIO(validatedParams.provider, keyRing)
                interpreter.createWallet(
                  validatedParams.password,
                  validatedParams.outputFile
                )
              case (BramblCliMode.transaction, BramblCliSubCmd.create) =>
                IO.println("Creating a new transaction")
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
        } yield ExitCode.Success
      case _ =>
        IO.pure(ExitCode.Error)
    }
  }

}
