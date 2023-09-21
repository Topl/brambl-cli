package co.topl.brambl.cli.mockbase

import cats.data.ValidatedNel
import co.topl.brambl.builders.locks.LockTemplate
import co.topl.brambl.dataApi.WalletStateAlgebra
import co.topl.brambl.models.Indices
import co.topl.brambl.models.box.Lock
import quivr.models.Preimage
import quivr.models.Proposition
import quivr.models.VerificationKey

class BaseWalletStateAlgebra[F[_]] extends WalletStateAlgebra[F] {

  override def getLockByAddress(
      lockAddress: String
  ): F[Option[Lock.Predicate]] = ???

  override def initWalletState(
      networkId: Int,
      ledgerId: Int,
      vk: VerificationKey
  ): F[Unit] = ???

  override def getIndicesBySignature(
      signatureProposition: Proposition.DigitalSignature
  ): F[Option[Indices]] = ???

  override def getPreimage(
      digestProposition: Proposition.Digest
  ): F[Option[Preimage]] = ???

  override def getCurrentAddress: F[String] = ???

  override def updateWalletState(
      lockPredicate: String,
      lockAddress: String,
      routine: Option[String],
      vk: Option[String],
      indices: Indices
  ): F[Unit] = ???

  override def getCurrentIndicesForFunds(
      party: String,
      contract: String,
      someState: Option[Int]
  ): F[Option[Indices]] = ???

  override def validateCurrentIndicesForFunds(
      party: String,
      contract: String,
      someState: Option[Int]
  ): F[ValidatedNel[String, Indices]] = ???

  override def getNextIndicesForFunds(
      party: String,
      contract: String
  ): F[Option[Indices]] = ???

  override def getLockByIndex(indices: Indices): F[Option[Lock.Predicate]] =
    ???

  override def addEntityVks(
      party: String,
      contract: String,
      entities: List[String]
  ): F[Unit] = ???

  override def getEntityVks(
      party: String,
      contract: String
  ): F[Option[List[String]]] = ???

  override def addNewLockTemplate(
      contract: String,
      lockTemplate: LockTemplate[F]
  ): F[Unit] = ???

  override def getLockTemplate(contract: String): F[Option[LockTemplate[F]]] =
    ???

  override def getLock(
      party: String,
      contract: String,
      nextState: Int
  ): F[Option[Lock]] = ???

  override def getAddress(
      party: String,
      contract: String,
      state: Option[Int]
  ): F[Option[String]] = ???
}
