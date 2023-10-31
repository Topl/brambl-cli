package co.topl.brambl.cli.impl

import cats.Monad
import cats.data.ValidatedNel
import co.topl.brambl.builders.locks.LockTemplate
import co.topl.brambl.builders.locks.PropositionTemplate
import co.topl.brambl.utils.Encoding
import com.google.protobuf.ByteString
import quivr.models.Data
import quivr.models

sealed trait ParseError {
  val location: Int
  val error: String
}

case class InvalidQuivrTemplate(location: Int, error: String) extends ParseError
case class TemplateParser(location: Int, error: String) extends ParseError

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

case class Height(location: Int, minHeight: Long, maxHeight: Long)
    extends TemplateAST

case class Locked(location: Int, someData: Option[String]) extends TemplateAST

case class Tick(location: Int, minTick: Long, maxTick: Long) extends TemplateAST

case class Digest(location: Int, digest: String) extends TemplateAST

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
            InvalidQuivrTemplate(
              0,
              "Threshold cannot be greater than the number of inner propositions"
            ).invalidNel
          )
        else if (threshold < 1)
          State.pure(
            InvalidQuivrTemplate(
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
            InvalidQuivrTemplate(
              location,
              "Index cannot be less than 0"
            ).invalidNel
          )
        else if (idx > 255)
          State.pure(
            InvalidQuivrTemplate(
              location,
              "Index cannot be greater than 255"
            ).invalidNel
          )
        else {
          State(interaction =>
            if (interaction.contains(idx))
              (
                interaction,
                InvalidQuivrTemplate(
                  location,
                  "Index cannot be used more than once"
                ).invalidNel
              )
            else
              (
                interaction + idx,
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
              InvalidQuivrTemplate(
                location,
                "Threshold cannot be greater than the number of inner propositions"
              ).invalidNel
            else if (threshold < 1)
              InvalidQuivrTemplate(
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
      case Locked(location, someData) =>
        State.pure(
          someData
            .map(data =>
              Encoding.decodeFromBase58(data) match {
                case Left(_) =>
                  InvalidQuivrTemplate(
                    location,
                    "Invalid base58 encoding"
                  ).invalidNel
                case Right(decoded) =>
                  PropositionTemplate
                    .LockedTemplate[F](
                      Some(
                        Data.defaultInstance
                          .withValue(ByteString.copyFrom(decoded))
                      )
                    )
                    .validNel
              }
            )
            .getOrElse(
              PropositionTemplate
                .LockedTemplate[F](None)
                .validNel
            )
        )
      case Height(location, min, max) =>
        State.pure(
          if (min < 0)
            InvalidQuivrTemplate(
              location,
              "Min height cannot be less than 0"
            ).invalidNel
          else if (max < 0)
            InvalidQuivrTemplate(
              location,
              "Max height cannot be less than 0"
            ).invalidNel
          else if (min > max)
            InvalidQuivrTemplate(
              location,
              "Min height cannot be greater than max height"
            ).invalidNel
          else
            PropositionTemplate
              .HeightTemplate[F]("header", min, max)
              .validNel
        )
      case Tick(location, min, max) =>
        State.pure(
          if (min < 0)
            InvalidQuivrTemplate(
              location,
              "Min tick cannot be less than 0"
            ).invalidNel
          else if (max < 0)
            InvalidQuivrTemplate(
              location,
              "Max tick cannot be less than 0"
            ).invalidNel
          else if (min > max)
            InvalidQuivrTemplate(
              location,
              "Min tick cannot be greater than max tick"
            ).invalidNel
          else
            PropositionTemplate
              .TickTemplate[F](min, max)
              .validNel
        )
      case Digest(location, digest) =>
        State.pure(
          Encoding.decodeFromBase58(digest) match {
            case Left(_) =>
              InvalidQuivrTemplate(
                location,
                "Invalid base58 encoding"
              ).invalidNel
            case Right(decoded) =>
              PropositionTemplate
                .DigestTemplate[F](
                  "Blake2b256",
                  new models.Digest(ByteString.copyFrom(decoded))
                )
                .validNel
          }
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
    threshold | signature | parenthesisExpr | locked | height | tick | digest
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

  def digest[$: P]: P[TemplateAST] =
    P(Index ~ P("digest") ~ P("(") ~ base58Chars ~ P(")")).map {
      case (location, base58) =>
        Digest(location, base58)
    }

  def locked[$: P]: P[TemplateAST] =
    P(Index ~ P("locked") ~ P("(") ~ base58CharsOrEmpty ~ P(")")).map {
      case (location, base58) =>
        Locked(location, if (base58.trim().isEmpty()) None else Some(base58))
    }

  def base58Char[$:P] = P(CharIn(
    "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
  ))

  def base58CharsOrEmpty[$: P]: P[String] =
    base58Char.rep.!

  def base58Chars[$: P]: P[String] =
    base58Char.rep(1).!

  def signature[$: P]: P[TemplateAST] =
    P(Index ~ P("sign") ~ P("(") ~ decimal ~ P(")")).map {
      case (location, idx) =>
        Sign(location, idx)
    }
  def height[$: P]: P[TemplateAST] =
    P(
      Index ~ P("height") ~ P("(") ~ decimalLong ~ P(",") ~ decimalLong ~ P(")")
    ).map { case (location, min, max) =>
      Height(location, min, max)
    }
  def tick[$: P]: P[TemplateAST] =
    P(
      Index ~ P("tick") ~ P("(") ~ decimalLong ~ P(",") ~ decimalLong ~ P(")")
    ).map { case (location, min, max) =>
      Tick(location, min, max)
    }

  def decimal[$: P]: P[Int] =
    P(P("0") | (P(CharIn("1-9")) ~ P(CharIn("0-9")).rep)).!.map(_.toInt)

  def decimalLong[$: P]: P[Long] =
    P(P("0") | (P(CharIn("1-9")) ~ P(CharIn("0-9")).rep)).!.map(_.toLong)

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
          val (interaction, res) =
            TemplateAST.compilePredicate[F](value).run(Set()).value
          if (interaction.toSeq.sorted != (0 until interaction.size).toSeq)
            (
              res,
              InvalidQuivrTemplate(
                0,
                "Index cannot be skipped"
              ).invalidNel[LockTemplate[F]]
            ).mapN((x, _) => x)
          else
            res
        case Parsed.Failure(_, location, _) =>
          TemplateParser(location, "Error parsing").invalidNel
      }
    }

  }
}
