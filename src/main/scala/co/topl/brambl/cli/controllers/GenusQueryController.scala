package co.topl.brambl.cli.controllers

import cats.Monad
import co.topl
import co.topl.brambl.cli.views.BlockDisplayOps
import co.topl.brambl.codecs.AddressCodecs
import co.topl.brambl.dataApi.GenusQueryAlgebra
import topl.brambl.dataApi.WalletStateAlgebra

class GenusQueryController[F[_]: Monad](
    walletStateAlgebra: WalletStateAlgebra[F],
    genusQueryAlgebra: GenusQueryAlgebra[F]
) {

  def queryUtxoFromParams(
      fromParty: String,
      fromContract: String,
      someFromState: Option[Int]
  ): F[Either[String, String]] = {

    import cats.implicits._
    walletStateAlgebra
      .getAddress(fromParty, fromContract, someFromState)
      .flatMap {
        case Some(address) =>
          genusQueryAlgebra
            .queryUtxo(AddressCodecs.decodeAddress(address).toOption.get)
            .map { txos =>
              if (txos.isEmpty) Left("No UTXO found")
              else
                Right((txos.map { txo =>
                  BlockDisplayOps.display(txo)
                }).mkString)
            }
        case None => Monad[F].pure(Left("Address not found"))
      }

  }
}
