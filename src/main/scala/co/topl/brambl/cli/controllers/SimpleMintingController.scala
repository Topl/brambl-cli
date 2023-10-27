package co.topl.brambl.cli.controllers

import cats.effect.kernel.Resource
import cats.effect.kernel.Sync
import co.topl.brambl.cli.impl.AssetMintingStatementParser
import co.topl.brambl.cli.impl.CreateTxError
import co.topl.brambl.cli.impl.GroupPolicyParser
import co.topl.brambl.cli.impl.SeriesPolicyParser
import co.topl.brambl.cli.impl.SimpleMintingAlgebra
import co.topl.brambl.cli.impl.SimpleTransactionAlgebraError
import co.topl.brambl.utils.Encoding
import com.google.protobuf.ByteString

import java.io.File

class SimpleMintingController[F[_]: Sync](
    groupPolicyParserAlgebra: GroupPolicyParser[F],
    seriesPolicyParserAlgebra: SeriesPolicyParser[F],
    assetMintingStatementParserAlgebra: AssetMintingStatementParser[F],
    simpleMintingOps: SimpleMintingAlgebra[F]
) {

  import cats.implicits._

  def createSimpleGroupMintingTransactionFromParams(
      inputFile: String,
      keyFile: String,
      password: String,
      fromFellowship: String,
      fromTemplate: String,
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
          fromFellowship,
          fromTemplate,
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
      fromFellowship: String,
      fromTemplate: String,
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
          fromFellowship,
          fromTemplate,
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

  private def loadJsonMetadata(someEphemeralMetadata: Option[File]) =
    (someEphemeralMetadata.map { ephemeralMetadata =>
      val inputRes = Resource.make(
        Sync[F]
          .delay(
            scala.io.Source.fromFile(ephemeralMetadata)
          )
      )(source => Sync[F].delay(source.close()))
      for {
        jsonInput <- inputRes.use(source =>
          Sync[F].delay(source.getLines.mkString)
        )
        json <- Sync[F].fromEither {
          import io.circe.parser.parse
          parse(jsonInput).leftMap(e => CreateTxError(e.toString()))
        }
      } yield json
    }).sequence

  def createSimpleAssetMintingTransactionFromParams(
      inputFile: String,
      keyFile: String,
      password: String,
      fromFellowship: String,
      fromTemplate: String,
      someFromState: Option[Int],
      fee: Long,
      ephemeralMetadata: Option[File],
      someCommitment: Option[String],
      outputFile: String
  ): F[Either[String, String]] =
    (for {
      ams <- assetMintingStatementParserAlgebra
        .parseAssetMintingStatement(
          Resource.make(
            Sync[F].delay(scala.io.Source.fromFile(inputFile))
          )(source => Sync[F].delay(source.close()))
        )
        .map(
          _.leftMap(e =>
            CreateTxError(
              "Error parsing asset minting statement: " + e.description
            )
          )
        )
      json <- loadJsonMetadata(ephemeralMetadata)
      eitherCommitment = someCommitment.map(x =>
        Sync[F].fromEither(Encoding.decodeFromHex(x))
      )
      commitment <- eitherCommitment.map(Some(_)).getOrElse(None).sequence
      statement <- Sync[F].fromEither(ams)
      _ <- simpleMintingOps
        .createSimpleAssetMintingTransactionFromParams(
          keyFile,
          password,
          fromFellowship,
          fromTemplate,
          someFromState,
          fee,
          outputFile,
          json,
          commitment.map(x => ByteString.copyFrom(x)),
          statement
        )
    } yield ()).attempt
      .map(_ match {
        case Right(_) => Right("Transaction successfully created")
        case Left(value: SimpleTransactionAlgebraError) =>
          Left(value.description)
        case Left(e) => Left(e.toString())
      })

}
