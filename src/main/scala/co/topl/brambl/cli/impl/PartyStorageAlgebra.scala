package co.topl.brambl.cli.impl

import cats.effect.kernel.Resource
import cats.effect.kernel.Sync
import co.topl.brambl.cli.model.WalletEntity

trait PartyStorageAlgebra[F[_]] {

  def findParties(): F[Seq[WalletEntity]]

  def addParty(walletEntity: WalletEntity): F[Int]

}
object PartyStorageAlgebra {
  def make[F[_]: Sync](
      connection: Resource[F, java.sql.Connection]
  ): PartyStorageAlgebra[F] = new PartyStorageAlgebra[F] {

    override def addParty(walletEntity: WalletEntity): F[Int] = {
      connection.use { conn =>
        import cats.implicits._
        for {
          stmnt <- Sync[F].blocking(conn.createStatement())
          inserted <- Sync[F].blocking(
            stmnt.executeUpdate(
              s"INSERT INTO parties (party) VALUES ('${walletEntity.name}')"
            )
          )
        } yield inserted
      }
    }

    override def findParties(): F[Seq[WalletEntity]] = {
      connection.use { conn =>
        import cats.implicits._
        for {
          stmnt <- Sync[F].blocking(conn.createStatement())
          rs <- Sync[F].blocking(stmnt.executeQuery("SELECT * FROM parties"))
        } yield {
          LazyList
            .unfold(rs) { rs =>
              if (rs.next()) {
                Some(
                  (
                    WalletEntity(
                      rs.getInt("x_party"),
                      rs.getString("party")
                    ),
                    rs
                  )
                )
              } else {
                None
              }
            }
            .force
            .toSeq
        }
      }
    }
  }
}
