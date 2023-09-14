package co.topl.brambl.cli.controllers

import cats.effect.kernel.Sync
import co.topl.brambl.cli.impl.TxParserAlgebra
import cats.effect.kernel.Resource
import java.io.FileOutputStream
import co.topl.brambl.cli.impl.CommonParserError
import cats.data.EitherT
import co.topl.brambl.models.transaction.IoTransaction

class TxController[F[_]: Sync](
    txParserAlgebra: TxParserAlgebra[F]
) {

  def createComplexTransaction(
      inputFile: String,
      outputFile: String
  ): F[Either[String, String]] = {
    import cats.implicits._
    (for {
      tx <- EitherT[F, CommonParserError, IoTransaction](
        txParserAlgebra.parseComplexTransaction(
          Resource.make(
            Sync[F].delay(scala.io.Source.fromFile(inputFile))
          )(source => Sync[F].delay(source.close()))
        )
      )
      _ <- EitherT.liftF[F, CommonParserError, Unit](
        Resource
          .make(
            Sync[F]
              .delay(new FileOutputStream(outputFile))
          )(fos => Sync[F].delay(fos.close()))
          .use(fos => Sync[F].delay(tx.writeTo(fos)))
      )
    } yield {
      "Transaction created"
    }).value.map(_.leftMap(_.description))
  }

}
