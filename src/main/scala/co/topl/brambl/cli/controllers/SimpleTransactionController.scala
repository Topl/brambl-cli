package co.topl.brambl.cli.controllers

import cats.Monad
import cats.data.Validated
import cats.effect.kernel.Resource
import cats.effect.kernel.Sync
import co.topl.brambl.cli.impl.SimpleTransactionAlgebra
import co.topl.brambl.dataApi.WalletStateAlgebra
import co.topl.brambl.models.LockAddress

import java.io.FileInputStream
import java.io.FileOutputStream

class SimpleTransactionController[F[_]: Sync](
    walletStateAlgebra: WalletStateAlgebra[F],
    simplTransactionOps: SimpleTransactionAlgebra[F]
) {

  def broadcastSimpleTransactionFromParams(
      provedTxFile: String
  ): F[Either[String, String]] = {
    import cats.implicits._
    simplTransactionOps
      .broadcastSimpleTransactionFromParams(
        provedTxFile
      )
      .map(_ match {
        case Right(_)    => Right("Transaction broadcasted")
        case Left(value) => Left(value.description)
      })
  }

  def proveSimpleTransactionFromParams(
      inputFile: String,
      keyFile: String,
      password: String,
      outputFile: String
  ): F[Either[String, String]] = {
    import cats.implicits._
    val inputRes = Resource
      .make {
        Sync[F].delay(new FileInputStream(inputFile))
      }(fos => Sync[F].delay(fos.close()))

    val outputRes = Resource
      .make(
        Sync[F].delay(new FileOutputStream(outputFile))
      )(fos => Sync[F].delay(fos.close()))

    simplTransactionOps
      .proveSimpleTransactionFromParams(
        inputRes,
        keyFile,
        password,
        outputRes
      )
      .map(_ match {
        case Right(_)    => Right("Transaction successfully proved")
        case Left(value) => Left(value.description)
      })
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
          .map(_ match {
            case Right(_)    => Right("Transaction successfully created")
            case Left(value) => Left(value.description)
          })
    }
  }
}
