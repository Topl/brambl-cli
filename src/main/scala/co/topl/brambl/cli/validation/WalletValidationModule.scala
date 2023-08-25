package co.topl.brambl.cli.validation

import cats.data.ValidatedNel
import co.topl.brambl.cli.BramblCliParams
import co.topl.brambl.utils.Encoding
import cats.data.Validated

trait WalletValidationModule {

  self: CommonValidationModule =>

  def validateKeyGenerationParams(
      paramConfig: BramblCliParams
  ): ValidatedNel[String, BramblCliParams] = {
    import cats.implicits._
    List(
      validateNonEmpty("Password", paramConfig.password),
      validatePassphrase(paramConfig.somePassphrase),
      validateOutputfile(paramConfig.someMnemonicFile, required = true)
    ).sequence.map(_ => paramConfig)
  }
 def validateKeyRecoveryParams(
      paramConfig: BramblCliParams
  ): ValidatedNel[String, BramblCliParams] = {
    import cats.implicits._
    List(
      validateNonEmpty("Password", paramConfig.password),
      validateMnemonic(paramConfig.mnemonic),
      validatePassphrase(paramConfig.somePassphrase)
    ).sequence.map(_ => paramConfig)
  }

  def validateInputKey(
      someInputFile: Option[String],
      required: Boolean
  ): ValidatedNel[String, Option[String]] = {
    import cats.implicits._
    someInputFile match {
      case Some(inputFile) =>
        if (inputFile.trim().length >= 0) {
          // check if the file exists
          if (new java.io.File(inputFile).exists) {
            Encoding.decodeFromBase58(
              scala.io.Source.fromFile(inputFile).getLines().mkString("")
            ) match {
              case Left(_) =>
                "Invalid key file".invalidNel
              case Right(_) =>
                Some(inputFile).validNel
            }
          } else {
            Validated.invalidNel(
              inputFile + " does not exist"
            )
          }
        } else {
          Validated.invalidNel(
            inputFile + " must not be empty"
          )
        }
      case None =>
        if (required) Validated.invalidNel("Key file is required")
        else Validated.validNel(None)
    }
  }

  def validateImportVksParam(
      paramConfig: BramblCliParams
  ): ValidatedNel[String, BramblCliParams] = {
    import cats.implicits._
    (paramConfig.inputVks.map(x =>
      validateInputFile(
        s"Input file $x",
        Some(x),
        required = true
      )
    ) ++ List(
      validateInputFile(
        "Keyfile",
        paramConfig.someKeyFile,
        required = true
      ),
      validateNonEmpty("Party name", paramConfig.partyName),
      validateNonEmpty("Contract name", paramConfig.contractName),
      validateNonEmpty("Password", paramConfig.password)
    )).sequence.map(_ => paramConfig)
  }

  def validateSyncParam(
      paramConfig: BramblCliParams
  ): ValidatedNel[String, BramblCliParams] = {
    import cats.implicits._
    (
      List(
        validateHost(paramConfig.host),
        validateNonEmpty("Party name", paramConfig.partyName),
        validateNonEmpty("Contract name", paramConfig.contractName)
      )
    ).sequence.map(_ => paramConfig)
  }

  def validateTxCreateParam(
      paramConfig: BramblCliParams
  ): ValidatedNel[String, BramblCliParams] = {
    import cats.implicits._
    List(
      validateInputFile(
        "Input transaction",
        paramConfig.someInputFile,
        required = true
      ),
      validateOutputfile(
        paramConfig.someOutputFile,
        required = true
      )
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
