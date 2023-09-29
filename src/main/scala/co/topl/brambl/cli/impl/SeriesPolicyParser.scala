package co.topl.brambl.cli.impl

import cats.effect.kernel.Resource
import cats.effect.kernel.Sync
import co.topl.brambl.models.Event
import co.topl.brambl.models.box.FungibilityType
import co.topl.brambl.models.box.QuantityDescriptorType
import com.google.protobuf.struct.ListValue
import com.google.protobuf.struct.NullValue
import com.google.protobuf.struct.Struct
import com.google.protobuf.struct.Value
import io.circe.Json

import scala.io.BufferedSource

case class SeriesPolicy(
    label: String,
    tokenSupply: Option[Int],
    registrationUtxo: String,
    fungibility: String,
    quantityDescriptor: String,
    ephemeralMetadataScheme: Option[Json],
    permanentMetadataScheme: Option[Json]
)

trait SeriesPolicyParser[F[_]] {
  def parseSeriesPolicy(
      inputFileRes: Resource[F, BufferedSource]
  ): F[Either[CommonParserError, Event.SeriesPolicy]]
}

object SeriesPolicyParser {

  def make[F[_]: Sync](
      networkId: Int
  ) = new SeriesPolicyParser[F] {

    import cats.implicits._
    import io.circe.generic.auto._
    import io.circe.yaml

    private def toStruct(json: Json): Value =
      json.fold[Value](
        jsonNull = Value(Value.Kind.NullValue(NullValue.NULL_VALUE)),
        jsonBoolean = b => Value(Value.Kind.BoolValue(b)),
        jsonNumber = n => Value(Value.Kind.NumberValue(n.toDouble)),
        jsonString = s => Value(Value.Kind.StringValue(s)),
        jsonArray =
          l => Value(Value.Kind.ListValue(ListValue(l.map(toStruct(_))))),
        jsonObject = jo =>
          Value(Value.Kind.StructValue(Struct(jo.toMap.map({ case (k, v) =>
            k -> toStruct(v)
          }))))
      )

    private def seriesPolicyToPBGroupPolicy(
        seriesPolicy: SeriesPolicy
    ): F[Event.SeriesPolicy] =
      for {
        label <-
          Sync[F].delay(
            seriesPolicy.label
          )
        someTokenSupply <- Sync[F].delay(seriesPolicy.tokenSupply)
        registrationUtxo <- Sync[F].fromEither(
          CommonParsingOps.parseTransactionOuputAddress(
            networkId,
            seriesPolicy.registrationUtxo
          )
        )
        fungibility <- Sync[F].delay(
          seriesPolicy.fungibility match {
            case "group"            => FungibilityType.GROUP
            case "series"           => FungibilityType.SERIES
            case "group-and-series" => FungibilityType.GROUP_AND_SERIES
            case _ =>
              throw InvalidFungibility(
                "Invalid fungibility: " + seriesPolicy.fungibility
              )
          }
        )
        quantityDescriptor <- Sync[F].delay(
          seriesPolicy.quantityDescriptor match {
            case "liquid"       => QuantityDescriptorType.LIQUID
            case "accumulator"  => QuantityDescriptorType.ACCUMULATOR
            case "fractionable" => QuantityDescriptorType.FRACTIONABLE
            case "immutable"    => QuantityDescriptorType.IMMUTABLE
            case _ =>
              throw InvalidQuantityDescriptor(
                "Invalid quantity descriptor: " + seriesPolicy.quantityDescriptor
              )
          }
        )
        ephemeralMetadataScheme <- Sync[F].delay(
          seriesPolicy.ephemeralMetadataScheme.map(toStruct(_).kind match {
            case Value.Kind.StructValue(struct) => struct
            case _ =>
              throw InvalidMetadataScheme(
                "Invalid ephemeral metadata scheme: " + seriesPolicy.ephemeralMetadataScheme
              )
          })
        )
        permanentMetadataScheme <- Sync[F].delay(
          seriesPolicy.permanentMetadataScheme.map(toStruct(_).kind match {
            case Value.Kind.StructValue(struct) => struct
            case _ =>
              throw InvalidMetadataScheme(
                "Invalid permanent metadata scheme: " + seriesPolicy.ephemeralMetadataScheme
              )
          })
        )
      } yield {
        Event.SeriesPolicy(
          label,
          someTokenSupply,
          registrationUtxo,
          quantityDescriptor,
          fungibility,
          ephemeralMetadataScheme,
          permanentMetadataScheme
        )
      }

    override def parseSeriesPolicy(
        inputFileRes: Resource[F, BufferedSource]
    ): F[Either[CommonParserError, Event.SeriesPolicy]] = (for {
      inputString <- inputFileRes.use(file =>
        Sync[F].blocking(file.getLines().mkString("\n"))
      )
      seriesPolicy <-
        Sync[F].fromEither(
          yaml.v12.parser
            .parse(inputString)
            .flatMap(tx => tx.as[SeriesPolicy])
            .leftMap { e =>
              InvalidYaml(e)
            }
        )
      sp <- seriesPolicyToPBGroupPolicy(seriesPolicy)
    } yield sp).attempt.map(_ match {
      case Right(value)               => Right(value)
      case Left(e: CommonParserError) => Left(e)
      case Left(e)                    => Left(InvalidYaml(e))
    })

  }

}
