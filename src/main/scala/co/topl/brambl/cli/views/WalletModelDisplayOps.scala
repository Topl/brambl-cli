package co.topl.brambl.cli.views

import co.topl.brambl.dataApi.{WalletEntity, WalletContract}

object WalletModelDisplayOps {

  def displayWalletEntityHeader(): String =
    s"""X Coordinate\tFellowship Name"""

  def displayWalletContractHeader(): String =
    s"""Y Coordinate\tContract Name\tLock Template"""

  def display(walletEntity: WalletEntity): String =
    s"""${walletEntity.xIdx}\t${walletEntity.name}"""

  def display(walletContract: WalletContract): String =
    s"""${walletContract.yIdx}\t${walletContract.name}\t${walletContract.lockTemplate}"""

}
