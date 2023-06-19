package co.topl.brambl.cli.controllers

import cats.effect.IO
import cats.effect.kernel.Resource
import co.topl.brambl.cli.BramblCliValidatedParams
import co.topl.brambl.cli.DefaultWalletKeyApi
import co.topl.brambl.builders.TransactionBuilderApi
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
    val dataApi = new DefaultWalletKeyApi[IO]()

    val walletApi = WalletApi.make(dataApi)
    val walletStateAlgebra = WalletStateAlgebra.make[IO](
      walletResource,
      transactionBuilderApi,
      walletApi
    )

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
    val dataApi = new DefaultWalletKeyApi[IO]()

    val walletApi = WalletApi.make(dataApi)
    val transactionBuilderApi = TransactionBuilderApi.make[IO](
      params.network.networkId,
      NetworkConstants.MAIN_LEDGER_ID
    )
    WalletStateAlgebra.make[IO](
      walletResource,
      transactionBuilderApi,
      walletApi
    ).getCurrentAddress.flatMap(address => IO(println(address)))
  }
}
