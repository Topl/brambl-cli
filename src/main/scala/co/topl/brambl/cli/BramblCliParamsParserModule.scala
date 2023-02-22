package co.topl.brambl.cli

import scopt.OParser

object BramblCliParamsParserModule {
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
      opt[Option[String]]('a', "top-api-key")
        .action((x, c) => c.copy(someApiKey = x))
        .text(
          "the API key for the Topl network"
        ),
      cmd("transaction")
        .action((_, c) => c.copy(mode = "transaction"))
        .text("Transaction mode")
        .children(
          cmd("broadcast")
            .action((_, c) => c.copy(subcmd = "broadcast"))
            .text("Broadcast a transaction")
            .children(
              opt[Option[String]]('i', "input-file")
                .action((x, c) => c.copy(someInputFile = x))
                .text(
                  "the input file"
                )
            ),
          cmd("create")
            .action((_, c) => c.copy(subcmd = "create"))
            .text("Create a new transaction")
            .children(
              opt[Option[String]]('o', "output-file")
                .action((x, c) => c.copy(someOutputFile = x))
                .text(
                  "the output file"
                ),
              opt[Seq[String]]('f', "from-addresses")
                .action((x, c) => c.copy(fromAddresses = x))
                .text(
                  "the address(es) to send from"
                ),
              opt[Option[String]]("token")
                .action((x, c) => c.copy(someToken = x))
                .text(
                  "the token that we are sending, possible values: poly"
                ),
              opt[Map[String, Int]]('t', "to-addresses")
                .action((x, c) => c.copy(toAddresses = x))
                .text(
                  "the address(es) to send to"
                ),
              opt[String]('c', "change-address")
                .action((x, c) => c.copy(changeAddress = x))
                .text(
                  "the address to send change to"
                ),
              opt[Int]('e', "fee")
                .action((x, c) => c.copy(fee = x))
                .text(
                  "the fee to pay"
                )
            )
        ),
      cmd("wallet")
        .action((_, c) => c.copy(mode = "wallet"))
        .text("Wallet mode")
        .children(
          cmd("sign")
            .action((_, c) => c.copy(subcmd = "sign"))
            .text("Sign transaction")
            .children(
              opt[Option[String]]("token")
                .action((x, c) => c.copy(someToken = x))
                .text(
                  "the token that we are sending, possible values: poly"
                ),
              opt[Option[String]]('o', "output-file")
                .action((x, c) => c.copy(someOutputFile = x))
                .text(
                  "the outputfile"
                ),
              opt[Option[String]]('i', "input-file")
                .action((x, c) => c.copy(someInputFile = x))
                .text(
                  "the input file"
                ),
              opt[Option[String]]('p', "password")
                .action((x, c) => c.copy(somePassword = x))
                .text(
                  "the password for the keyfile"
                ),
              opt[Option[String]]('k', "keyfile")
                .action((x, c) => c.copy(someKeyfile = x))
                .text(
                  "the file that contains the operator key, for example keyfile.json"
                )
            ),
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
              opt[Option[String]]('k', "keyfile")
                .action((x, c) => c.copy(someKeyfile = x))
                .text(
                  "the file that contains the operator key, for example keyfile.json"
                )
            ),
          cmd("balance")
            .action((_, c) => c.copy(subcmd = "balance"))
            .text("Check balance of a wallet")
            .children(
              opt[Seq[String]]('f', "from-addresses")
                .action((x, c) => c.copy(fromAddresses = x))
                .text(
                  "the address(es) from which we get the balances"
                )
            )
        )
    )
  }
}
