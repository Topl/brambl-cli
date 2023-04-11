package co.topl.brambl.cli

import scopt.OParser

object BramblCliParamsParserModule {
  val builder = OParser.builder[BramblCliParams]

  val paramParser = {
    import builder._
    OParser.sequence(
      cmd("key")
        .action((_, c) => c.copy(mode = "key"))
        .text("Key mode")
        .children(
          cmd("generate")
            .action((_, c) => c.copy(subcmd = "generate"))
            .text("Sign transaction")
        )
    )
  }
}
