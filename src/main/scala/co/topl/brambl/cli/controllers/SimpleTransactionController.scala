package co.topl.brambl.cli.controllers

import cats.data.Validated
import cats.effect.IO
import cats.effect.kernel.Resource
import co.topl.brambl.cli.BramblCliValidatedParams
import co.topl.brambl.cli.DefaultWalletKeyApi
import co.topl.brambl.dataApi.GenusQueryAlgebra
import co.topl.brambl.cli.impl.SimpleTransactionAlgebra
import co.topl.brambl.builders.TransactionBuilderApi
import co.topl.brambl.cli.impl.WalletStateAlgebra
import co.topl.brambl.constants.NetworkConstants
import co.topl.brambl.wallet.WalletApi
import io.grpc.ManagedChannel

import java.sql.Connection
import co.topl.brambl.cli.impl.WalletManagementUtils

class SimpleTransactionController(
    walletResource: Resource[IO, Connection],
    nodeChannelResource: Resource[IO, ManagedChannel]
) {

  def broadcastSimpleTransactionFromParams(params: BramblCliValidatedParams) = {
    val transactionBuilderApi = TransactionBuilderApi.make[IO](
      params.network.networkId,
      NetworkConstants.MAIN_LEDGER_ID
    )
    val dataApi = new DefaultWalletKeyApi[IO]()
    val walletApi = WalletApi.make(dataApi)
    val walletStateApi = WalletStateAlgebra.make[IO](
      walletResource,
      transactionBuilderApi,
      walletApi
    )
    val walletManagementUtils =
      new WalletManagementUtils[IO](walletApi, dataApi)
    val simplTransactionOps = SimpleTransactionAlgebra
      .make[IO](
        walletApi,
        walletStateApi,
        GenusQueryAlgebra.make[IO](
          nodeChannelResource
        ),
        transactionBuilderApi,
        walletManagementUtils,
        nodeChannelResource
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
    val dataApi = new DefaultWalletKeyApi[IO]()
    val walletApi = WalletApi.make(dataApi)
    val walletStateApi = WalletStateAlgebra.make[IO](
      walletResource,
      transactionBuilderApi,
      walletApi
    )
    val walletManagementUtils =
      new WalletManagementUtils[IO](walletApi, dataApi)
    val simplTransactionOps = SimpleTransactionAlgebra
      .make[IO](
        walletApi,
        walletStateApi,
        GenusQueryAlgebra.make[IO](
          nodeChannelResource
        ),
        transactionBuilderApi,
        walletManagementUtils,
        nodeChannelResource
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
    val dataApi = new DefaultWalletKeyApi[IO]()
    val walletApi = WalletApi.make(dataApi)
    val walletStateApi = WalletStateAlgebra.make[IO](
      walletResource,
      transactionBuilderApi,
      walletApi
    )
    val walletManagementUtils =
      new WalletManagementUtils[IO](walletApi, dataApi)
    val simplTransactionOps = SimpleTransactionAlgebra
      .make[IO](
        walletApi,
        walletStateApi,
        GenusQueryAlgebra.make[IO](
          nodeChannelResource
        ),
        transactionBuilderApi,
        walletManagementUtils,
        nodeChannelResource
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
