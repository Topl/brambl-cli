package co.topl.brambl.cli.impl

import co.topl.brambl.dataApi.WalletStateAlgebra
import cats.Monad
import co.topl.brambl.models.Indices
import co.topl.brambl.models.box.Lock

trait WalletApiHelpers[F[_]] {

  import cats.implicits._

  val wsa: WalletStateAlgebra[F]

  implicit val m: Monad[F]

  def getCurrentIndices(
      fromParty: String,
      fromContract: String,
      someFromState: Option[Int]
  ) = wsa.getCurrentIndicesForFunds(
    fromParty,
    fromContract,
    someFromState
  )

  def getPredicateFundsToUnlock(someIndices: Option[Indices]) =
    someIndices
      .map(currentIndices => wsa.getLockByIndex(currentIndices))
      .sequence
      .map(_.flatten.map(Lock().withPredicate(_)))

  def getNextIndices(
      fromParty: String,
      fromContract: String
  ) =
    wsa.getNextIndicesForFunds(
      if (fromParty == "nofellowship") "self" else fromParty,
      if (fromParty == "nofellowship") "default"
      else fromContract
    )

  def getChangeLockPredicate(
      someNextIndices: Option[Indices],
      fromParty: String,
      fromContract: String
  ) =
    someNextIndices
      .map(idx =>
        wsa.getLock(
          if (fromParty == "nofellowship") "self" else fromParty,
          if (fromParty == "nofellowship") "default"
          else fromContract,
          idx.z
        )
      )
      .sequence
      .map(_.flatten)

}
