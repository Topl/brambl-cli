package co.topl.brambl.cli.modules

import cats.effect.IO
import co.topl.brambl.cli.controllers.BifrostQueryController
import co.topl.brambl.cli.BramblCliSubCmd
import co.topl.brambl.dataApi.{BifrostQueryAlgebra, RpcChannelResource}
import co.topl.brambl.cli.BramblCliParams
import scopt.OParser
import co.topl.brambl.cli.BramblCliParamsParserModule

trait BifrostQueryModeModule extends RpcChannelResource {

  def bifrostQuerySubcmd(
      validateParams: BramblCliParams
  ): IO[Either[String, String]] = {
    val bifrostQueryAlgebra = BifrostQueryAlgebra.make[IO](
      channelResource(
        validateParams.host,
        validateParams.bifrostPort,
        validateParams.secureConnection
      )
    )
    validateParams.subcmd match {
      case BramblCliSubCmd.invalid =>
        IO.pure(
          Left(
            OParser.usage(
              BramblCliParamsParserModule.bifrostQueryMode
            ) + "\nA subcommand needs to be specified"
          )
        )
      case BramblCliSubCmd.mintblock =>
        new BifrostQueryController(bifrostQueryAlgebra)
          .makeBlock(
            validateParams.nbOfBlocks
          )
      case BramblCliSubCmd.blockbyheight =>
        new BifrostQueryController(
          bifrostQueryAlgebra
        ).blockByHeight(validateParams.height)
      case BramblCliSubCmd.blockbyid =>
        new BifrostQueryController(
          bifrostQueryAlgebra
        ).blockById(validateParams.blockId)
      case BramblCliSubCmd.transactionbyid =>
        new BifrostQueryController(
          bifrostQueryAlgebra
        ).fetchTransaction(validateParams.transactionId)
    }
  }

}
