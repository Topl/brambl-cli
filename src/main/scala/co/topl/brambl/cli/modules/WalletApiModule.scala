package co.topl.brambl.cli.modules

import co.topl.brambl.wallet.WalletApi

trait WalletApiModule extends DataApiModule {
  val walletApi = WalletApi.make(dataApi)
}
