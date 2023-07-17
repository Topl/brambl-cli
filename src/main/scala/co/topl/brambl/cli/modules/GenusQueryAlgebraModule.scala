package co.topl.brambl.cli.modules

import co.topl.brambl.dataApi.GenusQueryAlgebra
import cats.effect.IO

trait GenusQueryAlgebraModule extends ChannelResourceModule {

  def genusQueryAlgebra(host: String, port: Int) = GenusQueryAlgebra.make[IO](
    channelResource(
      host,
      port
    )
  )
}
