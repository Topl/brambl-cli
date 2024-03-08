package co.topl.brambl.cli.views

import cats.Id
import co.topl.brambl.builders.locks.LockTemplate
import co.topl.brambl.builders.locks.PropositionTemplate
import co.topl.brambl.dataApi.WalletFellowship
import co.topl.brambl.dataApi.WalletTemplate
import co.topl.brambl.utils.Encoding
import io.circe.parser.parse

object WalletModelDisplayOps {

  def displayWalletFellowshipHeader(): String =
    s"""X Coordinate\tFellowship Name"""

  def displayWalletTemplateHeader(): String =
    s"""Y Coordinate\tTemplate Name\tLock Template"""

  def display(walletEntity: WalletFellowship): String =
    s"""${walletEntity.xIdx}\t${walletEntity.name}"""

  def serialize[F[_]](lockTemplate: LockTemplate[F]): String = {
    lockTemplate match {
      case LockTemplate.PredicateTemplate(innerTemplates, threshold) =>
        s"threshold($threshold, ${innerTemplates.map(serialize[F]).mkString(", ")})"
    }
  }
  private def serialize[F[_]](lockTemplate: PropositionTemplate[F]): String = {
    lockTemplate match {
      case PropositionTemplate.OrTemplate(leftTemplate, rightTemplate) =>
        val left = leftTemplate match {
          case PropositionTemplate.AndTemplate(_, _) =>
            s"(${serialize[F](leftTemplate)})"
          case PropositionTemplate.OrTemplate(_, _) =>
            s"(${serialize[F](leftTemplate)})"
          case _ =>
            serialize[F](leftTemplate)
        }
        val right = rightTemplate match {
          case PropositionTemplate.AndTemplate(_, _) =>
            s"(${serialize[F](rightTemplate)})"
          case PropositionTemplate.OrTemplate(_, _) =>
            s"(${serialize[F](rightTemplate)})"
          case _ =>
            serialize[F](rightTemplate)
        }
        s"$left or $right"
      case PropositionTemplate.AndTemplate(leftTemplate, rightTemplate) =>
        val left = leftTemplate match {
          case PropositionTemplate.AndTemplate(_, _) =>
            s"(${serialize[F](leftTemplate)})"
          case PropositionTemplate.OrTemplate(_, _) =>
            s"(${serialize[F](leftTemplate)})"
          case _ =>
            serialize[F](leftTemplate)
        }
        val right = rightTemplate match {
          case PropositionTemplate.AndTemplate(_, _) =>
            s"(${serialize[F](rightTemplate)})"
          case PropositionTemplate.OrTemplate(_, _) =>
            s"(${serialize[F](rightTemplate)})"
          case _ =>
            serialize[F](rightTemplate)
        }
        s"$left and $right"
      case PropositionTemplate.ThresholdTemplate(innerTemplates, threshold) =>
        s"threshold($threshold, ${innerTemplates.map(serialize[F]).mkString(", ")})"
      case PropositionTemplate.SignatureTemplate(_, signIdx) =>
        s"sign($signIdx)"
      case PropositionTemplate.LockedTemplate(data) =>
        s"locked(${data.map(x => Encoding.encodeToBase58(x.value.toByteArray())).getOrElse("")})"
      case PropositionTemplate.HeightTemplate(_, min, max) =>
        s"height($min, $max)"
      case PropositionTemplate.TickTemplate(min, max) =>
        s"tick($min, $max)"
      case PropositionTemplate.DigestTemplate("Sha256", digest) =>
        s"sha256(${Encoding.encodeToHex(digest.value.toByteArray())})"
      case PropositionTemplate.DigestTemplate("Blake2b256", digest) =>
        s"blake2b(${Encoding.encodeToHex(digest.value.toByteArray())})"
    }
  }

  def display(walletTemplate: WalletTemplate): String = {
    import co.topl.brambl.codecs.LockTemplateCodecs.decodeLockTemplate
    (for {
      json <- Id(parse(walletTemplate.lockTemplate))
      decoded <- decodeLockTemplate[Id](json)
    } yield s"""${walletTemplate.yIdx}\t\t${walletTemplate.name}\t\t${serialize(
        decoded
      )}""").toOption.get
  }

}
