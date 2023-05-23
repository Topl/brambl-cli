package co.topl.brambl.cli.controllers

import cats.effect.IO
import cats.effect.kernel.Resource
import co.topl.brambl.cli.BramblCliValidatedParams
import co.topl.brambl.cli.DefaultDataApi
import co.topl.brambl.cli.impl.TransactionBuilderApi
import co.topl.brambl.cli.impl.WalletAlgebra
import co.topl.brambl.cli.impl.WalletStateAlgebra
import co.topl.brambl.constants.NetworkConstants
import co.topl.brambl.wallet.WalletApi

import java.sql.Connection

class WalletController(walletResource: Resource[IO, Connection]) {

  def createWalletFromParams(
      params: BramblCliValidatedParams
  ): IO[Unit] = {
    val transactionBuilderApi = TransactionBuilderApi.make[IO](
      params.network.networkId,
      NetworkConstants.MAIN_LEDGER_ID
    )
    val walletStateAlgebra = WalletStateAlgebra.make[IO](
      walletResource,
      transactionBuilderApi
    )
    val dataApi = new DefaultDataApi[IO](walletStateAlgebra)

    val walletApi = WalletApi.make(dataApi)
    WalletAlgebra
      .make[IO](
        walletApi,
        walletStateAlgebra
      )
      .createWalletFromParams(params)
  }

  def currentaddress(
      params: BramblCliValidatedParams
  ): IO[Unit] = {
    val transactionBuilderApi = TransactionBuilderApi.make[IO](
      params.network.networkId,
      NetworkConstants.MAIN_LEDGER_ID
    )
    WalletStateAlgebra.make[IO](
      walletResource,
      transactionBuilderApi
    ).getCurrentAddress().flatMap(address => IO(println(address)))
  }
}
