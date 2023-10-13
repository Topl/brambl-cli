package co.topl.brambl.cli

import co.topl.brambl.codecs.AddressCodecs
import co.topl.brambl.models.LockAddress
import co.topl.brambl.utils.Encoding
import scopt.OParser

import java.io.File
import java.nio.file.Paths
import co.topl.brambl.models.GroupId
import com.google.protobuf.ByteString

object BramblCliParamsParserModule {

  implicit val tokenTypeRead: scopt.Read[TokenType.Value] =
    scopt.Read.reads(TokenType.withName)

  val builder = OParser.builder[BramblCliParams]

  import builder._

  implicit val networkRead: scopt.Read[NetworkIdentifiers] =
    scopt.Read.reads(NetworkIdentifiers.fromString(_).get)

  implicit val arrayByteRead: scopt.Read[GroupId] =
    scopt.Read.reads { x =>
      val array = Encoding.decodeFromHex(x).toOption.get
      GroupId(ByteString.copyFrom(array))
    }

  val inputFileArg = opt[String]('i', "input")
    .action((x, c) => c.copy(someInputFile = Some(x)))
    .text("The input file. (mandatory)")
    .validate(x =>
      if (x.trim().isEmpty) failure("Input file may not be empty")
      else if (!new java.io.File(x).exists())
        failure(s"Input file $x does not exist")
      else success
    )

  val feeArg = opt[Long]("fee")
    .action((x, c) => c.copy(fee = x))
    .text("Fee paid for the transaction")
    .validate(x =>
      if (x > 0) success
      else failure("Amount must be greater than 0")
    )
    .required()

  val passphraseArg =
    opt[String]('P', "passphrase")
      .action((x, c) => c.copy(somePassphrase = Some(x)))
      .text("Passphrase for the encrypted key. (optional))")
      .validate(x =>
        if (x.trim().isEmpty) failure("Passphrase may not be empty")
        else success
      )

  val amountArg = opt[Long]('a', "amount")
    .action((x, c) => c.copy(amount = x))
    .text("Amount to send")
    .validate(x =>
      if (x > 0) success
      else failure("Amount must be greater than 0")
    )

  val mintAmountArg = opt[Long]("mint-amount")
    .action((x, c) => c.copy(amount = x))
    .text("Amount to mint")
    .optional()

  val newwalletdbArg = opt[String]("newwalletdb")
    .action((x, c) => c.copy(walletFile = x))
    .text("Wallet DB file. (mandatory)")
    .validate(x =>
      if (Paths.get(x).toFile().exists()) {
        failure("Wallet file " + x + " already exists")
      } else {
        success
      }
    )

  val outputArg = opt[String]('o', "output")
    .action((x, c) => c.copy(someOutputFile = Some(x)))
    .text("The output file. (mandatory)")
    .validate(x =>
      if (x.trim().isEmpty) failure("Output file may not be empty")
      else if (Paths.get(x).toFile().exists()) {
        failure("Output file already exists")
      } else {
        success
      }
    )
    .required()

  val walletDbArg = opt[String]("walletdb")
    .action((x, c) => c.copy(walletFile = x))
    .validate(validateWalletDbFile(_))
    .text("Wallet DB file. (mandatory)")

  val contractNameArg = opt[String]("contract-name")
    .validate(x =>
      if (x.trim().isEmpty) failure("Contract name may not be empty")
      else success
    )
    .action((x, c) => c.copy(contractName = x))
    .text("Name of the contract. (mandatory)")

  val networkArg = opt[NetworkIdentifiers]('n', "network")
    .action((x, c) => c.copy(network = x))
    .text(
      "Network name: Possible values: mainnet, testnet, private. (mandatory)"
    )

  val passwordArg = opt[String]('w', "password")
    .action((x, c) => c.copy(password = x))
    .validate(x =>
      if (x.trim().isEmpty) failure("Password may not be empty")
      else success
    )
    .text("Password for the encrypted key. (mandatory)")

  val partyNameArg = opt[String]("party-name")
    .validate(x =>
      if (x.trim().isEmpty) failure("Party name may not be empty")
      else success
    )
    .action((x, c) => c.copy(partyName = x))
    .text("Name of the party. (mandatory)")

  def validateWalletDbFile(walletDbFile: String): Either[String, Unit] =
    if (walletDbFile.trim().isEmpty) failure("Wallet file may not be empty")
    else if (new java.io.File(walletDbFile).exists()) success
    else failure(s"Wallet file $walletDbFile does not exist")

  def hostArg =
    opt[String]('h', "host")
      .action((x, c) => c.copy(host = x))
      .text("The host of the node. (mandatory)")
      .validate(x =>
        if (x.trim().isEmpty) failure("Host may not be empty") else success
      )
      .required()

  def portArg = opt[Int]("port")
    .action((x, c) => c.copy(bifrostPort = x))
    .text("Port Bifrost node. (mandatory)")
    .validate(x =>
      if (x >= 0 && x <= 65536) success
      else failure("Port must be between 0 and 65536")
    )
    .required()

  val hostPort = Seq(
    hostArg,
    portArg
  )

  val groupId = opt[Option[GroupId]]("group-id")
    .action((x, c) => c.copy(someGroupId = x))
    .text("Group id.")

  val hostPortNetwork =
    Seq(
      networkArg,
      hostArg,
      portArg
    )

  val tokenType = opt[TokenType.Value]("token")
    .action((x, c) => c.copy(tokenType = x))
    .text(
      "The token type. The valid token types are 'lvl', 'topl', 'asset', 'group', 'series', and 'all'"
    )

  val mintTokenType = opt[TokenType.Value]("mint-token")
    .action((x, c) => c.copy(tokenType = x))
    .text(
      "The token type. The valid token types are 'asset', 'group', 'series'."
    )
    .validate(x =>
      if (x == TokenType.lvl || x == TokenType.topl || x == TokenType.all) {
        failure(
          "Invalid token type, supported types are asset, group and series"
        )
      } else {
        success
      }
    )

  val transferTokenType = opt[TokenType.Value]("transfer-token")
    .action((x, c) => c.copy(tokenType = x))
    .text(
      "The token type. The valid token types are 'lvl', 'asset', 'group', 'series'."
    )
    .validate(x =>
      if (x == TokenType.topl || x == TokenType.all) {
        failure(
          "Invalid token type, supported types are lvl, asset, group and series"
        )
      } else {
        success
      }
    )
    .required()

  val coordinates = {
    import builder._
    Seq(
      opt[String]("from-party")
        .action((x, c) => c.copy(fromParty = x))
        .text("Party where we are sending the funds from"),
      opt[String]("from-contract")
        .action((x, c) => c.copy(fromContract = x))
        .text("Contract where we are sending the funds from"),
      opt[Option[Int]]("from-state")
        .action((x, c) => c.copy(someFromState = x))
        .text("State from where we are sending the funds from"),
      checkConfig(c =>
        if (c.fromParty == "noparty") {
          if (c.someFromState.isEmpty) {
            failure("You must specify a from-state when using noparty")
          } else {
            success
          }
        } else {
          success
        }
      )
    )
  }

  val keyfileArg = opt[String]('k', "keyfile")
    .action((x, c) => c.copy(someKeyFile = Some(x)))
    .text("The key file.")
    .validate(x =>
      if (x.trim().isEmpty) failure("Key file may not be empty")
      else if (!new java.io.File(x).exists())
        failure(s"Key file $x does not exist")
      else success
    )

  val keyfileAndPassword = {
    Seq(
      keyfileArg,
      passwordArg,
      walletDbArg
    )
  }

  val contractsMode = cmd("contracts")
    .action((_, c) => c.copy(mode = BramblCliMode.contracts))
    .text("Contract mode")
    .children(
      cmd("list")
        .action((_, c) => c.copy(subcmd = BramblCliSubCmd.list))
        .text("List existing contracts")
        .children(
          walletDbArg
        ),
      cmd("add")
        .action((_, c) => c.copy(subcmd = BramblCliSubCmd.add))
        .text("Add a new contracts")
        .children(
          walletDbArg,
          contractNameArg,
          opt[String]("contract-template")
            .validate(x =>
              if (x.trim().isEmpty)
                failure("Contract template may not be empty")
              else success
            )
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
          walletDbArg
        ),
      cmd("add")
        .action((_, c) => c.copy(subcmd = BramblCliSubCmd.add))
        .text("Add a new parties")
        .children(
          Seq(
            walletDbArg,
            partyNameArg
          ): _*
        )
    )

  implicit val lockAddressRead: scopt.Read[LockAddress] =
    scopt.Read.reads(
      AddressCodecs
        .decodeAddress(_)
        .toOption
        .get
    )

  val genusQueryMode = cmd("genus-query")
    .action((_, c) => c.copy(mode = BramblCliMode.genusquery))
    .text("Genus query mode")
    .children(
      cmd("utxo-by-address")
        .action((_, c) => c.copy(subcmd = BramblCliSubCmd.utxobyaddress))
        .text("Query utxo")
        .children(
          (coordinates ++ hostPort ++ Seq(
            walletDbArg,
            tokenType.optional()
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
              .validate(x =>
                if (x >= 0) success
                else failure("Height must be greater than or equal to 0")
              )
          )): _*
        ),
      cmd("block-by-id")
        .action((_, c) => c.copy(subcmd = BramblCliSubCmd.blockbyid))
        .text("Get the block with a given id")
        .children(
          (hostPort ++ Seq(
            opt[String]("block-id")
              .validate(x =>
                Encoding.decodeFromBase58(x) match {
                  case Left(_)  => failure("Invalid block id")
                  case Right(_) => success
                }
              )
              .action((x, c) => c.copy(blockId = x))
              .text("The id of the block in base 58. (mandatory)")
          )): _*
        ),
      cmd("transaction-by-id")
        .action((_, c) => c.copy(subcmd = BramblCliSubCmd.transactionbyid))
        .text("Get the transaction with a given id")
        .children(
          (hostPort ++ Seq(
            opt[String]("transaction-id")
              .validate(x =>
                Encoding.decodeFromBase58(x) match {
                  case Left(_)  => failure("Invalid transaction id")
                  case Right(_) => success
                }
              )
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
          (hostPortNetwork ++ keyfileAndPassword ++ (Seq(
            partyNameArg,
            contractNameArg,
            walletDbArg
          ))): _*
        ),
      cmd("init")
        .action((_, c) => c.copy(subcmd = BramblCliSubCmd.init))
        .text("Initialize wallet")
        .children(
          (
            Seq(
              networkArg.required(),
              passwordArg.required(),
              outputArg.optional(),
              newwalletdbArg.required(),
              passphraseArg.optional(),
              opt[Option[String]]("mnemonicfile")
                .action((x, c) => c.copy(someMnemonicFile = x))
                .text("Mnemonic output file. (mandatory)")
                .required()
                .validate(x =>
                  x.map(f =>
                    if (Paths.get(f).toFile().exists()) {
                      failure("Mnemonic file already exists")
                    } else {
                      success
                    }
                  ).getOrElse(success)
                )
            )
          ): _*
        ),
      cmd("recover-keys")
        .action((_, c) => c.copy(subcmd = BramblCliSubCmd.recoverkeys))
        .text("Recover Wallet Main Key")
        .children(
          (
            Seq(
              networkArg,
              opt[Seq[String]]('m', "mnemonic")
                .action((x, c) => c.copy(mnemonic = x))
                .text("Mnemonic for the key. (mandatory)")
                .validate(x =>
                  if (List(12, 15, 18, 21, 24).contains(x.length)) success
                  else failure("Mnemonic must be 12, 15, 18, 21 or 24 words")
                ),
              passwordArg,
              outputArg,
              newwalletdbArg,
              passphraseArg
            )
          ): _*
        ),
      cmd("current-address")
        .action((_, c) => c.copy(subcmd = BramblCliSubCmd.currentaddress))
        .text("Obtain current address")
        .children(
          walletDbArg
        ),
      cmd("export-vk")
        .action((_, c) => c.copy(subcmd = BramblCliSubCmd.exportvk))
        .text("Export verification key")
        .children(
          (keyfileAndPassword ++ Seq(
            outputArg,
            walletDbArg,
            partyNameArg,
            contractNameArg,
            opt[Option[Int]]("state")
              .action((x, c) => c.copy(someFromState = x))
              .text("State from where we are sending the funds from")
          )): _*
        ),
      cmd("import-vks")
        .action((_, c) => c.copy(subcmd = BramblCliSubCmd.importvks))
        .text("Import verification key")
        .children(
          (keyfileAndPassword ++ Seq(
            partyNameArg,
            contractNameArg,
            opt[Seq[File]]("input-vks")
              .action((x, c) => c.copy(inputVks = x))
              .text("The keys to import. (mandatory)")
          )): _*
        )
    )

  val transactionMode = cmd("tx")
    .action((_, c) => c.copy(mode = BramblCliMode.tx))
    .text("Transaction mode")
    .children(
      cmd("inspect")
        .action((_, c) => c.copy(subcmd = BramblCliSubCmd.inspect))
        .text("Inspect transaction")
        .children(
          inputFileArg
        ),
      cmd("broadcast")
        .action((_, c) => c.copy(subcmd = BramblCliSubCmd.broadcast))
        .text("Broadcast transaction")
        .children(
          ((hostPortNetwork ++ Seq(
            inputFileArg.required()
          ))): _*
        ),
      cmd("prove")
        .action((_, c) => c.copy(subcmd = BramblCliSubCmd.prove))
        .text("Prove transaction")
        .children(
          ((keyfileAndPassword ++ Seq(
            outputArg.required(),
            inputFileArg.required()
          ))): _*
        ),
      cmd("create")
        .action((_, c) => c.copy(subcmd = BramblCliSubCmd.create))
        .text("Create transaction")
        .children(
          ((hostPortNetwork ++ Seq(
            outputArg,
            inputFileArg
          ))): _*
        )
    )

  val simpleMintingMode = cmd("simple-minting")
    .action((_, c) => c.copy(mode = BramblCliMode.simpleminting))
    .text("Simple minting mode")
    .children(
      cmd("create")
        .action((_, c) => c.copy(subcmd = BramblCliSubCmd.create))
        .text("Create minting transaction")
        .children(
          ((coordinates ++ hostPortNetwork ++ keyfileAndPassword ++ Seq(
            outputArg.required(),
            inputFileArg.required(),
            opt[String]("commitment")
              .action((x, c) => c.copy(someCommitment = Some(x)))
              .text(
                "The commitment to use, 32 bytes in hexadecimal formal. (optional)"
              )
              .validate(x =>
                Encoding
                  .decodeFromHex(x)
                  .fold(
                    _ => failure("Invalid commitment"),
                    a =>
                      if (a.length == 32) success
                      else failure("Invalid commitment: Length must be 32")
                  )
              ),
            opt[File]("ephemeralMetadata")
              .action((x, c) => c.copy(ephemeralMetadata = Some(x)))
              .text(
                "A file containing the JSON metadata for the ephemeral metadata of the asset. (optional)"
              )
              .validate(x =>
                if (x.exists()) success
                else failure("Ephemeral metadata file does not exist")
              )
          )) ++
            Seq(
              mintAmountArg,
              feeArg,
              mintTokenType.required(),
              checkConfig(c =>
                if (
                  c.mode == BramblCliMode.simpleminting &&
                  c.tokenType != TokenType.group &&
                  c.tokenType != TokenType.series &&
                  c.tokenType != TokenType.asset
                )
                  failure(
                    "Invalid asset to mint, supported assets are group, series and asset"
                  )
                else {
                  if (
                    c.mode == BramblCliMode.simpleminting &&
                    c.subcmd == BramblCliSubCmd.create
                  ) {
                    if (c.tokenType == TokenType.asset) {
                      if (c.amount < 0) { // not set
                        success
                      } else {
                        failure(
                          "Amount is only mandatory for group and series minting"
                        )
                      }
                    } else {
                      if (c.amount > 0) {
                        success
                      } else {
                        failure(
                          "Amount is mandatory for group and series minting"
                        )
                      }
                    }
                  } else
                    success
                }
              )
            )): _*
        )
    )

  val simpleTransactionMode = cmd("simple-transaction")
    .action((_, c) => c.copy(mode = BramblCliMode.simpletransaction))
    .text("Simple transaction mode")
    .children(
      cmd("create")
        .action((_, c) => c.copy(subcmd = BramblCliSubCmd.create))
        .text("Create transaction")
        .children(
          ((coordinates ++ hostPortNetwork ++ keyfileAndPassword ++ Seq(
            outputArg.required()
          )) ++
            Seq(
              feeArg,
              opt[Option[LockAddress]]('t', "to")
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
              amountArg,
              transferTokenType,
              groupId,
              checkConfig { c =>
                if (
                  c.mode == BramblCliMode.simpletransaction && c.subcmd == BramblCliSubCmd.create
                )
                  (c.toAddress, c.someToParty, c.someToContract) match {
                    case (Some(_), None, None) =>
                      checkTokenAndId(c.tokenType, c.someGroupId)
                    case (None, Some(_), Some(_)) =>
                      checkTokenAndId(c.tokenType, c.someGroupId)
                    case _ =>
                      failure(
                        "Exactly toParty and toContract together or only toAddress must be specified"
                      )
                  }
                else
                  success
              }
            )): _*
        )
    )

  private def checkTokenAndId(
      tokenType: TokenType.Value,
      groupId: Option[GroupId]
  ) = {
    (tokenType, groupId) match {
      case (TokenType.group, Some(_)) =>
        success
      case (TokenType.lvl, None) =>
        success
      case _ =>
        failure(
          "Exactly group and groupId together or only lvl must be specified"
        )
    }
  }

  val paramParser = {
    OParser.sequence(
      contractsMode,
      partiesMode,
      genusQueryMode,
      bifrostQueryMode,
      walletMode,
      transactionMode,
      simpleTransactionMode,
      simpleMintingMode
    )
  }
}
