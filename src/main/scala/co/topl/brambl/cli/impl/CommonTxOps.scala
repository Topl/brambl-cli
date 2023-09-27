package co.topl.brambl.cli.impl

import co.topl.genus.services.Txo

trait CommonTxOps {
  
  def computeLvlQuantity(lvlTxos: Seq[Txo]) = lvlTxos
    .foldLeft(
      BigInt(0)
    )((acc, x) =>
      acc + x.transactionOutput.value.value.lvl
        .map(y => BigInt(y.quantity.value.toByteArray))
        .getOrElse(BigInt(0))
    )

}
