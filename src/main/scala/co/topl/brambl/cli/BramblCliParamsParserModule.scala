package co.topl.brambl.cli

import scopt.OParser

object BramblCliParamsParserModule {
  val builder = OParser.builder[BramblCliParams]

  val hostPort = {
    import builder._
    Seq(
      opt[String]('h', "host")
        .action((x, c) => c.copy(host = x))
        .text("The host of the node. (mandatory)"),
      opt[Int]("bifrost-port")
        .action((x, c) => c.copy(bifrostPort = x))
        .text("Port Bifrost node. (mandatory)")
    )
  }
  val hostPortNetwork = {
    import builder._
    Seq(
      opt[String]('n', "network")
        .action((x, c) => c.copy(network = x))
        .text(
          "Network name: Possible values: mainnet, testnet, private. (mandatory)"
        )
        .validate(x =>
          if (NetworkIdentifiers.fromString(x).isDefined) success
          else
            failure(
              s"Network $x is not supported.  Possible values: mainnet, testnet, private."
            )
        ),
      opt[String]('h', "host")
        .action((x, c) => c.copy(host = x))
        .text("The host of the node. (mandatory)"),
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

  import builder._

  val contractsMode = cmd("contracts")
    .action((_, c) => c.copy(mode = BramblCliMode.contracts))
    .text("Contract mode")
    .children(
      cmd("list")
        .action((_, c) => c.copy(subcmd = BramblCliSubCmd.list))
        .text("List existing contracts")
        .children(
          opt[Option[String]]("walletdb")
            .action((x, c) => c.copy(someWalletFile = x))
            .text("Wallet DB file. (mandatory)")
        ),
      cmd("add")
        .action((_, c) => c.copy(subcmd = BramblCliSubCmd.add))
        .text("Add a new contracts")
        .children(
          opt[Option[String]]("walletdb")
            .action((x, c) => c.copy(someWalletFile = x))
            .text("Wallet DB file. (mandatory)"),
          opt[String]("contract-name")
            .action((x, c) => c.copy(contractName = x))
            .text("Name of the contract. (mandatory)"),
          opt[String]("contract-template")
            .action((x, c) => c.copy(lockTemplate = x))
            .text("Contract template. (mandatory)")
        )
    )

  val partiesMode = cmd("parties")
    .action((_, c) => c.copy(mode = BramblCliMode.parties))
    .text("Entity mode")
    .children(
      cmd("list")
        .action((_, c) => c.copy(subcmd = BramblCliSubCmd.list))
        .text("List existing parties")
        .children(
          opt[Option[String]]("walletdb")
            .action((x, c) => c.copy(someWalletFile = x))
            .text("Wallet DB file. (mandatory)")
        ),
      cmd("add")
        .action((_, c) => c.copy(subcmd = BramblCliSubCmd.add))
        .text("Add a new parties")
        .children(
          hostPortNetwork ++ Seq(
            opt[Option[String]]("walletdb")
              .action((x, c) => c.copy(someWalletFile = x))
              .text("Wallet DB file. (mandatory)"),
            opt[String]("party-name")
              .action((x, c) => c.copy(partyName = x))
              .text("Name of the party. (mandatory)")
          ): _*
        )
    )

  implicit val tokenTypeRead: scopt.Read[TokenType.Value] =
    scopt.Read.reads(TokenType.withName)

  val genusQueryMode = cmd("genus-query")
    .action((_, c) => c.copy(mode = BramblCliMode.genusquery))
    .text("Genus query mode")
    .children(
      cmd("utxo-by-address")
        .action((_, c) => c.copy(subcmd = BramblCliSubCmd.utxobyaddress))
        .text("Query utxo")
        .children(
          (coordinates ++ hostPort ++ Seq(
            opt[Option[String]]("walletdb")
              .action((x, c) => c.copy(someWalletFile = x))
              .text("Wallet DB file. (mandatory)"),
            opt[TokenType.Value]("token")
              .action((x, c) => c.copy(tokenType = x))
              .text("The token type. (optional). The valid token types are 'lvl', 'topl', 'asset' and 'all'")
              .optional()
          )): _*
        )
    )
  val bifrostQueryMode = cmd("bifrost-query")
    .action((_, c) => c.copy(mode = BramblCliMode.bifrostquery))
    .text("Bifrost query mode")
    .children(
      cmd("block-by-height")
        .action((_, c) => c.copy(subcmd = BramblCliSubCmd.blockbyheight))
        .text("Get the block at a given height")
        .children(
          (hostPort ++ Seq(
            opt[Long]("height")
              .action((x, c) => c.copy(height = x))
              .text("The height of the block. (mandatory)")
          )): _*
        ),
      cmd("block-by-id")
        .action((_, c) => c.copy(subcmd = BramblCliSubCmd.blockbyid))
        .text("Get the block with a given id")
        .children(
          (hostPort ++ Seq(
            opt[Option[String]]("block-id")
              .action((x, c) => c.copy(blockId = x))
              .text("The id of the block in base 58. (mandatory)")
          )): _*
        ),
      cmd("transaction-by-id")
        .action((_, c) => c.copy(subcmd = BramblCliSubCmd.transactionbyid))
        .text("Get the transaction with a given id")
        .children(
          (hostPort ++ Seq(
            opt[Option[String]]("transaction-id")
              .action((x, c) => c.copy(transactionId = x))
              .text("The id of the transaction in base 58. (mandatory)")
          )): _*
        )
    )

  val walletMode = cmd("wallet")
    .action((_, c) => c.copy(mode = BramblCliMode.wallet))
    .text("Wallet mode")
    .children(
      cmd("sync")
        .action((_, c) => c.copy(subcmd = BramblCliSubCmd.sync))
        .text("Sync wallet")
        .children(
          (hostPortNetwork ++ (Seq(
            opt[String]("party-name")
              .action((x, c) => c.copy(partyName = x))
              .text("Name of the party. (mandatory)"),
            opt[String]("contract-name")
              .action((x, c) => c.copy(contractName = x))
              .text("Name of the contract. (mandatory)"),
            opt[Option[String]]("walletdb")
              .action((x, c) => c.copy(someWalletFile = x))
              .text("Wallet DB file. (mandatory)")
          ))): _*
        ),
      cmd("init")
        .action((_, c) => c.copy(subcmd = BramblCliSubCmd.init))
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
              .text("Wallet DB file. (mandatory)"),
            opt[Option[String]]("mnemonicfile")
              .action((x, c) => c.copy(someMnemonicFile = x))
              .text("Mnemonic output file. (mandatory)")
          ) ++
            Seq(
              opt[String]('P', "passphrase")
                .action((x, c) => c.copy(somePassphrase = Some(x)))
                .text("Passphrase for the encrypted key. (optional))")
            )): _*
        ),
      cmd("recover-keys")
        .action((_, c) => c.copy(subcmd = BramblCliSubCmd.recoverkeys))
        .text("Recover Wallet Main Key")
        .children(
          (Seq(
            opt[String]('n', "network")
              .action((x, c) => c.copy(network = x))
              .text(
                "Network name: Possible values: mainnet, testnet, private. (mandatory)"
              ),
            opt[Seq[String]]('m', "mnemonic")
              .action((x, c) => c.copy(mnemonic = x))
              .text("Mnemonic for the key. (mandatory)"),
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
        .action((_, c) => c.copy(subcmd = BramblCliSubCmd.currentaddress))
        .text("Obtain current address")
        .children(
          opt[Option[String]]("walletdb")
            .action((x, c) => c.copy(someWalletFile = x))
            .text("Wallet DB file. (mandatory)")
        ),
      cmd("export-vk")
        .action((_, c) => c.copy(subcmd = BramblCliSubCmd.exportvk))
        .text("Export verification key")
        .children(
          (keyfileAndPassword ++ Seq(
            opt[String]('o', "output")
              .action((x, c) => c.copy(someOutputFile = Some(x)))
              .text("The output file."),
            opt[Option[String]]("walletdb")
              .action((x, c) => c.copy(someWalletFile = x))
              .text("Wallet DB file. (mandatory)"),
            opt[String]("party-name")
              .action((x, c) => c.copy(partyName = x))
              .text("Name of the party. (mandatory)"),
            opt[String]("contract-name")
              .action((x, c) => c.copy(contractName = x))
              .text("Name of the contract. (mandatory)"),
            opt[Option[String]]("state")
              .action((x, c) => c.copy(someFromState = x))
              .text("State from where we are sending the funds from")
          )): _*
        ),
      cmd("import-vks")
        .action((_, c) => c.copy(subcmd = BramblCliSubCmd.importvks))
        .text("Import verification key")
        .children(
          opt[Option[String]]("walletdb")
            .action((x, c) => c.copy(someWalletFile = x))
            .text("Wallet DB file. (mandatory)"),
          opt[String]("party-name")
            .action((x, c) => c.copy(partyName = x))
            .text("Name of the party. (mandatory)"),
          opt[String]("contract-name")
            .action((x, c) => c.copy(contractName = x))
            .text("Name of the contract. (mandatory)"),
          opt[Seq[String]]("input-vks")
            .action((x, c) => c.copy(inputVks = x))
            .text("The keys to import. (mandatory)")
        )
    )

  val transactionMode = cmd("tx")
    .action((_, c) => c.copy(mode = BramblCliMode.tx))
    .text("Transaction mode")
    .children(
      cmd("create")
        .action((_, c) => c.copy(subcmd = BramblCliSubCmd.create))
        .text("Create transaction")
        .children(
          ((hostPortNetwork ++ Seq(
            opt[String]('o', "output")
              .action((x, c) => c.copy(someOutputFile = Some(x)))
              .text("The output file. (mandatory)"),
            opt[String]('i', "input")
              .action((x, c) => c.copy(someInputFile = Some(x)))
              .text("The input file. (mandatory)")
          ))): _*
        )
    )
  val simpleTransactionMode = cmd("simpletransaction")
    .action((_, c) => c.copy(mode = BramblCliMode.simpletransaction))
    .text("Simple transaction mode")
    .children(
      cmd("create")
        .action((_, c) => c.copy(subcmd = BramblCliSubCmd.create))
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
                .text(
                  "Address to send LVLs to. (mandatory if to-party and to-contract are not provided)"
                ),
              opt[Option[String]]("to-party")
                .action((x, c) => c.copy(someToParty = x))
                .text(
                  "Party to send LVLs to. (mandatory if to is not provided)"
                ),
              opt[Option[String]]("to-contract")
                .action((x, c) => c.copy(someToContract = x))
                .text(
                  "Contract to send LVLs to. (mandatory if to is not provided)"
                ),
              opt[Long]('a', "amount")
                .action((x, c) => c.copy(amount = x))
                .text("Amount to send simple transaction")
            )): _*
        ),
      cmd("broadcast")
        .action((_, c) => c.copy(subcmd = BramblCliSubCmd.broadcast))
        .text("Broadcast transaction")
        .children(
          ((hostPortNetwork ++ Seq(
            opt[String]('i', "input")
              .action((x, c) => c.copy(someInputFile = Some(x)))
              .text("The input file. (mandatory)")
          ))): _*
        ),
      cmd("prove")
        .action((_, c) => c.copy(subcmd = BramblCliSubCmd.prove))
        .text("Prove transaction")
        .children(
          ((keyfileAndPassword ++ Seq(
            opt[String]('o', "output")
              .action((x, c) => c.copy(someOutputFile = Some(x)))
              .text("The output file. (mandatory)"),
            opt[String]('i', "input")
              .action((x, c) => c.copy(someInputFile = Some(x)))
              .text("The input file. (mandatory)")
          ))): _*
        )
    )

  val paramParser = {
    OParser.sequence(
      contractsMode,
      partiesMode,
      genusQueryMode,
      bifrostQueryMode,
      walletMode,
      transactionMode,
      simpleTransactionMode
    )
  }
}
