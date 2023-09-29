package co.topl.brambl.cli.controllers

import cats.effect.kernel.Resource
import cats.effect.kernel.Sync
import co.topl.brambl.cli.impl.CreateTxError
import co.topl.brambl.cli.impl.GroupPolicyParser
import co.topl.brambl.cli.impl.SeriesPolicyParser
import co.topl.brambl.cli.impl.SimpleMintingAlgebra
import co.topl.brambl.cli.impl.SimpleTransactionAlgebraError

class SimpleMintingController[F[_]: Sync](
    groupPolicyParserAlgebra: GroupPolicyParser[F],
    seriesPolicyParserAlgebra: SeriesPolicyParser[F],
    simpleMintingOps: SimpleMintingAlgebra[F]
) {

  import cats.implicits._

  def createSimpleGroupMintingTransactionFromParams(
      inputFile: String,
      keyFile: String,
      password: String,
      fromParty: String,
      fromContract: String,
      someFromState: Option[Int],
      amount: Long,
      fee: Long,
      outputFile: String
  ): F[Either[String, String]] =
    (for {
      gp <- groupPolicyParserAlgebra
        .parseGroupPolicy(
          Resource.make(
            Sync[F].delay(scala.io.Source.fromFile(inputFile))
          )(source => Sync[F].delay(source.close()))
        )
        .map(
          _.leftMap(e =>
            CreateTxError("Error parsing group policy: " + e.description)
          )
        )
      policy <- Sync[F].fromEither(gp)
      _ <- simpleMintingOps
        .createSimpleGroupMintingTransactionFromParams(
          keyFile,
          password,
          fromParty,
          fromContract,
          someFromState,
          amount,
          fee,
          outputFile,
          policy
        )
    } yield ()).attempt
      .map(_ match {
        case Right(_) => Right("Transaction successfully created")
        case Left(value: SimpleTransactionAlgebraError) =>
          Left(value.description)
        case Left(e) => Left(e.toString())
      })

  def createSimpleSeriesMintingTransactionFromParams(
      inputFile: String,
      keyFile: String,
      password: String,
      fromParty: String,
      fromContract: String,
      someFromState: Option[Int],
      amount: Long,
      fee: Long,
      outputFile: String
  ): F[Either[String, String]] =
    (for {
      gp <- seriesPolicyParserAlgebra
        .parseSeriesPolicy(
          Resource.make(
            Sync[F].delay(scala.io.Source.fromFile(inputFile))
          )(source => Sync[F].delay(source.close()))
        )
        .map(
          _.leftMap(e =>
            CreateTxError("Error parsing series policy: " + e.description)
          )
        )
      policy <- Sync[F].fromEither(gp)
      _ <- simpleMintingOps
        .createSimpleSeriesMintingTransactionFromParams(
          keyFile,
          password,
          fromParty,
          fromContract,
          someFromState,
          amount,
          fee,
          outputFile,
          policy
        )
    } yield ()).attempt
      .map(_ match {
        case Right(_) => Right("Transaction successfully created")
        case Left(value: SimpleTransactionAlgebraError) =>
          Left(value.description)
        case Left(e) => Left(e.toString())
      })

}
