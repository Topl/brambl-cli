package co.topl.brambl.cli

import cats.data.Validated
import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import co.topl.brambl.cli.impl.WalletOps
import co.topl.brambl.cli.validation.BramblCliParamsValidatorModule
import co.topl.brambl.models.transaction.IoTransaction
import co.topl.brambl.utils.Encoding
import co.topl.brambl.wallet.WalletApi
import co.topl.node.services.FetchBlockBodyReq
import co.topl.node.services.FetchBlockIdAtHeightReq
import co.topl.node.services.FetchTransactionReq
import co.topl.node.services.NodeRpcGrpc
import io.grpc.ManagedChannelBuilder
import scopt.OParser
import co.topl.brambl.cli.impl.WalletStateApi

object Main extends IOApp with WalletOps {

  import BramblCliParamsValidatorModule._

  import BramblCliParamsParserModule._

  val dataApi = new DefaultDataApi[IO]()

  val walletApi = WalletApi.make(dataApi)

  lazy val walletStateApi = WalletStateApi.makeIO

  override def run(args: List[String]): IO[ExitCode] = {
    OParser.parse(paramParser, args, BramblCliParams()) match {
      case Some(params) =>
        val op = validateParams(params) match {
          case Validated.Valid(validateParams) =>
            (validateParams.mode, validateParams.subcmd) match {
              case (BramblCliMode.wallet, BramblCliSubCmd.init) =>
                createWalletFromParams(validateParams)
              case (BramblCliMode.utxo, BramblCliSubCmd.query) =>
                val channel = ManagedChannelBuilder
                  .forAddress("localhost", 9084)
                  .usePlaintext()
                  .build
                val blockingStub = NodeRpcGrpc.blockingStub(channel)
                val responseBlockId =
                  blockingStub.fetchBlockIdAtHeight(FetchBlockIdAtHeightReq(1))

                val responseBlockBody = blockingStub.fetchBlockBody(
                  FetchBlockBodyReq(responseBlockId.blockId.get)
                )
                val responseTransaction = blockingStub.fetchTransaction(
                  FetchTransactionReq(
                    responseBlockBody.body.get.transactionIds.head
                  )
                )
                IoTransaction

                IO(
                  println(
                    "Get block Id: " + Encoding.encodeToBase58(
                      responseBlockId.blockId.get.value.toByteArray()
                    )
                  )
                ) >> IO(
                  println(
                    "Get transactionId: " + Encoding.encodeToBase58(
                      responseBlockBody.body.get.transactionIds.head.value
                        .toByteArray()
                    )
                  )
                ) >> IO(
                  println(
                    "Get UTXOs: \n" +
                      responseTransaction.transaction.get.outputs.zipWithIndex
                        .map(x =>
                          s"Index: ${x._2}, LVL: ${x._1.value.value.lvl
                              .map(x => BigInt(x.quantity.value.toByteArray()))
                              .getOrElse("No LVLs here")}, Evidence: ${Encoding
                              .encodeToBase58(x._1.address.id.value.toByteArray())}"
                        )
                        .mkString("\n")
                  )
                )

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
