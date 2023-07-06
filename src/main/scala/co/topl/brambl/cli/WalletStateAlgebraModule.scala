package co.topl.brambl.cli

import cats.effect.IO
import cats.effect.IO
import co.topl.brambl.cli.BramblCliValidatedParams
import co.topl.brambl.cli.controllers.GenusQueryController
import co.topl.brambl.cli.impl.WalletStateAlgebra
import co.topl.brambl.constants.NetworkConstants
import co.topl.brambl.dataApi.GenusQueryAlgebra

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
