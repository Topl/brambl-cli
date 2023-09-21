package co.topl.brambl.cli.impl

import cats.data.Validated
import cats.effect.kernel.Resource
import cats.effect.kernel.Sync
import co.topl.brambl.builders.TransactionBuilderApi
import co.topl.brambl.builders.locks.LockTemplate
import co.topl.brambl.cli.NetworkIdentifiers
import co.topl.brambl.models.box.Value
import co.topl.brambl.models.transaction.IoTransaction
import co.topl.brambl.models.transaction.SpentTransactionOutput
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
      ): F[LockTemplate[F]] = for {
        lockTemplateStruct <- Sync[F].delay(
          QuivrFastParser.make[F].parseQuivr(proposition)
        )
        res <- lockTemplateStruct match {
          case Validated.Valid(lockTemplate) =>
            Sync[F].delay(lockTemplate)
          case Validated.Invalid(e) =>
            e.toList
              .traverse(x =>
                Sync[F].delay(s"Error at ${x.location}: ${x.error}")
              )
              .map(x => PropositionParseError(x.mkString("\n")))
              .flatMap(e => Sync[F].raiseError(e): F[LockTemplate[F]])
        }
      } yield res

      private def parseKeys(keys: List[GlobalKeyEntry]) =
        keys
          .traverse(key =>
            Sync[F]
              .fromEither(
                Encoding
                  .decodeFromBase58(key.vk)
              )
              .map(vk => (key.id, VerificationKey.parseFrom(vk)))
              .adaptErr(_ =>
                InvalidVerificationKey(
                  "Error decoding verification key: " + key.vk
                ): CommonParserError
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
      ): F[SpentTransactionOutput] = for {
        toa <- Sync[F].fromEither(
          CommonParsingOps.parseTransactionOuputAddress(networkId, address)
        )
        lockTemplate <- parsePropositionTemaplate(proposition)
        vks <- Sync[F].fromEither(
          keyMap
            .traverse({ x =>
              encodedVks
                .get(x.identifier)
                .map(vk => (vk, x.index))
                .toRight(
                  InvalidVerificationKey(
                    "Verification key not found for identifier: " + x.identifier
                  ): CommonParserError
                )
            })
            .map(_.sortBy(_._2).map(_._1))
        )
        eitherLock <- lockTemplate
          .build(vks)
          .map(
            _.leftMap(_ =>
              PropositionInstantationError(
                "Error instanciating proposition: " + proposition
              ): CommonParserError
            )
          )
        lock <- Sync[F].fromEither(eitherLock)
        attestation <- transactionBuilderApi
          .unprovenAttestation(
            lock.value.predicate.get
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

      def txToIoTransaction(tx: Tx): F[IoTransaction] =
        for {
          network <- Sync[F].delay(
            NetworkIdentifiers
              .fromString(tx.network)
              .map(_.networkId)
              .getOrElse(
                throw InvalidNetwork
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
            Sync[F].fromEither(
              CommonParsingOps.parseUnspentTransactionOutput(
                utxo.address,
                utxo.value
              )
            )
          )
          datum <- transactionBuilderApi.datum()
        } yield IoTransaction(
          None,
          inputs,
          outputs,
          datum
        )

      def parseComplexTransaction(
          inputFileRes: Resource[F, BufferedSource]
      ): F[Either[CommonParserError, IoTransaction]] = {
        import cats.implicits._
        (for {
          inputString <- inputFileRes.use(file =>
            Sync[F].blocking(file.getLines().mkString("\n"))
          )
          txOrFailure <- Sync[F]
            .fromEither(
              yaml.v12.parser
                .parse(inputString)
                .flatMap(tx => tx.as[Tx])
                .leftMap(e => InvalidYaml(e))
            )
          tx <- txToIoTransaction(txOrFailure)
        } yield tx).attempt.map(_ match {
          case Right(tx)                      => Right(tx)
          case Left(value: CommonParserError) => Left(value)
          case Left(e)                        => Left(UnknownError(e))
        })
      }
    }
}
