package co.topl.brambl.cli.validation

import cats.data.ValidatedNel
import co.topl.brambl.cli.BramblCliParams

trait WalletValidationModule {

  self: CommonValidationModule =>

  def validateKeyGenerationParams(
      paramConfig: BramblCliParams
  ): ValidatedNel[String, BramblCliParams] = {
    import cats.implicits._
    List(
      validateNonEmpty("Password", paramConfig.password),
      validatePassphrase(paramConfig.somePassphrase)
    ).sequence.map(_ => paramConfig)
  }

  def validateExportVkParam(
      paramConfig: BramblCliParams
  ): ValidatedNel[String, BramblCliParams] = {
    import cats.implicits._
    List(
      validateInputFile(
        "Keyfile",
        paramConfig.someKeyFile,
        required = true
      ),
      validateInputFile(
        "Wallet DB",
        paramConfig.someWalletFile,
        required = true
      ),
      validateOutputfile(
        paramConfig.someOutputFile,
        required = true
      ),
      validateNonEmpty("Party name", paramConfig.partyName),
      validateNonEmpty("Contract name", paramConfig.contractName),
      validateNonEmpty("Password", paramConfig.password)
    ).sequence.map(_ => paramConfig)
  }

}
