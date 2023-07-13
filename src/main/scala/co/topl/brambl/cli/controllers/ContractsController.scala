package co.topl.brambl.cli.controllers

import cats.Id
import cats.data.Validated
import cats.effect.kernel.Sync
import co.topl.brambl.cli.impl.ContractStorageAlgebra
import co.topl.brambl.cli.impl.QuivrFastParser
import co.topl.brambl.cli.model.WalletContract
import co.topl.brambl.codecs.LockTemplateCodecs

class ContractsController[F[_]: Sync](
    contractStorageAlgebra: ContractStorageAlgebra[F]
) {

  def addContract(
      name: String,
      lockTemplate: String
  ): F[Either[String, String]] = {
    import cats.implicits._
    for {
      lockTemplateStruct <- Sync[F].delay(
        QuivrFastParser.make[Id].parseQuivr(lockTemplate)
      )
      res <- lockTemplateStruct match {
        case Validated.Valid(lockTemplate) =>
          for {
            lockTemplateAsJson <-
              Sync[F].delay(
                LockTemplateCodecs.encodeLockTemplate[Id](
                  lockTemplate
                )
              )
            added <- contractStorageAlgebra.addContract(
              WalletContract(0, name, lockTemplateAsJson.noSpaces)
            )
          } yield
            if (added == 1) Right("Contract added successfully")
            else Left("Failed to add contract")
        case Validated.Invalid(e) =>
          import cats.implicits._

          e.toList
            .traverse(x => Sync[F].delay(s"Error at ${x.location}: ${x.error}"))
            .map(x => Left(x.mkString("\n")))
      }
    } yield res
  }

  def listContracts(): F[Either[String, String]] = {
    import co.topl.brambl.cli.views.WalletModelDisplayOps._
    import cats.implicits._

    contractStorageAlgebra
      .findContracts()
      .map(contracts =>
        Right(
          displayWalletContractHeader() + "\n" + contracts
            .map(display)
            .mkString("\n")
        )
      )
  }
}
