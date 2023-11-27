package co.topl.brambl.cli.impl

import cats.effect.kernel.Sync
import cats.effect.kernel.Resource
import scala.io.BufferedSource
import co.topl.brambl.models.Event
import co.topl.brambl.utils.Encoding
import com.google.protobuf.ByteString
import co.topl.brambl.models.SeriesId

case class GroupPolicy(
    label: String,
    fixedSeries: Option[String],
    registrationUtxo: String
)

trait GroupPolicyParser[F[_]] {
  def parseGroupPolicy(
      inputFileRes: Resource[F, BufferedSource]
  ): F[Either[CommonParserError, Event.GroupPolicy]]
}

object GroupPolicyParser {

  def make[F[_]: Sync](
      networkId: Int
  ): GroupPolicyParser[F] = new GroupPolicyParser[F] {
    import cats.implicits._
    import io.circe.generic.auto._
    import io.circe.yaml

    private def groupPolicyToPBGroupPolicy(
        groupPolicy: GroupPolicy
    ): F[Event.GroupPolicy] =
      for {
        label <-
          Sync[F].delay(
            groupPolicy.label
          )
        registrationUtxo <- Sync[F].fromEither(
          CommonParsingOps.parseTransactionOuputAddress(
            networkId,
            groupPolicy.registrationUtxo
          )
        )
        someSeriesId <-
          groupPolicy.fixedSeries
            .map(s =>
              Sync[F].fromEither(
                Encoding
                  .decodeFromHex(s)
                  .leftMap(_ => InvalidHex("Invalid hex string: " + s))
              )
            )
            .sequence
        _ <- someSeriesId
          .map(seriesId =>
            if (seriesId.length != 32)
              Sync[F].raiseError(
                InvalidHex(
                  "The hex string for the series must be 32 bytes long"
                )
              )
            else Sync[F].point(())
          )
          .getOrElse(Sync[F].point(()))
      } yield Event.GroupPolicy(
        label,
        registrationUtxo,
        someSeriesId.map(s => SeriesId(ByteString.copyFrom(s)))
      )

    def parseGroupPolicy(
        inputFileRes: Resource[F, BufferedSource]
    ): F[Either[CommonParserError, Event.GroupPolicy]] = (for {
      inputString <- inputFileRes.use(file =>
        Sync[F].blocking(file.getLines().mkString("\n"))
      )
      groupPolicy <-
        Sync[F].fromEither(
          yaml.v12.parser
            .parse(inputString)
            .flatMap(tx => tx.as[GroupPolicy])
            .leftMap { e =>
              InvalidYaml(e)
            }
        )
      gp <- groupPolicyToPBGroupPolicy(groupPolicy)
    } yield gp).attempt.map(_ match {
      case Right(tx)                  => tx.asRight[CommonParserError]
      case Left(e: CommonParserError) => e.asLeft[Event.GroupPolicy]
      case Left(e) => UnknownError(e).asLeft[Event.GroupPolicy]
    })

  }

}
