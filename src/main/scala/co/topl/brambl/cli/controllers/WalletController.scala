package co.topl.brambl.cli.controllers

import cats.data.OptionT
import cats.effect.IO
import cats.effect.kernel.Resource
import co.topl.brambl.builders.TransactionBuilderApi
import co.topl.brambl.cli.BramblCliValidatedParams
import co.topl.brambl.cli.DefaultWalletKeyApi
import co.topl.brambl.cli.impl.WalletAlgebra
import co.topl.brambl.cli.impl.WalletManagementUtils
import co.topl.brambl.cli.impl.WalletStateAlgebra
import co.topl.brambl.constants.NetworkConstants
import co.topl.brambl.utils.Encoding
import co.topl.brambl.wallet.WalletApi
import quivr.models.VerificationKey

import java.io.PrintWriter
import java.sql.Connection

class WalletController(walletResource: Resource[IO, Connection]) {

  def importVk(
      params: BramblCliValidatedParams
  ): IO[Unit] = {
    val transactionBuilderApi = TransactionBuilderApi.make[IO](
      params.network.networkId,
      NetworkConstants.MAIN_LEDGER_ID
    )
    val dataApi = new DefaultWalletKeyApi[IO]()

    val walletApi = WalletApi.make(dataApi)
    val walletStateAlgebra = WalletStateAlgebra.make[IO](
      walletResource,
      transactionBuilderApi,
      walletApi
    )
    import cats.implicits._
    for {
      keyAndEncodedKeys <- (params.inputVks
        .map { file =>
          Resource
            .make(IO(scala.io.Source.fromFile(file)))(file => IO(file.close()))
            .use { file =>
              IO(file.getLines().toList.mkString)
            }
        })
        .sequence
        .map(
          _.map(
            // TODO: replace with proper serialization in TSDK-476
            vk =>
              (
                VerificationKey.parseFrom(
                  Encoding.decodeFromBase58(vk).toOption.get
                ),
                vk
              )
          )
        )
      lockTempl <- walletStateAlgebra
        .getLockTemplate(params.contractName)
        .map(_.get) // it exists because of the validation
      _ <- walletStateAlgebra.addEntityVks(
        params.partyName,
        params.contractName,
        keyAndEncodedKeys.toList.map(_._2)
      )
      _ <- lockTempl.build(keyAndEncodedKeys.toList.map(_._1))
    } yield ()
  }

  def exportVk(
      params: BramblCliValidatedParams
  ): IO[Unit] = {
    val transactionBuilderApi = TransactionBuilderApi.make[IO](
      params.network.networkId,
      NetworkConstants.MAIN_LEDGER_ID
    )
    val dataApi = new DefaultWalletKeyApi[IO]()

    val walletApi = WalletApi.make(dataApi)
    val walletStateAlgebra = WalletStateAlgebra.make[IO](
      walletResource,
      transactionBuilderApi,
      walletApi
    )
    val walletManagementUtils =
      new WalletManagementUtils[IO](walletApi, dataApi)
    (for {
      indices <- OptionT(
        walletStateAlgebra.getCurrentIndicesForFunds(
          params.partyName,
          params.contractName,
          None
        )
      )
      keypair <- OptionT(
        walletManagementUtils.loadKeysFromParam(params).map(x => Option(x))
      )
      deriveChildKey <- OptionT(
        walletApi
          .deriveChildKeysPartial(keypair, indices.x, indices.y)
          .map(
            Option(_)
          )
      )
    } yield {
      Resource
        .make(IO(new PrintWriter(params.someOutputFile.get)))(file =>
          IO(file.close())
        )
        .use { file =>
          for {
            _ <- IO.blocking(
              file.write(Encoding.encodeToBase58(deriveChildKey.vk.toByteArray))
            )
          } yield ()
        }
    }).value.map(_.get).flatten
  }

  def createWalletFromParams(
      params: BramblCliValidatedParams
  ): IO[Unit] = {
    val transactionBuilderApi = TransactionBuilderApi.make[IO](
      params.network.networkId,
      NetworkConstants.MAIN_LEDGER_ID
    )
    val dataApi = new DefaultWalletKeyApi[IO]()

    val walletApi = WalletApi.make(dataApi)
    val walletStateAlgebra = WalletStateAlgebra.make[IO](
      walletResource,
      transactionBuilderApi,
      walletApi
    )

    WalletAlgebra
      .make[IO](
        walletApi,
        walletStateAlgebra
      )
      .createWalletFromParams(params)
  }

  def currentaddress(
      params: BramblCliValidatedParams
  ): IO[Unit] = {
    val dataApi = new DefaultWalletKeyApi[IO]()

    val walletApi = WalletApi.make(dataApi)
    val transactionBuilderApi = TransactionBuilderApi.make[IO](
      params.network.networkId,
      NetworkConstants.MAIN_LEDGER_ID
    )
    WalletStateAlgebra
      .make[IO](
        walletResource,
        transactionBuilderApi,
        walletApi
      )
      .getCurrentAddress
      .flatMap(address => IO(println(address)))
  }
}
