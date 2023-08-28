package co.topl.brambl.cli.validation

import cats.data.NonEmptyList
import cats.data.ValidatedNel
import cats.implicits.catsSyntaxValidatedId
import cats.syntax.either._
import co.topl.brambl.cli.BramblCliParams
import co.topl.brambl.codecs.AddressCodecs
import co.topl.brambl.models.LockAddress
import co.topl.brambl.utils.EncodingError

trait SimpleTransactionValidationModule {
  self: CommonValidationModule =>

  def validateAddress(
      someAddress: Option[String]
  ): ValidatedNel[String, LockAddress] = {
    someAddress
      .map(address =>
        AddressCodecs
          .decodeAddress(address)
          .toValidatedNel
          .bimap(
            (x: NonEmptyList[EncodingError]) =>
              x.map(y => s"Invalid address: ${y.getMessage()}"),
            x => identity(x)
          )
      )
      .getOrElse("Address is required".invalidNel)
  }

  def validateAddressOrCoordinates(
      someAddress: Option[String],
      someToParty: Option[String],
      someToContract: Option[String]
  ): ValidatedNel[String, Unit] =
    (someAddress, someToParty, someToContract) match {
      case (Some(addr), None, None) =>
        validateAddress(Some(addr)).map(_ => ())
      case (None, Some(_), Some(_)) =>
        ().validNel
      case _ =>
        "Exactly toParty and toContract together or only toAddress must be specified".invalidNel
    }

  def validateHeight(height: Long) = {
    import cats.implicits._
    if (height >= 0) {
      height.validNel
    } else {
      "Height must be greater or equal to zero.".invalidNel
    }
  }

  def validateFromCoordinates(
      someFromParty: Option[String],
      someFromContract: Option[String],
      someFromState: Option[String]
  ) = {
    import cats.implicits._
    if (someFromParty.map(_ == "noparty").getOrElse(false)) {
      if (someFromState.isEmpty) {
        "You must specify a from-state when using noparty".invalidNel
      } else {
        (someFromParty, someFromContract, someFromState).validNel
      }
    } else {
      (someFromParty, someFromContract, someFromState).validNel
    }
  }

  def validateSimpleTransactionCreateParams(
      paramConfig: BramblCliParams
  ): ValidatedNel[String, BramblCliParams] = {
    import cats.implicits._
    List(
      validateAddressOrCoordinates(
        paramConfig.toAddress,
        paramConfig.someToParty,
        paramConfig.someToContract
      ),
      validateHost(paramConfig.host),
      validateFromCoordinates(
        paramConfig.someFromParty,
        paramConfig.someFromContract,
        paramConfig.someFromState
      ),
      validateOutputfile(paramConfig.someOutputFile, required = true),
      validateInputFile("Key file", paramConfig.someKeyFile, required = true)
    ).sequence.map(_ => paramConfig)
  }

  def validateSimpleTransactionProveParams(
      paramConfig: BramblCliParams
  ): ValidatedNel[String, BramblCliParams] = {
    import cats.implicits._
    List(
      validateOutputfile(paramConfig.someOutputFile, required = true),
      validateInputFile("Key file", paramConfig.someKeyFile, required = true),
      validateInputFile(
        "Transaction file",
        paramConfig.someInputFile,
        required = true
      )
    ).sequence.map(_ => paramConfig)
  }

  def validateSimpleTransactionBroadcastParams(
      paramConfig: BramblCliParams
  ): ValidatedNel[String, BramblCliParams] = {
    import cats.implicits._
    List(
      validateHost(paramConfig.host),
      validateInputFile(
        "Transaction file",
        paramConfig.someInputFile,
        required = true
      )
    ).sequence.map(_ => paramConfig)
  }

  def validateUtxoQueryParams(
      paramConfig: BramblCliParams
  ): ValidatedNel[String, BramblCliParams] = {
    import cats.implicits._
    List(
      validateHost(paramConfig.host),
      validateFromCoordinates(
        paramConfig.someFromParty,
        paramConfig.someFromContract,
        paramConfig.someFromState
      )
    ).sequence.map(_ => paramConfig)
  }
  def validateBlockByHeightQueryParams(
      paramConfig: BramblCliParams
  ): ValidatedNel[String, BramblCliParams] = {
    import cats.implicits._
    List(
      validateHeight(paramConfig.height)
    ).sequence.map(_ => paramConfig)
  }

}
