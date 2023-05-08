package co.topl.brambl.cli.impl

import cats.effect.kernel.Sync
import co.topl.brambl.codecs.AddressCodecs
import co.topl.brambl.models.Datum
import co.topl.brambl.models.Event
import co.topl.brambl.models.LockAddress
import co.topl.brambl.models.LockId
import co.topl.brambl.models.box.Attestation
import co.topl.brambl.models.box.Challenge
import co.topl.brambl.models.box.Lock
import co.topl.brambl.models.box.Value
import co.topl.brambl.models.transaction.IoTransaction
import co.topl.brambl.models.transaction.Schedule
import co.topl.brambl.models.transaction.SpentTransactionOutput
import co.topl.brambl.models.transaction.UnspentTransactionOutput
import co.topl.genus.services.Txo
import co.topl.quivr.api.Proposer
import com.google.protobuf.ByteString
import quivr.models.Int128
import quivr.models.SmallData
import quivr.models.VerificationKey

trait TransactionBuilderApi[F[_]] {

  def lockPredicateSignature(vk: VerificationKey): F[Lock.Predicate]

  def unprovenAttestation(lockPredicate: Lock.Predicate): F[Attestation]

  def lockPredicateHeight(minHeight: Long, maxHeight: Long): F[Lock.Predicate]

  def lockAddress(
      predicate: Lock.Predicate
  ): F[LockAddress]

  def lvlOuput(
      predicate: Lock.Predicate,
      amount: Int128
  ): F[UnspentTransactionOutput]

  def lvlOuput(
      lockAddress: LockAddress,
      amount: Int128
  ): F[UnspentTransactionOutput]

  def datum(): F[Datum.IoTransaction]

  def buildSimpleLvlTransaction(
      lvlTxos: Seq[Txo],
      lockPredicateFrom: Lock.Predicate,
      lockPredicateForChange: Lock.Predicate,
      recipientLockAddress: LockAddress,
      amount: Long
  ): F[IoTransaction]

}

object TransactionBuilderApi {

  object implicits {

    case class LockAddressOps(
        lockAddress: LockAddress
    ) {
      def toBase58(): String = AddressCodecs.encodeAddress(lockAddress)
    }

    implicit def lockAddressOps(
        lockAddress: LockAddress
    ): LockAddressOps = LockAddressOps(lockAddress)

  }

  def make[F[_]: Sync](
      networkId: Int,
      ledgerId: Int
  ): TransactionBuilderApi[F] =
    new TransactionBuilderApi[F] {

      override def buildSimpleLvlTransaction(
          lvlTxos: Seq[Txo],
          lockPredicateFrom: Lock.Predicate,
          lockPredicateForChange: Lock.Predicate,
          recipientLockAddress: LockAddress,
          amount: Long
      ): F[IoTransaction] = {
        import cats.implicits._
        for {
          unprovenAttestationToProve <- unprovenAttestation(lockPredicateFrom)
          datum <- datum()
          lvlOutputForChange <- lvlOuput(
            lockPredicateForChange,
            Int128(ByteString.copyFrom(BigInt(amount).toByteArray))
          )
          lvlOutputForRecipient <- lvlOuput(
            recipientLockAddress,
            Int128(ByteString.copyFrom(BigInt(amount).toByteArray))
          )
          ioTransaction = IoTransaction(
            None,
            lvlTxos.map(x =>
              SpentTransactionOutput(
                x.outputAddress,
                unprovenAttestationToProve,
                x.transactionOutput.value
              )
            ),
            Seq(lvlOutputForChange, lvlOutputForRecipient),
            datum = datum
          )
        } yield ioTransaction
      }

      override def lvlOuput(
          lockAddress: LockAddress,
          amount: Int128
      ): F[UnspentTransactionOutput] =
        Sync[F].point(
          UnspentTransactionOutput(
            lockAddress,
            Value().withLvl(Value.LVL(amount))
          )
        )

      override def lockAddress(
          predicate: Lock.Predicate
      ): F[LockAddress] = {
        import co.topl.brambl.common.ContainsEvidence.Ops
        import co.topl.brambl.common.ContainsImmutable.instances._
        import cats.implicits._
        for {
          lock <- Sync[F].point(Lock().withPredicate(predicate))
          lockId <- Sync[F].point(
            LockId(lock.sizedEvidence.digest.value)
          )
          lockAddress <- Sync[F].point(
            LockAddress(
              networkId,
              ledgerId,
              lockId
            )
          )
        } yield lockAddress
      }

      override def lvlOuput(
          predicate: Lock.Predicate,
          amount: Int128
      ): F[UnspentTransactionOutput] = {
        import co.topl.brambl.common.ContainsEvidence.Ops
        import co.topl.brambl.common.ContainsImmutable.instances._
        Sync[F].point(
          UnspentTransactionOutput(
            LockAddress(
              networkId,
              ledgerId,
              LockId(Lock().withPredicate(predicate).sizedEvidence.digest.value)
            ),
            Value().withLvl(Value.LVL(amount))
          )
        )
      }

      override def datum(): F[Datum.IoTransaction] = {
        import cats.implicits._
        for {
          timestamp <- Sync[F].realTimeInstant
          schedule <- Sync[F].point(
            Schedule(0, Long.MaxValue, timestamp.toEpochMilli())
          )
        } yield Datum.IoTransaction(
          Event.IoTransaction(schedule, SmallData.defaultInstance)
        )
      }

      override def unprovenAttestation(
          predicate: Lock.Predicate
      ): F[Attestation] = {
        import cats.implicits._
        Attestation(
          Attestation.Value.Predicate(
            Attestation.Predicate(
              predicate,
              Nil
            )
          )
        ).pure[F]
      }

      override def lockPredicateHeight(
          minHeight: Long,
          maxHeight: Long
      ): F[Lock.Predicate] =
        Sync[F].point(
          Lock.Predicate(
            List(
              Challenge().withRevealed(
                Proposer
                  .heightProposer(cats.catsInstancesForId)
                  .propose(("header", minHeight, maxHeight))
              )
            )
          )
        )

      override def lockPredicateSignature(
          vk: VerificationKey
      ): F[Lock.Predicate] =
        Sync[F].point(
          Lock.Predicate(
            List(
              Challenge().withRevealed(
                Proposer
                  .signatureProposer(cats.catsInstancesForId)
                  .propose(("ExtendedEd25519", vk))
              )
            )
          )
        )

    }

}
