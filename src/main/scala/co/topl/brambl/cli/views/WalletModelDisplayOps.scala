package co.topl.brambl.cli.views

import co.topl.brambl.dataApi.{WalletEntity, WalletContract}

object WalletModelDisplayOps {

  def displayWalletEntityHeader(): String =
    s"""X Coordinate\tFellowship Name"""

  def displayWalletTemplateHeader(): String =
    s"""Y Coordinate\tTemplate Name\tLock Template"""

  def display(walletEntity: WalletEntity): String =
    s"""${walletEntity.xIdx}\t${walletEntity.name}"""

  def display(walletTemplate: WalletContract): String =
    s"""${walletTemplate.yIdx}\t${walletTemplate.name}\t${walletTemplate.lockTemplate}"""

}
