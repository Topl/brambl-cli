package co.topl.brambl.cli.controllers

import cats.effect.IO
import cats.effect.kernel.Resource
import co.topl.brambl.cli.impl.PartyStorageAlgebra
import co.topl.brambl.cli.model.WalletEntity

import java.sql.Connection

class PartiesController(walletResource: Resource[IO, Connection]) {

  val partyStorageAlgebra = PartyStorageAlgebra.make(walletResource)

  def addParty(name: String): IO[Int] = {
    partyStorageAlgebra.addParty(WalletEntity(0, name))
  }

  def listParties(): IO[Unit] = {
    import co.topl.brambl.cli.views.WalletModelDisplayOps._
    import cats.implicits._
    IO.println(displayWalletEntityHeader()) >>
      partyStorageAlgebra
        .findParties()
        .flatMap(parties => parties.map(display).map(IO.println).sequence.void)
  }

}
