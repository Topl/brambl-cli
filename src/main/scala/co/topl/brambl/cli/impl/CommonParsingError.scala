package co.topl.brambl.cli.impl

sealed trait CommonParserError extends Throwable {
  val description: String
}

case object InvalidNetwork extends CommonParserError {
  val description = "Invalid network"
}
case object InvalidYaml extends CommonParserError {
  val description = "Invalid yaml"
}

case class UnknownError(t: Throwable) extends CommonParserError {
  val description = "There was an unknown error parsing the group policy"
}

case class PropositionParseError(description: String) extends CommonParserError
case class PropositionInstantationError(description: String)
    extends CommonParserError
case class InvalidAddress(description: String) extends CommonParserError

case class InvalidHex(description: String) extends CommonParserError
case class InvalidVerificationKey(description: String) extends CommonParserError
