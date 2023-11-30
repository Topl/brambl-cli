package co.topl.app

import co.topl.shared.models.BalanceRequestDTO
import co.topl.shared.models.BalanceResponseDTO
import co.topl.shared.models.FellowshipDTO
import co.topl.shared.models.SimpleErrorDTO
import co.topl.shared.models.TemplateDTO
import com.raquo.laminar.api.L._
import io.circe.Json
import io.circe.generic.auto._
import io.circe.parser.parse

case class FromSectionComponent(
    currentSection: Var[TxSection],
    fromFellowship: Var[String],
    fromTemplate: Var[String],
    fromInteraction: Var[String]
) {

  private val fellowshipOptions: Var[Seq[Node]] = Var(
    Seq(
      option(
        value := "-1",
        "Please select a fellowship"
      )
    )
  )
  private val templateOptions: Var[Seq[Node]] = Var(
    Seq(
      option(
        value := "-1",
        "Please select a template"
      )
    )
  )
  private val lvlBalance: Var[Either[String, String]] = Var(Right("0"))

  private lazy val fetchTemplates = FetchStream
    .withDecoder[String](x => EventStream.fromJsPromise(x.text(), true))
    .get(
      "http://localhost:3000/api/wallet/templates",
      _.headers(
        "Content-Type" -> "application/json"
      )
    )
    .map { x =>
      parse(x).toOption
        .map({ x =>
          import io.circe.generic.auto._
          val res = x.as[Seq[TemplateDTO]].toOption.get
          (
            option(
              value := res.filter(_.idx == 2).head.name,
              res
                .filter(_.idx == 2)
                .head
                .name + s" [${res.filter(_.idx == 2).head.lockTemplate})]",
              selected := true
            ),
            res.filter(_.idx == 2).head.name
          ) +: res.filter(_.idx != 2).map { x =>
            (
              option(
                value := x.name,
                x.name + s" [${x.lockTemplate})]"
              ),
              x.name
            )
          }
        })
        .get
    } --> { x =>
    templateOptions.update(_ => x.map(_._1))
    fromTemplate.update(_ => x.head._2)
  }

  private lazy val fetchFellowships = FetchStream
    .withDecoder[String](x => EventStream.fromJsPromise(x.text(), true))
    .get(
      "http://localhost:3000/api/wallet/fellowships",
      _.headers(
        "Content-Type" -> "application/json"
      )
    )
    .map { x =>
      parse(x).toOption
        .map({ x =>
          import io.circe.generic.auto._
          val res = x.as[Seq[FellowshipDTO]].toOption.get
          (
            option(
              value := res.filter(_.idx == 0).head.name,
              res.filter(_.idx == 0).head.name,
              selected := true
            ),
            res.head.name
          ) +: res.filter(_.idx != 0).map { x =>
            (
              option(
                value := x.name,
                x.name
              ),
              x.name
            )
          }

        })
        .get
    } --> { x =>
    fellowshipOptions.update(_ => x.map(_._1))
    fromFellowship.update(_ => x.head._2)
  }

  lazy val fellowShipTemplateInteraction = fromFellowship.signal
    .combineWith(fromTemplate.signal)
    .combineWith(fromInteraction.signal)

  private def getHeader(
      txSection: TxSection,
      fromFellowship: String,
      fromTemplate: String
  ) =
    txSection match {
      case FromSection =>
        h4("Please select from which coordinates you want to send the funds.")
      case _ =>
        h4(
          "From: ",
          span(cls := "badge bg-secondary", fromFellowship),
          " Template: ",
          span(cls := "badge bg-secondary", fromTemplate)
        )
    }

  lazy val component = div(
    cls := "accordion-item",
    h2(
      cls := "accordion-header",
      idAttr := "headingOne",
      button(
        cls := "accordion-button",
        cls <-- currentSection.signal.map { e =>
          e match {
            case FromSection => ""
            case _           => "collapsed"
          }
        },
        tpe := "button",
        dataAttr("data-bs-toggle") := "collapse",
        dataAttr("data-bs-target") := "#collapseOne",
        dataAttr("aria-expanded") <-- UIUtils
          .isExpanded(FromSection, currentSection),
        dataAttr("aria-controls") := "collapseOne",
        onClick --> { _ =>
          currentSection.update { e =>
            e match {
              case FromSection => ToSection
              case _           => FromSection
            }
          }
        },
        child <-- currentSection.signal.map { e =>
          e match {
            case FromSection =>
              h4(
                "Please select from which coordinates you want to send the funds."
              )
            case _ =>
              getHeader(e, fromFellowship.now(), fromTemplate.now())
          }
        }
      )
    ),
    div(
      idAttr := "collapseOne",
      cls := "accordion-collapse collapse",
      cls <-- currentSection.signal.map { e =>
        e match {
          case FromSection => "show"
          case _           => ""
        }
      },
      dataAttr("aria-labelledby") := "headingOne",
      dataAttr("data-bs-parent") := "#accordionExample",
      div(
        cls := "accordion-body",
        form(
          label(forId := "fellowship", cls := "form-label", "Fellowship"),
          select(
            cls := "form-select form-select-lg mb-3",
            onChange.mapToValue.setAsValue --> fromFellowship.writer,
            dataAttr("aria-label") := ".form-select-lg example",
            children <-- fellowshipOptions
          ),
          label(forId := "templates", cls := "form-label", "Template"),
          select(
            cls := "form-select form-select-lg mb-3",
            onChange.mapToValue.setAsValue --> fromTemplate.writer,
            dataAttr("aria-label") := ".form-select-lg example",
            children <-- templateOptions
          ),
          label(
            forId := "interaction",
            cls := "form-label",
            "Interaction"
          ),
          input(
            tpe := "text",
            cls := "form-control",
            cls <-- fromFellowship.signal
              .combineWith(fromInteraction.signal)
              .map { e =>
                val (_, fromInt) = e
                if (fromInt.trim().isEmpty) {
                  "is-valid"
                } else {
                  if (UIUtils.isAmount(fromInt)) "is-valid"
                  else "is-invalid"
                }
              },
            idAttr := "interaction",
            onInput.mapToValue.setAsValue --> fromInteraction.writer,
            dataAttr("aria-describedby") := "interactionHelp"
          ),
          div(
            idAttr := "interactionHelp",
            cls := "form-text",
            "The interaction where the funds come from."
          ),
          child <-- fromFellowship.signal
            .combineWith(fromInteraction.signal)
            .map { e =>
              val (_, fromInt) = e
              if (fromInt.trim().isEmpty) {
                None
              } else {
                if (UIUtils.isAmount(fromInt)) None
                else Some("Please provide a number")
              }
            }
            .splitOption(
              (_, errorSignal) =>
                div(
                  idAttr := "validationMessage",
                  cls := "invalid-feedback",
                  child.text <-- errorSignal
                ),
              ifEmpty = emptyNode
            )
        ),
        div(
          cls := "alert",
          cls <-- lvlBalance.signal.map { x =>
            if (x.isLeft) "alert-danger"
            else "alert-success"
          },
          role := "alert",
          child.text <-- lvlBalance.signal.map(x =>
            x match {
              case Left(error) => error
              case Right(x)    => s"Current balance is $x"
            }
          )
        ),
        fetchFellowships,
        fetchTemplates,
        fellowShipTemplateInteraction.flatMap { e =>
          import io.circe.generic.auto._
          import io.circe.syntax._
          val (fellowship, template, interaction) = e
          if (fellowship.isEmpty || template.isEmpty()) EventStream.empty
          else
            FetchStream
              .withCodec[Json, (String, Int)](
                _.noSpaces,
                x =>
                  EventStream
                    .fromJsPromise(x.text(), true)
                    .combineWith(EventStream.fromValue(x.status))
              )
              .post(
                "http://localhost:3000/api/wallet/balance",
                _.body(
                  BalanceRequestDTO(
                    fellowship,
                    template,
                    if (interaction.trim().isEmpty) None
                    else Some(interaction)
                  ).asJson
                ),
                _.headers(
                  "Content-Type" -> "application/json"
                )
              )
        } --> { result =>
          val (text, status) = result
          if (status != 200) {
            parse(text).toOption
              .map(_.as[SimpleErrorDTO].toOption match {
                case Some(x) => lvlBalance.update(_ => Left(x.error))
                case None =>
                  lvlBalance.update(_ => Left("Error decoding JSON!"))
              })
              .getOrElse {
                lvlBalance.update(_ => Left("Error fetching balance!"))
              }
          } else {
            import io.circe.parser.parse
            parse(text).toOption
              .map(_.as[BalanceResponseDTO].toOption match {
                case Some(x) => lvlBalance.update(_ => Right(x.lvlBalance))
                case None =>
                  lvlBalance.update(_ => Left("Error decoding JSON!"))

              })
              .getOrElse(
                lvlBalance.update(_ => Left("Error parsing JSON!"))
              )
          }
        }
      )
    )
  )

}
