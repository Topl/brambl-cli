package co.topl.brambl.cli.validation

import cats.data.Validated
import co.topl.utils.NetworkType
import co.topl.brambl.cli.BramblCliParams
import scala.util.Try
import co.topl.utils.StringDataTypes

trait TransactionValidationModule extends CommonValidationModule {

  private def validateToAddresses(
      toAddresses: Map[String, Int]
  )(implicit networkPrefix: NetworkType.NetworkPrefix) = {
    if (toAddresses.isEmpty)
      Validated.invalidNel(
        "At least one to address is required"
      )
    else {
      import cats.implicits._
      toAddresses
        .map(x => validateAddress(x._1).map(y => (y, x._2)))
        .toList
        .sequence
    }
  }

  private def validateFromAddresses(
      fromAddresses: Seq[String]
  )(implicit networkPrefix: NetworkType.NetworkPrefix) = {
    if (fromAddresses.isEmpty)
      Validated.invalidNel(
        "At least one from address is required"
      )
    else {
      import cats.implicits._
      fromAddresses.map(x => validateAddress(x)).sequence
    }
  }

  def validatTransactionCreate(
      paramConfig: BramblCliParams
  )(implicit networkPrefix: NetworkType.NetworkPrefix) = {
    (
      validateToplNetworkUri(paramConfig.someNetworkUri.getOrElse("")),
      validateTokenType(paramConfig.someToken.getOrElse("")),
      validateFromAddresses(paramConfig.fromAddresses),
      validateToAddresses(paramConfig.toAddresses),
      validateAddress(paramConfig.changeAddress),
      validateFee(paramConfig.fee)
    )
  }

  private def validateAddress(
      address: String
  )(implicit networkPrefix: NetworkType.NetworkPrefix) = {
    import co.topl.attestation.AddressCodec.implicits._
    import co.topl.utils.IdiomaticScalaTransition.implicits.toValidatedOps
    Try(
      StringDataTypes.Base58Data.unsafe(address).decodeAddress.getOrThrow()
    ).toOption match {
      case Some(_) => Validated.validNel(address)
      case None    => Validated.invalidNel("Invalid address: " + address)
    }
  }

  private def validateFee(fee: Int) = {
    if (fee < 0)
      Validated.invalidNel(
        "Fee must be greater than or equal to 0"
      )
    else
      Validated.validNel(fee)
  }

  def validateTransactionBroadcast(paramConfig: BramblCliParams) = {
    import cats.implicits._
    (
      paramConfig.someInputFile
        .map(validateFileExists("input file", _))
        .sequence
    )
  }
  
  def validateWalletBalance(
      paramConfig: BramblCliParams
  )(implicit networkPrefix: NetworkType.NetworkPrefix) = {
    (
      validateFromAddresses(paramConfig.fromAddresses)
    )
  }

}
