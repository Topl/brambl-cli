package co.topl.brambl.cli.modules

import cats.effect.IO
import co.topl.brambl.cli.impl.WalletStateAlgebra
import co.topl.brambl.constants.NetworkConstants

trait WalletStateAlgebraModule
    extends WalletResourceModule
    with WalletApiModule
    with TransactionBuilderApiModule {

  def walletStateAlgebra(file: String, networkId: Int) = WalletStateAlgebra
    .make[IO](
      walletResource(file),
      transactionBuilderApi(
        networkId,
        NetworkConstants.MAIN_LEDGER_ID
      ),
      walletApi
    )
}
