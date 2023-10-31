package co.topl.brambl.cli.controllers

import cats.Applicative
import co.topl.brambl.dataApi.{PartyStorageAlgebra, WalletEntity}


class FellowshipsController[F[_]: Applicative](
    fellowshipStorageAlgebra: PartyStorageAlgebra[F]
) {

  def addParty(name: String): F[Either[String, String]] = {
    import cats.implicits._
    for {
      added <- fellowshipStorageAlgebra.addParty(WalletEntity(0, name))
    } yield
      if (added == 1) Right(s"Fellowship $name added successfully")
      else Left("Failed to add fellowship")
  }

  def listFellowships(): F[Either[String, String]] = {
    import co.topl.brambl.cli.views.WalletModelDisplayOps._
    import cats.implicits._
    fellowshipStorageAlgebra
      .findParties()
      .map(fellowships =>
        Right(
          displayWalletEntityHeader() + "\n" + fellowships
            .map(display)
            .mkString("\n")
        )
      )
  }

}
