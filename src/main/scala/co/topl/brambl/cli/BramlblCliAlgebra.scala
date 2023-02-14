package co.topl.brambl.cli

import akka.actor.ActorSystem
import cats.data.NonEmptyChain
import cats.effect.IO
import co.topl.akkahttprpc.RpcClientFailure
import co.topl.akkahttprpc.implicits.client.rpcToClient
import co.topl.attestation.AddressCodec.implicits._
import co.topl.attestation.PublicKeyPropositionCurve25519
import co.topl.attestation.keyManagement.KeyRing
import co.topl.attestation.keyManagement.KeyfileCurve25519
import co.topl.attestation.keyManagement.PrivateKeyCurve25519
import co.topl.client.Brambl
import co.topl.client.Provider
import co.topl.modifier.transaction.builder.BoxSelectionAlgorithms
import co.topl.rpc.ToplRpc
import co.topl.rpc.implicits.client._
import co.topl.utils.IdiomaticScalaTransition.implicits.toValidatedOps
import co.topl.utils.Int128
import co.topl.utils.StringDataTypes
import io.circe.Json

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import scala.concurrent.ExecutionContext
import scala.io.Source

case class RpcClientFailureException(failure: RpcClientFailure)
    extends Throwable

object BramlblCli {
  trait BramblCliAlgebra[F[_]] {

    def createWallet(password: String, outputFile: String): F[Unit]

    def createUnsignedPolyTransfer(
          keyfile: String,
          password: String,
          fromAddresses: Seq[String],
          toAddresses: Seq[(String, Int)],
          changeAddress: String,
          fee: Int
    ): F[String]
  }
}

object BramblCliInterpreter {

  def makeIO(
      provider: Provider,
      keyRing: KeyRing[PrivateKeyCurve25519, KeyfileCurve25519]
  )(implicit executionContext: ExecutionContext, system: ActorSystem): BramlblCli.BramblCliAlgebra[IO] =
    new BramlblCli.BramblCliAlgebra[IO] {
      import provider._

      def importKeyM(
          jsonKey: Json,
          password: String,
          keyRing: KeyRing[PrivateKeyCurve25519, KeyfileCurve25519]
      ) =
        IO.fromEither(
          Brambl
            .importCurve25519JsonToKeyRing(jsonKey, password, keyRing)
            .left
            .map(RpcClientFailureException(_))
        )

      def readFileM(fileName: String) =
        IO.blocking(
          Source.fromFile(new File(fileName)).getLines().mkString("").mkString
        )

      def createWallet(password: String, outputFile: String): IO[Unit] = {
        import co.topl.attestation.keyManagement.Keyfile._
        import io.circe.syntax._
        for {
          _ <- IO.fromEither(keyRing.generateNewKeyPairs().toEither)
          keyfile <- IO.fromEither(
            Brambl
              .generateNewCurve25519Keyfile(password, keyRing)
              .left
              .map(x => new RuntimeException(x.toString))
          )
          jsonString <-IO(keyfile.asJson.noSpacesSortKeys)
            _ <- IO.blocking(Files.write(Paths.get(outputFile), jsonString.getBytes))
            _ <- IO.println("Wallet created successfully")
        } yield ()

      }

      def createParamsM(
          fromAddresses: Seq[String],
          toAddresses: Seq[(String, Int)],
          changeAddress: String,
          fee: Int
      ): IO[ToplRpc.Transaction.RawPolyTransfer.Params] =
        IO(
          ToplRpc.Transaction.RawPolyTransfer.Params(
            propositionType =
              PublicKeyPropositionCurve25519.typeString, // required fixed string for now, exciting possibilities in the future!
            sender = NonEmptyChain
              .fromSeq(
                fromAddresses
                  .map(StringDataTypes.Base58Data.unsafe)
                  .map(_.decodeAddress.getOrThrow())
              )
              .get, // Set of addresses whose state you want to use for the transaction
            recipients = NonEmptyChain
              .fromSeq(
                toAddresses.map(x =>
                  (
                    StringDataTypes.Base58Data
                      .unsafe(x._1)
                      .decodeAddress
                      .getOrThrow(),
                    Int128(x._2.intValue())
                  )
                )
              )
              .get, // Chain of (Recipients, Value) tuples that represent the output boxes
            fee = Int128(
              fee
            ), // fee to be paid to the network for the transaction (unit is nanoPoly)
            changeAddress = StringDataTypes.Base58Data
              .unsafe(changeAddress)
              .decodeAddress
              .getOrThrow(), // who will get ALL the change from the transaction?
            data =
              None, // upto 128 Latin-1 encoded characters of optional data,
            boxSelectionAlgorithm = BoxSelectionAlgorithms.All
          )
        )

      def createRawTxM(params: ToplRpc.Transaction.RawPolyTransfer.Params) =
        for {
          eitherTx <- IO.fromFuture(
            IO(
              ToplRpc.Transaction.RawPolyTransfer
                .rpc(params)
                .value
            )
          )
          rawTx <- IO.fromEither(
            eitherTx.left.map(x => RpcClientFailureException(x))
          )
        } yield rawTx.rawTx

      def createUnsignedPolyTransfer(
          keyfile: String,
          password: String,
          fromAddresses: Seq[String],
          toAddresses: Seq[(String, Int)],
          changeAddress: String,
          fee: Int
      ): IO[String] = for {
        params <- createParamsM(fromAddresses, toAddresses, changeAddress, fee)
        rawTransaction <- createRawTxM(params)
        encodedTx <- IO {
          import io.circe.syntax._
          rawTransaction.asJson.noSpaces
        }
      } yield encodedTx
    }
}
