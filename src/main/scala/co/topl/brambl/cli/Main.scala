package co.topl.brambl.cli

import cats.data.Validated
import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.kernel.Resource
import co.topl.brambl.cli.impl.SimpleTransactionAlgebra
import co.topl.brambl.cli.impl.TransactionBuilderApi
import co.topl.brambl.cli.impl.WalletAlgebra
import co.topl.brambl.cli.impl.WalletStateAlgebra
import co.topl.brambl.cli.validation.BramblCliParamsValidatorModule
import co.topl.brambl.constants.NetworkConstants
import co.topl.brambl.wallet.WalletApi
import scopt.OParser

import java.sql.DriverManager
import cats.effect.kernel.Sync
import io.grpc.ManagedChannelBuilder
import co.topl.brambl.cli.impl.UtxoAlgebra
import co.topl.brambl.utils.Encoding
import co.topl.brambl.codecs.AddressCodecs
import co.topl.brambl.models.box.Value

object Main extends IOApp {

  import BramblCliParamsValidatorModule._

  import BramblCliParamsParserModule._

  val dataApi = new DefaultDataApi[IO]()

  val walletApi = WalletApi.make(dataApi)

  def channelResource[F[_]: Sync](address: String, port: Int) = {
    Resource
      .make {
        Sync[F].delay(
          ManagedChannelBuilder
            .forAddress(address, port)
            .usePlaintext()
            .build
        )
      }(channel => Sync[F].delay(channel.shutdown()))
  }

  def walletResource(name: String) = Resource
    .make(
      IO.delay(
        DriverManager.getConnection(
          s"jdbc:sqlite:${name}"
        )
      )
    )(conn => IO.delay(conn.close()))

  private def createWalletFromParams(
      params: BramblCliValidatedParams
  ): IO[Unit] = {
    val transactionBuilderApi = TransactionBuilderApi.make[IO](
      params.network.networkId,
      NetworkConstants.MAIN_LEDGER_ID
    )
    WalletAlgebra
      .make[IO](
        Main.this.walletApi,
        WalletStateAlgebra.make[IO](
          walletResource(params.walletFile),
          transactionBuilderApi
        )
      )
      .createWalletFromParams(params)
  }

  private def createSimpleTransactionFromParams(
      params: BramblCliValidatedParams
  ): IO[Unit] = {
    val transactionBuilderApi = TransactionBuilderApi.make[IO](
      params.network.networkId,
      NetworkConstants.MAIN_LEDGER_ID
    )
    val walletStateApi = WalletStateAlgebra.make[IO](
      walletResource(params.walletFile),
      transactionBuilderApi
    )
    val simplTransactionOps = SimpleTransactionAlgebra
      .make[IO](
        Main.this.dataApi,
        Main.this.walletApi,
        walletStateApi,
        UtxoAlgebra.make[IO](channelResource(params.host, params.port)),
        transactionBuilderApi
      )
    walletStateApi.validateCurrentIndicesForFunds(
      params.fromParty,
      params.fromContract,
      params.someFromState
    ) flatMap {
      case Validated.Invalid(errors) =>
        IO.println("Invalid params") *> IO.println(
          errors.toList.mkString(", ")
        ) *> IO.print(OParser.usage(paramParser))
      case Validated.Valid(_) =>
        simplTransactionOps.createSimpleTransactionFromParams(
          params
        )
    }
  }

  private def txoType(txoValue: Value.Value) =
    if (txoValue.isLvl) "LVL"
    else if (txoValue.isAsset) "Asset"
    else if (txoValue.isRegistration) "Registration"
    else if (txoValue.isTopl) "TOPL"
    else "Unknown txo type"

  private def value(txoValue: Value.Value): String =
    if (txoValue.isLvl)
      BigInt(txoValue.lvl.get.quantity.value.toByteArray()).toString()
    else if (txoValue.isAsset)
      BigInt(txoValue.asset.get.quantity.value.toByteArray())
        .toString() + txoValue.asset.get.label
    else if (txoValue.isRegistration)
      "Registration"
    else if (txoValue.isTopl)
      BigInt(txoValue.topl.get.quantity.value.toByteArray()).toString()
    else "Undefine type"

  def queryUtxoFromParams(params: BramblCliValidatedParams): IO[Unit] = {
    val transactionBuilderApi = TransactionBuilderApi.make[IO](
      params.network.networkId,
      NetworkConstants.MAIN_LEDGER_ID
    )
    WalletStateAlgebra
      .make[IO](
        walletResource(params.walletFile),
        transactionBuilderApi
      )
      .getAddress(params.fromParty, params.fromContract, params.someFromState)
      .flatMap {
        case Some(address) =>
          UtxoAlgebra
            .make[IO](channelResource(params.host, params.port))
            .queryUtxo(AddressCodecs.decodeAddress(address).toOption.get)
            .flatMap { txos =>
              import cats.implicits._
              (txos
                .map { txo =>
s"""---------------------------------
TxoAddress: ${Encoding.encodeToBase58Check(
            txo.outputAddress.id.value.toByteArray()
          )}#${txo.outputAddress.index}
LockAddress: ${AddressCodecs.encodeAddress(
            txo.transactionOutput.address
          )}
Type: ${txoType(txo.transactionOutput.value.value)}
Value: ${value(txo.transactionOutput.value.value)}
"""
                })
                .map(x => IO(println(x)))
                .sequence
                .map(_ => ())
            }
        case None => IO.raiseError(new Exception("Address not found"))
      }

  }

  override def run(args: List[String]): IO[ExitCode] = {
    OParser.parse(paramParser, args, BramblCliParams()) match {
      case Some(params) =>
        val op = validateParams(params) match {
          case Validated.Valid(validateParams) =>
            (validateParams.mode, validateParams.subcmd) match {
              case (BramblCliMode.wallet, BramblCliSubCmd.init) =>
                createWalletFromParams(validateParams)
              case (BramblCliMode.simpletransaction, BramblCliSubCmd.create) =>
                createSimpleTransactionFromParams(validateParams)
              case (BramblCliMode.utxo, BramblCliSubCmd.query) =>
                queryUtxoFromParams(validateParams)
            }
          case Validated.Invalid(errors) =>
            IO.println("Invalid params") *> IO.println(
              errors.toList.mkString(", ")
            ) *> IO.print(OParser.usage(paramParser))
        }
        for {
          _ <- op
        } yield ExitCode.Success
      case _ =>
        IO.pure(ExitCode.Error)
    }
  }

}
