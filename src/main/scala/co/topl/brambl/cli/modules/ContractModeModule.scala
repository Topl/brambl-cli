package co.topl.brambl.cli.modules

import cats.effect.IO
import co.topl.brambl.cli.BramblCliValidatedParams
import co.topl.brambl.cli.controllers.ContractsController
import co.topl.brambl.cli.impl.ContractStorageAlgebra
import co.topl.brambl.cli.BramblCliSubCmd

trait ContractModeModule extends WalletResourceModule {
  def contractModeSubcmds(
      validateParams: BramblCliValidatedParams
  ): IO[Either[String, String]] = {
    val contractStorageAlgebra = ContractStorageAlgebra.make[IO](
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
