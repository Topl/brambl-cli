package co.topl.brambl.cli.impl

import cats.effect.kernel.Sync
import co.topl.brambl.codecs.AddressCodecs
import co.topl.brambl.dataApi
import co.topl.brambl.syntax.{AssetType, GroupType, LvlType, SeriesType}
import co.topl.brambl.utils.Encoding
import co.topl.genus.services.{Txo, TxoState}
import co.topl.shared.models._

case class WalletModeHelper[F[_]: Sync](
    walletStateAlgebra: dataApi.WalletStateAlgebra[F],
    genusQueryAlgebra: dataApi.GenusQueryAlgebra[F]
) {

  def getBalance(
      someAddress: Option[String],
      someFellowship: Option[String],
      someTemplate: Option[String],
      someInteraction: Option[Int]
  ) = {

    import cats.implicits._
    val addressGetter: F[Option[String]] =
      (someAddress, someFellowship, someTemplate) match {
        case (Some(address), None, None) =>
          Sync[F].delay(Some(address))
        case (None, Some(fellowship), Some(template)) =>
          Sync[F].defer(
            walletStateAlgebra.getAddress(
              fellowship,
              template,
              someInteraction
            )
          )
        case (_, _, _) =>
          Sync[F].raiseError(
            new Exception("Invalid arguments (should not happen)")
          )
      }
    (for {
      someAddress <- addressGetter
      _ <- Sync[F]
        .raiseError(
          new IllegalArgumentException(
            "Could not find address. Check the fellowship or template."
          )
        )
        .whenA(someAddress.isEmpty)
      balance <- someAddress
        .map(address =>
          genusQueryAlgebra
            .queryUtxo(
              AddressCodecs.decodeAddress(address).toOption.get,
              TxoState.UNSPENT
            )
        )
        .getOrElse(Sync[F].pure(Seq.empty[Txo]))
        .handleErrorWith(_ =>
          Sync[F].raiseError(
            new IllegalStateException(
              "Could not get UTXOs. Check the network connection."
            )
          )
        )
    } yield {
      val assetMap = balance.groupBy(x =>
        if (x.transactionOutput.value.value.isLvl)
          LvlType
        else if (x.transactionOutput.value.value.isGroup)
          GroupType(x.transactionOutput.value.value.group.get.groupId)
        else if (x.transactionOutput.value.value.isSeries)
          SeriesType(x.transactionOutput.value.value.series.get.seriesId)
        else if (x.transactionOutput.value.value.isAsset)
          AssetType(
            x.transactionOutput.value.value.asset.get.groupId.get.value,
            x.transactionOutput.value.value.asset.get.seriesId.get.value
          )
        else ()
      )
      val res = assetMap.map { e =>
        val (key, value) = e
        val result = value.foldl(BigInt(0))((a, c) => {
          a + (if (c.transactionOutput.value.value.isLvl)
                 BigInt(
                   c.transactionOutput.value.value.lvl.get.quantity.value.toByteArray
                 )
               else if (c.transactionOutput.value.value.isGroup)
                 BigInt(
                   c.transactionOutput.value.value.group.get.quantity.value.toByteArray
                 )
               else if (c.transactionOutput.value.value.isSeries)
                 BigInt(
                   c.transactionOutput.value.value.series.get.quantity.value.toByteArray
                 )
               else if (c.transactionOutput.value.value.isAsset)
                 BigInt(
                   c.transactionOutput.value.value.asset.get.quantity.value.toByteArray
                 )
               else BigInt(0))
        })

        val keyIdentifier: BalanceDTO = key match {
          case LvlType => LvlBalance(result.toString)
          case GroupType(groupId) =>
            GroupTokenBalanceDTO(
              Encoding.encodeToHex(groupId.value.toByteArray),
              result.toString
            )
          case SeriesType(seriesId) =>
            SeriesTokenBalanceDTO(
              Encoding.encodeToHex(seriesId.value.toByteArray),
              result.toString
            )
          case AssetType(groupId, seriesId) =>
            AssetTokenBalanceDTO(
              Encoding.encodeToHex(groupId.toByteArray),
              Encoding.encodeToHex(seriesId.toByteArray),
              result.toString
            )
          case _ =>
            // "Unknown"
            UnknownBalanceDTO(result.toString)
        }
        keyIdentifier

      }
      if (res.isEmpty)
        Left("No balance found at address")
      else
        Right(
          res.toList.filterNot(x =>
            x match {
              case UnknownBalanceDTO(_) => true
              case _                    => false
            }
          )
        ): Either[String, List[BalanceDTO]]
    }).handleError(f => Left(f.getMessage))

  }

}
