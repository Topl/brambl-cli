package co.topl.app

import com.raquo.laminar.api.L._

case class ToSectionComponent(
    currentSection: Var[TxSection],
    networkVar: Var[String],
    addressVar: Var[String],
    amountVar: Var[String],
    feeVar: Var[String],
    txStatusVar: Var[Option[Either[String, String]]],
    currentAsset: Var[String],
    availableAssets: Var[List[(Option[String], Option[String])]]
) {

  private lazy val networkAndAddressSignal =
    networkVar.signal.combineWith(addressVar.signal)

  private lazy val networkAddressAndAmountSignal =
    networkAndAddressSignal.combineWith(amountVar.signal)

  private lazy val networdAddressAndAmountAndCurrentSectionSignal =
    networkAddressAndAmountSignal.combineWith(currentSection.signal)

  private def getHeader(address: String, network: String, amount: String) =
    if (
      Validation
        .decodeAddress(address, UIUtils.hexToInt(network))
        .isRight &&
      UIUtils.isAmount(amount)
    )
      h4(
        s"Send ",
        span(cls := "badge bg-secondary", amount, "LVLs"),
        " to ",
        span(cls := "badge bg-secondary", address)
      )
    else if (
      Validation
        .decodeAddress(address, UIUtils.hexToInt(network))
        .isRight
    )
      h4(s"Please provide a valid amount")
    else if (UIUtils.isAmount(amount))
      h4(s"Please provide a valid address")
    else
      h4(s"Please provide a valid address and amount")

  lazy val component = div(
    cls := "accordion-item",
    h2(
      cls := "accordion-header",
      idAttr := "headingTwo",
      button(
        onClick --> { _ =>
          currentSection.update { e =>
            e match {
              case ToSection => SentTxSection
              case _         => ToSection
            }
          }
        },
        cls := "accordion-button",
        cls <-- currentSection.signal.map { e =>
          e match {
            case ToSection => ""
            case _         => "collapsed"
          }
        },
        tpe := "button",
        dataAttr("data-bs-toggle") := "collapse",
        dataAttr("data-bs-target") := "#collapseTwo",
        dataAttr("aria-expanded") <-- UIUtils
          .isExpanded(ToSection, currentSection),
        dataAttr("aria-controls") := "collapseTwo",
        child <-- networdAddressAndAmountAndCurrentSectionSignal.map { e =>
          val (network, address, amount, s) = e
          s match {
            case ToSection =>
              h4("Please select the address where you want to send the funds.")
            case _ =>
              getHeader(
                address,
                network,
                amount
              )
          }
        }
      )
    ),
    div(
      idAttr := "collapseTwo",
      cls := "accordion-collapse collapse",
      cls <-- currentSection.signal.map { e =>
        e match {
          case ToSection => "show"
          case _         => ""
        }
      },
      dataAttr("aria-labelledby") := "headingTwo",
      dataAttr("data-bs-parent") := "#accordionExample",
      form(
        div(
          cls := "accordion-body",
          div(
            cls := "mb-3",
            label(
              forId := "address",
              cls := "form-label",
              "Address"
            ),
            input(
              tpe := "text",
              cls := "form-control",
              cls <-- networkAndAddressSignal.map { e =>
                val (network, address) = e
                if (
                  Validation
                    .decodeAddress(address, UIUtils.hexToInt(network))
                    .isRight
                )
                  "is-valid"
                else "is-invalid"
              },
              idAttr := "address",
              onInput.mapToValue.setAsValue --> addressVar.writer,
              dataAttr("aria-describedby") := "addressHelp"
            ),
            div(
              idAttr := "addressHelp",
              cls := "form-text",
              "The address where to send the funds."
            ),
            child <-- networkAndAddressSignal
              .map { e =>
                val (network, address) = e
                Validation
                  .decodeAddress(address, UIUtils.hexToInt(network))
                  .swap
                  .toOption
              }
              .splitOption(
                (_, errorSignal) =>
                  div(
                    idAttr := "validationMessage",
                    cls := "invalid-feedback",
                    child.text <-- errorSignal.map(_ match {
                      case InvalidInputString =>
                        "Invalid input string"
                      case InvalidNetwork =>
                        "Invalid network"
                      case InvalidLeger =>
                        "Invalid ledger"
                    })
                  ),
                ifEmpty = emptyNode
              )
          ),
          div(
            cls := "mb-3",
            label(
              forId := "amount",
              cls := "form-label",
              "Amount"
            ),
            input(
              tpe := "text",
              cls := "form-control",
              idAttr := "amount",
              cls <-- amountVar.signal.map { amount =>
                if (UIUtils.isAmount(amount)) "is-valid"
                else "is-invalid"
              },
              onInput.mapToValue.setAsValue --> amountVar.writer,
              dataAttr("aria-describedby") := "amountHelp"
            ),
            div(
              idAttr := "amountHelp",
              cls := "form-text",
              "The amount of assets to be sent."
            )
          ),
          label(
            forId := "token",
            cls := "form-label",
            "Token"
          ),
          select(
            cls := "form-select form-select-lg mb-3",
            onChange.mapToValue.setAsValue --> currentAsset.writer,
            dataAttr("aria-label") := ".form-select-lg example",
            children <-- availableAssets.signal.map { e =>
              e.map(_ match {
                case (None, None) =>
                  option(
                    value := "LVL",
                    "LVL"
                  )
                case (Some(id), None) =>
                  option(value := s"$id:", s"Group Token [$id]")
                case (None, Some(id)) =>
                  option(value := s":$id", s"Series Token [$id]")
                case (Some(group), Some(series)) =>
                  option(
                    value := s"$group:$series",
                    s"Asset Token [$group]:[$series]"
                  )
              })

            }
          ),
          div(
            cls := "mb-3",
            label(
              forId := "fee",
              cls := "form-label",
              "Fee"
            ),
            input(
              tpe := "text",
              cls := "form-control",
              idAttr := "fee",
              cls <-- feeVar.signal.map { fee =>
                if (UIUtils.isAmount(fee)) "is-valid"
                else "is-invalid"
              },
              controlled(
                value <-- feeVar.signal,
                onInput.mapToValue --> feeVar.writer
              ),
              dataAttr("aria-describedby") := "feeHelp"
            ),
            div(
              idAttr := "feeHelp",
              cls := "form-text",
              "The fee to be paid."
            )
          )
        )
      )
    )
  )

}
