package co.topl.brambl.cli

import cats.effect.kernel.Resource
import java.io.PrintWriter
import cats.effect.IO

trait PolicyTemplates {

  def basicGroupPolicyTemplate(
      label: String,
      registrationUtxo: String
  ) =
    s"""|label: $label
        |registrationUtxo: $registrationUtxo
    """.stripMargin

  def basicAssetMintingStatementTemplate(
      groupTokenUtxo: String,
      seriesTokenUtxo: String,
      quantity: Long
  ) = s"""
    |groupTokenUtxo: $groupTokenUtxo
    |seriesTokenUtxo: $seriesTokenUtxo
    |quantity: $quantity
    |""".stripMargin

  def basicSeriesPolicyTemplate(
      label: String,
      registrationUtxo: String,
      fungibility: String,
      quantityDescriptor: String
  ) =
    s"""|label: $label
        |registrationUtxo: $registrationUtxo
        |fungibility: $fungibility
        |quantityDescriptor: $quantityDescriptor
        |permanentMetadata:
        |  type: object
        |  properties:
        |    name:
        |      type: string
        |    tickerName:
        |      type: string
        |    description:
        |      type: string
        |ephemeralMetadata:
        |  type: object
        |  properties:
        |    url:
        |      type: string
        |    image:
        |      type: string
    """.stripMargin

  def createAliceGroupPolicy(
      fileName: String,
      label: String,
      utxo: String
  ) = {
    Resource.make(IO(new PrintWriter(fileName)))(f => IO(f.close)).use { file =>
      IO(
        file.write(
          basicGroupPolicyTemplate(
            label,
            utxo
          )
        )
      )
    }
  }
  def createAliceSeriesPolicy(
      fileName: String,
      label: String,
      fungibility: String,
      quantityDescriptor: String,
      utxo: String
  ) = {
    Resource.make(IO(new PrintWriter(fileName)))(f => IO(f.close)).use { file =>
      IO(
        file.write(
          basicSeriesPolicyTemplate(
            label,
            utxo,
            fungibility,
            quantityDescriptor
          )
        )
      )
    }
  }

  def createAliceAssetMintingStatement(
      fileName: String,
      groupTokenUtxo: String,
      seriesTokenUtxo: String,
      quantity: Long
  ) = {
    Resource.make(IO(new PrintWriter(fileName)))(f => IO(f.close)).use { file =>
      IO(
        file.write(
          basicAssetMintingStatementTemplate(
            groupTokenUtxo,
            seriesTokenUtxo,
            quantity
          )
        )
      )
    }
  }

  def createAliceEphemeralMetadata(
    fileName: String,
    url: String,
    image: String,
    number: Int
  ) = {
    Resource.make(IO(new PrintWriter(fileName)))(f => IO(f.close)).use { file =>
      IO(
        file.write(
          s"""{
          |"url": "$url",
          |"image": "$image",
          |"number": $number
          |}""".stripMargin
        )
      )
    }
  }

}
