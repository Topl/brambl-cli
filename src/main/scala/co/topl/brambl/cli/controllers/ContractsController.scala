package co.topl.brambl.cli.controllers

import cats.effect.kernel.Resource
import cats.effect.IO
import java.sql.Connection
import co.topl.brambl.cli.impl.ContractStorageAlgebra
import co.topl.brambl.cli.model.WalletContract
import co.topl.brambl.cli.impl.QuivrFastParser
import co.topl.brambl.codecs.LockTemplateCodecs
import cats.Id
import cats.data.Validated

class ContractsController(walletResource: Resource[IO, Connection]) {

  val contractStorageAlgebra = ContractStorageAlgebra.make(walletResource)

  def addContract(name: String, lockTemplate: String): IO[Int] = {
    for {
      lockTemplateStruct <- IO(
        QuivrFastParser.make[Id].parseQuivr(lockTemplate)
      )
      res <- lockTemplateStruct match {
        case Validated.Valid(lockTemplate) =>
          for {
            lockTemplateAsJson <-
              IO(
                LockTemplateCodecs.encodeLockTemplate[Id](
                  lockTemplate
                )
              )
            result <- contractStorageAlgebra.addContract(
              WalletContract(0, name, lockTemplateAsJson.noSpaces)
            )
          } yield result
        case Validated.Invalid(e) =>
          import cats.implicits._
          e.toList.traverse(x =>
            IO.println(s"Error at ${x.location}: ${x.error}")
          ) >>
            IO(0)
      }
    } yield res
  }

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
