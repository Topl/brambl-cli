package co.topl.brambl.cli.views

import co.topl.brambl.display.DisplayOps.DisplayTOps
import co.topl.brambl.models.transaction.IoTransaction
import co.topl.consensus.models.BlockId

object BlockDisplayOps {

  def display(
      blockId: BlockId,
      ioTransactions: Seq[IoTransaction]
  ): String =
    s"""
BlockId: ${blockId.display}

Block Body:
${ioTransactions.map(_.display).mkString("\n------------\n")}
"""

}
