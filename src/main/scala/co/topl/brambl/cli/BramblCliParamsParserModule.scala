package co.topl.brambl.cli

import co.topl.brambl.codecs.AddressCodecs
import co.topl.brambl.models.LockAddress
import co.topl.brambl.utils.Encoding
import scopt.OParser

import java.io.File
import java.nio.file.Paths
import co.topl.brambl.models.GroupId
import com.google.protobuf.ByteString
import co.topl.brambl.models.SeriesId
import scala.util.Try
import co.topl.brambl.constants.NetworkConstants

object BramblCliParamsParserModule {

  implicit val tokenTypeRead: scopt.Read[TokenType.Value] =
    Try(scopt.Read.reads(TokenType.withName)) match {
      case scala.util.Success(value) => value
      case scala.util.Failure(_) =>
        throw new IllegalArgumentException(
          "Invalid token type. Possible values: lvl, topl, asset, group, series, all"
        )
    }

  val builder = OParser.builder[BramblCliParams]

  import builder._

  implicit val networkRead: scopt.Read[NetworkIdentifiers] =
    scopt.Read
      .reads(NetworkIdentifiers.fromString(_))
      .map(_ match {
        case Some(value) => value
        case None =>
          throw new IllegalArgumentException(
            "Invalid network. Possible values: mainnet, testnet, private"
          )
      })

  implicit val groupIdRead: scopt.Read[GroupId] =
    scopt.Read.reads { x =>
      val array = Encoding.decodeFromHex(x).toOption match {
        case Some(value) => value
        case None =>
          throw new IllegalArgumentException("Invalid group id")
      }
      GroupId(ByteString.copyFrom(array))
    }

  implicit val seriesIdRead: scopt.Read[SeriesId] =
    scopt.Read.reads { x =>
      val array = Encoding.decodeFromHex(x).toOption match {
        case Some(value) => value
        case None =>
          throw new IllegalArgumentException("Invalid series id")
      }
      SeriesId(ByteString.copyFrom(array))
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

  def walletDbArg = opt[String]("walletdb")
    .action((x, c) => c.copy(walletFile = x))
    .validate(validateWalletDbFile(_))
    .text("Wallet DB file. (mandatory)")
    .required()

  val templateNameArg = opt[String]("template-name")
    .validate(x =>
      if (x.trim().isEmpty) failure("Template name may not be empty")
      else success
    )
    .action((x, c) => c.copy(templateName = x))
    .text("Name of the template. (mandatory)")

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

  val fellowshipNameArg = opt[String]("fellowship-name")
    .validate(x =>
      if (x.trim().isEmpty) failure("Fellowship name may not be empty")
      else success
    )
    .action((x, c) => c.copy(fellowshipName = x))
    .text("Name of the fellowship. (mandatory)")

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

  def secureArg =
    opt[Boolean]('s', "secure")
      .action((x, c) => c.copy(secureConnection = x))
      .text("Enables the secure connection to the node. (optional)")

  val hostPort = Seq(
    hostArg,
    portArg,
    secureArg
  )

  val groupId = opt[Option[GroupId]]("group-id")
    .action((x, c) => c.copy(someGroupId = x))
    .text("Group id.")

  val seriesId = opt[Option[SeriesId]]("series-id")
    .action((x, c) => c.copy(someSeriesId = x))
    .text("Series id.")

  val hostPortNetwork =
    Seq(
      networkArg,
      hostArg,
      portArg,
      secureArg
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

  def changeCoordinates = {
    import builder._
    Seq(
      opt[Option[String]]("change-fellowship")
        .action((x, c) => c.copy(someChangeFellowship = x))
        .text("Fellowship where we are sending the change to")
        .optional(),
      opt[Option[String]]("change-template")
        .action((x, c) => c.copy(someChangeTemplate = x))
        .text("Template where we are sending the change to")
        .optional(),
      opt[Option[Int]]("change-interaction")
        .action((x, c) => c.copy(someChangeInteraction = x))
        .text("Interaction where we are sending the change to")
        .optional(),
      checkConfig(c =>
        if (
          c.mode == BramblCliMode.simpletransaction && c.subcmd == BramblCliSubCmd.create
        ) {
          if (c.fromFellowship == "nofellowship") {
            (
              c.someChangeFellowship,
              c.someChangeTemplate,
              c.someChangeInteraction
            ) match {
              case (Some(_), Some(_), Some(_)) =>
                success
              case (_, _, _) =>
                failure(
                  "You must specify a change-fellowship, change-template and change-interaction when using nofellowship"
                )
            }
          } else {
            (
              c.someChangeFellowship,
              c.someChangeTemplate,
              c.someChangeInteraction
            ) match {
              case (Some(_), Some(_), Some(_)) =>
                success
              case (None, None, None) =>
                success
              case (_, _, _) =>
                failure(
                  "You must specify a change-fellowship, change-template and change-interaction or not specify any of them"
                )
            }
          }
        } else // if you need to set the change you set all the parameters
          (
            c.someChangeFellowship,
            c.someChangeTemplate,
            c.someChangeInteraction
          ) match {
            case (Some(_), Some(_), Some(_)) =>
              success
            case (None, None, None) =>
              success
            case (_, _, _) =>
              failure(
                "You must specify a change-fellowship, change-template and change-interaction or not specify any of them"
              )
          }
      )
    )
  }

  val fromAddress = opt[Option[String]]("from-address")
    .action((x, c) => c.copy(fromAddress = x))
    .text("Address where we are sending the funds from")
    .validate(someAddress =>
      someAddress
        .map(AddressCodecs.decodeAddress(_))
        .map(_ match {
          case Left(_)  => failure("Invalid from address")
          case Right(_) => success
        })
        .getOrElse(success)
    )

  val coordinates = {
    import builder._
    Seq(
      opt[String]("from-fellowship")
        .action((x, c) => c.copy(fromFellowship = x))
        .text("Fellowship where we are sending the funds from")
        .optional(),
      opt[String]("from-template")
        .action((x, c) => c.copy(fromTemplate = x))
        .text("Template where we are sending the funds from")
        .optional(),
      opt[Option[Int]]("from-interaction")
        .action((x, c) => c.copy(someFromInteraction = x))
        .validate(
          _.map(x =>
            if (x >= 1) success
            else failure("Interaction needs to be greater or equal to 1")
          ).getOrElse(success)
        )
        .text("Interaction from where we are sending the funds from")
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
      passwordArg
    )
  }

  val templatesMode = cmd("templates")
    .action((_, c) => c.copy(mode = BramblCliMode.templates))
    .text("Template mode")
    .children(
      cmd("list")
        .action((_, c) => c.copy(subcmd = BramblCliSubCmd.list))
        .text("List existing templates")
        .children(
          walletDbArg
        ),
      cmd("add")
        .action((_, c) => c.copy(subcmd = BramblCliSubCmd.add))
        .text("Add a new templates")
        .children(
          walletDbArg,
          templateNameArg,
          opt[String]("lock-template")
            .validate(x =>
              if (x.trim().isEmpty)
                failure("Template template may not be empty")
              else success
            )
            .action((x, c) => c.copy(lockTemplate = x))
            .text("Template template. (mandatory)")
        )
    )

  val fellowshipsMode = cmd("fellowships")
    .action((_, c) => c.copy(mode = BramblCliMode.fellowships))
    .text("Fellowship mode")
    .children(
      cmd("list")
        .action((_, c) => c.copy(subcmd = BramblCliSubCmd.list))
        .text("List existing fellowships")
        .children(
          walletDbArg
        ),
      cmd("add")
        .action((_, c) => c.copy(subcmd = BramblCliSubCmd.add))
        .text("Add a new fellowships")
        .children(
          Seq(
            walletDbArg,
            fellowshipNameArg
          ): _*
        )
    )

  implicit val lockAddressRead: scopt.Read[LockAddress] =
    scopt.Read.reads(
      AddressCodecs
        .decodeAddress(_)
        .toOption match {
        case None =>
          throw new IllegalArgumentException(
            "Invalid address, could not decode."
          )
        case Some(value) => value
      }
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
            fromAddress,
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
      cmd("balance")
        .action((_, c) => c.copy(subcmd = BramblCliSubCmd.balance))
        .text("Get balance of wallet")
        .children(
          (hostPortNetwork ++ coordinates ++ (Seq(
            fromAddress,
            walletDbArg
          ))): _*
        ),
      cmd("set-interaction")
        .action((_, c) => c.copy(subcmd = BramblCliSubCmd.setinteraction))
        .text("Set the current interaction")
        .children(
          coordinates.map(_.required()) ++
            Seq(
              walletDbArg
            ): _*
        ),
      cmd("list-interactions")
        .action((_, c) => c.copy(subcmd = BramblCliSubCmd.listinteraction))
        .text("List the interactions for a given fellowship and template")
        .children(
          fellowshipNameArg,
          templateNameArg,
          walletDbArg
        ),
      cmd("sync")
        .action((_, c) => c.copy(subcmd = BramblCliSubCmd.sync))
        .text("Sync wallet")
        .children(
          (hostPortNetwork ++ keyfileAndPassword ++ (Seq(
            fellowshipNameArg,
            templateNameArg,
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
          (Seq(walletDbArg) ++ coordinates): _*
        ),
      cmd("export-vk")
        .action((_, c) => c.copy(subcmd = BramblCliSubCmd.exportvk))
        .text("Export verification key")
        .children(
          (keyfileAndPassword ++ Seq(
            outputArg,
            walletDbArg,
            fellowshipNameArg,
            templateNameArg,
            opt[Option[Int]]("interaction")
              .action((x, c) => c.copy(someFromInteraction = x))
              .text("Interaction from where we are sending the funds from")
          )): _*
        ),
      cmd("import-vks")
        .action((_, c) => c.copy(subcmd = BramblCliSubCmd.importvks))
        .text("Import verification key")
        .children(
          (keyfileAndPassword ++ Seq(
            walletDbArg,
            fellowshipNameArg,
            templateNameArg,
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
            walletDbArg,
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
            walletDbArg,
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
                    if (c.fromAddress.isDefined) {
                      failure(
                        "From address is not supported for minting"
                      )
                    } else if (c.tokenType == TokenType.asset) {
                      if (c.amount < 0) { // not set
                        success
                      } else {
                        failure(
                          "Amount already defined in the asset minting statement"
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
          ((coordinates ++ changeCoordinates ++ hostPortNetwork ++ keyfileAndPassword ++ Seq(
            walletDbArg,
            outputArg.required()
          )) ++
            Seq(
              feeArg,
              opt[Option[LockAddress]]('t', "to")
                .action((x, c) => c.copy(toAddress = x))
                .text(
                  "Address to send LVLs to. (mandatory if to-fellowship and to-template are not provided)"
                ),
              opt[Option[String]]("to-fellowship")
                .action((x, c) => c.copy(someToFellowship = x))
                .text(
                  "Fellowship to send LVLs to. (mandatory if to is not provided)"
                ),
              opt[Option[String]]("to-template")
                .action((x, c) => c.copy(someToTemplate = x))
                .text(
                  "Template to send LVLs to. (mandatory if to is not provided)"
                ),
              amountArg,
              transferTokenType,
              groupId,
              seriesId,
              checkConfig { c =>
                if (
                  c.mode == BramblCliMode.simpletransaction && c.subcmd == BramblCliSubCmd.create
                )
                  if (c.fromAddress.isDefined) {
                    failure(
                      "From address is not supported for simple transactions"
                    )
                  } else
                    (c.toAddress, c.someToFellowship, c.someToTemplate) match {
                      case (Some(address), None, None) =>
                        checkAddress(address, c.network).flatMap(_ =>
                          checkTokenAndId(
                            c.tokenType,
                            c.someGroupId,
                            c.someSeriesId
                          )
                        )
                      case (None, Some(_), Some(_)) =>
                        checkTokenAndId(
                          c.tokenType,
                          c.someGroupId,
                          c.someSeriesId
                        )
                      case _ =>
                        failure(
                          "Exactly toFellowship and toTemplate together or only toAddress must be specified"
                        )
                    }
                else
                  success
              }
            )): _*
        )
    )

  private def checkAddress(
      lockAddress: LockAddress,
      networkId: NetworkIdentifiers
  ) = {
    if (lockAddress.ledger != NetworkConstants.MAIN_LEDGER_ID) {
      failure("Invalid ledger id")
    } else if (lockAddress.network != networkId.networkId) {
      failure(
        "Invalid network id. Address is using a different network id than the one passed as a parameter: " + networkId
          .toString()
      )
    } else {
      success
    }
  }

  private def checkTokenAndId(
      tokenType: TokenType.Value,
      groupId: Option[GroupId],
      seriesId: Option[SeriesId]
  ): Either[String, Unit] = {
    (tokenType, groupId, seriesId) match {
      case (TokenType.group, Some(_), None) =>
        success
      case (TokenType.series, None, Some(_)) =>
        success
      case (TokenType.asset, Some(_), Some(_)) =>
        success
      case (TokenType.lvl, None, None) =>
        success
      case _ =>
        failure(
          "Exactly group and groupId together, or series and seriesId, or only lvl must be specified"
        )
    }
  }

  val paramParser = {
    OParser.sequence(
      templatesMode,
      fellowshipsMode,
      genusQueryMode,
      bifrostQueryMode,
      walletMode,
      transactionMode,
      simpleTransactionMode,
      simpleMintingMode
    )
  }
}
