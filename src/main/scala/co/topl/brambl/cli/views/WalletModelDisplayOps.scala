package co.topl.brambl.cli.views

import co.topl.brambl.cli.model.WalletEntity

object WalletModelDisplayOps {

  def displayWalletEntityHeader(): String =
    s"""X Coordinate\tParty Name"""

  def display(walletEntity: WalletEntity): String =
    s"""${walletEntity.idx}\t${walletEntity.name}"""

}
