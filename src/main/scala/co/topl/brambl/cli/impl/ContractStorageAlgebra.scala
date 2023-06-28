package co.topl.brambl.cli.impl

import cats.effect.kernel.Resource
import cats.effect.kernel.Sync
import co.topl.brambl.cli.model.WalletContract

trait ContractStorageAlgebra[F[_]] {

  def findContracts(): F[Seq[WalletContract]]

  def addContract(walletContract: WalletContract): F[Int]

}

object ContractStorageAlgebra {

  def make[F[_]: Sync](
      connection: Resource[F, java.sql.Connection]
  ): ContractStorageAlgebra[F] = new ContractStorageAlgebra[F] {

    override def addContract(walletContract: WalletContract): F[Int] = {
      connection.use { conn =>
        import cats.implicits._
        for {
          stmnt <- Sync[F].blocking(conn.createStatement())
          inserted <- Sync[F].blocking(
            stmnt.executeUpdate(
              s"INSERT INTO contracts (contract, lock) VALUES ('${walletContract.name}', '${walletContract.lockTemplate}')"
            )
          )
        } yield inserted
      }
    }

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
