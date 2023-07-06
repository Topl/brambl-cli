package co.topl.brambl.cli.controllers

import cats.Monad
import cats.effect.IO
import co.topl.brambl.cli.BaseWalletStateAlgebra
import co.topl.brambl.codecs.AddressCodecs
import co.topl.brambl.dataApi.GenusQueryAlgebra
import co.topl.brambl.models.LockAddress
import co.topl.brambl.models.TransactionId
import co.topl.brambl.models.TransactionOutputAddress
import co.topl.brambl.models.box.Value
import co.topl.brambl.models.transaction.UnspentTransactionOutput
import co.topl.genus.services.Txo
import co.topl.genus.services.TxoState
import com.google.protobuf.ByteString
import munit.CatsEffectSuite
import quivr.models.Int128

class GenusQueryControllerTest extends CatsEffectSuite {

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
        val lockAddress = AddressCodecs
          .decodeAddress(
            "ptetP7jshHVrEKqDRdKAZtuybPZoMWTKKM2ngaJ7L5iZnxP5BprDB3hGJEFr"
          )
          .toOption
          .get
        Monad[F].pure(
          Seq(
            Txo(
              UnspentTransactionOutput(
                lockAddress,
                Value(
                  Value.Value.Lvl(
                    Value.LVL(
                      Int128(ByteString.copyFrom(BigInt(100L).toByteArray))
                    )
                  )
                )
              ),
              co.topl.genus.services.TxoState.UNSPENT,
              TransactionOutputAddress(
                lockAddress.network,
                lockAddress.ledger,
                1,
                TransactionId(
                  ByteString.copyFrom(
                    Array.fill[Byte](32)(0)
                  )
                )
              )
            )
          )
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
    assertIO(result, "Address not found")
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
      """
TxoAddress : 11111111111111111111111111111111#1
LockAddress: ptetP7jshHVrEKqDRdKAZtuybPZoMWTKKM2ngaJ7L5iZnxP5BprDB3hGJEFr
Type       : LVL
Value      : 100
"""
    )
  }
}
