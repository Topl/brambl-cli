package co.topl.brambl.cli.controllers

import cats.effect.IO
import cats.effect.kernel.Resource
import co.topl.brambl.cli.{BramblCliValidatedParams, DefaultWalletKeyApi}
import co.topl.brambl.dataApi.GenusQueryAlgebra
import co.topl.brambl.builders.TransactionBuilderApi
import co.topl.brambl.cli.impl.WalletStateAlgebra
import co.topl.brambl.cli.views.BlockDisplayOps
import co.topl.brambl.codecs.AddressCodecs
import co.topl.brambl.constants.NetworkConstants
import co.topl.brambl.wallet.WalletApi
import io.grpc.ManagedChannel

import java.sql.Connection

class GenusQueryController(
    walletResource: Resource[IO, Connection],
    genusChannelResource: Resource[IO, ManagedChannel]
) {

  def queryUtxoFromParams(params: BramblCliValidatedParams): IO[String] = {
    val transactionBuilderApi = TransactionBuilderApi.make[IO](
      params.network.networkId,
      NetworkConstants.MAIN_LEDGER_ID
    )
    val dataApi = new DefaultWalletKeyApi[IO]()
    val walletApi = WalletApi.make(dataApi)
    WalletStateAlgebra
      .make[IO](
        walletResource,
        transactionBuilderApi,
        walletApi
      )
      .getAddress(params.fromParty, params.fromContract, params.someFromState)
      .flatMap {
        case Some(address) =>
          GenusQueryAlgebra
            .make[IO](genusChannelResource)
            .queryUtxo(AddressCodecs.decodeAddress(address).toOption.get)
            .map { txos =>
              (txos
                .map { txo =>
                  BlockDisplayOps.display(txo)
                })
                .mkString
            }
        case None => IO.raiseError(new Exception("Address not found"))
      }

  }
}
