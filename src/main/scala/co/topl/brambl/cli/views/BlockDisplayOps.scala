package co.topl.brambl.cli.views

import co.topl.brambl.codecs.AddressCodecs
import co.topl.brambl.models.Datum
import co.topl.brambl.models.LockAddress
import co.topl.brambl.models.TransactionId
import co.topl.brambl.models.TransactionOutputAddress
import co.topl.brambl.models.box.FungibilityType
import co.topl.brambl.models.box.QuantityDescriptorType
import co.topl.brambl.models.box.Value
import co.topl.brambl.models.transaction.IoTransaction
import co.topl.brambl.models.transaction.SpentTransactionOutput
import co.topl.brambl.models.transaction.UnspentTransactionOutput
import co.topl.brambl.utils.Encoding
import co.topl.consensus.models.BlockId
import co.topl.genus.services.Txo
import com.google.protobuf.struct.Struct
import com.google.protobuf.struct
import com.google.protobuf
import co.topl.brambl.models.box.AssetMintingStatement

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
TransactionId : ${display(transaction.transactionId)}

Group Policies
==============

${transaction.groupPolicies.map(display).mkString("\n-----------\n")}

Series Policies
===============

${transaction.seriesPolicies.map(display).mkString("\n-----------\n")}

Asset Minting Statements
========================
  
${transaction.mintingStatements.map(display).mkString("\n-----------\n")}


Inputs
======
${if (transaction.inputs.isEmpty) ("No inputs")
      else transaction.inputs.map(display).mkString("\n-----------\n")}
Outputs
=======
${transaction.outputs.map(display).mkString("\n-----------\n")}
Datum        :
${display(transaction.datum)}
"""

  def display(datum: Datum): String =
    s"""
Value         : ${display(datum.value)}
"""

  def display(datumIoTransation: Datum.IoTransaction): String =
    s"""
Value      : ${Encoding.encodeToBase58(
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

  def fungibilityToString(ft: FungibilityType) = ft match {
    case FungibilityType.GROUP_AND_SERIES => "group-and-series"
    case FungibilityType.GROUP            => "group"
    case FungibilityType.SERIES           => "series"
  }

  def quantityDescriptorToString(qt: QuantityDescriptorType) = qt match {
    case QuantityDescriptorType.LIQUID       => "liquid"
    case QuantityDescriptorType.ACCUMULATOR  => "accumulator"
    case QuantityDescriptorType.FRACTIONABLE => "fractionable"
    case QuantityDescriptorType.IMMUTABLE    => "immutable"
  }

  def displayType(txoValue: Value.Value) =
    if (txoValue.isLvl) "LVL"
    else if (txoValue.isGroup) s"Group Constructor\n" +
      s"Id           : ${Encoding.encodeToHex(txoValue.group.get.groupId.value.toByteArray())}\n" +
      s"Fixed-Series : ${txoValue.group.get.fixedSeries.map(x => Encoding.encodeToHex(x.value.toByteArray())).getOrElse("NO FIXED SERIES")}"
    else if (txoValue.isSeries) s"Series Constructor\n" +
      s"Id           : ${Encoding.encodeToHex(txoValue.series.get.seriesId.value.toByteArray())}\n" +
      s"Fungibility  : ${fungibilityToString(txoValue.series.get.fungibility)}\n" +
      s"Token-Supply : ${txoValue.series.get.tokenSupply.getOrElse("UNLIMITED")}\n" +
      s"Quant-Descr. : ${quantityDescriptorToString(txoValue.series.get.quantityDescriptor)}"
    else if (txoValue.isAsset)
      s"Asset\n" +
        s"GroupId      : ${Encoding.encodeToHex(txoValue.asset.get.groupId.get.value.toByteArray())}\n" +
        s"SeriesId     : ${Encoding.encodeToHex(txoValue.asset.get.seriesId.get.value.toByteArray())}\n" + 
      s"Commitment   : ${txoValue.asset.get.commitment.map(x => Encoding.encodeToHex(x.toByteArray())).getOrElse("No commitment")}\n" +
      s"Ephemeral-Metadata: \n" +
      s"${txoValue.asset.get.ephemeralMetadata.map(x => displayFirst(x, 2)).getOrElse("No ephemeral metadata")}"
    else if (txoValue.isTopl) "TOPL"
    else "Unknown txo type"

  def display(groupPolicy: Datum.GroupPolicy): String =
    s"""
Label: ${groupPolicy.event.label}
Regitratioin-Utxo: ${display(groupPolicy.event.registrationUtxo)}
Fixed-Series: ${groupPolicy.event.fixedSeries.map(x => Encoding.encodeToHex(x.value.toByteArray())).getOrElse("No fixed series")}  
    """

    def display(mintingStatement: AssetMintingStatement): String = 
      s"""
Group-Token-Utxo: ${display(mintingStatement.groupTokenUtxo)}
Series-Token-Utxo: ${display(mintingStatement.seriesTokenUtxo)}
Quantity: ${BigInt(mintingStatement.quantity.value.toByteArray()).toString()}
Permanent-Metadata:
${mintingStatement.permanentMetadata.map(displayFirst(_, 2)).getOrElse("No permanent metadata")}
      """

    def display(seriesPolicy: Datum.SeriesPolicy): String = 
    s"""
Label: ${seriesPolicy.event.label}
Regitratioin-Utxo: ${display(seriesPolicy.event.registrationUtxo)}
Fungibility: ${fungibilityToString(seriesPolicy.event.fungibility)}
Quantity-Descriptor: ${quantityDescriptorToString(seriesPolicy.event.quantityDescriptor)}
Token-Supply: ${seriesPolicy.event.tokenSupply.getOrElse("UNLIMITED")}
Permanent-Metadata-Scheme:
${seriesPolicy.event.permanentMetadataScheme.map(displayFirst(_, 2)).getOrElse("No permanent metadata")}
Ephemeral-Metadata-Scheme:
${seriesPolicy.event.ephemeralMetadataScheme.map(displayFirst(_, 2)).getOrElse("No ephemeral metadata")}
    """

    def display(v: protobuf.struct.Value, indent: Int): String = v match {
      case struct.Value(protobuf.struct.Value.Kind.NullValue(_), _) => "null"
      case struct.Value(protobuf.struct.Value.Kind.Empty, _) => "empty"
      case struct.Value(protobuf.struct.Value.Kind.BoolValue(b), _) => b.toString()
      case struct.Value(protobuf.struct.Value.Kind.NumberValue(n), _) => n.toString()
      case struct.Value(protobuf.struct.Value.Kind.StringValue(s), _) => s
      case struct.Value(protobuf.struct.Value.Kind.ListValue(l), _) =>  l.values.map(s =>" "*indent + s"- ${display(s, 0)}").mkString("\n")
      case struct.Value(protobuf.struct.Value.Kind.StructValue(s), _) => " " * indent + display(s, indent)
    }
    def displayFirst(struct: Struct, indent: Int): String = {
      " " * indent + struct.fields.view.keys.map({key => 
        struct.fields.get(key).get.kind match {
          case protobuf.struct.Value.Kind.StructValue(s) => s"$key:\n" + " "*(indent + 2) + s"${display(s, indent + 2)}"
          case protobuf.struct.Value.Kind.ListValue(l) => s"$key:\n" + l.values.map(s =>" "*(indent + 2) + s"-${display(s, 0)}").mkString("\n")
          case _ => s"$key: ${display(struct.fields.get(key).get, indent)}"
        }
      }).mkString("\n" + " " * indent)
    }
    def display(struct: Struct, indent: Int): String = {
      struct.fields.view.keys.map({key => 
        struct.fields.get(key).get.kind match {
          case protobuf.struct.Value.Kind.StructValue(s) => s"$key:\n" + " "*(indent + 2) + s"${display(s, indent + 2)}"
          case protobuf.struct.Value.Kind.ListValue(l) => s"$key:\n" + l.values.map(s =>" "*(indent + 2) + s"-${display(s, 0)}").mkString("\n")
          case _ => s"$key: ${display(struct.fields.get(key).get, indent)}"
        }
      }).mkString("\n" + " " * indent)
    }
    
  def display(txoValue: Value.Value) =
    if (txoValue.isLvl)
      BigInt(txoValue.lvl.get.quantity.value.toByteArray()).toString()
    else if (txoValue.isAsset)
      BigInt(txoValue.asset.get.quantity.value.toByteArray())
        .toString()
    else if (txoValue.isTopl)
      BigInt(txoValue.topl.get.quantity.value.toByteArray()).toString()
    else if (txoValue.isGroup)
      BigInt(txoValue.group.get.quantity.value.toByteArray()).toString()
    else if (txoValue.isSeries)
      BigInt(txoValue.series.get.quantity.value.toByteArray()).toString()
    else "Undefine type"

}
