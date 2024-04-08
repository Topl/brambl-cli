package co.topl.brambl.cli.impl

import cats.data.EitherT
import cats.effect.IO
import co.topl.brambl.cli.NetworkIdentifiers
import co.topl.brambl.cli.TokenType
import co.topl.brambl.cli.controllers.SimpleTransactionController
import co.topl.brambl.cli.controllers.TxController
import co.topl.brambl.cli.modules.SimpleTransactionModeModule
import co.topl.brambl.cli.modules.TxModeModule
import co.topl.brambl.cli.modules.WalletModeModule
import co.topl.brambl.constants.NetworkConstants
import co.topl.brambl.models.LockAddress
import co.topl.brambl.models.GroupId
import co.topl.brambl.utils.Encoding
import com.google.protobuf.ByteString
import co.topl.brambl.models.SeriesId

object FullTxOps
    extends WalletModeModule
    with SimpleTransactionModeModule
    with TxModeModule {

  def sendFunds(
      networkId: NetworkIdentifiers,
      password: String,
      walletFile: String,
      keyFile: String,
      fromFellowship: String,
      fromTemplate: String,
      someFromInteraction: Option[Int],
      someChangeFellowship: Option[String],
      someChangeTemplate: Option[String],
      someChangeInteraction: Option[Int],
      toAddress: Option[LockAddress],
      amount: Long,
      fee: Long,
      token: String,
      txFile: String,
      provedTxFile: String,
      host: String,
      bifrostPort: Int,
      secureConnection: Boolean
  ): IO[Either[String, String]] = {
    val simpleTxController = new SimpleTransactionController(
      walletStateAlgebra(
        walletFile
      ),
      simplTransactionOps(
        walletFile,
        networkId.networkId,
        host,
        bifrostPort,
        secureConnection
      )
    )

    val txController = new TxController(
      txParserAlgebra(
        networkId.networkId,
        NetworkConstants.MAIN_LEDGER_ID
      ),
      transactionOps(
        walletFile,
        host,
        bifrostPort,
        secureConnection
      )
    )

    (for {
      _ <- EitherT(
        simpleTxController.createSimpleTransactionFromParams(
          keyFile,
          password,
          (
            fromFellowship,
            fromTemplate,
            someFromInteraction
          ),
          (
            someChangeFellowship,
            someChangeTemplate,
            someChangeInteraction
          ),
          toAddress,
          None,
          None,
          amount,
          fee,
          txFile,
          if (token == "LVL")
            TokenType.lvl
          else if (token.startsWith(":"))
            TokenType.series
          else if (token.endsWith(":"))
            TokenType.group
          else
            TokenType.asset,
          if (token.endsWith(":"))
            Some(
              GroupId(
                ByteString.copyFrom(
                  Encoding.decodeFromHex(token.dropRight(1)).toOption.get
                )
              )
            )
          else if (token.startsWith(":"))
            None
          else if (token == "LVL")
            None
          else
            Some(
              GroupId(
                ByteString.copyFrom(
                  Encoding.decodeFromHex(token.split(":").head).toOption.get
                )
              )
            ),
          if (token.startsWith(":"))
            Some(
              SeriesId(
                ByteString.copyFrom(
                  Encoding.decodeFromHex(token.drop(1)).toOption.get
                )
              )
            )
          else if (token.endsWith(":"))
            None
          else if (token == "LVL")
            None
          else
            Some(
              SeriesId(
                ByteString.copyFrom(
                  Encoding.decodeFromHex(token.split(":").last).toOption.get
                )
              )
            )
        )
      )
      _ <- EitherT(
        txController.proveSimpleTransactionFromParams(
          txFile,
          keyFile,
          password,
          provedTxFile
        )
      )
      res <- EitherT(
        txController.broadcastSimpleTransactionFromParams(
          provedTxFile
        )
      )
    } yield "Transaction id: " + res).value.map {
      case Left(err) =>
        println(err)
        Left(err)
      case Right(res) => Right(res)
    }
  }

}
