package co.topl.brambl.cli.controllers

import cats.data.Validated
import cats.effect.IO
import cats.effect.kernel.Resource
import co.topl.brambl.cli.BramblCliValidatedParams
import co.topl.brambl.cli.DefaultDataApi
import co.topl.brambl.cli.impl.GenusQueryAlgebra
import co.topl.brambl.cli.impl.SimpleTransactionAlgebra
import co.topl.brambl.cli.impl.TransactionBuilderApi
import co.topl.brambl.cli.impl.WalletStateAlgebra
import co.topl.brambl.constants.NetworkConstants
import co.topl.brambl.wallet.WalletApi
import io.grpc.ManagedChannel

import java.sql.Connection

class SimpleTransactionController(
    walletResource: Resource[IO, Connection],
    genusChannelResource: Resource[IO, ManagedChannel],
    bifrostChannelResource: Resource[IO, ManagedChannel]
) {

  def broadcastSimpleTransactionFromParams(params: BramblCliValidatedParams) = {
    val transactionBuilderApi = TransactionBuilderApi.make[IO](
      params.network.networkId,
      NetworkConstants.MAIN_LEDGER_ID
    )
    val walletStateAlgebra = WalletStateAlgebra.make[IO](
      walletResource,
      transactionBuilderApi
    )
    val dataApi = new DefaultDataApi[IO]()
    val walletStateApi = WalletStateAlgebra.make[IO](
      walletResource,
      transactionBuilderApi
    )
    val walletApi = WalletApi.make(dataApi)
    val simplTransactionOps = SimpleTransactionAlgebra
      .make[IO](
        dataApi,
        walletApi,
        walletStateApi,
        GenusQueryAlgebra.make[IO](
          genusChannelResource
        ),
        transactionBuilderApi,
        bifrostChannelResource
      )
    simplTransactionOps.broadcastSimpleTransactionFromParams(
      params
    )
  }

  def proveSimpleTransactionFromParams(params: BramblCliValidatedParams) = {
    val transactionBuilderApi = TransactionBuilderApi.make[IO](
      params.network.networkId,
      NetworkConstants.MAIN_LEDGER_ID
    )
    val walletStateAlgebra = WalletStateAlgebra.make[IO](
      walletResource,
      transactionBuilderApi
    )
    val dataApi = new DefaultDataApi[IO]()
    val walletStateApi = WalletStateAlgebra.make[IO](
      walletResource,
      transactionBuilderApi
    )
    val walletApi = WalletApi.make(dataApi)
    val simplTransactionOps = SimpleTransactionAlgebra
      .make[IO](
        dataApi,
        walletApi,
        walletStateApi,
        GenusQueryAlgebra.make[IO](
          genusChannelResource
        ),
        transactionBuilderApi,
        bifrostChannelResource
      )
    walletStateApi.validateCurrentIndicesForFunds(
      params.fromParty,
      params.fromContract,
      params.someFromState
    ) flatMap {
      case Validated.Invalid(errors) =>
        IO.println("Invalid params") *> IO.println(
          errors.toList.mkString(", ")
        )
      case Validated.Valid(_) =>
        simplTransactionOps.proveSimpleTransactionFromParams(
          params
        )
    }
  }

  def createSimpleTransactionFromParams(
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
    val dataApi = new DefaultDataApi[IO]()
    val walletStateApi = WalletStateAlgebra.make[IO](
      walletResource,
      transactionBuilderApi
    )
    val walletApi = WalletApi.make(dataApi)
    val simplTransactionOps = SimpleTransactionAlgebra
      .make[IO](
        dataApi,
        walletApi,
        walletStateApi,
        GenusQueryAlgebra.make[IO](
          genusChannelResource
        ),
        transactionBuilderApi,
        bifrostChannelResource
      )
    walletStateApi.validateCurrentIndicesForFunds(
      params.fromParty,
      params.fromContract,
      params.someFromState
    ) flatMap {
      case Validated.Invalid(errors) =>
        IO.println("Invalid params") *> IO.println(
          errors.toList.mkString(", ")
        )
      case Validated.Valid(_) =>
        simplTransactionOps.createSimpleTransactionFromParams(
          params
        )
    }
  }
}
