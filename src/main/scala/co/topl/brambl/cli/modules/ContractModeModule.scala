package co.topl.brambl.cli.modules

import cats.effect.IO
import co.topl.brambl.cli.controllers.ContractsController
import co.topl.brambl.servicekit.{ContractStorageApi, WalletStateResource}
import co.topl.brambl.cli.BramblCliSubCmd
import co.topl.brambl.cli.BramblCliParams

trait ContractModeModule extends WalletStateResource {
  def contractModeSubcmds(
      validateParams: BramblCliParams
  ): IO[Either[String, String]] = {
    val contractStorageAlgebra = ContractStorageApi.make[IO](
      walletResource(validateParams.walletFile)
    )
    validateParams.subcmd match {
      case BramblCliSubCmd.list =>
        new ContractsController(
          contractStorageAlgebra
        )
          .listContracts()
      case BramblCliSubCmd.add =>
        new ContractsController(
          contractStorageAlgebra
        )
          .addContract(
            validateParams.contractName,
            validateParams.lockTemplate
          )
    }
  }
}
