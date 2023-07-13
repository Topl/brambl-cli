package co.topl.brambl.cli.controllers

import cats.Monad
import cats.data.Validated
import co.topl.brambl.cli.impl.SimpleTransactionAlgebra
import co.topl.brambl.dataApi.WalletStateAlgebra
import co.topl.brambl.models.LockAddress

class SimpleTransactionController[F[_]: Monad](
    walletStateAlgebra: WalletStateAlgebra[F],
    simplTransactionOps: SimpleTransactionAlgebra[F]
) {

  def broadcastSimpleTransactionFromParams(
      provedTxFile: String
  ): F[Either[String, String]] = {
    simplTransactionOps.broadcastSimpleTransactionFromParams(
      provedTxFile
    )
  }

  def proveSimpleTransactionFromParams(
      fromParty: String,
      fromContract: String,
      someFromState: Option[Int],
      inputFile: String,
      keyFile: String,
      password: String,
      outputFile: String
  ): F[Either[String, String]] = {
    import cats.implicits._
    walletStateAlgebra
      .validateCurrentIndicesForFunds(
        fromParty,
        fromContract,
        someFromState
      ) flatMap {
      case Validated.Invalid(errors) =>
        Monad[F].pure(
          Left(
            "Invalid params" + "\n" +
              errors.toList.mkString(", ")
          )
        )
      case Validated.Valid(_) =>
        simplTransactionOps
          .proveSimpleTransactionFromParams(
            inputFile,
            keyFile,
            password,
            outputFile
          )
          .map(_ => Right("Transaction successfully proved"))
    }
  }

  def createSimpleTransactionFromParams(
      keyfile: String,
      password: String,
      fromParty: String,
      fromContract: String,
      someFromState: Option[Int],
      someToAddress: Option[LockAddress],
      someToParty: Option[String],
      someToContract: Option[String],
      amount: Long,
      outputFile: String
  ): F[Either[String, String]] = {
    import cats.implicits._
    walletStateAlgebra
      .validateCurrentIndicesForFunds(
        fromParty,
        fromContract,
        someFromState
      ) flatMap {
      case Validated.Invalid(errors) =>
        Monad[F].point(Left("Invalid params\n" + errors.toList.mkString(", ")))
      case Validated.Valid(_) =>
        simplTransactionOps
          .createSimpleTransactionFromParams(
            keyfile,
            password,
            fromParty,
            fromContract,
            someFromState,
            someToAddress,
            someToParty,
            someToContract,
            amount,
            outputFile
          )
    }
  }
}
