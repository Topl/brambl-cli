package co.topl.brambl.cli.modules

import cats.effect.IO
import co.topl.brambl.cli.impl.TransactionAlgebra

trait TransactionAlgebraModule
    extends WalletStateAlgebraModule
    with WalletManagementUtilsModule
    with ChannelResourceModule {
      
  def transactionOps(
      walletFile: String,
      host: String,
      port: Int,
      secureConnection: Boolean
  ) = TransactionAlgebra
    .make[IO](
      walletApi,
      walletStateAlgebra(walletFile),
      walletManagementUtils,
      channelResource(
        host,
        port,
        secureConnection
      )
    )
}
