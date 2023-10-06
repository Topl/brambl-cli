package co.topl.brambl.cli.impl

import cats.Monad
import co.topl.brambl.models.GroupId
import co.topl.brambl.models.LockAddress
import co.topl.brambl.models.SeriesId
import co.topl.brambl.models.box
import co.topl.brambl.models.transaction.UnspentTransactionOutput
import co.topl.genus.services.Txo
import com.google.protobuf.struct.ListValue
import com.google.protobuf.struct.NullValue
import com.google.protobuf.struct.Struct
import com.google.protobuf.struct.Value
import io.circe.Json
import quivr.models.Int128
import com.google.protobuf.ByteString

trait CommonTxOps {

  import cats.implicits._

  def groupOutput[F[_]: Monad](
      lockAddress: LockAddress,
      quantity: Int128,
      groupId: GroupId,
      fixedSeries: Option[SeriesId]
  ): F[UnspentTransactionOutput] =
    UnspentTransactionOutput(
      lockAddress,
      box.Value.defaultInstance.withGroup(
        box.Value.Group(
          groupId = groupId,
          quantity = quantity,
          fixedSeries = fixedSeries
        )
      )
    ).pure[F]

  def seriesOutput[F[_]: Monad](
      lockAddress: LockAddress,
      quantity: Int128,
      seriesId: SeriesId,
      tokenSupply: Option[Int],
      quantityDescriptor: box.QuantityDescriptorType,
      fungibility: box.FungibilityType
  ): F[UnspentTransactionOutput] =
    UnspentTransactionOutput(
      lockAddress,
      box.Value.defaultInstance.withSeries(
        box.Value.Series(
          seriesId = seriesId,
          quantity = quantity,
          tokenSupply = tokenSupply,
          quantityDescriptor = quantityDescriptor,
          fungibility = fungibility
        )
      )
    ).pure[F]

  def assetOutput[F[_]: Monad](
      lockAddress: LockAddress,
      quantity: Int128,
      groupId: GroupId,
      seriesId: SeriesId,
      quantityDescriptor: box.QuantityDescriptorType,
      fungibility: box.FungibilityType,
      ephemeralMetadata: Option[Struct],
      commitment: Option[ByteString]
  ): F[UnspentTransactionOutput] =
    UnspentTransactionOutput(
      lockAddress,
      box.Value.defaultInstance.withAsset(
        box.Value.Asset(
          groupId = Some(groupId),
          seriesId = Some(seriesId),
          quantity = quantity,
          quantityDescriptor = quantityDescriptor,
          fungibility = fungibility,
          ephemeralMetadata = ephemeralMetadata,
          commitment = commitment
        )
      )
    ).pure[F]

  def computeLvlQuantity(lvlTxos: Seq[Txo]) = lvlTxos
    .foldLeft(
      BigInt(0)
    )((acc, x) =>
      acc + x.transactionOutput.value.value.lvl
        .map(y => BigInt(y.quantity.value.toByteArray))
        .getOrElse(BigInt(0))
    )

  def toStruct(json: Json): Value =
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

}
