package co.topl.brambl.cli

import cats.effect.kernel.Resource
import cats.effect.kernel.Sync
import io.grpc.ManagedChannelBuilder

trait ChannelResourceModule {
  def channelResource[F[_]: Sync](address: String, port: Int) = {
    Resource
      .make {
        Sync[F].delay(
          ManagedChannelBuilder
            .forAddress(address, port)
            .usePlaintext()
            .build
        )
      }(channel => Sync[F].delay(channel.shutdown()))
  }

}
