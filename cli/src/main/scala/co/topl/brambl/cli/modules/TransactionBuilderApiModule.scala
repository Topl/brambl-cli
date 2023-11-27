package co.topl.brambl.cli.modules

import cats.effect.IO
import co.topl.brambl.builders.TransactionBuilderApi

trait TransactionBuilderApiModule {
  def transactionBuilderApi(networkId: Int, ledgerId: Int) =
    TransactionBuilderApi.make[IO](
      networkId,
      ledgerId
    )
}
