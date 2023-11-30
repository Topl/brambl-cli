package co.topl.app

import com.raquo.laminar.api.L._

object UIUtils {

  def hexToInt(s: String): Int = {
    BigInt(Validation.decodeFromHex(s).toOption.get).toInt
  }

  def isAmount(amount: String): Boolean =
    amount.matches("^[1-9]+([0-9]+)?$")

  def isExpanded(txSection: TxSection, currentSection: Var[TxSection]) =
    currentSection.signal.map { e =>
      if (txSection == e) "show"
      else ""
    }

}
