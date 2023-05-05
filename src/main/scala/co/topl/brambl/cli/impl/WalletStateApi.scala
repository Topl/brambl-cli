package co.topl.brambl.cli.impl

import cats.effect.kernel.Resource
import cats.effect.kernel.Sync
import quivr.models.VerificationKey

import java.sql.DriverManager
import co.topl.brambl.models.Indices

abstract class WalletStateApiFailure extends RuntimeException

trait WalletStateApi[F[_]] {

  def initWalletState(
      vk: VerificationKey
  ): F[Unit]

  def getCurrentAddress(): F[String]

  def updateWalletState(
      address: String,
      indices: Indices
  ): F[Unit]

  def getCurrentIndicesForDefaultFunds(): F[Indices]

  def getNextIndicesForDefaultFunds(): F[Indices]

}

object WalletStateApi {

  def make[F[_]: Sync](
      transactionBuilderApi: TransactionBuilderApi[F]
  ): WalletStateApi[F] =
    new WalletStateApi[F] {

      override def updateWalletState(
          address: String,
          indices: Indices
      ): F[Unit] = {
        connection().use { conn =>
          import cats.implicits._
          for {
            stmnt <- Sync[F].blocking(conn.createStatement())
            inserted <- Sync[F].blocking(
              stmnt.executeUpdate(
                s"INSERT INTO cartesian (x_party, y_contract, z_state, address) VALUES (${indices.x}, ${indices.y}, ${indices.z}, '" +
                  address + "')"
              )
            )
          } yield {
            println("Inserted into cartesian table: " + inserted)
          }
        }
      }

      override def getNextIndicesForDefaultFunds(): F[Indices] = {
        connection().use { conn =>
          import cats.implicits._
          for {
            stmnt <- Sync[F].blocking(conn.createStatement())
            rs <- Sync[F].blocking(
              stmnt.executeQuery(
                "SELECT x_party, y_contract, MAX(z_state) as z_index FROM cartesian WHERE x_party = 1 AND y_contract = 1"
              )
            )
            x <- Sync[F].delay(rs.getInt("x_party"))
            y <- Sync[F].delay(rs.getInt("y_contract"))
            z <- Sync[F].delay(rs.getInt("z_index"))
          } yield Indices(x, y, z + 1)
        }
      }

      override def getCurrentIndicesForDefaultFunds(): F[Indices] = {
        connection().use { conn =>
          import cats.implicits._
          for {
            stmnt <- Sync[F].blocking(conn.createStatement())
            rs <- Sync[F].blocking(
              stmnt.executeQuery(
                "SELECT x_party, y_contract, MAX(z_state) as z_index FROM cartesian WHERE x_party = 1 AND y_contract = 1"
              )
            )
            x <- Sync[F].delay(rs.getInt("x_party"))
            y <- Sync[F].delay(rs.getInt("y_contract"))
            z <- Sync[F].delay(rs.getInt("z_index"))
          } yield Indices(x, y, z)
        }
      }

      def connection() = Resource
        .make(
          Sync[F].delay(DriverManager.getConnection("jdbc:sqlite:wallet.db"))
        )(conn => Sync[F].delay(conn.close()))

      override def getCurrentAddress(): F[String] = {
        connection().use { conn =>
          import cats.implicits._
          for {
            stmnt <- Sync[F].blocking(conn.createStatement())
            rs <- Sync[F].blocking(
              stmnt.executeQuery(
                "SELECT address FROM cartesian WHERE x_party = 1 AND y_contract = 1 AND z_state = MAX(z_state)"
              )
            )
            address <- Sync[F].delay(rs.getString("address"))
          } yield address
        }
      }

      override def initWalletState(
          vk: VerificationKey
      ): F[Unit] = {
        import TransactionBuilderApi.implicits._
        connection().use { conn =>
          import cats.implicits._
          for {
            stmnt <- Sync[F].delay(conn.createStatement())
            _ <- Sync[F].delay(
              stmnt.execute(
                "CREATE TABLE IF NOT EXISTS cartesian (id INTEGER PRIMARY KEY," +
                  " x_party INTEGER NOT NULL, y_contract INTEGER NOT NULL, z_state INTEGER NOT NULL, " +
                  "address TEXT NOT NULL)"
              )
            )
            _ <- Sync[F].delay(
              stmnt.execute(
                "CREATE INDEX IF NOT EXISTS cartesian_coordinates ON cartesian (x_party, y_contract, z_state)"
              )
            )
            lockAddress <- transactionBuilderApi
              .lockAddress(
                vk
              )
            inserted <- Sync[F].delay(
              stmnt.executeUpdate(
                "INSERT INTO cartesian (x_party, y_contract, z_state, address) VALUES (1, 1, 1, '" +
                  lockAddress.toBase58 + "')"
              )
            )
            _ <- Sync[F].delay(stmnt.close())
          } yield {
            println("Initialized wallet state: " + inserted)
          }
        }
      }

    }
}
