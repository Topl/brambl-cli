package co.topl.brambl.cli.modules

import cats.effect.IO
import co.topl.brambl.constants.NetworkConstants
import co.topl.brambl.servicekit.{WalletStateApi, WalletStateResource}

trait WalletStateAlgebraModule
    extends WalletStateResource
    with WalletApiModule
    with TransactionBuilderApiModule {

  def walletStateAlgebra(file: String, networkId: Int) = WalletStateApi
    .make[IO](
      walletResource(file),
      transactionBuilderApi(
        networkId,
        NetworkConstants.MAIN_LEDGER_ID
      ),
      walletApi
    )
}
