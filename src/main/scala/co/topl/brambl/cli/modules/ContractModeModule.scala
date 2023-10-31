package co.topl.brambl.cli.modules

import cats.effect.IO
import co.topl.brambl.cli.controllers.TemplatesController
import co.topl.brambl.servicekit.{ContractStorageApi, WalletStateResource}
import co.topl.brambl.cli.BramblCliSubCmd
import co.topl.brambl.cli.BramblCliParams

trait TemplateModeModule extends WalletStateResource {
  def templateModeSubcmds(
      validateParams: BramblCliParams
  ): IO[Either[String, String]] = {
    val templateStorageAlgebra = ContractStorageApi.make[IO](
      walletResource(validateParams.walletFile)
    )
    validateParams.subcmd match {
      case BramblCliSubCmd.invalid =>
        IO.pure(Left("A subcommand needs to be specified"))
      case BramblCliSubCmd.list =>
        new TemplatesController(
          templateStorageAlgebra
        )
          .listTemplates()
      case BramblCliSubCmd.add =>
        new TemplatesController(
          templateStorageAlgebra
        )
          .addTemplate(
            validateParams.templateName,
            validateParams.lockTemplate
          )
    }
  }
}
