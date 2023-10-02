package co.topl.brambl.cli.impl

import cats.effect.kernel.Resource
import cats.effect.kernel.Sync
import co.topl.brambl.builders.TransactionBuilderApi
import co.topl.brambl.dataApi.WalletStateAlgebra
import co.topl.brambl.models.Indices
import co.topl.brambl.models.LockAddress
import co.topl.brambl.models.TransactionOutputAddress
import co.topl.brambl.models.box
import co.topl.brambl.models.box.AssetMintingStatement
import co.topl.brambl.models.box.Lock
import co.topl.brambl.models.box.Value
import co.topl.brambl.models.transaction.IoTransaction
import co.topl.brambl.models.transaction.SpentTransactionOutput
import co.topl.brambl.syntax._
import co.topl.brambl.utils.Encoding
import co.topl.brambl.wallet.WalletApi
import co.topl.genus.services.Txo
import com.google.protobuf.ByteString
import com.google.protobuf.struct.Struct
import quivr.models.KeyPair

import java.io.FileOutputStream

import TransactionBuilderApi.implicits._
import io.circe.Json

trait AssetMintingOps[G[_]] extends CommonTxOps {

  import cats.implicits._

  implicit val sync: Sync[G]

  val tba: TransactionBuilderApi[G]

  val wsa: WalletStateAlgebra[G]

  val wa: WalletApi[G]

  def buildAssetTxAux(
      keyPair: KeyPair,
      outputFile: String,
      lvlTxos: Seq[Txo],
      nonLvlTxos: Seq[Txo],
      lockPredicateFrom: Lock.Predicate,
      amount: Long,
      fee: Long,
      group: Value.Group,
      groupUtxoAddress: TransactionOutputAddress,
      someNextIndices: Option[Indices],
      series: Value.Series,
      seriesUtxoAddress: TransactionOutputAddress,
      permanentMetadata: Option[Struct],
      ephemeralMetadata: Option[Json],
      commitment: Option[ByteString],
      changeLock: Option[Lock]
  ) = (if (lvlTxos.isEmpty) {
         Sync[G].raiseError(CreateTxError("No LVL txos found"))
       } else {
         changeLock match {
           case Some(lockPredicateForChange) =>
             tba
               .lockAddress(lockPredicateForChange)
               .flatMap { changeAddress =>
                 buildAssetTransaction(
                   keyPair,
                   outputFile,
                   lvlTxos,
                   nonLvlTxos,
                   lockPredicateFrom,
                   changeLock.get,
                   changeAddress,
                   amount,
                   fee,
                   group,
                   groupUtxoAddress,
                   someNextIndices,
                   series,
                   seriesUtxoAddress,
                   permanentMetadata,
                   ephemeralMetadata.map(toStruct(_).getStructValue),
                   commitment
                 )
               }
           case None =>
             Sync[G].raiseError(
               CreateTxError("Unable to generate change lock")
             )
         }
       })

  private def buildAssetTransaction(
      keyPair: KeyPair,
      outputFile: String,
      lvlTxos: Seq[Txo],
      nonLvlTxos: Seq[Txo],
      lockPredicateFrom: Lock.Predicate,
      lockForChange: Lock,
      recipientLockAddress: LockAddress,
      amount: Long,
      fee: Long,
      group: Value.Group,
      groupUtxoAddress: TransactionOutputAddress,
      someNextIndices: Option[Indices],
      series: Value.Series,
      seriesUtxoAddress: TransactionOutputAddress,
      permanentMetadata: Option[Struct],
      ephemeralMetadata: Option[Struct],
      commitment: Option[ByteString]
  ): G[Unit] =
    for {
      ioTransaction <-
        buildSimpleAssetMintingTransaction(
          lvlTxos,
          nonLvlTxos,
          lockPredicateFrom,
          recipientLockAddress,
          amount,
          fee,
          group,
          groupUtxoAddress,
          series,
          seriesUtxoAddress,
          permanentMetadata,
          ephemeralMetadata,
          commitment
        )
      // Only save to wallet state if there is a change output in the transaction
      _ <-
        if (ioTransaction.outputs.length >= 2) for {
          lockAddress <- tba.lockAddress(
            lockForChange
          )
          vk <- someNextIndices
            .map(nextIndices =>
              wa
                .deriveChildKeys(keyPair, nextIndices)
                .map(_.vk)
            )
            .sequence
          _ <- wsa.updateWalletState(
            Encoding.encodeToBase58Check(
              lockForChange.getPredicate.toByteArray
            ),
            lockAddress.toBase58(),
            vk.map(_ => "ExtendedEd25519"),
            vk.map(x => Encoding.encodeToBase58(x.toByteArray)),
            someNextIndices.get
          )
        } yield ()
        else {
          Sync[G].delay(())
        }
      _ <-
        Resource
          .make(
            Sync[G]
              .delay(
                new FileOutputStream(outputFile)
              )
          )(fos => Sync[G].delay(fos.close()))
          .use { fos =>
            Sync[G]
              .delay(ioTransaction.writeTo(fos))
              .onError(_ =>
                Sync[G].raiseError(
                  CannotSerializeProtobufFile(
                    "Cannot write to file"
                  )
                )
              )
          }
    } yield ()

  private def buildSimpleAssetMintingTransaction(
      lvlTxos: Seq[Txo],
      nonLvlTxos: Seq[Txo],
      lockPredicateFrom: Lock.Predicate,
      recipientLockAddress: LockAddress,
      amount: Long,
      fee: Long,
      group: Value.Group,
      groupUtxoAddress: TransactionOutputAddress,
      series: Value.Series,
      seriesUtxoAddress: TransactionOutputAddress,
      permanentMetadata: Option[Struct],
      ephemeralMetadata: Option[Struct],
      commitment: Option[ByteString]
  ): G[IoTransaction] =
    for {
      unprovenAttestationToProve <- tba.unprovenAttestation(
        lockPredicateFrom
      )
      totalValues = computeLvlQuantity(lvlTxos)
      datum <- tba.datum()
      lvlOutputForChange <- tba.lvlOutput(
        recipientLockAddress,
        (totalValues.toLong - fee)
      )
      gOutput <- groupOutput[G](
        recipientLockAddress,
        group.quantity,
        group.groupId,
        group.fixedSeries
      )
      sOutput <- seriesOutput[G](
        recipientLockAddress,
        series.quantity,
        series.seriesId,
        series.tokenSupply,
        series.quantityDescriptor,
        series.fungibility
      )
      aOutput <- assetOutput[G](
        recipientLockAddress,
        amount,
        group.groupId,
        series.seriesId,
        series.quantityDescriptor,
        series.fungibility,
        ephemeralMetadata,
        commitment
      )
      ioTransaction = IoTransaction.defaultInstance
        .withInputs(
          lvlTxos.map(x =>
            SpentTransactionOutput(
              x.outputAddress,
              unprovenAttestationToProve,
              x.transactionOutput.value
            )
          ) ++ Seq(
            SpentTransactionOutput(
              groupUtxoAddress,
              unprovenAttestationToProve,
              Value(
                group
              )
            )
          ) ++
            series.tokenSupply
              .map(supply =>
                Seq(
                  SpentTransactionOutput(
                    seriesUtxoAddress,
                    unprovenAttestationToProve,
                    Value(
                      Value.Value.Series(
                        box.Value.Series(
                          seriesId = series.seriesId,
                          quantity =
                            series.quantity - (amount / supply), // burn
                          tokenSupply = series.tokenSupply,
                          quantityDescriptor = series.quantityDescriptor,
                          fungibility = series.fungibility
                        )
                      )
                    )
                  )
                )
              )
              .getOrElse(
                Seq(
                  SpentTransactionOutput(
                    seriesUtxoAddress,
                    unprovenAttestationToProve,
                    Value(
                      Value.Value.Series(
                        box.Value.Series(
                          seriesId = series.seriesId,
                          quantity = series.quantity, // no burn
                          tokenSupply = series.tokenSupply,
                          quantityDescriptor = series.quantityDescriptor,
                          fungibility = series.fungibility
                        )
                      )
                    )
                  )
                )
              )
        )
        .withOutputs(
          // If there is no change, we don't need to add it to the outputs
          (if (totalValues.toLong - fee > 0)
             Seq(lvlOutputForChange, gOutput, sOutput, aOutput)
           else
             Seq(gOutput, sOutput, aOutput)) ++ nonLvlTxos
            .map(
              _.transactionOutput.copy(address = recipientLockAddress)
            )
        )
        .withDatum(datum)
        .withMintingStatements(
          Seq(
            AssetMintingStatement(
              groupUtxoAddress,
              seriesUtxoAddress,
              amount,
              permanentMetadata
            )
          )
        )
    } yield ioTransaction

}
