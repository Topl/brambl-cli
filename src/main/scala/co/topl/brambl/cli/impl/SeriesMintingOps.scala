package co.topl.brambl.cli.impl

import cats.effect.kernel.Resource
import cats.effect.kernel.Sync
import co.topl.brambl.builders.TransactionBuilderApi
import co.topl.brambl.dataApi.WalletStateAlgebra
import co.topl.brambl.models.Datum
import co.topl.brambl.models.Event
import co.topl.brambl.models.Indices
import co.topl.brambl.models.LockAddress
import co.topl.brambl.models.SeriesId
import co.topl.brambl.models.TransactionOutputAddress
import co.topl.brambl.models.box.FungibilityType
import co.topl.brambl.models.box.Lock
import co.topl.brambl.models.box.QuantityDescriptorType
import co.topl.brambl.models.box.Value
import co.topl.brambl.models.transaction.IoTransaction
import co.topl.brambl.models.transaction.SpentTransactionOutput
import co.topl.brambl.models.transaction.UnspentTransactionOutput
import co.topl.brambl.utils.Encoding
import co.topl.brambl.wallet.WalletApi
import co.topl.genus.services.Txo
import com.google.protobuf.ByteString
import quivr.models.Int128
import quivr.models.KeyPair

import java.io.FileOutputStream

import TransactionBuilderApi.implicits._
import com.google.protobuf.struct.Struct

trait SeriesMintingOps[G[_]] extends CommonTxOps {

  import cats.implicits._

  implicit val sync: Sync[G]

  val tba: TransactionBuilderApi[G]

  val wsa: WalletStateAlgebra[G]

  val wa: WalletApi[G]

  def seriesOutput(
      lockAddress: LockAddress,
      quantity: Int128,
      seriesId: SeriesId,
      tokenSupply: Option[Int],
      quantityDescriptor: QuantityDescriptorType,
      fungibility: FungibilityType
  ): G[UnspentTransactionOutput] =
    UnspentTransactionOutput(
      lockAddress,
      Value.defaultInstance.withSeries(
        Value.Series(
          seriesId = seriesId,
          quantity = quantity,
          tokenSupply = tokenSupply,
          quantityDescriptor = quantityDescriptor,
          fungibility = fungibility
        )
      )
    ).pure[G]

  private def buildSimpleSeriesMintingTransaction(
      lvlTxos: Seq[Txo],
      lockPredicateFrom: Lock.Predicate,
      recipientLockAddress: LockAddress,
      amount: Long,
      fee: Long,
      seriesId: SeriesId,
      label: String,
      tokenSupply: Option[Int],
      quantityDescriptor: QuantityDescriptorType,
      fungibility: FungibilityType,
      registrationUtxo: TransactionOutputAddress,
      ephemeralMetadataScheme: Option[Struct],
      permanentMetadataScheme: Option[Struct]
  ): G[IoTransaction] =
    for {
      unprovenAttestationToProve <- tba.unprovenAttestation(
        lockPredicateFrom
      )
      totalValues = computeLvlQuantity(lvlTxos)
      datum <- tba.datum()
      lvlOutputForChange <- tba.lvlOutput(
        recipientLockAddress,
        Int128(
          ByteString.copyFrom(
            BigInt(totalValues.toLong - fee).toByteArray
          )
        )
      )
      gOutput <- seriesOutput(
        recipientLockAddress,
        Int128(ByteString.copyFrom(BigInt(amount).toByteArray)),
        seriesId,
        tokenSupply,
        quantityDescriptor,
        fungibility
      )
      _ = Sync[G].delay(println("gOutput: " + gOutput))
      ioTransaction = IoTransaction.defaultInstance
        .withInputs(
          lvlTxos.map(x =>
            SpentTransactionOutput(
              x.outputAddress,
              unprovenAttestationToProve,
              x.transactionOutput.value
            )
          )
        )
        .withOutputs(
          // If there is no change, we don't need to add it to the outputs
          if (totalValues.toLong - fee > 0)
            Seq(lvlOutputForChange, gOutput)
          else
            Seq(gOutput)
        )
        .withDatum(datum)
        .withSeriesPolicies(
          Seq(
            Datum.SeriesPolicy(
              Event.SeriesPolicy(
                label,
                tokenSupply,
                registrationUtxo,
                quantityDescriptor,
                fungibility,
                ephemeralMetadataScheme,
                permanentMetadataScheme
              )
            )
          )
        )
    } yield ioTransaction

  private def buildSeriesTransaction(
      lvlTxos: Seq[Txo],
      predicateFundsToUnlock: Lock.Predicate,
      lockForChange: Lock,
      recipientLockAddress: LockAddress,
      amount: Long,
      fee: Long,
      someNextIndices: Option[Indices],
      keyPair: KeyPair,
      outputFile: String,
      seriesId: SeriesId,
      label: String,
      tokenSupply: Option[Int],
      quantityDescriptor: QuantityDescriptorType,
      fungibility: FungibilityType,
      registrationUtxo: TransactionOutputAddress,
      ephemeralMetadataScheme: Option[Struct],
      permanentMetadataScheme: Option[Struct]
  ): G[Unit] =
    for {
      ioTransaction <-
        buildSimpleSeriesMintingTransaction(
          lvlTxos,
          predicateFundsToUnlock,
          recipientLockAddress,
          amount,
          fee,
          seriesId,
          label,
          tokenSupply,
          quantityDescriptor,
          fungibility,
          registrationUtxo,
          ephemeralMetadataScheme,
          permanentMetadataScheme
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

  def buildSeriesTxAux(
      lvlTxos: Seq[Txo],
      predicateFundsToUnlock: Lock.Predicate,
      amount: Long,
      fee: Long,
      someNextIndices: Option[Indices],
      keyPair: KeyPair,
      outputFile: String,
      seriesId: SeriesId,
      label: String,
      tokenSupply: Option[Int],
      quantityDescriptor: QuantityDescriptorType,
      fungibility: FungibilityType,
      registrationUtxo: TransactionOutputAddress,
      ephemeralMetadataScheme: Option[Struct],
      permanentMetadataScheme: Option[Struct],
      changeLock: Option[Lock]
  ) = (if (lvlTxos.isEmpty) {
         Sync[G].raiseError(CreateTxError("No LVL txos found"))
       } else {
         changeLock match {
           case Some(lockPredicateForChange) =>
             tba
               .lockAddress(lockPredicateForChange)
               .flatMap { changeAddress =>
                 buildSeriesTransaction(
                   lvlTxos,
                   predicateFundsToUnlock,
                   lockPredicateForChange,
                   changeAddress,
                   amount,
                   fee,
                   someNextIndices,
                   keyPair,
                   outputFile,
                   seriesId,
                   label,
                   tokenSupply,
                   quantityDescriptor,
                   fungibility,
                   registrationUtxo,
                   ephemeralMetadataScheme,
                   permanentMetadataScheme
                 )
               }
           case None =>
             Sync[G].raiseError(
               CreateTxError("Unable to generate change lock")
             )
         }
       })
}
