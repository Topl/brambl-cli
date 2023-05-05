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
      opt[String]('i', "input")
        .action((x, c) => c.copy(someInputFile = Some(x)))
        .text("The input file. (optional)"),
      opt[String]('w', "password")
        .action((x, c) => c.copy(password = x))
        .text("Password for the encrypted key. (mandatory)"),
      opt[String]('h', "host")
        .action((x, c) => c.copy(host = x))
        .text("The host of the Bifrost node. (mandatory)"),
      opt[Int]('p', "port")
        .action((x, c) => c.copy(port = x))
        .text("Port Bifrost node. (mandatory)"),
      opt[String]('n', "network")
        .action((x, c) => c.copy(network = x))
        .text(
          "Network name: Possible values: mainnet, testnet, private. (mandatory)"
        ),
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
            )
        ),
      cmd("simpletransaction")
        .action((_, c) => c.copy(mode = "simpletransaction"))
        .text("Simple transaction mode")
        .children(
          cmd("create")
            .action((_, c) => c.copy(subcmd = "create"))
            .text("Create transaction")
            .children(
              opt[Option[String]]('t', "to")
                .action((x, c) => c.copy(toAddress = x))
                .text("Address to send polys to. (mandatory)"),
              opt[Long]('a', "amount")
                .action((x, c) => c.copy(amount = x))
                .text("Amount to send simple transaction")
            )
        )
    )
  }
}
