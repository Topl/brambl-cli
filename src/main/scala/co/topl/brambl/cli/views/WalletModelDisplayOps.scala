package co.topl.brambl.cli.views

import co.topl.brambl.cli.model.WalletEntity
import co.topl.brambl.cli.model.WalletContract

object WalletModelDisplayOps {

  def displayWalletEntityHeader(): String =
    s"""X Coordinate\tParty Name"""

  def displayWalletContractHeader(): String =
    s"""Y Coordinate\tContract Name\tLock Template"""

  def display(walletEntity: WalletEntity): String =
    s"""${walletEntity.idx}\t${walletEntity.name}"""

  def display(walletContract: WalletContract): String =
    s"""${walletContract.idx}\t${walletContract.name}\t${walletContract.lockTemplate}"""

}
