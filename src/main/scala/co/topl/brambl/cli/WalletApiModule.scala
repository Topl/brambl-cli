package co.topl.brambl.cli

import co.topl.brambl.wallet.WalletApi

trait WalletApiModule extends DataApiModule {
  val walletApi = WalletApi.make(dataApi)
}
