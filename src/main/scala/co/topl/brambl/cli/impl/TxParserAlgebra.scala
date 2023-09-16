package co.topl.brambl.cli.impl

import cats.data.EitherT
import cats.data.Validated
import cats.effect.kernel.Resource
import cats.effect.kernel.Sync
import co.topl.brambl.builders.TransactionBuilderApi
import co.topl.brambl.builders.locks.LockTemplate
import co.topl.brambl.cli.NetworkIdentifiers
import co.topl.brambl.models.Datum
import co.topl.brambl.models.TransactionOutputAddress
import co.topl.brambl.models.box.Attestation
import co.topl.brambl.models.box.Value
import co.topl.brambl.models.transaction.IoTransaction
import co.topl.brambl.models.transaction.SpentTransactionOutput
import co.topl.brambl.models.transaction.UnspentTransactionOutput
import co.topl.brambl.utils.Encoding
import com.google.protobuf.ByteString
import quivr.models.Int128
import quivr.models.VerificationKey

import scala.io.BufferedSource

case class Tx(
    network: String,
    keys: List[GlobalKeyEntry],
    inputs: List[Stxo],
    outputs: List[UtxoAddress]
)

case class GlobalKeyEntry(id: String, vk: String)
case class IdxMapping(index: Int, identifier: String)
case class Stxo(
    address: String,
    keyMap: List[IdxMapping],
    proposition: String,
    value: Long
)
case class UtxoAddress(address: String, value: Long)

trait TxParserAlgebra[F[_]] {

  def parseComplexTransaction(
      inputFileRes: Resource[F, BufferedSource]
  ): F[Either[CommonParserError, IoTransaction]]

}

object TxParserAlgebra {
  def make[F[_]: Sync](
      transactionBuilderApi: TransactionBuilderApi[F]
  ) =
    new TxParserAlgebra[F] {

      import io.circe.generic.auto._
      import io.circe.yaml
      import cats.implicits._

      private def parsePropositionTemaplate(
          proposition: String
      ): EitherT[F, CommonParserError, LockTemplate[F]] = EitherT(for {
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

      private def parseKeys(keys: List[GlobalKeyEntry]) =
        keys
          .traverse(key =>
            EitherT[F, CommonParserError, (String, VerificationKey)](
              Sync[F].delay(
                Encoding
                  .decodeFromBase58(key.vk)
                  .leftMap(_ =>
                    InvalidVerificationKey(
                      "Error decoding verification key: " + key.vk
                    ): CommonParserError
                  )
                  .map(vk => (key.id, VerificationKey.parseFrom(vk)))
              )
            )
          )
          .map(Map(_: _*))

      private def parseSpentTransactionOutput(
          networkId: Int,
          address: String,
          keyMap: List[IdxMapping],
          encodedVks: Map[String, VerificationKey],
          proposition: String,
          value: Long
      ): EitherT[F, CommonParserError, SpentTransactionOutput] = for {
        toa <- EitherT[F, CommonParserError, TransactionOutputAddress](
          Sync[F].delay(
            CommonParsingOps.parseTransactionOuputAddress(networkId, address)
          )
        )
        lockTemplate <- parsePropositionTemaplate(proposition)
        vks <- EitherT(
          Sync[F].delay(
            keyMap
              .traverse({ x =>
                encodedVks
                  .get(x.identifier)
                  .toRight(
                    InvalidVerificationKey(
                      "Verification key not found for identifier: " + x.identifier
                    ): CommonParserError
                  )
                  .map(vk => (vk, x.index))

              })
              .map(_.sortBy(_._2).map(_._1))
          )
        )
        lock <- EitherT(
          lockTemplate
            .build(vks)
            .map(
              _.leftMap(_ =>
                PropositionInstantationError(
                  "Error instanciating proposition: " + proposition
                ): CommonParserError
              )
            )
        )
        attestation <- EitherT[F, CommonParserError, Attestation](
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
                .toRight(InvalidNetwork: CommonParserError)
            )
          )
          keys <- parseKeys(tx.keys)
          inputs <- tx.inputs
            .traverse(stxo =>
              parseSpentTransactionOutput(
                network,
                stxo.address,
                stxo.keyMap,
                keys,
                stxo.proposition,
                stxo.value
              )
            )
          outputs <- tx.outputs.traverse(utxo =>
            EitherT[F, CommonParserError, UnspentTransactionOutput](
              Sync[F].delay(
                CommonParsingOps.parseUnspentTransactionOutput(
                  utxo.address,
                  utxo.value
                )
              )
            )
          )
          datum <- EitherT[F, CommonParserError, Datum.IoTransaction](
            transactionBuilderApi.datum().map(Right(_))
          )
        } yield IoTransaction(
          None,
          inputs,
          outputs,
          datum
        )

      def parseComplexTransaction(
          inputFileRes: Resource[F, BufferedSource]
      ): F[Either[CommonParserError, IoTransaction]] = (for {
        inputString <- EitherT[F, CommonParserError, String](
          inputFileRes.use(file =>
            Sync[F].blocking(Right(file.getLines().mkString("\n")))
          )
        )
        txOrFailure <- EitherT[F, CommonParserError, Tx](
          Sync[F].delay(
            yaml.v12.parser
              .parse(inputString)
              .flatMap(tx => tx.as[Tx])
              .leftMap { e =>
                InvalidYaml(e)
              }
          )
        )
        tx <- txToIoTransaction(txOrFailure)
      } yield tx).value
    }
}
