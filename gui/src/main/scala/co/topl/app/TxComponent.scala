package co.topl.app

import com.raquo.laminar.api.L._
import co.topl.shared.models.NetworkResponseDTO

sealed trait TxSection

case object FromSection extends TxSection

case object ToSection extends TxSection

case object SentTxSection extends TxSection

class TxComponent(networkVar: Var[String]) {

  lazy val fromFellowship = Var("")

  lazy val fromTemplate = Var("")

  lazy val availableAssets: Var[List[(Option[String], Option[String])]] = Var(List())

  lazy val currentAsset: Var[String] = Var("LVL")

  lazy val addressVar = Var("")

  lazy val fromInteractionVar = Var("")

  lazy val currentSection = Var[TxSection](FromSection)

  val addressValueSignal = addressVar.signal

  val addressInputObserver = addressVar.writer

  lazy val amountVar = Var("")

  lazy val feeVar = Var("10")

  val amountValueSignal = amountVar.signal

  lazy val txStatusVar: Var[Option[Either[String, String]]] = Var(None)

  private lazy val fetchNetwork = FetchStream
    .withDecoder[String](x => EventStream.fromJsPromise(x.text(), true))
    .get(
      "http://localhost:3000/api/wallet/network",
      _.headers(
        "Content-Type" -> "application/json"
      )
    )
    .map { x =>
      import io.circe.parser.parse
      parse(x).toOption
        .map({ x =>
          import io.circe.generic.auto._
          x.as[NetworkResponseDTO].toOption.get
        })
    } --> { x =>
    networkVar.update(_ => x.map(_.networkId).get)
  } 


  lazy val component = {
    div(
      cls := "container-fluid",
      h1(
        cls := "mt-4",
        "Transaction"
      ),
      div(
        cls := "accordion",
        idAttr := "accordionExample",
        FromSectionComponent(
          currentSection,
          fromFellowship,
          fromTemplate,
          fromInteractionVar,
          availableAssets
        ).component,
        ToSectionComponent(
          currentSection,
          networkVar,
          addressVar,
          amountVar,
          feeVar,
          txStatusVar,
          currentAsset,
          availableAssets
        ).component,
        SendTxComponent(
          currentSection,
          fromFellowship,
          fromTemplate,
          fromInteractionVar,
          networkVar,
          addressVar,
          amountVar,
          feeVar,
          currentAsset,
          txStatusVar
        ).component
      ),
      fetchNetwork
    )
  }
}
