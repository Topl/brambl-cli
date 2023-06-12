package co.topl.brambl.cli

import scopt.OParser

object BramblCliParamsParserModule {
  val builder = OParser.builder[BramblCliParams]

  val hostPortNetwork = {
    import builder._
    Seq(
      opt[String]('n', "network")
        .action((x, c) => c.copy(network = x))
        .text(
          "Network name: Possible values: mainnet, testnet, private. (mandatory)"
        ),
      opt[String]('h', "host")
        .action((x, c) => c.copy(host = x))
        .text("The host of the Genus node. (mandatory)"),
      opt[Int]("genus-port")
        .action((x, c) => c.copy(genusPort = x))
        .text("Port Genus node. (mandatory)"),
      opt[Int]("bifrost-port")
        .action((x, c) => c.copy(bifrostPort = x))
        .text("Port Bifrost node. (mandatory)")
    )
  }

  val coordinates = {
    import builder._
    Seq(
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
  }

  val keyfileAndPassword = {
    import builder._
    Seq(
      opt[String]('k', "keyfile")
        .action((x, c) => c.copy(someKeyFile = Some(x)))
        .text("The key file."),
      opt[String]('w', "password")
        .action((x, c) => c.copy(password = x))
        .text("Password for the encrypted key. (mandatory)"),
      opt[Option[String]]("walletdb")
        .action((x, c) => c.copy(someWalletFile = x))
        .text("Wallet DB file. (mandatory)")
    )
  }

  val paramParser = {
    import builder._
    OParser.sequence(
      cmd("genus-query")
        .action((_, c) => c.copy(mode = "genusquery"))
        .text("Genus query mode")
        .children(
          cmd("utxo-by-address")
            .action((_, c) => c.copy(subcmd = "utxobyaddress"))
            .text("Query utxo")
            .children(
              (coordinates ++ hostPortNetwork ++ Seq(
                opt[Option[String]]("walletdb")
                  .action((x, c) => c.copy(someWalletFile = x))
                  .text("Wallet DB file. (mandatory)")
              )): _*
            )
        ),
      cmd("bifrost-query")
        .action((_, c) => c.copy(mode = "bifrostquery"))
        .text("Bifrost query mode")
        .children(
          cmd("block-by-height")
            .action((_, c) => c.copy(subcmd = "blockbyheight"))
            .text("Get the block at a given height")
            .children(
              (hostPortNetwork ++ Seq(
                opt[Long]("height")
                  .action((x, c) => c.copy(height = x))
                  .text("The height of the block. (mandatory)")
              )): _*
            ),
          cmd("block-by-id")
            .action((_, c) => c.copy(subcmd = "blockbyid"))
            .text("Get the block with a given id")
            .children(
              (hostPortNetwork ++ Seq(
                opt[Option[String]]("block-id")
                  .action((x, c) => c.copy(blockId = x))
                  .text("The id of the block in base 58. (mandatory)")
              )): _*
            ),
          cmd("transaction-by-id")
            .action((_, c) => c.copy(subcmd = "transactionbyid"))
            .text("Get the transaction with a given id")
            .children(
              (hostPortNetwork ++ Seq(
                opt[Option[String]]("transaction-id")
                  .action((x, c) => c.copy(transactionId = x))
                  .text("The id of the transaction in base 58. (mandatory)")
              )): _*
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
              (Seq(
                opt[String]('n', "network")
                  .action((x, c) => c.copy(network = x))
                  .text(
                    "Network name: Possible values: mainnet, testnet, private. (mandatory)"
                  ),
                opt[String]('w', "password")
                  .action((x, c) => c.copy(password = x))
                  .text("Password for the encrypted key. (mandatory)"),
                opt[String]('o', "output")
                  .action((x, c) => c.copy(someOutputFile = Some(x)))
                  .text("The output file. (optional)"),
                opt[Option[String]]("walletdb")
                  .action((x, c) => c.copy(someWalletFile = x))
                  .text("Wallet DB file. (mandatory)")
              ) ++
                Seq(
                  opt[String]('P', "passphrase")
                    .action((x, c) => c.copy(somePassphrase = Some(x)))
                    .text("Passphrase for the encrypted key. (optional))")
                )): _*
            ),
          cmd("current-address")
            .action((_, c) => c.copy(subcmd = "currentaddress"))
            .text("Initialize wallet")
        ),
      cmd("simpletransaction")
        .action((_, c) => c.copy(mode = "simpletransaction"))
        .text("Simple transaction mode")
        .children(
          cmd("create")
            .action((_, c) => c.copy(subcmd = "create"))
            .text("Create transaction")
            .children(
              ((coordinates ++ hostPortNetwork ++ keyfileAndPassword ++ Seq(
                opt[String]('o', "output")
                  .action((x, c) => c.copy(someOutputFile = Some(x)))
                  .text("The output file. (mandatory)")
              )) ++
                Seq(
                  opt[Option[String]]('t', "to")
                    .action((x, c) => c.copy(toAddress = x))
                    .text("Address to send LVLs to. (mandatory if toParty and toContract are not provided)"),
                  opt[Option[String]]('t', "toParty")
                    .action((x, c) => c.copy(someToParty = x))
                    .text("Party to send LVLs to. (mandatory if to (address) is not provided)"),
                  opt[Option[String]]('t', "toContract")
                    .action((x, c) => c.copy(someToContract = x))
                    .text("Contract to send LVLs to. (mandatory if to (address) is not provided)"),
                  opt[Long]('a', "amount")
                    .action((x, c) => c.copy(amount = x))
                    .text("Amount to send simple transaction")
                )): _*
            ),
          cmd("broadcast")
            .action((_, c) => c.copy(subcmd = "broadcast"))
            .text("Broadcast transaction")
            .children(
              ((hostPortNetwork ++ Seq(
                opt[String]('i', "input")
                  .action((x, c) => c.copy(someInputFile = Some(x)))
                  .text("The input file. (mandatory)")
              ))): _*
            ),
          cmd("prove")
            .action((_, c) => c.copy(subcmd = "prove"))
            .text("Prove transaction")
            .children(
              ((coordinates ++ keyfileAndPassword ++ Seq(
                opt[String]('o', "output")
                  .action((x, c) => c.copy(someOutputFile = Some(x)))
                  .text("The output file. (mandatory)"),
                opt[String]('i', "input")
                  .action((x, c) => c.copy(someInputFile = Some(x)))
                  .text("The input file. (mandatory)")
              ))): _*
            )
        )
    )
  }
}
