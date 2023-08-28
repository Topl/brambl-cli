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


  def validateSimpleTransactionCreateParams(
      paramConfig: BramblCliParams
  ): ValidatedNel[String, BramblCliParams] = {
    import cats.implicits._
    List(
      validateAddressOrCoordinates(
        paramConfig.toAddress,
        paramConfig.someToParty,
        paramConfig.someToContract
      )
    ).sequence.map(_ => paramConfig)
  }

}
