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
    subcmd: String = "",
    password: String = "",
    somePassphrase: Option[String] = None,
    someOutputFile: Option[String] = None
)
final case class BramblCliValidatedParams(
    mode: BramblCliMode.Value,
    subcmd: BramblCliSubCmd.Value,
    password: String,
    somePassphrase: Option[String],
    someOutputFile: Option[String]
)
