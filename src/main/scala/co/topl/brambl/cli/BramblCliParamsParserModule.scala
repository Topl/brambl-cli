package co.topl.brambl.cli

import scopt.OParser

object BramblCliParamsParserModule {
  val builder = OParser.builder[BramblCliParams]

  val paramParser = {
    import builder._
    OParser.sequence(
      opt[String]('o', "output")
        .action((x, c) => c.copy(someOutputFile = Some(x)))
        .text("The output file. (optional)"),
      opt[String]('p', "password")
        .action((x, c) => c.copy(password = x))
        .text("Password for the encrypted key. (mandatory)"),
      cmd("utxo")
        .action((_, c) => c.copy(mode = "utxo"))
        .text("UTXO mode")
        .children(
          cmd("query")
            .action((_, c) => c.copy(subcmd = "query"))
            .text("Query UTXOs")
        ),
      cmd("wallet")
        .action((_, c) => c.copy(mode = "wallet"))
        .text("Wallet mode")
        .children(
          cmd("init")
            .action((_, c) => c.copy(subcmd = "init"))
            .text("Initialize wallet")
            .children(
              opt[String]('P', "passphrase")
                .action((x, c) => c.copy(somePassphrase = Some(x)))
                .text("Passphrase for the encrypted key. (optional))")
            ),
          cmd("derive")
            .action((_, c) => c.copy(subcmd = "derive"))
            .text("Derive child key from parent key")
            .children(
              opt[Seq[String]]('C', "coordinates")
                .action((x, c) => c.copy(coordinates = x))
                .text("Coordinates for the derivation. (mandatory)"),
              opt[String]('i', "input")
                .action((x, c) => c.copy(someInputFile = Some(x)))
                .text("The input file. (mandatory)")
            )
        )
    )
  }
}
