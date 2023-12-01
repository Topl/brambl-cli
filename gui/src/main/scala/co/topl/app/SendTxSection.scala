package co.topl.app

import co.topl.shared.models.TxResponse
import com.raquo.laminar.api.L._
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import co.topl.shared.models.TxRequest

case class SendTxComponent(
    currentSection: Var[TxSection],
    fromFellowshipVar: Var[String],
    fromTemplateVar: Var[String],
    fromInteractionVar: Var[String],
    networkVar: Var[String],
    addressVar: Var[String],
    amountVar: Var[String],
    feeVar: Var[String],
    txStatusVar: Var[Option[Either[String, String]]]
) {

  private lazy val networkAndAddressSignal =
    networkVar.signal.combineWith(addressVar.signal)

  private lazy val networkAddressAndAmountSignal =
    networkAndAddressSignal.combineWith(amountVar.signal)

  private lazy val networdAddressAndAmountAndCurrentSectionSignal =
    networkAddressAndAmountSignal.combineWith(currentSection.signal)

  lazy val component = div(
    cls := "accordion-item",
    h2(
      cls := "accordion-header",
      idAttr := "headingThree",
      button(
        onClick --> { _ =>
          currentSection.update { e =>
            e match {
              case SentTxSection => ToSection
              case _             => SentTxSection
            }
          }
        },
        cls := "accordion-button",
        cls <-- currentSection.signal.map { e =>
          e match {
            case SentTxSection => ""
            case _             => "collapsed"
          }
        },
        tpe := "button",
        dataAttr("data-bs-toggle") := "collapse",
        dataAttr("data-bs-target") := "#collapseTwo",
        dataAttr("aria-expanded") <-- UIUtils
          .isExpanded(SentTxSection, currentSection),
        dataAttr("aria-controls") := "collapseTwo",
        child <-- networdAddressAndAmountAndCurrentSectionSignal.map { e =>
          val (_, _, _, s) = e
          s match {
            case SentTxSection =>
              h4("Make sure input is correct.")
            case _ =>
              h4("Send funds!")
          }
        }
      )
    ),
    div(
      idAttr := "collapseTwo",
      cls := "accordion-collapse collapse",
      cls <-- currentSection.signal.map { e =>
        e match {
          case SentTxSection => "show"
          case _             => ""
        }
      },
      dataAttr("aria-labelledby") := "headingTwo",
      dataAttr("data-bs-parent") := "#accordionExample",
      div(
        cls := "accordion-body",
        form(
          div(
            cls := "alert",
            cls <-- txStatusVar.signal.map { e =>
              e match {
                case Some(Left(_))  => "alert-danger"
                case Some(Right(_)) => "alert-success"
                case None           => "alert-primary"
              }
            },
            role := "alert",
            child.text <-- txStatusVar.signal.map(
              _ match {
                case Some(Left(error)) => error
                case Some(Right(txId)) => txId
                case None =>
                  "The button will activate when all fields are filled correctly."
              }
            )
          ),
          button(
            tpe := "submit",
            cls := "btn btn-primary",
            cls <-- networkAddressAndAmountSignal
              .combineWith(fromFellowshipVar)
              .combineWith(fromTemplateVar)
              .combineWith(fromInteractionVar)
              .map { e =>
                val (network, address, amount, _, _, fromInt) = e
                if (
                  Validation
                    .decodeAddress(address, UIUtils.hexToInt(network))
                    .isRight &&
                  UIUtils.isAmount(amount)
                ) {
                  if (fromInt.trim().isEmpty) {
                    Map("disabled" -> false)
                  } else {
                    if (UIUtils.isAmount(fromInt)) Map("disabled" -> false)
                    else Map("disabled" -> true)
                  }
                } else {
                  Map("disabled" -> true)
                }
              },
            onClick.preventDefault.mapTo(()) --> { _ =>
              txStatusVar.update(_ =>
                Some(Right("Waiting for the transaction..."))
              )
            },
            onClick.preventDefault.flatMap(_ =>
              FetchStream
                .withCodec[Json, String](
                  _.noSpaces,
                  x => EventStream.fromJsPromise(x.text(), true)
                )
                .post(
                  "http://localhost:3000/api/tx/send",
                  _.body(
                    TxRequest(
                      fromFellowshipVar.now(),
                      fromTemplateVar.now(),
                      if (fromInteractionVar.now().trim().isEmpty) None
                      else Some(fromInteractionVar.now()),
                      addressVar.now(),
                      amountVar.now(),
                      feeVar.now(),
                      networkVar.now()
                    ).asJson
                  ),
                  _.headers(
                    "Content-Type" -> "application/json"
                  )
                )
            ) --> { result =>
              import io.circe.parser.parse
              parse(result).toOption
                .map(_.as[TxResponse].toOption match {
                  case Some(x) => txStatusVar.update(_ => Some(x.value))
                  case None =>
                    txStatusVar.update(_ => Some(Left("Error decoding JSON!")))

                })
                .getOrElse(
                  txStatusVar.update(_ => Some(Left("Error parsing JSON!")))
                )
            },
            "Broadcast transaction!"
          )
        )
      )
    )
  )
}
