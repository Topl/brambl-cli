package co.topl.brambl.cli.modules

import co.topl.brambl.cli.impl.WalletManagementUtils
import cats.effect.IO

trait WalletManagementUtilsModule extends WalletApiModule {

  val walletManagementUtils =
    new WalletManagementUtils[IO](walletApi, walletKeyApi)
}
