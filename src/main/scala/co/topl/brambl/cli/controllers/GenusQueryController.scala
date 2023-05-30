package co.topl.brambl.cli.controllers

import cats.effect.IO
import cats.effect.kernel.Resource
import co.topl.brambl.cli.{BramblCliValidatedParams, DefaultDataApi}
import co.topl.brambl.cli.impl.GenusQueryAlgebra
import co.topl.brambl.cli.impl.TransactionBuilderApi
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

  def queryUtxoFromParams(params: BramblCliValidatedParams): IO[Unit] = {
    val transactionBuilderApi = TransactionBuilderApi.make[IO](
      params.network.networkId,
      NetworkConstants.MAIN_LEDGER_ID
    )
    val dataApi = new DefaultDataApi[IO]()
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
            .flatMap { txos =>
              import cats.implicits._
              (txos
                .map { txo =>
                  BlockDisplayOps.display(txo)
                })
                .map(x => IO(println(x)))
                .sequence
                .map(_ => ())
            }
        case None => IO.raiseError(new Exception("Address not found"))
      }

  }
}
