package co.topl.brambl.cli

import cats.effect.IO

trait DataApiModule {
  val dataApi = new DefaultWalletKeyApi[IO]()
}
