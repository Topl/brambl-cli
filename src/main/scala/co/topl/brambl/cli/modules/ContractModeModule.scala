package co.topl.brambl.cli.modules

import cats.effect.IO
import co.topl.brambl.cli.controllers.TemplatesController
import co.topl.brambl.servicekit.{TemplateStorageApi, WalletStateResource}
import co.topl.brambl.cli.BramblCliSubCmd
import co.topl.brambl.cli.BramblCliParams
import scopt.OParser
import co.topl.brambl.cli.BramblCliParamsParserModule

trait TemplateModeModule extends WalletStateResource {
  def templateModeSubcmds(
      validateParams: BramblCliParams
  ): IO[Either[String, String]] = {
    val templateStorageAlgebra = TemplateStorageApi.make[IO](
      walletResource(validateParams.walletFile)
    )
    validateParams.subcmd match {
      case BramblCliSubCmd.invalid =>
        IO.pure(
          Left(
            OParser.usage(
              BramblCliParamsParserModule.templatesMode
            ) + "\nA subcommand needs to be specified"
          )
        )
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
