package co.topl.brambl.cli.modules

import cats.effect.IO
import co.topl.brambl.servicekit.WalletKeyApi

trait WalletKeyApiModule {
  val walletKeyApi = WalletKeyApi.make[IO]()
}
