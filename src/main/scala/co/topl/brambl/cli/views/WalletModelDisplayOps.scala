package co.topl.brambl.cli.views

import co.topl.brambl.dataApi.{WalletFellowship, WalletTemplate}

object WalletModelDisplayOps {

  def displayWalletFellowshipHeader(): String =
    s"""X Coordinate\tFellowship Name"""

  def displayWalletTemplateHeader(): String =
    s"""Y Coordinate\tTemplate Name\tLock Template"""

  def display(walletEntity: WalletFellowship): String =
    s"""${walletEntity.xIdx}\t${walletEntity.name}"""

  def display(walletTemplate: WalletTemplate): String =
    s"""${walletTemplate.yIdx}\t${walletTemplate.name}\t${walletTemplate.lockTemplate}"""

}
