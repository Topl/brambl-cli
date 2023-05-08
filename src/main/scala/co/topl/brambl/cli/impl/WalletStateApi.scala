package co.topl.brambl.cli.impl

import cats.effect.kernel.Resource
import cats.effect.kernel.Sync
import co.topl.brambl.models.Indices
import quivr.models.VerificationKey

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

  def getCurrentIndicesForFunds(
      party: String,
      contract: String
  ): F[Option[Indices]]

  def getNextIndicesForFunds(
      party: String,
      contract: String
  ): F[Option[Indices]]

}

object WalletStateApi {

  def make[F[_]: Sync](
      connection: () => Resource[F, java.sql.Connection],
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
            _ <- Sync[F].blocking(
              stmnt.executeUpdate(
                s"INSERT INTO cartesian (x_party, y_contract, z_state, address) VALUES (${indices.x}, ${indices.y}, ${indices.z}, '" +
                  address + "')"
              )
            )
          } yield ()
        }
      }

      override def getNextIndicesForFunds(
          party: String,
          contract: String
      ): F[Option[Indices]] = {
        connection().use { conn =>
          import cats.implicits._
          for {
            stmnt <- Sync[F].blocking(conn.createStatement())
            rs <- Sync[F].blocking(
              stmnt.executeQuery(
                s"SELECT x_party, party FROM parties WHERE party = '${party}'"
              )
            )
            x <- Sync[F].delay(rs.getInt("x_party"))
            rs <- Sync[F].blocking(
              stmnt.executeQuery(
                s"SELECT y_contract, contract FROM contracts WHERE contract = '${contract}'"
              )
            )
            y <- Sync[F].delay(rs.getInt("y_contract"))
            rs <- Sync[F].blocking(
              stmnt.executeQuery(
                s"SELECT x_party, y_contract, MAX(z_state) as z_index FROM cartesian WHERE x_party = ${x} AND y_contract = 1"
              )
            )
            y <- Sync[F].delay(rs.getInt("y_contract"))
            z <- Sync[F].delay(rs.getInt("z_index"))
          } yield if (x == 0) None else Some(Indices(x, y, z + 1))
        }
      }

      override def getCurrentIndicesForFunds(
          party: String,
          contract: String
      ): F[Option[Indices]] = {
        connection().use { conn =>
          import cats.implicits._
          for {
            stmnt <- Sync[F].blocking(conn.createStatement())
            rs <- Sync[F].blocking(
              stmnt.executeQuery(
                s"SELECT x_party, party FROM parties WHERE party = '${party}'"
              )
            )
            x <- Sync[F].delay(rs.getInt("x_party"))
            rs <- Sync[F].blocking(
              stmnt.executeQuery(
                s"SELECT x_party, y_contract, MAX(z_state) as z_index FROM cartesian WHERE x_party = ${x} AND y_contract = 1"
              )
            )
            y <- Sync[F].delay(rs.getInt("y_contract"))
            z <- Sync[F].delay(rs.getInt("z_index"))
          } yield if (x == 0) None else Some(Indices(x, y, z))
        }
      }

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
                "CREATE TABLE IF NOT EXISTS parties (party TEXT PRIMARY KEY," +
                  " x_party INTEGER NOT NULL)"
              )
            )
            _ <- Sync[F].delay(
              stmnt.execute(
                "CREATE TABLE IF NOT EXISTS contracts (contract TEXT PRIMARY KEY," +
                  " y_contract INTEGER NOT NULL, prover TEXT NOT NULL)"
              )
            )
            _ <- Sync[F].delay(
              stmnt.execute(
                "CREATE INDEX IF NOT EXISTS cartesian_coordinates ON cartesian (x_party, y_contract, z_state)"
              )
            )
            predicate <- transactionBuilderApi
              .lockPredicateSignature(
                vk
              )
            lockAddress <- transactionBuilderApi
              .lockAddress(
                predicate
              )
            _ <- Sync[F].delay(
              stmnt.executeUpdate(
                "INSERT INTO cartesian (x_party, y_contract, z_state, address) VALUES (1, 1, 1, '" +
                  lockAddress.toBase58 + "')"
              )
            )
            _ <- Sync[F].delay(
              stmnt.executeUpdate(
                "INSERT INTO parties (party, x_party) VALUES ('noparty', 0)"
              )
            )
            _ <- Sync[F].delay(
              stmnt.executeUpdate(
                "INSERT INTO parties (party, x_party) VALUES ('self', 1)"
              )
            )
            _ <- Sync[F].delay(
              stmnt.executeUpdate(
                "INSERT INTO contracts (contract, y_contract, prover) VALUES ('default', 1, 'signatureProver')"
              )
            )
            _ <- Sync[F].delay(
              stmnt.executeUpdate(
                "INSERT INTO contracts (contract, y_contract, prover) VALUES ('genesis', 2, 'heightProver')"
              )
            )
            _ <- Sync[F].delay(stmnt.close())
          } yield ()
        }
      }

    }
}
