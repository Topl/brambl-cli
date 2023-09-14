package co.topl.brambl.cli.impl

import cats.data.EitherT
import cats.Monad

object TransactionUtils {

  def liftF[F[_]: Monad, A](a: F[A]) = {
    import cats.implicits._
    EitherT[F, SimpleTransactionAlgebraError, A](a.map(Right(_)))
  }

  def lift[F[_], A](a: F[Either[SimpleTransactionAlgebraError, A]]) =
    EitherT[F, SimpleTransactionAlgebraError, A](a)

}
