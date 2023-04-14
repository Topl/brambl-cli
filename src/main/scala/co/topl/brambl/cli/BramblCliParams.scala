package co.topl.brambl.cli


object BramblCliMode extends Enumeration {
  type BramblCliMode = Value

  val key = Value
}

object BramblCliSubCmd extends Enumeration {
  type BramblCliSubCmd = Value

  val generate, derive = Value
}

object TokenType extends Enumeration {
  type TokenType = Value

  val poly = Value
}

final case class BramblCliParams(
    mode: String = "",
    subcmd: String = "",
    password: String = "",
    coordinates: Seq[String] = Seq(),
    somePassphrase: Option[String] = None,
    someInputFile: Option[String] = None,
    someOutputFile: Option[String] = None
)
final case class BramblCliValidatedParams(
    mode: BramblCliMode.Value,
    subcmd: BramblCliSubCmd.Value,
    password: String,
    coordinates: Seq[String] = Seq(),
    somePassphrase: Option[String],
    someInputFile: Option[String] = None,
    someOutputFile: Option[String]
)
