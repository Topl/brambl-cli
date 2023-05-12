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
      opt[Option[String]]("walletdb")
        .action((x, c) => c.copy(someWalletFile = x))
        .text("Wallet DB file. (mandatory)"),
      opt[String]('n', "network")
        .action((x, c) => c.copy(network = x))
        .text(
          "Network name: Possible values: mainnet, testnet, private. (mandatory)"
        ),
      cmd("utxo")
        .action((_, c) => c.copy(mode = "utxo"))
        .text("Utxo mode")
        .children(
          cmd("query")
            .action((_, c) => c.copy(subcmd = "query"))
            .text("Query utxo")
            .children(
              opt[Option[String]]("from-party")
                .action((x, c) => c.copy(someFromParty = x))
                .text("Party where we are sending the funds from"),
              opt[Option[String]]("from-contract")
                .action((x, c) => c.copy(someFromContract = x))
                .text("Contract where we are sending the funds from"),
              opt[Option[String]]("from-state")
                .action((x, c) => c.copy(someFromState = x))
                .text("State from where we are sending the funds from")
            )
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
              opt[Option[String]]("from-party")
                .action((x, c) => c.copy(someFromParty = x))
                .text("Party where we are sending the funds from"),
              opt[Option[String]]("from-contract")
                .action((x, c) => c.copy(someFromContract = x))
                .text("Contract where we are sending the funds from"),
              opt[Option[String]]("from-state")
                .action((x, c) => c.copy(someFromState = x))
                .text("State from where we are sending the funds from"),
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
