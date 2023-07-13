package co.topl.brambl.cli.controllers

import cats.Monad
import cats.effect.IO
import co.topl.brambl.cli.mockbase.BaseWalletStateAlgebra
import co.topl.brambl.cli.modules.DummyObjects
import co.topl.brambl.dataApi.GenusQueryAlgebra
import co.topl.brambl.models.LockAddress
import co.topl.genus.services.Txo
import co.topl.genus.services.TxoState
import munit.CatsEffectSuite
import co.topl.brambl.cli.views.BlockDisplayOps

class GenusQueryControllerSpec extends CatsEffectSuite with DummyObjects {

  def makeWalletStateAlgebraMock[F[_]: Monad] = new BaseWalletStateAlgebra[F] {

    override def getAddress(
        party: String,
        contract: String,
        state: Option[Int]
    ): F[Option[String]] = Monad[F].pure(None)
  }
  def makeWalletStateAlgebraMockWithAddress[F[_]: Monad] =
    new BaseWalletStateAlgebra[F] {

      override def getAddress(
          party: String,
          contract: String,
          state: Option[Int]
      ): F[Option[String]] = Monad[F].pure(
        Some("ptetP7jshHVrEKqDRdKAZtuybPZoMWTKKM2ngaJ7L5iZnxP5BprDB3hGJEFr")
      )
    }

  def makeGenusQueryAlgebraMock[F[_]: Monad] = new GenusQueryAlgebra[F] {

    override def queryUtxo(
        fromAddress: LockAddress,
        txoState: TxoState
    ): F[Seq[Txo]] = Monad[F].pure(Seq.empty)

  }
  def makeGenusQueryAlgebraMockWithAddress[F[_]: Monad] =
    new GenusQueryAlgebra[F] {

      override def queryUtxo(
          fromAddress: LockAddress,
          txoState: TxoState
      ): F[Seq[Txo]] = {
        Monad[F].pure(
          Seq(txo01)
        )
      }
    }
  test(
    "queryUtxoFromParams should return an error if the address is not there"
  ) {
    val walletStateAlgebra = makeWalletStateAlgebraMock[IO]
    val genusQueryAlgebra = makeGenusQueryAlgebraMock[IO]
    val genusQueryController =
      new GenusQueryController[IO](walletStateAlgebra, genusQueryAlgebra)
    val result =
      genusQueryController.queryUtxoFromParams("party", "contract", None)
    assertIO(result, Left("Address not found"))
  }

  test(
    "queryUtxoFromParams should return a formatted string if the address is there"
  ) {
    val walletStateAlgebra = makeWalletStateAlgebraMockWithAddress[IO]
    val genusQueryAlgebra = makeGenusQueryAlgebraMockWithAddress[IO]
    val genusQueryController =
      new GenusQueryController[IO](walletStateAlgebra, genusQueryAlgebra)
    val result =
      genusQueryController.queryUtxoFromParams("party", "contract", None)
    assertIO(
      result,
      Right(BlockDisplayOps.display(txo01))
    )
  }
}
