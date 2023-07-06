package co.topl.brambl.cli.modules

import cats.effect.IO
import co.topl.brambl.cli.DefaultWalletKeyApi

trait DataApiModule {
  val dataApi = new DefaultWalletKeyApi[IO]()
}
