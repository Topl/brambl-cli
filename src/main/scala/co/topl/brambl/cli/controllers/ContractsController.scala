package co.topl.brambl.cli.controllers

import cats.effect.kernel.Resource
import cats.effect.IO
import java.sql.Connection
import co.topl.brambl.cli.impl.ContractStorageAlgebra

class ContractsController(walletResource: Resource[IO, Connection]) {

  val contractStorageAlgebra = ContractStorageAlgebra.make(walletResource)

  def listContracts(): IO[Unit] = {
    import co.topl.brambl.cli.views.WalletModelDisplayOps._
    import cats.implicits._
    IO.println(displayWalletContractHeader()) >>
      contractStorageAlgebra
        .findContracts()
        .flatMap(contracts =>
          contracts.map(display).map(IO.println).sequence.void
        )

  }
}
