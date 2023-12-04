package co.topl.brambl.cli.impl

import cats.effect.kernel.Resource
import cats.effect.kernel.Sync
import co.topl.brambl.models.box.{
  AssetMintingStatement => PBAssetMintingStatement
}
import io.circe.Json
import com.google.protobuf.struct.Value

import scala.io.BufferedSource

case class AssetMintingStatement(
    groupTokenUtxo: String,
    seriesTokenUtxo: String,
    quantity: Long,
    permanentMetadata: Option[Json]
)

trait AssetMintingStatementParser[F[_]] {
  def parseAssetMintingStatement(
      inputFileRes: Resource[F, BufferedSource]
  ): F[Either[CommonParserError, PBAssetMintingStatement]]
}

object AssetMintingStatementParser {

  def make[F[_]: Sync](networkId: Int) = new AssetMintingStatementParser[F]
    with CommonTxOps {

    import cats.implicits._
    import io.circe.generic.auto._
    import io.circe.yaml
    import co.topl.brambl.syntax._

    private def assetMintingStatementToPBAMS(
        assetMintingStatement: AssetMintingStatement
    ): F[PBAssetMintingStatement] = for {
      groupTokenUtxo <- Sync[F].fromEither(
        CommonParsingOps.parseTransactionOuputAddress(
          networkId,
          assetMintingStatement.groupTokenUtxo
        )
      )
      seriesTokenUtxo <- Sync[F].fromEither(
        CommonParsingOps.parseTransactionOuputAddress(
          networkId,
          assetMintingStatement.seriesTokenUtxo
        )
      )
      permanentMetadata <- Sync[F].delay(
        assetMintingStatement.permanentMetadata.map(
          toStruct(_).kind match {
            case Value.Kind.StructValue(struct) => struct
            case _ =>
              throw InvalidMetadataScheme(
                "Invalid permanent metadata: " + assetMintingStatement.permanentMetadata
              )
          }
        )
      )
    } yield PBAssetMintingStatement(
      groupTokenUtxo = groupTokenUtxo,
      seriesTokenUtxo = seriesTokenUtxo,
      quantity = assetMintingStatement.quantity,
      permanentMetadata = permanentMetadata
    )

    override def parseAssetMintingStatement(
        inputFileRes: Resource[F, BufferedSource]
    ): F[Either[CommonParserError, PBAssetMintingStatement]] = (for {
      inputString <- inputFileRes.use(file =>
        Sync[F].blocking(file.getLines().mkString("\n"))
      )
      assetMintingStatement <-
        Sync[F].fromEither(
          yaml.v12.parser
            .parse(inputString)
            .flatMap(tx => tx.as[AssetMintingStatement])
            .leftMap { e =>
              InvalidYaml(e)
            }
        )
      ams <- assetMintingStatementToPBAMS(assetMintingStatement)
    } yield ams).attempt.map(_ match {
      case Right(value)               => Right(value)
      case Left(e: CommonParserError) => Left(e)
      case Left(e)                    => Left(InvalidYaml(e))
    })

  }

}
