package co.topl.brambl.cli

import scopt.OParser

object BramblCliParamsParserModule {
  val builder = OParser.builder[BramblCliParams]

  val paramParser = {
    import builder._
    OParser.sequence(
      opt[String]('o', "output")
        .action((x, c) => c.copy(someOutputFile = Some(x)))
        .text("The output file."),
      cmd("key")
        .action((_, c) => c.copy(mode = "key"))
        .text("Key mode")
        .children(
          cmd("generate")
            .action((_, c) => c.copy(subcmd = "generate"))
            .text("Generate ")
            .children(
              opt[String]('p', "password")
                .action((x, c) => c.copy(password = x))
                .text("Password for the encrypted key.")
                .required(),
              opt[String]('P', "passphrase")
                .action((x, c) => c.copy(somePassphrase = Some(x)))
                .text("Passphrase for the encrypted key.")
            )
        )
    )
  }
}
