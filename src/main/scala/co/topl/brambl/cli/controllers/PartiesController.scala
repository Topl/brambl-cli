package co.topl.brambl.cli.controllers

import cats.Applicative
import co.topl.brambl.dataApi.{PartyStorageAlgebra, WalletEntity}


class PartiesController[F[_]: Applicative](
    partyStorageAlgebra: PartyStorageAlgebra[F]
) {

  def addParty(name: String): F[Either[String, String]] = {
    import cats.implicits._
    for {
      added <- partyStorageAlgebra.addParty(WalletEntity(0, name))
    } yield
      if (added == 1) Right(s"Party $name added successfully")
      else Left("Failed to add party")
  }

  def listParties(): F[Either[String, String]] = {
    import co.topl.brambl.cli.views.WalletModelDisplayOps._
    import cats.implicits._
    partyStorageAlgebra
      .findParties()
      .map(parties =>
        Right(
          displayWalletEntityHeader() + "\n" + parties
            .map(display)
            .mkString("\n")
        )
      )
  }

}
