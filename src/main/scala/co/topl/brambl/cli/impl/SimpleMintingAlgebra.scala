package co.topl.brambl.cli.impl

import cats.Monad
import cats.effect.kernel.Resource
import cats.effect.kernel.Sync
import co.topl.brambl.builders.TransactionBuilderApi
import co.topl.brambl.dataApi.GenusQueryAlgebra
import co.topl.brambl.dataApi.WalletStateAlgebra
import co.topl.brambl.models.GroupId
import co.topl.brambl.models.Indices
import co.topl.brambl.models.LockAddress
import co.topl.brambl.models.SeriesId
import co.topl.brambl.models.box.Lock
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

trait SimpleMintingAlgebra[F[_]] {
  def createSimpleGroupMintingTransactionFromParams(
      keyFile: String,
      password: String,
      fromParty: String,
      fromContract: String,
      someFromState: Option[Int],
      amount: Long,
      fee: Long,
      outputFile: String,
      groupId: GroupId,
      fixedSeries: Option[SeriesId]
  ): F[Unit]
}

object SimpleMintingAlgebra {

  import cats.implicits._

  def make[F[_]: Sync](
      walletApi: WalletApi[F],
      walletStateApi: WalletStateAlgebra[F],
      walletManagementUtils: WalletManagementUtils[F],
      transactionBuilderApi: TransactionBuilderApi[F],
      utxoAlgebra: GenusQueryAlgebra[F]
  ): SimpleMintingAlgebra[F] = new SimpleMintingAlgebra[F]
    with WalletApiHelpers[F] {

    val wsa: WalletStateAlgebra[F] = walletStateApi

    implicit val m: Monad[F] = Sync[F]

    private def groupOutput(
        lockAddress: LockAddress,
        quantity: Int128,
        groupId: GroupId,
        fixedSeries: Option[SeriesId]
    ): F[UnspentTransactionOutput] =
      UnspentTransactionOutput(
        lockAddress,
        Value.defaultInstance.withGroup(
          Value.Group(
            groupId = groupId,
            quantity = quantity,
            fixedSeries = fixedSeries
          )
        )
      ).pure[F]

    private def computeLvlQuantity(lvlTxos: Seq[Txo]) = lvlTxos
      .foldLeft(
        BigInt(0)
      )((acc, x) =>
        acc + x.transactionOutput.value.value.lvl
          .map(y => BigInt(y.quantity.value.toByteArray))
          .getOrElse(BigInt(0))
      )

    private def buildSimpleGroupMiningTransaction(
        lvlTxos: Seq[Txo],
        lockPredicateFrom: Lock.Predicate,
        recipientLockAddress: LockAddress,
        fee: Long,
        amount: Long,
        groupId: GroupId,
        fixedSeries: Option[SeriesId]
    ): F[IoTransaction] =
      for {
        unprovenAttestationToProve <- transactionBuilderApi.unprovenAttestation(
          lockPredicateFrom
        )
        totalValues = computeLvlQuantity(lvlTxos)
        datum <- transactionBuilderApi.datum()
        lvlOutputForChange <- transactionBuilderApi.lvlOutput(
          recipientLockAddress,
          Int128(
            ByteString.copyFrom(
              BigInt(totalValues.toLong - fee).toByteArray
            )
          )
        )
        gOutput <- groupOutput(
          recipientLockAddress,
          Int128(ByteString.copyFrom(BigInt(amount).toByteArray)),
          groupId,
          fixedSeries
        )
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
      } yield ioTransaction

    private def buildTransaction(
        lvlTxos: Seq[Txo],
        predicateFundsToUnlock: Lock.Predicate,
        lockForChange: Lock,
        recipientLockAddress: LockAddress,
        fee: Long,
        amount: Long,
        someNextIndices: Option[Indices],
        keyPair: KeyPair,
        outputFile: String,
        groupId: GroupId,
        fixedSeries: Option[SeriesId]
    ): F[Unit] =
      for {
        ioTransaction <-
          buildSimpleGroupMiningTransaction(
            lvlTxos,
            predicateFundsToUnlock,
            recipientLockAddress,
            fee,
            amount,
            groupId,
            fixedSeries
          )
        // Only save to wallet state if there is a change output in the transaction
        _ <-
          if (ioTransaction.outputs.length >= 2) for {
            lockAddress <- transactionBuilderApi.lockAddress(
              lockForChange
            )
            vk <- someNextIndices
              .map(nextIndices =>
                walletApi
                  .deriveChildKeys(keyPair, nextIndices)
                  .map(_.vk)
              )
              .sequence
            _ <- walletStateApi.updateWalletState(
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
            Sync[F].delay(())
          }
        _ <-
          Resource
            .make(
              Sync[F]
                .delay(
                  new FileOutputStream(outputFile)
                )
            )(fos => Sync[F].delay(fos.close()))
            .use { fos =>
              Sync[F]
                .delay(ioTransaction.writeTo(fos))
                .onError(_ =>
                  Sync[F].raiseError(
                    CannotSerializeProtobufFile(
                      "Cannot write to file"
                    )
                  )
                )
            }
      } yield ()

    private def buildTxAux(
        lvlTxos: Seq[Txo],
        predicateFundsToUnlock: Lock.Predicate,
        fee: Long,
        amount: Long,
        someNextIndices: Option[Indices],
        keyPair: KeyPair,
        outputFile: String,
        groupId: GroupId,
        fixedSeries: Option[SeriesId],
        changeLock: Option[Lock]
    ) = (if (lvlTxos.isEmpty) {
           Sync[F].raiseError(CreateTxError("No LVL txos found"))
         } else {
           changeLock match {
             case Some(lockPredicateForChange) =>
               transactionBuilderApi
                 .lockAddress(lockPredicateForChange)
                 .flatMap { changeAddress =>
                   buildTransaction(
                     lvlTxos,
                     predicateFundsToUnlock,
                     lockPredicateForChange,
                     changeAddress,
                     amount,
                     fee,
                     someNextIndices,
                     keyPair,
                     outputFile,
                     groupId,
                     fixedSeries
                   )
                 }
             case None =>
               Sync[F].raiseError(
                 CreateTxError("Unable to generate change lock")
               )
           }
         })

    override def createSimpleGroupMintingTransactionFromParams(
        keyfile: String,
        password: String,
        fromParty: String,
        fromContract: String,
        someFromState: Option[Int],
        amount: Long,
        fee: Long,
        outputFile: String,
        groupId: GroupId,
        fixedSeries: Option[SeriesId]
    ): F[Unit] = for {
      keyPair <-
        walletManagementUtils
          .loadKeys(
            keyfile,
            password
          )
      someCurrentIndices <- getCurrentIndices(
        fromParty,
        fromContract,
        someFromState
      )
      predicateFundsToUnlock <- getPredicateFundsToUnlock(someCurrentIndices)
      someNextIndices <- getNextIndices(fromParty, fromContract)
      changeLock <- getChangeLockPredicate(
        someNextIndices,
        fromParty,
        fromContract
      )
      fromAddress <- transactionBuilderApi.lockAddress(
        predicateFundsToUnlock.get
      )
      response <- utxoAlgebra.queryUtxo(fromAddress)
      lvlTxos = response.filter(
        _.transactionOutput.value.value.isLvl
      )
      _ <- buildTxAux(
        lvlTxos,
        predicateFundsToUnlock.get.getPredicate,
        fee,
        amount,
        someNextIndices,
        keyPair,
        outputFile,
        groupId,
        fixedSeries,
        changeLock
      )
    } yield ()
  }

}
