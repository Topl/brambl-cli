package co.topl.brambl.cli.impl

import cats.data.EitherT
import cats.data.Validated
import cats.effect.kernel.Resource
import cats.effect.kernel.Sync
import co.topl.brambl.builders.TransactionBuilderApi
import co.topl.brambl.builders.locks.LockTemplate
import co.topl.brambl.cli.NetworkIdentifiers
import co.topl.brambl.codecs.AddressCodecs
import co.topl.brambl.constants.NetworkConstants
import co.topl.brambl.models.Datum
import co.topl.brambl.models.TransactionId
import co.topl.brambl.models.TransactionOutputAddress
import co.topl.brambl.models.box.Attestation
import co.topl.brambl.models.box.Value
import co.topl.brambl.models.transaction.IoTransaction
import co.topl.brambl.models.transaction.SpentTransactionOutput
import co.topl.brambl.models.transaction.UnspentTransactionOutput
import co.topl.brambl.utils.Encoding
import com.google.protobuf.ByteString
import quivr.models.Int128

import scala.io.BufferedSource
import scala.util.Try
import quivr.models.VerificationKey

case class Tx(
    network: String,
    keys: List[String],
    inputs: List[Stxo],
    outputs: List[Utxo]
)
case class Stxo(address: String, proposition: String, value: Long)
case class Utxo(address: String, value: Long)

sealed trait TxParserError {
  val description: String
}

case object InvalidNetwork extends TxParserError {
  val description = "Invalid network"
}
case object InvalidYaml extends TxParserError {
  val description = "Invalid yaml"
}
case class PropositionParseError(description: String) extends TxParserError
case class PropositionInstantationError(description: String)
    extends TxParserError
case class InvalidAddress(description: String) extends TxParserError
case class InvalidVerificationKey(description: String) extends TxParserError

trait TxParserAlgebra[F[_]] {

  def parseComplexTransaction(
      inputFile: String
  ): F[Either[TxParserError, IoTransaction]]

}

object TxParserAlgebra {
  def make[F[_]: Sync](
      transactionBuilderApi: TransactionBuilderApi[F],
      inputFileRes: Resource[F, BufferedSource]
  ) =
    new TxParserAlgebra[F] {

      import io.circe.generic.auto._
      import io.circe.yaml
      import cats.implicits._

      private def parseTransactionOuputAddress(
          networkId: Int,
          address: String
      ) = for {
        sp <- EitherT[F, TxParserError, Array[String]](
          Sync[F].delay(Right(address.split("#")))
        )
        idx <- EitherT[F, TxParserError, Int](
          Sync[F].delay(
            Try(sp(1).toInt).toEither.leftMap(_ =>
              InvalidAddress("Invalid index for address: " + address)
            )
          )
        )
        txIdByteArray <- EitherT[F, TxParserError, Array[Byte]](
          Sync[F].delay(
            Encoding
              .decodeFromBase58(sp(0))
              .leftMap(_ =>
                InvalidAddress("Invalid transaction id for: " + sp(0))
              )
          )
        )
        txId <- EitherT[F, TxParserError, TransactionId](
          Sync[F].delay(
            Right(
              TransactionId(
                ByteString.copyFrom(
                  txIdByteArray
                )
              )
            )
          )
        )
      } yield TransactionOutputAddress(
        networkId,
        NetworkConstants.MAIN_LEDGER_ID,
        idx,
        txId
      )

      private def parsePropositionTemaplate(
          proposition: String
      ): EitherT[F, TxParserError, LockTemplate[F]] = EitherT(for {
        lockTemplateStruct <- Sync[F].delay(
          QuivrFastParser.make[F].parseQuivr(proposition)
        )
        res <- lockTemplateStruct match {
          case Validated.Valid(lockTemplate) =>
            Sync[F].delay(Right(lockTemplate))
          case Validated.Invalid(e) =>
            e.toList
              .traverse(x =>
                Sync[F].delay(s"Error at ${x.location}: ${x.error}")
              )
              .map(x => Left(PropositionParseError(x.mkString("\n"))))
        }
      } yield res)

      private def parseUnspentTransactionOutput(
          lockAddressString: String,
          value: Long
      ): EitherT[F, TxParserError, UnspentTransactionOutput] =
        for {
          lockAddress <- EitherT(
            Sync[F].delay(
              AddressCodecs
                .decodeAddress(lockAddressString)
                .leftMap(_ =>
                  InvalidAddress(
                    "Invalid address for unspent transaction output: " + lockAddressString
                  ): TxParserError
                )
            )
          )
        } yield UnspentTransactionOutput(
          lockAddress,
          Value(
            Value.Value.Lvl(
              Value.LVL(
                Int128(ByteString.copyFrom(BigInt(value).toByteArray))
              )
            )
          )
        )

      private def parseSpentTransactionOutput(
          networkId: Int,
          address: String,
          encodedVks: List[String],
          proposition: String,
          value: Long
      ): EitherT[F, TxParserError, SpentTransactionOutput] = for {
        toa <- parseTransactionOuputAddress(networkId, address)
        lockTemplate <- parsePropositionTemaplate(proposition)
        aBytes <-
          encodedVks.traverse(vk =>
            EitherT[F, TxParserError, Array[Byte]](
              Sync[F].delay(
                Encoding
                  .decodeFromBase58(vk)
                  .leftMap(_ =>
                    InvalidVerificationKey(
                      "Error decoding verification key: " + vk
                    ): TxParserError
                  )
              )
            )
          )
        vks <- aBytes.traverse(b =>
          EitherT[F, TxParserError, VerificationKey](
            Sync[F].delay(Right(VerificationKey.parseFrom(b)))
          )
        )
        lock <- EitherT(
          lockTemplate
            .build(vks)
            .map(
              _.leftMap(_ =>
                PropositionInstantationError(
                  "Error instanciating proposition: " + proposition
                ): TxParserError
              )
            )
        )
        attestation <- EitherT[F, TxParserError, Attestation](
          transactionBuilderApi
            .unprovenAttestation(
              lock.value.predicate.get
            )
            .map(Right(_))
        )
      } yield SpentTransactionOutput(
        toa,
        attestation,
        Value(
          Value.Value.Lvl(
            Value.LVL(
              Int128(ByteString.copyFrom(BigInt(value).toByteArray))
            )
          )
        )
      )

      def txToIoTransaction(tx: Tx) =
        for {
          network <- EitherT(
            Sync[F].delay(
              NetworkIdentifiers
                .fromString(tx.network)
                .map(_.networkId)
                .toRight(InvalidNetwork)
            )
          )
          inputs <- tx.inputs
            .traverse(stxo =>
              parseSpentTransactionOutput(
                network,
                stxo.address,
                tx.keys,
                stxo.proposition,
                stxo.value
              )
            )
          outputs <- tx.outputs.traverse(utxo =>
            parseUnspentTransactionOutput(utxo.address, utxo.value)
          )
        } yield IoTransaction(
          None,
          inputs,
          outputs,
          Datum.IoTransaction.defaultInstance
        )

      def parseComplexTransaction(
          inputFile: String
      ): F[Either[TxParserError, IoTransaction]] = (for {
        inputString <- EitherT[F, TxParserError, String](
          inputFileRes.use(file =>
            Sync[F].blocking(Right(file.getLines().mkString("\n")))
          )
        )
        txOrFailure <- EitherT[F, TxParserError, Tx](
          Sync[F].delay(
            yaml.v12.parser
              .parse(inputString)
              .flatMap(tx => tx.as[Tx])
              .leftMap(_ => InvalidYaml)
          )
        )
        tx <- txToIoTransaction(txOrFailure)
      } yield tx).value
    }
}
