package co.topl.brambl.cli.impl

import cats.Monad
import cats.effect.kernel.MonadCancel
import cats.effect.kernel.Resource

import java.sql.DriverManager
import cats.effect.IO

abstract class WalletStateApiFailure extends RuntimeException

trait WalletStateApi[F[_]] {

  def initWalletState(): F[Unit]

}

object WalletStateApi {

  type WalletStateApiF[F[_]] = MonadCancel[F, Throwable]

  def makeIO(): WalletStateApi[IO] =
    new WalletStateApi[IO] {

      override def initWalletState(): IO[Unit] = {
        Resource
          .make(IO(DriverManager.getConnection("jdbc:sqlite:wallet.db")))(
            conn => IO(conn.close())
          )
          .use { conn =>
            for {
              stmnt <- IO(conn.createStatement())
              _ <- IO(
                stmnt.execute(
                  "CREATE TABLE IF NOT EXISTS cartesian (id INTEGER PRIMARY KEY," +
                    " x_party INTEGER NOT NULL, y_contract INTEGER NOT NULL, z_state INTEGER NOT NULL, " +
                    "address TEXT NOT NULL)"
                )
              )
              _ <- IO(
                stmnt.execute(
                  "CREATE INDEX IF NOT EXISTS cartesian_coordinates ON cartesian (x_party, y_contract, z_state)"
                )
              )
            } yield ()
          }
      }

    }
}
