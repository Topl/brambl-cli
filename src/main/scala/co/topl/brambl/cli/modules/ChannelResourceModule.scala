package co.topl.brambl.cli.modules

import cats.effect.kernel.Resource
import cats.effect.kernel.Sync
import io.grpc.ManagedChannelBuilder

trait ChannelResourceModule {
  def channelResource[F[_]: Sync](
      address: String,
      port: Int,
      secureConnection: Boolean
  ) = {
    Resource
      .make {
        Sync[F].delay(
          if (secureConnection)
            ManagedChannelBuilder
              .forAddress(address, port)
              .build
          else
            ManagedChannelBuilder
              .forAddress(address, port)
              .usePlaintext()
              .build
        )
      }(channel => Sync[F].delay(channel.shutdown()))
  }

}
