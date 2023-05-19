package co.topl.brambl.cli.validation

import cats.data.NonEmptyList
import cats.data.ValidatedNel
import co.topl.brambl.cli.BramblCliParams
import co.topl.brambl.codecs.AddressCodecs
import co.topl.brambl.models.LockAddress
import co.topl.brambl.utils.EncodingError
import co.topl.brambl.utils.Encoding
import co.topl.consensus.models.BlockId
import com.google.protobuf.ByteString

trait SimpleTransactionValidationModule {
  self: CommonValidationModule =>

  def validateNoAddress(
      someAddress: Option[String]
  ): ValidatedNel[String, Unit] = {
    import cats.implicits._
    if (someAddress.isDefined) {
      s"Address is not required".invalidNel
    } else {
      ().validNel
    }
  }

  def validateAddress(
      someAddress: Option[String]
  ): ValidatedNel[String, LockAddress] = {
    import cats.syntax.either._
    import cats.syntax.validated._
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

  def validateAmount(amount: Long) = {
    import cats.implicits._
    if (amount > 0) {
      amount.validNel
    } else {
      "Amount must be greater than 0".invalidNel
    }
  }

  def validateNoAmount(amount: Long) = {
    import cats.implicits._
    if (amount == 0) {
      amount.validNel
    } else {
      "Amount is not required".invalidNel
    }
  }

  def validateHeight(height: Long) = {
    import cats.implicits._
    if (height >= 0) {
      height.validNel
    } else {
      "Height must be greater or equal to zero.".invalidNel
    }
  }
  def validateBlockId(blockId: Option[String]) = {
    import cats.implicits._
    blockId
      .map(bId => {
        Encoding
          .decodeFromBase58(bId)
          .left
          .map(x => s"Invalid blockId: ${x.getMessage()}")
          .toValidatedNel
      })
      .getOrElse(
        "BlockId is a required field".invalidNel
      )
  }
  def validateTransactionId(transactionId: Option[String]) = {
    import cats.implicits._
    transactionId
      .map(tId => {
        Encoding
          .decodeFromBase58(tId)
          .left
          .map(x => s"Invalid transaction id: ${x.getMessage()}")
          .toValidatedNel
      })
      .getOrElse(
        "TransactionId is a required field".invalidNel
      )
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
      validateAddress(paramConfig.toAddress),
      validateNoPassphrase(paramConfig.somePassphrase),
      validatePassword(paramConfig.password),
      validatePort(paramConfig.genusPort),
      validateHost(paramConfig.host),
      validateFromCoordinates(
        paramConfig.someFromParty,
        paramConfig.someFromContract,
        paramConfig.someFromState
      ),
      validateOutputfile(paramConfig.someOutputFile, required = true),
      validateInputFile("Key file", paramConfig.someKeyFile, required = true),
      validateAmount(paramConfig.amount)
    ).sequence.map(_ => paramConfig)
  }

  def validateSimpleTransactionProveParams(
      paramConfig: BramblCliParams
  ): ValidatedNel[String, BramblCliParams] = {
    import cats.implicits._
    List(
      validateNoPassphrase(paramConfig.somePassphrase),
      validatePassword(paramConfig.password),
      validateFromCoordinates(
        paramConfig.someFromParty,
        paramConfig.someFromContract,
        paramConfig.someFromState
      ),
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
      validateNoPassphrase(paramConfig.somePassphrase),
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
      validateNoAddress(paramConfig.toAddress),
      validateNoPassphrase(paramConfig.somePassphrase),
      validateNoPassword(paramConfig.password),
      validatePort(paramConfig.genusPort),
      validateHost(paramConfig.host),
      validateFromCoordinates(
        paramConfig.someFromParty,
        paramConfig.someFromContract,
        paramConfig.someFromState
      ),
      validateNoAmount(paramConfig.amount)
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
  def validateBlockByIdQueryParams(
      paramConfig: BramblCliParams
  ): ValidatedNel[String, BramblCliParams] = {
    import cats.implicits._
    List(
      validateBlockId(paramConfig.blockId)
    ).sequence.map(_ => paramConfig)
  }
  def validateTransactionByIdQueryParams(
      paramConfig: BramblCliParams
  ): ValidatedNel[String, BramblCliParams] = {
    import cats.implicits._
    List(
      validateTransactionId(paramConfig.transactionId)
    ).sequence.map(_ => paramConfig)
  }

}
