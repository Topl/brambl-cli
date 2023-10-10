package co.topl.brambl.cli.mockbase

import co.topl.brambl.builders.BuilderError
import co.topl.brambl.builders.TransactionBuilderApi
import co.topl.brambl.models.Datum
import co.topl.brambl.models.Event
import co.topl.brambl.models.GroupId
import co.topl.brambl.models.LockAddress
import co.topl.brambl.models.SeriesId
import co.topl.brambl.models.box.AssetMintingStatement
import co.topl.brambl.models.box.Attestation
import co.topl.brambl.models.box.FungibilityType
import co.topl.brambl.models.box.Lock
import co.topl.brambl.models.box.QuantityDescriptorType
import co.topl.brambl.models.transaction.IoTransaction
import co.topl.brambl.models.transaction.UnspentTransactionOutput
import co.topl.brambl.syntax.ValueTypeIdentifier
import co.topl.genus.services.Txo
import com.google.protobuf.ByteString
import com.google.protobuf.struct.Struct
import quivr.models.Int128

class BaseTransactionBuilderApi[F[_]] extends TransactionBuilderApi[F] {

  override def buildTransferAllTransaction(txos: Seq[Txo], lockPredicateFrom: Lock.Predicate, recipientLockAddress: LockAddress, changeLockAddress: LockAddress, fee: Long, tokenIdentifier: Option[ValueTypeIdentifier]): F[Either[BuilderError,IoTransaction]] = ???

  override def buildTransferAmountTransaction(tokenIdentifier: ValueTypeIdentifier, txos: Seq[Txo], lockPredicateFrom: Lock.Predicate, amount: Long, recipientLockAddress: LockAddress, changeLockAddress: LockAddress, fee: Long): F[Either[BuilderError,IoTransaction]] = ???


  override def groupOutput(lockAddress: LockAddress, quantity: Int128, groupId: GroupId, fixedSeries: Option[SeriesId]): F[UnspentTransactionOutput] = ???

  override def seriesOutput(lockAddress: LockAddress, quantity: Int128, seriesId: SeriesId, tokenSupply: Option[Int], fungibility: FungibilityType, quantityDescriptor: QuantityDescriptorType): F[UnspentTransactionOutput] = ???

  override def assetOutput(lockAddress: LockAddress, quantity: Int128, groupId: GroupId, seriesId: SeriesId, fungibilityType: FungibilityType, quantityDescriptorType: QuantityDescriptorType, metadata: Option[Struct], commitment: Option[ByteString]): F[UnspentTransactionOutput] = ???



  override def buildSimpleGroupMintingTransaction(
      registrationTxo: Txo,
      registrationLock: Lock.Predicate,
      groupPolicy: Event.GroupPolicy,
      quantityToMint: Int128,
      mintedConstructorLockAddress: LockAddress
  ): F[Either[BuilderError, IoTransaction]] = ???

  override def buildSimpleSeriesMintingTransaction(
      registrationTxo: Txo,
      registrationLock: Lock.Predicate,
      seriesPolicy: Event.SeriesPolicy,
      quantityToMint: Int128,
      mintedConstructorLockAddress: LockAddress
  ): F[Either[BuilderError, IoTransaction]] = ???

  override def buildSimpleAssetMintingTransaction(
      mintingStatement: AssetMintingStatement,
      groupTxo: Txo,
      seriesTxo: Txo,
      groupLock: Lock.Predicate,
      seriesLock: Lock.Predicate,
      mintedAssetLockAddress: LockAddress,
      ephemeralMetadata: Option[Struct],
      commitment: Option[Array[Byte]]
  ): F[Either[BuilderError, IoTransaction]] = ???

  override def unprovenAttestation(
      lockPredicate: Lock.Predicate
  ): F[Attestation] = ???

  override def lockAddress(lock: Lock): F[LockAddress] = ???

  override def lvlOutput(
      predicate: Lock.Predicate,
      amount: Int128
  ): F[UnspentTransactionOutput] = ???

  override def lvlOutput(
      lockAddress: LockAddress,
      amount: Int128
  ): F[UnspentTransactionOutput] = ???

  override def datum(): F[Datum.IoTransaction] = ???

  override def buildSimpleLvlTransaction(
      lvlTxos: Seq[Txo],
      lockPredicateFrom: Lock.Predicate,
      lockPredicateForChange: Lock.Predicate,
      recipientLockAddress: LockAddress,
      amount: Long
  ): F[IoTransaction] = ???

}
