package co.topl.brambl.cli.modules

import co.topl.brambl.cli.impl.WalletAlgebra

trait WalletAlgebraModule
    extends WalletStateAlgebraModule
    with WalletApiModule {
  def walletAlgebra(file: String) = WalletAlgebra.make(
    walletApi,
    walletStateAlgebra(file)
  )
}
