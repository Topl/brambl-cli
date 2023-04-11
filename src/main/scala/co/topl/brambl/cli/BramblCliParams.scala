package co.topl.brambl.cli


object BramblCliMode extends Enumeration {
  type BramblCliMode = Value

  val key = Value
}

object BramblCliSubCmd extends Enumeration {
  type BramblCliSubCmd = Value

  val generate = Value
}

object TokenType extends Enumeration {
  type TokenType = Value

  val poly = Value
}

final case class BramblCliParams(
    mode: String = "",
    subcmd: String = ""
)
final case class BramblCliValidatedParams(
    mode: BramblCliMode.Value,
    subcmd: BramblCliSubCmd.Value
)
