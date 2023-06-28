package co.topl.brambl.cli.impl

import co.topl.brambl.builders.locks.LockTemplate
import cats.Monad
import co.topl.brambl.builders.locks.PropositionTemplate
import cats.data.ValidatedNel

sealed trait ParseError {
  val location: Int
  val error: String
}

case class InvalidQuivrContract(location: Int, error: String) extends ParseError
case class ContractParser(location: Int, error: String) extends ParseError

sealed trait TemplateAST {
  val location: Int
}

case class Sign(location: Int, idx: Int) extends TemplateAST

case class ThresholdPredicate(
    threshold: Int,
    innerPropositions: Seq[TemplateAST]
)

case class Threshold(
    location: Int,
    threshold: Int,
    innerPropositions: Seq[TemplateAST]
) extends TemplateAST

case class And(location: Int, left: TemplateAST, right: TemplateAST)
    extends TemplateAST

case class Or(location: Int, left: TemplateAST, right: TemplateAST)
    extends TemplateAST

object TemplateAST {

  import cats.data.State

  type ValidationState = Set[Int]

  type ValidationStateM[A] = State[ValidationState, A]

  def compilePredicate[F[_]: Monad](
      template: ThresholdPredicate
  ): ValidationStateM[ValidatedNel[ParseError, LockTemplate[F]]] = {
    import co.topl.brambl.builders.locks._
    import cats.implicits._
    template match {
      case ThresholdPredicate(threshold, innerPropositions) =>
        if (threshold > innerPropositions.length)
          State.pure(
            InvalidQuivrContract(
              0,
              "Threshold cannot be greater than the number of inner propositions"
            ).invalidNel
          )
        else if (threshold < 1)
          State.pure(
            InvalidQuivrContract(
              0,
              "Threshold cannot be less than 1"
            ).invalidNel
          )
        else {
          innerPropositions.map(x => compile[F](x)).sequence.map { templatesS =>
            templatesS.sequence.map { innerProps =>
              LockTemplate
                .PredicateTemplate[F](
                  innerProps,
                  threshold
                )
            }
          }
        }
    }
  }
  def compile[F[_]: Monad](
      template: TemplateAST
  ): ValidationStateM[ValidatedNel[ParseError, PropositionTemplate[F]]] = {
    import cats.implicits._
    template match {
      case Sign(location, idx) =>
        if (idx < 0)
          State.pure(
            InvalidQuivrContract(
              location,
              "Index cannot be less than 0"
            ).invalidNel
          )
        else if (idx > 255)
          State.pure(
            InvalidQuivrContract(
              location,
              "Index cannot be greater than 255"
            ).invalidNel
          )
        else {
          State(state =>
            if (state.contains(idx))
              (
                state,
                InvalidQuivrContract(
                  location,
                  "Index cannot be used more than once"
                ).invalidNel
              )
            else
              (
                state + idx,
                PropositionTemplate
                  .SignatureTemplate[F]("ExtendedEd25519", idx)
                  .validNel
              )
          )
        }
      case Threshold(location, threshold, innerPropositions) =>
        innerPropositions.map(x => compile[F](x)).sequence.map {
          innerPropositions =>
            if (threshold > innerPropositions.length)
              InvalidQuivrContract(
                location,
                "Threshold cannot be greater than the number of inner propositions"
              ).invalidNel
            else if (threshold < 1)
              InvalidQuivrContract(
                location,
                "Threshold cannot be less than 1"
              ).invalidNel
            else {
              innerPropositions.sequence.map { innerProps =>
                PropositionTemplate
                  .ThresholdTemplate[F](
                    innerProps,
                    threshold
                  )
              }
            }
        }
      case And(_, left, right) =>
        (compile[F](left), compile[F](right)).mapN((l, r) =>
          (l, r).mapN((l, r) => PropositionTemplate.AndTemplate(l, r))
        )
      case Or(_, left, right) =>
        (compile[F](left), compile[F](right)).mapN((l, r) =>
          (l, r).mapN((l, r) => PropositionTemplate.OrTemplate(l, r))
        )

    }
  }

}

trait QuivrFastParser[F[_]] {
  import fastparse._, MultiLineWhitespace._

  def parseQuivr(input: String): ValidatedNel[ParseError, LockTemplate[F]]

  def thresholdPredicate[$: P]: P[ThresholdPredicate] =
    P(P("threshold") ~ P("(") ~ decimal ~ P(",") ~ exprSeq ~ P(")")).map {
      case (threshold, innerPropositions) =>
        ThresholdPredicate(threshold.toInt, innerPropositions)
    }

  def threshold[$: P]: P[TemplateAST] =
    P(Index ~ P("threshold") ~ P("(") ~ decimal ~ P(",") ~ exprSeq ~ P(")"))
      .map { case (location, threshold, innerPropositions) =>
        Threshold(location, threshold, innerPropositions)
      }

  def exprSeq[$: P] = P(expr.rep(sep = P(",")))

  def expr[$: P]: P[TemplateAST] = P(
     booleanExpr
  )

  def parenthesisExpr[$: P]: P[TemplateAST] = P("(" ~/ expr ~ ")")

  def atomicExpr[$: P]: P[TemplateAST] = P(
    threshold | signature | parenthesisExpr
  )
  def booleanExpr[$: P]: P[TemplateAST] =
    P(Index ~ atomicExpr ~ (P(P("and") | P("or")).! ~/ atomicExpr).rep).map {
      case (location, z, l) =>
        l.foldLeft(z) { case (left, opAndRight) =>
          val (operator, right) = opAndRight
          if (operator == "or")
            Or(location, left, right)
          else
            And(location, left, right)
        }
    }

  def signature[$: P]: P[TemplateAST] =
    P(Index ~ P("sign") ~ P("(") ~ decimal ~ P(")")).map {
      case (location, idx) =>
        Sign(location, idx)
    }

  def decimal[$: P]: P[Int] =
    P(P("0") | (P(CharIn("1-9")) ~ P(CharIn("0-9")).rep)).!.map(_.toInt)

}

object QuivrFastParser {
  import fastparse._

  def make[F[_]: Monad]: QuivrFastParser[F] = new QuivrFastParser[F] {

    def parseQuivr(
        input: String
    ): ValidatedNel[ParseError, LockTemplate[F]] = {
      import cats.implicits._
      parse(input, thresholdPredicate(_)) match {
        case Parsed.Success(value, _) =>
          val (state, res) =
            TemplateAST.compilePredicate[F](value).run(Set()).value
          if (state.toSeq.sorted != (0 until state.size).toSeq)
            (
              res,
              InvalidQuivrContract(
                0,
                "Index cannot be skipped"
              ).invalidNel[LockTemplate[F]]
            ).mapN((x, _) => x)
          else
            res
        case Parsed.Failure(_, location, _) =>
          ContractParser(location, "Error parsing").invalidNel
      }
    }

  }
}
