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
      fellowship: String,
      template: String,
      someState: Option[Int]
  ): F[Option[Indices]] = ???

  override def validateCurrentIndicesForFunds(
      fellowship: String,
      template: String,
      someState: Option[Int]
  ): F[ValidatedNel[String, Indices]] = ???

  override def getNextIndicesForFunds(
      fellowship: String,
      template: String
  ): F[Option[Indices]] = ???

  override def getLockByIndex(indices: Indices): F[Option[Lock.Predicate]] =
    ???

  override def addEntityVks(
      fellowship: String,
      template: String,
      entities: List[String]
  ): F[Unit] = ???

  override def getEntityVks(
      fellowship: String,
      template: String
  ): F[Option[List[String]]] = ???

  override def addNewLockTemplate(
      template: String,
      lockTemplate: LockTemplate[F]
  ): F[Unit] = ???

  override def getLockTemplate(template: String): F[Option[LockTemplate[F]]] =
    ???

  override def getLock(
      fellowship: String,
      template: String,
      nextState: Int
  ): F[Option[Lock]] = ???

  override def getAddress(
      fellowship: String,
      template: String,
      interaction: Option[Int]
  ): F[Option[String]] = ???
}
