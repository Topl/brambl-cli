package co.topl.brambl.cli.impl

import cats.effect.kernel.Resource
import co.topl.brambl.cli.model.WalletContract
import cats.effect.kernel.Sync
import io.circe.Json

trait ContractStorageAlgebra[F[_]] {

  def findContracts(): F[Seq[WalletContract]]
}

object ContractStorageAlgebra {

  def make[F[_]: Sync](
      connection: Resource[F, java.sql.Connection]
  ): ContractStorageAlgebra[F] = new ContractStorageAlgebra[F] {

    override def findContracts(): F[Seq[WalletContract]] = {
      connection.use { conn =>
        import cats.implicits._
        import io.circe.parser._
        for {
          stmnt <- Sync[F].blocking(conn.createStatement())
          rs <- Sync[F].blocking(stmnt.executeQuery("SELECT * FROM contracts"))
        } yield {
          LazyList
            .unfold(rs) { rs =>
              if (rs.next()) {
                Some(
                  (
                    WalletContract(
                      rs.getInt("y_contract"),
                      rs.getString("contract"),
                      parse(rs.getString("lock")).toOption.get.noSpaces,
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
