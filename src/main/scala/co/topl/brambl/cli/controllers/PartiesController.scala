package co.topl.brambl.cli.controllers

import cats.Applicative
import co.topl.brambl.cli.impl.PartyStorageAlgebra
import co.topl.brambl.cli.model.WalletEntity

class PartiesController[F[_]: Applicative](
    partyStorageAlgebra: PartyStorageAlgebra[F]
) {

  def addParty(name: String): F[String] = {
    import cats.implicits._
    partyStorageAlgebra.addParty(WalletEntity(0, name)) *>
      s"Party $name added successfully".pure[F]
  }

  def listParties(): F[String] = {
    import co.topl.brambl.cli.views.WalletModelDisplayOps._
    import cats.implicits._
    partyStorageAlgebra
      .findParties()
      .map(parties =>
        displayWalletEntityHeader() + "\n" + parties
          .map(display)
          .mkString("\n")
      )
  }

}
