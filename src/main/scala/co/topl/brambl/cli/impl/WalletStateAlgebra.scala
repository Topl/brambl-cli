package co.topl.brambl.cli.impl

import cats.data.Validated
import cats.data.ValidatedNel
import cats.effect.kernel.Resource
import cats.effect.kernel.Sync
import co.topl.brambl.builders.locks.LockTemplate
import co.topl.brambl.models.Indices
import co.topl.brambl.models.box.Lock
import co.topl.brambl.utils.Encoding
import co.topl.brambl.wallet.WalletStateAlgebra
import quivr.models.{Preimage, Proposition, VerificationKey}
import co.topl.brambl.codecs.LockTemplateCodecs.{decodeLockTemplate, encodeLockTemplate}
import io.circe.parser._
import io.circe.syntax.EncoderOps

object WalletStateAlgebra {

  def make[F[_]: Sync](
      connection: Resource[F, java.sql.Connection],
      transactionBuilderApi: TransactionBuilderApi[F]
  ): WalletStateAlgebra[F] =
    new WalletStateAlgebra[F] {

      override def getIndicesBySignature(
          signatureProposition: Proposition.DigitalSignature
      ): F[Option[Indices]] = connection.use { conn =>
        import cats.implicits._
        for {
          stmnt <- Sync[F].blocking(conn.createStatement())
          rs <- Sync[F].blocking(
            stmnt.executeQuery(
              s"SELECT x_party, y_contract, z_state, routine, vk FROM " +
                s"cartesian WHERE routine = '${signatureProposition.routine}' AND " +
                s"vk = '${Encoding.encodeToBase58(signatureProposition.verificationKey.toByteArray)}'"
            )
          )
          x <- Sync[F].delay(rs.getInt("x_party"))
          y <- Sync[F].delay(rs.getInt("y_contract"))
          z <- Sync[F].delay(rs.getInt("z_state"))
        } yield if(rs.next()) Some(Indices(x, y, z)) else None
      }

      def getLockByIndex(indices: Indices): F[Option[Lock.Predicate]] =
        connection.use { conn =>
          import cats.implicits._
          for {
            stmnt <- Sync[F].blocking(conn.createStatement())
            rs <- Sync[F].blocking(
              stmnt.executeQuery(
                s"SELECT x_party, y_contract, z_state, lock_predicate FROM " +
                  s"cartesian WHERE x_party = ${indices.x} AND " +
                  s"y_contract = ${indices.y} AND " +
                  s"z_state = ${indices.z}"
              )
            )
            lock_predicate <- Sync[F].delay(rs.getString("lock_predicate"))
          } yield Some(
            Lock.Predicate.parseFrom(
              Encoding.decodeFromBase58Check(lock_predicate).toOption.get
            )
          )
        }

      override def updateWalletState(
          lockAddress: String,
          lock_predicate: String,
          routine: Option[String],
          vk: Option[String],
          indices: Indices
      ): F[Unit] = {
        connection.use { conn =>
          import cats.implicits._
          for {
            stmnt <- Sync[F].blocking(conn.createStatement())
            statement =
              s"INSERT INTO cartesian (x_party, y_contract, z_state, lock_predicate, address, routine, vk) VALUES (${indices.x}, ${indices.y}, ${indices.z}, '${lock_predicate}', '" +
                lockAddress + "', " + routine
                  .map(x => s"'$x'")
                  .getOrElse("NULL") + ", " + vk
                  .map(x => s"'$x'")
                  .getOrElse("NULL") + ")"
            _ <- Sync[F].blocking(
              stmnt.executeUpdate(statement)
            )
          } yield ()
        }
      }

      override def getNextIndicesForFunds(
          party: String,
          contract: String
      ): F[Option[Indices]] = {
        connection.use { conn =>
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
            z <- Sync[F].delay(rs.getInt("z_index"))
          } yield if (x == 0) None else Some(Indices(x, y, z + 1))
        }
      }

      private def validateParty(
          party: String
      ): F[ValidatedNel[String, String]] =
        connection.use { conn =>
          import cats.implicits._
          for {
            stmnt <- Sync[F].blocking(conn.createStatement())
            rs <- Sync[F].blocking(
              stmnt.executeQuery(
                s"SELECT x_party, party FROM parties WHERE party = '${party}'"
              )
            )
          } yield
            if (rs.next()) Validated.validNel(party)
            else Validated.invalidNel("Party not found")
        }

      private def validateContract(
          contract: String
      ): F[ValidatedNel[String, String]] =
        connection.use { conn =>
          import cats.implicits._
          for {
            stmnt <- Sync[F].blocking(conn.createStatement())
            rs <- Sync[F].blocking(
              stmnt.executeQuery(
                s"SELECT y_contract, contract FROM contracts WHERE contract = '${contract}'"
              )
            )
          } yield
            if (rs.next()) Validated.validNel(contract)
            else Validated.invalidNel("Contract not found")
        }

      def validateCurrentIndicesForFunds(
          party: String,
          contract: String,
          someState: Option[Int]
      ): F[ValidatedNel[String, Indices]] = {
        import cats.implicits._
        for {
          validatedParty <- validateParty(party)
          validatedContract <- validateContract(contract)
          indices <- getCurrentIndicesForFunds(party, contract, someState)
        } yield (
          validatedParty,
          validatedContract,
          indices.toValidNel("Indices not found")
        ).mapN((_, _, index) => index)
      }

      override def getAddress(
          party: String,
          contract: String,
          someState: Option[Int]
      ): F[Option[String]] = {
        connection.use { conn =>
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
                s"SELECT address, x_party, y_contract, " + someState
                  .map(_ => "z_state as z_index")
                  .getOrElse(
                    "MAX(z_state) as z_index"
                  ) + s" FROM cartesian WHERE x_party = ${x} AND y_contract = ${y}" + someState
                  .map(x => s" AND z_state = ${x}")
                  .getOrElse("")
              )
            )
            address <- Sync[F].delay(rs.getString("address"))
          } yield if (rs.next()) Some(address) else None
        }
      }

      override def getCurrentIndicesForFunds(
          party: String,
          contract: String,
          someState: Option[Int]
      ): F[Option[Indices]] = {
        connection.use { conn =>
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
                s"SELECT x_party, y_contract, " + someState
                  .map(_ => "z_state as z_index")
                  .getOrElse(
                    "MAX(z_state) as z_index"
                  ) + s" FROM cartesian WHERE x_party = ${x} AND y_contract = ${y}" + someState
                  .map(x => s" AND z_state = ${x}")
                  .getOrElse("")
              )
            )
            z <- someState
              .map(x => Sync[F].point(x))
              .getOrElse(Sync[F].delay(rs.getInt("z_index")))
          } yield if (rs.next()) Some(Indices(x, y, z)) else None
        }
      }

      override def getCurrentAddress(): F[String] = {
        connection.use { conn =>
          import cats.implicits._
          for {
            stmnt <- Sync[F].blocking(conn.createStatement())
            rs <- Sync[F].blocking(
              stmnt.executeQuery(
                "SELECT address, MAX(z_state) FROM cartesian WHERE x_party = 1 AND y_contract = 1  group by x_party, y_contract"
              )
            )
            lockAddress <- Sync[F].delay(rs.getString("address"))
          } yield lockAddress
        }
      }

      override def initWalletState(
          vk: VerificationKey
      ): F[Unit] = {
        import TransactionBuilderApi.implicits._
        connection.use { conn =>
          import cats.implicits._
          for {
            stmnt <- Sync[F].delay(conn.createStatement())
            _ <- Sync[F].delay(
              stmnt.execute(
                "CREATE TABLE IF NOT EXISTS cartesian (id INTEGER PRIMARY KEY," +
                  " x_party INTEGER NOT NULL, y_contract INTEGER NOT NULL, z_state INTEGER NOT NULL, " +
                  "lock_predicate TEXT NOT NULL, address TEXT NOT NULL, routine TEXT, vk TEXT)"
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
                  " y_contract INTEGER NOT NULL,  lock TEXT NOT NULL)"
              )
            )
            _ <- Sync[F].delay(
              stmnt.execute(
                "CREATE TABLE IF NOT EXISTS verification_keys (x_party INTEGER NOT NULL," +
                  " y_contract INTEGER NOT NULL, vks TEXT NOT NULL)"
              )
            )
            _ <- Sync[F].delay(
              stmnt.execute(
                "CREATE INDEX IF NOT EXISTS cartesian_coordinates ON cartesian (x_party, y_contract, z_state)"
              )
            )
            _ <- Sync[F].delay(
              stmnt.execute(
                "CREATE INDEX IF NOT EXISTS signature_idx ON cartesian (routine, vk)"
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
            heighPredicate <- transactionBuilderApi
              .lockPredicateHeight(
                1,
                Long.MaxValue
              )
            heightLockAddress <- transactionBuilderApi
              .lockAddress(
                heighPredicate
              )
            _ <- Sync[F].delay(
              stmnt.executeUpdate(
                "INSERT INTO cartesian (x_party, y_contract, z_state, lock_predicate, address, routine, vk) VALUES (1, 1, 1, '" +
                  Encoding
                    .encodeToBase58Check(predicate.toByteArray) +
                  "', '" +
                  lockAddress.toBase58 + "', " + "'ExtendedEd25519', " + "'" +
                  Encoding
                    .encodeToBase58Check(vk.toByteArray)
                  + "'" + ")"
              )
            )
            _ <- Sync[F].delay(
              stmnt.executeUpdate(
                "INSERT INTO cartesian (x_party, y_contract, z_state, lock_predicate, address) VALUES (0, 2, 1, '" +
                  Encoding
                    .encodeToBase58Check(heighPredicate.toByteArray) +
                  "', '" +
                  heightLockAddress.toBase58 + "')"
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
                "INSERT INTO contracts (contract, y_contract, lock) VALUES ('default', 1, 'signatureProver')"
              )
            )
            _ <- Sync[F].delay(
              stmnt.executeUpdate(
                "INSERT INTO contracts (contract, y_contract, lock) VALUES ('genesis', 2, 'heightProver')"
              )
            )
            _ <- Sync[F].delay(stmnt.close())
          } yield ()
        }
      }

      override def getPreimage(digestProposition: Proposition.Digest): F[Option[Preimage]] =
        Sync[F].delay(None) // We are not yet supporting Digest Propositions in brambl-cli

      override def addEntityVks(party: String, contract: String, entities: List[String]): F[Unit] = connection.use { conn =>
        import cats.implicits._
        for {
          stmnt <- Sync[F].blocking(conn.createStatement())
          rs <- Sync[F].blocking(stmnt.executeQuery(s"SELECT x_party FROM parties WHERE party = '${party}'"))
          x <- Sync[F].delay(rs.getInt("x_party"))
          rs <- Sync[F].blocking(stmnt.executeQuery(s"SELECT y_contract FROM contracts WHERE contract = '${contract}'"))
          y <- Sync[F].delay(rs.getInt("y_contract"))
          statement =
            s"INSERT INTO verification_keys (x_party, y_contract, vks) VALUES (${x}, ${y}, " +
              s"'${entities.asJson.toString}'"
          _ <- Sync[F].blocking(
            stmnt.executeUpdate(statement)
          )
        } yield ()
      }

      override def getEntityVks(party: String, contract: String): F[Option[List[String]]] = connection.use { conn =>
        import cats.implicits._
        for {
          stmnt <- Sync[F].blocking(conn.createStatement())
          rs <- Sync[F].blocking(stmnt.executeQuery(s"SELECT x_party FROM parties WHERE party = '${party}'"))
          x <- Sync[F].delay(rs.getInt("x_party"))
          rs <- Sync[F].blocking(stmnt.executeQuery(s"SELECT y_contract FROM contracts WHERE contract = '${contract}'"))
          y <- Sync[F].delay(rs.getInt("y_contract"))
          rs <- Sync[F].blocking(stmnt.executeQuery(s"SELECT vks FROM verification_keys WHERE x_party = ${x} AND y_contract = ${y}"))
          vks <- Sync[F].delay(rs.getString("lock"))
        } yield if (!rs.next()) None else parse(vks).toOption.flatMap(_.as[List[String]].toOption)
      }

      override def addNewLockTemplate(contract: String, lockTemplate: LockTemplate[F]): F[Unit] = connection.use { conn =>
        import cats.implicits._
        for {
          stmnt <- Sync[F].blocking(conn.createStatement())
          rs <- Sync[F].blocking(stmnt.executeQuery(s"SELECT MAX(y_contract) as y_index FROM contracts"))
          y <- Sync[F].delay(rs.getInt("y_index"))
          statement =
            s"INSERT INTO contracts (contract, y_contract, lock) VALUES ('${contract}', ${y+1}, '${encodeLockTemplate(lockTemplate).toString}'"
          _ <- Sync[F].blocking(
            stmnt.executeUpdate(statement)
          )
        } yield ()
      }
      override def getLockTemplate(contract: String): F[Option[LockTemplate[F]]] = connection.use { conn =>
        import cats.implicits._
        for {
          stmnt <- Sync[F].blocking(conn.createStatement())
          rs <- Sync[F].blocking(stmnt.executeQuery(s"SELECT lock FROM contracts WHERE contract = '${contract}'"))
          lockStr <- Sync[F].delay(rs.getString("lock"))
        } yield if(!rs.next()) None else parse(lockStr).toOption.flatMap(decodeLockTemplate[F](_).toOption)
      }
    }
}
