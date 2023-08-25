package co.topl.brambl.cli.controllers

import cats.Monad
import co.topl
import co.topl.brambl.cli.views.BlockDisplayOps
import co.topl.brambl.codecs.AddressCodecs
import co.topl.brambl.dataApi.GenusQueryAlgebra
import topl.brambl.dataApi.WalletStateAlgebra
import topl.brambl.cli.TokenType

class GenusQueryController[F[_]: Monad](
    walletStateAlgebra: WalletStateAlgebra[F],
    genusQueryAlgebra: GenusQueryAlgebra[F]
) {

  def queryUtxoFromParams(
      fromParty: String,
      fromContract: String,
      someFromState: Option[Int],
      tokenType: TokenType.Value = TokenType.all
  ): F[Either[String, String]] = {

    import cats.implicits._
    walletStateAlgebra
      .getAddress(fromParty, fromContract, someFromState)
      .flatMap {
        case Some(address) =>
          genusQueryAlgebra
            .queryUtxo(AddressCodecs.decodeAddress(address).toOption.get)
            .map(_.filter{x => 
              import monocle.macros.syntax.lens._
              val lens = x.focus(_.transactionOutput.value.value)
              if (tokenType == TokenType.lvl)
                lens.get.isLvl
              else if (tokenType == TokenType.topl)
                lens.get.isTopl
              else if (tokenType == TokenType.asset)
                lens.get.isAsset
              else
                true
            })
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
