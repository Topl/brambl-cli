package co.topl.brambl.cli.controllers

import cats.effect.IO
import cats.effect.kernel.Resource
import co.topl.brambl.cli.impl.PartyStorageAlgebra

import java.sql.Connection

class PartiesController(walletResource: Resource[IO, Connection]) {

  def listParties(): IO[Unit] = {
    import co.topl.brambl.cli.views.WalletModelDisplayOps._
    import cats.implicits._
    val partyStorageAlgebra = PartyStorageAlgebra.make(walletResource)
    IO.println(displayWalletEntityHeader()) >>
      partyStorageAlgebra
        .findParties()
        .flatMap(parties => parties.map(display).map(IO.println).sequence.void)
  }

}
