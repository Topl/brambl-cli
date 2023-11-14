package co.topl.brambl.cli.modules

import co.topl.brambl.dataApi.{GenusQueryAlgebra, RpcChannelResource}
import cats.effect.IO

trait GenusQueryAlgebraModule extends RpcChannelResource {

  def genusQueryAlgebra(host: String, port: Int, secureConnection: Boolean) =
    GenusQueryAlgebra.make[IO](
      channelResource(
        host,
        port,
        secureConnection
      )
    )
}
