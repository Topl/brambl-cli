package co.topl.brambl.cli

import scopt.OParser

import java.io.File

trait BramblCliParamsParserModule {
  val builder = OParser.builder[BramblCliParams]

  val paramParser = {
    import builder._
    OParser.sequence(
      opt[String]('n', "network")
        .action((x, c) => c.copy(networkType = x))
        .text(
          "the password for the keyfile"
        ),
      opt[Option[String]]('u', "network-uri")
        .action((x, c) => c.copy(someNetworkUri = x))
        .text(
          "the URI of the network"
        ),
      cmd("wallet")
        .action((_, c) => c.copy(mode = "wallet"))
        .text("Wallet mode")
        .children(
          cmd("create")
            .action((_, c) => c.copy(subcmd = "create"))
            .text("Create a new wallet")
            .children(
              opt[Option[String]]('o', "output-file")
                .action((x, c) => c.copy(someOutputFile = x))
                .text(
                  "the outputfile"
                ),
              opt[Option[String]]('p', "password")
                .action((x, c) => c.copy(somePassword = x))
                .text(
                  "the password for the keyfile"
                ),
              opt[Option[File]]('k', "keyfile")
                .action((x, c) => c.copy(someKeyfile = x))
                .text(
                  "the file that contains the operator key, for example keyfile.json"
                )
            )
        )
    )
  }
}
