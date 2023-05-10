package co.topl.brambl.cli

import cats.data.Validated
import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.kernel.Resource
import co.topl.brambl.cli.impl.SimpleTransactionOps
import co.topl.brambl.cli.impl.TransactionBuilderApi
import co.topl.brambl.cli.impl.WalletOps
import co.topl.brambl.cli.impl.WalletStateApi
import co.topl.brambl.cli.validation.BramblCliParamsValidatorModule
import co.topl.brambl.constants.NetworkConstants
import co.topl.brambl.wallet.WalletApi
import scopt.OParser

import java.sql.DriverManager

object Main extends IOApp {

  import BramblCliParamsValidatorModule._

  import BramblCliParamsParserModule._

  val dataApi = new DefaultDataApi[IO]()

  val walletApi = WalletApi.make(dataApi)

  override def run(args: List[String]): IO[ExitCode] = {
    OParser.parse(paramParser, args, BramblCliParams()) match {
      case Some(params) =>
        val op = validateParams(params) match {
          case Validated.Valid(validateParams) =>
            (validateParams.mode, validateParams.subcmd) match {
              case (BramblCliMode.wallet, BramblCliSubCmd.init) =>
                val transactionBuilderApi = TransactionBuilderApi.make[IO](
                  validateParams.network.networkId,
                  NetworkConstants.MAIN_LEDGER_ID
                )
                WalletOps
                  .make[IO](
                    Main.this.walletApi,
                    WalletStateApi.make[IO](
                      () =>
                        Resource
                          .make(
                            IO.delay(
                              DriverManager.getConnection(
                                s"jdbc:sqlite:${validateParams.walletFile}"
                              )
                            )
                          )(conn => IO.delay(conn.close())),
                      transactionBuilderApi
                    )
                  )
                  .createWalletFromParams(validateParams)
              case (
                    BramblCliMode.simpletransaction,
                    BramblCliSubCmd.create
                  ) =>
                val transactionBuilderApi = TransactionBuilderApi.make[IO](
                  validateParams.network.networkId,
                  NetworkConstants.MAIN_LEDGER_ID
                )
                val walletStateApi = WalletStateApi.make[IO](
                  () =>
                    Resource
                      .make(
                        IO.delay(
                          DriverManager.getConnection(
                            s"jdbc:sqlite:${validateParams.walletFile}"
                          )
                        )
                      )(conn => IO.delay(conn.close())),
                  transactionBuilderApi
                )
                val simplTransactionOps = SimpleTransactionOps
                  .make[IO](
                    Main.this.dataApi,
                    Main.this.walletApi,
                    walletStateApi,
                    transactionBuilderApi
                  )
                walletStateApi.validateCurrentIndicesForFunds(
                  validateParams.fromParty,
                  validateParams.fromContract,
                  validateParams.someFromState
                ) flatMap {
                  case Validated.Invalid(errors) =>
                    IO.println("Invalid params") *> IO.println(
                      errors.toList.mkString(", ")
                    ) *> IO.print(OParser.usage(paramParser))
                  case Validated.Valid(_) =>
                    simplTransactionOps.createSimpleTransactionFromParams(
                      validateParams
                    )
                }
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
