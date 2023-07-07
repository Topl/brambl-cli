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
  ): F[String] = {
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
  ): F[String] = {
    import cats.implicits._
    walletStateAlgebra
      .validateCurrentIndicesForFunds(
        fromParty,
        fromContract,
        someFromState
      ) flatMap {
      case Validated.Invalid(errors) =>
        Monad[F].pure(
          "Invalid params" + "\n" +
            errors.toList.mkString(", ")
        )
      case Validated.Valid(_) =>
        simplTransactionOps
          .proveSimpleTransactionFromParams(
            inputFile,
            keyFile,
            password,
            outputFile
          )
          .map(_ => "Transaction successfully proved")
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
  ): F[String] = {
    import cats.implicits._
    walletStateAlgebra
      .validateCurrentIndicesForFunds(
        fromParty,
        fromContract,
        someFromState
      ) flatMap {
      case Validated.Invalid(errors) =>
        Monad[F].point("Invalid params\n" + errors.toList.mkString(", "))
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
          .map(_ => "Transaction successfully created")
    }
  }
}
