package co.topl.brambl.cli.controllers

import cats.effect.kernel.Resource
import cats.effect.kernel.Sync
import co.topl.brambl.cli.impl.CommonParserError
import co.topl.brambl.cli.impl.TxParserAlgebra

import java.io.FileOutputStream

class TxController[F[_]: Sync](
    txParserAlgebra: TxParserAlgebra[F]
) {

  def createComplexTransaction(
      inputFile: String,
      outputFile: String
  ): F[Either[String, String]] = {
    import cats.implicits._
    (for {
      eitherTx <- txParserAlgebra.parseComplexTransaction(
        Resource.make(
          Sync[F].delay(scala.io.Source.fromFile(inputFile))
        )(source => Sync[F].delay(source.close()))
      )
      tx <- Sync[F].fromEither(eitherTx)
      _ <- Resource
        .make(
          Sync[F]
            .delay(new FileOutputStream(outputFile))
        )(fos => Sync[F].delay(fos.close()))
        .use(fos => Sync[F].delay(tx.writeTo(fos)))
    } yield {
      "Transaction created"
    }).attempt.map(_ match {
      case Right(_)                       => Right("Transaction created")
      case Left(value: CommonParserError) => Left(value.description)
      case Left(e)                        => Left(e.getMessage())
    })
  }

}
