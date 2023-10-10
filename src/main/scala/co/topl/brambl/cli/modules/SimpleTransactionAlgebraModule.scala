package co.topl.brambl.cli.modules

import co.topl.brambl.constants.NetworkConstants
import cats.effect.IO
import co.topl.brambl.cli.impl.SimpleTransactionAlgebra

trait SimpleTransactionAlgebraModule
    extends WalletStateAlgebraModule
    with WalletManagementUtilsModule
    with GenusQueryAlgebraModule {

  def simplTransactionOps(
      walletFile: String,
      networkId: Int,
      host: String,
      bifrostPort: Int
  ) = SimpleTransactionAlgebra
    .make[IO](
      walletApi,
      walletStateAlgebra(walletFile),
      genusQueryAlgebra(
        host,
        bifrostPort
      ),
      transactionBuilderApi(
        networkId,
        NetworkConstants.MAIN_LEDGER_ID
      ),
      walletManagementUtils
    )
}
