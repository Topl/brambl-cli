package co.topl.brambl.cli.controllers

import cats.Monad
import cats.data.Validated
import cats.effect.kernel.Sync
import co.topl.brambl.cli.TokenType
import co.topl.brambl.cli.impl.SimpleTransactionAlgebra
import co.topl.brambl.dataApi.WalletStateAlgebra
import co.topl.brambl.models.GroupId
import co.topl.brambl.models.LockAddress
import co.topl.brambl.models.SeriesId
import co.topl.brambl.syntax.AssetType
import co.topl.brambl.syntax.GroupType
import co.topl.brambl.syntax.LvlType
import co.topl.brambl.syntax.SeriesType

class SimpleTransactionController[F[_]: Sync](
    walletStateAlgebra: WalletStateAlgebra[F],
    simplTransactionOps: SimpleTransactionAlgebra[F]
) {

  def createSimpleTransactionFromParams(
      keyfile: String,
      password: String,
      fromCoordinates: (String, String, Option[Int]),
      changeCoordinates: (Option[String], Option[String], Option[Int]),
      someToAddress: Option[LockAddress],
      someToFellowship: Option[String],
      someToTemplate: Option[String],
      amount: Long,
      fee: Long,
      outputFile: String,
      tokenType: TokenType.Value,
      groupId: Option[GroupId],
      seriesId: Option[SeriesId]
  ): F[Either[String, String]] = {
    import cats.implicits._
    val (fromFellowship, fromTemplate, someFromInteraction) = fromCoordinates
    val (someChangeFellowship, someChangeTemplate, someChangeInteraction) =
      changeCoordinates
    walletStateAlgebra
      .validateCurrentIndicesForFunds(
        fromFellowship,
        fromTemplate,
        someFromInteraction
      ) flatMap {
      case Validated.Invalid(errors) =>
        Monad[F].point(Left("Invalid params\n" + errors.toList.mkString(", ")))
      case Validated.Valid(_) =>
        (for {
          tt <- Sync[F].delay(tokenType match {
            case TokenType.lvl    => LvlType
            case TokenType.group  => GroupType(groupId.get)
            case TokenType.series => SeriesType(seriesId.get)
            case TokenType.asset => AssetType(groupId.get.value, seriesId.get.value)
            case _ => throw new Exception("Token type not supported")
          })
          res <- simplTransactionOps
            .createSimpleTransactionFromParams(
              keyfile,
              password,
              fromFellowship,
              fromTemplate,
              someFromInteraction,
              someChangeFellowship,
              someChangeTemplate,
              someChangeInteraction,
              someToAddress,
              someToFellowship,
              someToTemplate,
              amount,
              fee,
              outputFile,
              tt
            )
        } yield res)
          .map(_ match {
            case Right(_)    => Right("Transaction successfully created")
            case Left(value) => Left(value.description)
          })
    }
  }
}
