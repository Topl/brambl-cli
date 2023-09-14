package co.topl.brambl.cli.modules

import cats.effect.IO
import co.topl.brambl.cli.impl.SimpleMintingAlgebra

trait SimpleMintingAlgebraModule
    extends WalletStateAlgebraModule
    with WalletManagementUtilsModule
    with TransactionBuilderApiModule
    with GenusQueryAlgebraModule {

  def simpleMintingAlgebra(
      walletFile: String,
      networkId: Int,
      ledgerId: Int,
      host: String,
      bifrostPort: Int
  ) = SimpleMintingAlgebra.make[IO](
    walletApi,
    walletStateAlgebra(walletFile),
    walletManagementUtils,
    transactionBuilderApi(networkId, ledgerId),
    genusQueryAlgebra(host, bifrostPort)
  )

}
