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
      opt[String]('p', "password")
        .action((x, c) => c.copy(password = x))
        .text("Password for the encrypted key."),
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
            ),
          cmd("derive")
            .action((_, c) => c.copy(subcmd = "derive"))
            .text("Derive child key from parent key")
            .children(
              opt[Seq[String]]('C', "coordinates")
                .action((x, c) => c.copy(coordinates = x))
                .text("Coordinates for the derivation."),
              opt[String]('i', "input")
                .action((x, c) => c.copy(someInputFile = Some(x)))
                .text("The output file.")
            )
        )
    )
  }
}
