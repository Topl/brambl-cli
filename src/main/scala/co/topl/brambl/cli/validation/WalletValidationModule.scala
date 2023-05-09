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
      validatePassword(paramConfig.password),
      validatePassphrase(paramConfig.somePassphrase)
    ).sequence.map(_ => paramConfig)
  }

}
