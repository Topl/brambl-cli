package co.topl.brambl.cli.views

import co.topl.brambl.codecs.AddressCodecs
import co.topl.brambl.models.Datum
import co.topl.brambl.models.LockAddress
import co.topl.brambl.models.TransactionId
import co.topl.brambl.models.TransactionOutputAddress
import co.topl.brambl.models.box.Value
import co.topl.brambl.models.transaction.IoTransaction
import co.topl.brambl.models.transaction.SpentTransactionOutput
import co.topl.brambl.models.transaction.UnspentTransactionOutput
import co.topl.brambl.utils.Encoding
import co.topl.consensus.models.BlockId
import co.topl.genus.services.Txo

object BlockDisplayOps {

  def display(
      blockId: BlockId,
      ioTransactions: Seq[IoTransaction]
  ): String =
    s"""
BlockId: ${display(blockId)}

Block Body:
${ioTransactions.map(display).mkString("\n------------\n")}
"""

  def display(blockId: BlockId): String =
    Encoding.encodeToBase58(blockId.value.toByteArray())

  def display(transaction: IoTransaction): String =
    s"""
TransactionId: ${display(transaction.transactionId)}

Inputs       : ${if (transaction.inputs.isEmpty) ("No inputs")
      else transaction.inputs.map(display).mkString("\n-----------\n")}
Outputs      :
${transaction.outputs.map(display).mkString("\n-----------\n")}
Datum        :
${display(transaction.datum)}
"""

  def display(datum: Datum): String =
    s"""
Value      : ${display(datum.value)}
"""

  def display(datumIoTransation: Datum.IoTransaction): String =
    s"""
Value: ${Encoding.encodeToBase58(
        datumIoTransation.event.metadata.value.toByteArray()
      )}
"""

  def display(datumValue: Datum.Value): String =
    s"""
Eon        : 
${datumValue.eon
        .map(x => s"Begin Slot: ${x.event.beginSlot} Height: ${x.event.height}")
        .getOrElse("No eon")}
Epoch      : 
${datumValue.epoch
        .map(x => s"Begin Slot: ${x.event.beginSlot} Height: ${x.event.height}")
        .getOrElse("No eon")}
Era        : 
${datumValue.era
        .map(x => s"Begin Slot: ${x.event.beginSlot} Height: ${x.event.height}")
        .getOrElse("No eon")}
Header     : 
${datumValue.header.map(x => s"Height: ${x.event.height}").getOrElse("No eon")}
Metadata     : 
${datumValue.ioTransaction
        .map(x =>
          s"Value: ${Encoding.encodeToBase58(x.event.metadata.value.toByteArray())}"
        )
        .getOrElse("No eon")}
"""

  def display(stxo: SpentTransactionOutput): String =
    s"""
TxoAddress   : ${display(stxo.address)}
Attestation  : Not implemented
Value        : ${display(stxo.value.value)}
"""

  def display(stxo: UnspentTransactionOutput): String =
    s"""
LockAddress  : ${display(stxo.address)}
Type         : ${displayType(stxo.value.value)}
Value        : ${display(stxo.value.value)}
"""

  def display(txo: Txo): String =
    s"""
TxoAddress : ${display(txo.outputAddress)}
LockAddress: ${display(txo.transactionOutput.address)}
Type       : ${displayType(txo.transactionOutput.value.value)}
Value      : ${display(txo.transactionOutput.value.value)}
"""

  def display(transactionOutputAddress: TransactionOutputAddress) =
    s"${Encoding.encodeToBase58(
        transactionOutputAddress.id.value.toByteArray()
      )}#${transactionOutputAddress.index}"

  def display(transactionId: Option[TransactionId]): String =
    transactionId
      .map(id => Encoding.encodeToBase58(id.value.toByteArray()))
      .getOrElse("No transaction id")

  def display(lockAddress: LockAddress): String =
    AddressCodecs.encodeAddress(lockAddress)

  def displayType(txoValue: Value.Value) =
    if (txoValue.isLvl) "LVL"
    else if (txoValue.isAsset) "Asset"
    else if (txoValue.isTopl) "TOPL"
    else "Unknown txo type"

  def display(txoValue: Value.Value) =
    if (txoValue.isLvl)
      BigInt(txoValue.lvl.get.quantity.value.toByteArray()).toString()
    else if (txoValue.isAsset)
      BigInt(txoValue.asset.get.quantity.value.toByteArray())
        .toString() + txoValue.asset.get.groupId.get // TODO: adapt to when we need
    else if (txoValue.isTopl)
      BigInt(txoValue.topl.get.quantity.value.toByteArray()).toString()
    else "Undefine type"

}
