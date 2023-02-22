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
import io.circe.parser.parse

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import scala.concurrent.ExecutionContext
import scala.io.Source
import co.topl.modifier.transaction.PolyTransfer
import co.topl.attestation.Address
import co.topl.attestation.Proposition

case class RpcClientFailureException(failure: RpcClientFailure)
    extends Throwable

object BramlblCli {
  trait BramblCliAlgebra[F[_]] {

    def createWallet(password: String, someOutputFile: Option[String]): F[Unit]

    def createUnsignedPolyTransfer(
        someOutputFile: Option[String],
        fromAddresses: Seq[String],
        toAddresses: Seq[(String, Int)],
        changeAddress: String,
        fee: Int
    ): F[Unit]

    def signTransferPoly(
        keyFile: String,
        password: String,
        someOutputFile: Option[String],
        someInputFile: Option[String]
    ): F[Unit]

    def broadcastPolyTransfer(
        someInputFile: Option[String]
    ): F[Unit]

    def balancePolys(
        addresses: Seq[String],
        someOutputFile: Option[String]
    ): F[Unit]

  }
}

object BramblCliInterpreter {

  def makeIO(
      provider: Provider,
      keyRing: KeyRing[PrivateKeyCurve25519, KeyfileCurve25519]
  )(implicit
      executionContext: ExecutionContext,
      system: ActorSystem
  ): BramlblCli.BramblCliAlgebra[IO] =
    new BramlblCli.BramblCliAlgebra[IO] {
      import provider._

      def decodeAddressM(address: String) = IO(
        StringDataTypes.Base58Data.unsafe(address).decodeAddress.getOrThrow()
      )

      def getBalanceM(param: ToplRpc.NodeView.Balances.Params) = for {
        eitherBalances <- IO.fromFuture(
          IO(
            ToplRpc.NodeView.Balances
              .rpc(param)
              .leftMap(f => RpcClientFailureException(f))
              .value
          )
        )
        balance <- IO.fromEither(eitherBalances)
      } yield balance

      def getParamsM(address: Seq[Address]) = IO(
        ToplRpc.NodeView.Balances
          .Params(
            address.toList
          )
      )

      def balancePolys(addresses: Seq[String], someOutputFile: Option[String]): IO[Unit] = {
        import cats.implicits._
        for {
          decodedAddresses <- addresses.map(decodeAddressM(_)).sequence
          params <- getParamsM(decodedAddresses)
          balance <- getBalanceM(params)
          polyAmount = decodedAddresses.map{address => balance
          .get(address)
          .map(
            _.Boxes.PolyBox
              .map(_.value.quantity)
              .fold(Int128(0))(_ + _)
          )
          .getOrElse(Int128(0))}
          _ <- IO.println("Available polys: " + polyAmount.fold(Int128(0))(_ + _))
        } yield ()
      }
      def broadcastTransactionM(
          signedTx: PolyTransfer[_ <: Proposition]
      ) = for {
        eitherBroadcast <- IO.fromFuture(
          IO(
            ToplRpc.Transaction.BroadcastTx
              .rpc(ToplRpc.Transaction.BroadcastTx.Params(signedTx))
              .leftMap(failure =>
                RpcClientFailureException(
                  failure
                )
              )
              .value
          )
        )
        broadcast <- IO.fromEither(eitherBroadcast)
      } yield broadcast

      def broadcastPolyTransfer(someInputFile: Option[String]): IO[Unit] = for {
        signedTx <- someInputFile match {
          case Some(inputFile) =>
            readFileM(inputFile)
          case None =>
            readFromStdin
        }
        transactionAsBytes <- decodeTransactionM(signedTx)
        signedTx <- parseTxPolyM(transactionAsBytes)
        success <- broadcastTransactionM(signedTx)
      } yield {
        import io.circe.syntax._
        success.asJson.noSpaces
      }

      def readFromStdin() =
        IO.blocking(
          Source.stdin.getLines().mkString("").mkString
        )

      def parseTxPolyM(msg2Sign: Array[Byte]) = IO.fromEither {
        import io.circe.parser._
        import co.topl.modifier.transaction.PolyTransfer.jsonDecoder
        parse(new String(msg2Sign)).flatMap(jsonDecoder.decodeJson)
      }

      def decodeTransactionM(tx: String) = IO(tx.getBytes())

      def signTxM(rawTx: PolyTransfer[_ <: Proposition]) = IO {
        val signFunc = (addr: Address) =>
          keyRing.generateAttestation(addr)(rawTx.messageToSign)
        val signatures = keyRing.addresses.map(signFunc).reduce(_ ++ _)
        rawTx.copy(attestation = signatures)
      }

      def encodeTransferM(
          assetTransfer: PolyTransfer[PublicKeyPropositionCurve25519]
      ): IO[String] = for {
        transferRequest <- IO {
          import io.circe.syntax._
          assetTransfer.asJson.noSpaces
        }
      } yield transferRequest

      def signTransferPoly(
          keyFile: String,
          password: String,
          someOutputFile: Option[String],
          someInputFile: Option[String]
      ): IO[Unit] = for {
        keyfile <- readFileM(keyFile)
        jsonKey <- IO.fromEither(parse(keyfile))
        _ <- importKeyM(jsonKey, password, keyRing)
        unsignedTx <- someInputFile match {
          case Some(inputFile) =>
            readFileM(inputFile)
          case None =>
            readFromStdin
        }
        msg2Sign <- decodeTransactionM(unsignedTx)
        rawTx <- parseTxPolyM(msg2Sign)
        signedTx <- signTxM(rawTx)
        signedTxString <- encodeTransferM(signedTx)
        _ <- someOutputFile match {
          case Some(outputFile) =>
            IO.blocking(
              Files.write(
                Paths.get(outputFile),
                signedTxString.getBytes
              )
            )
          case None =>
            IO.println(signedTxString)
        }
      } yield ()

      def importKeyM(
          jsonKey: Json,
          password: String,
          keyRing: KeyRing[PrivateKeyCurve25519, KeyfileCurve25519]
      ) =
        IO.fromEither(
          Brambl
            .importCurve25519JsonToKeyRing(jsonKey, password, keyRing)
            .left
            .map(x => { println(x); RpcClientFailureException(x) })
        )

      def readFileM(fileName: String) =
        IO.blocking(
          Source.fromFile(new File(fileName)).getLines().mkString("").mkString
        )

      def createWallet(
          password: String,
          someOutputFile: Option[String]
      ): IO[Unit] = {
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
          jsonString <- IO(keyfile.asJson.noSpacesSortKeys)
          _ <- someOutputFile
            .map(outputFile =>
              IO.blocking(
                Files.write(Paths.get(outputFile), jsonString.getBytes)
              )
            )
            .getOrElse(IO.println(jsonString))
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
            eitherTx.left.map(x => { println(x); RpcClientFailureException(x) })
          )
        } yield rawTx.rawTx

      def createUnsignedPolyTransfer(
          someOutputFile: Option[String],
          fromAddresses: Seq[String],
          toAddresses: Seq[(String, Int)],
          changeAddress: String,
          fee: Int
      ): IO[Unit] = for {
        params <- createParamsM(fromAddresses, toAddresses, changeAddress, fee)
        rawTransaction <- createRawTxM(params)
        encodedTx <- IO {
          import io.circe.syntax._
          rawTransaction.asJson.noSpaces
        }
        _ <- someOutputFile
          .map(outputFile =>
            IO.blocking(
              Files.write(Paths.get(outputFile), encodedTx.getBytes)
            )
          )
          .getOrElse(IO.println(encodedTx))
      } yield ()
    }
}
