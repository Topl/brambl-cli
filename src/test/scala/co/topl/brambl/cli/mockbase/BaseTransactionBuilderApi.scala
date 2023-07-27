package co.topl.brambl.cli.mockbase

import co.topl.brambl.builders.TransactionBuilderApi
import co.topl.brambl.models.Datum
import co.topl.brambl.models.LockAddress
import co.topl.brambl.models.box.Attestation
import co.topl.brambl.models.box.Lock
import co.topl.brambl.models.transaction.IoTransaction
import co.topl.brambl.models.transaction.UnspentTransactionOutput
import co.topl.genus.services.Txo
import quivr.models.Int128

class BaseTransactionBuilderApi[F[_]] extends TransactionBuilderApi[F] {

  override def unprovenAttestation(lockPredicate: Lock.Predicate): F[Attestation] = ???

  override def lockAddress(lock: Lock): F[LockAddress] = ???

  override def lvlOutput(predicate: Lock.Predicate, amount: Int128): F[UnspentTransactionOutput] = ???

  override def lvlOutput(lockAddress: LockAddress, amount: Int128): F[UnspentTransactionOutput] = ???

  override def datum(): F[Datum.IoTransaction] = ???

  override def buildSimpleLvlTransaction(lvlTxos: Seq[Txo], lockPredicateFrom: Lock.Predicate, lockPredicateForChange: Lock.Predicate, recipientLockAddress: LockAddress, amount: Long): F[IoTransaction] = ???


}
