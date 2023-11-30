package co.topl.app

import com.raquo.laminar.api.L._

sealed trait TxSection

case object FromSection extends TxSection

case object ToSection extends TxSection

case object SentTxSection extends TxSection

class TxComponent(networkVar: Var[String]) {

  lazy val fromFellowship = Var("")

  lazy val fromTemplate = Var("")

  lazy val addressVar = Var("")

  lazy val fromInteractionVar = Var("")

  lazy val currentSection = Var[TxSection](FromSection)

  val addressValueSignal = addressVar.signal

  val addressInputObserver = addressVar.writer

  lazy val amountVar = Var("")

  val amountValueSignal = amountVar.signal

  val networkValueSignal = networkVar.signal

  lazy val txStatusVar: Var[Option[Either[String, String]]] = Var(None)

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
          fromInteractionVar
        ).component,
        ToSectionComponent(
          currentSection,
          networkVar,
          addressVar,
          amountVar,
          txStatusVar
        ).component,
        SendTxComponent(
          currentSection,
          fromFellowship,
          fromTemplate,
          fromInteractionVar,
          networkVar,
          addressVar,
          amountVar,
          txStatusVar
        ).component
      )
    )
  }
}
