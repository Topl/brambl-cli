package co.topl.brambl.cli.modules

import cats.effect.IO
import co.topl.brambl.servicekit.WalletStateApi
import co.topl.brambl.servicekit.WalletStateResource

trait WalletStateAlgebraModule
    extends WalletStateResource
    with WalletApiModule
    with TransactionBuilderApiModule {

  def walletStateAlgebra(file: String) = WalletStateApi
    .make[IO](
      walletResource(file),
      walletApi
    )
}
