package co.topl.brambl.cli.controllers

import cats.data.OptionT
import cats.effect.kernel.Resource
import cats.effect.kernel.Sync
import co.topl.brambl.cli.BramblCliValidatedParams
import co.topl.brambl.cli.impl.WalletAlgebra
import co.topl.brambl.cli.impl.WalletManagementUtils
import co.topl.brambl.dataApi
import co.topl.brambl.utils.Encoding
import co.topl.brambl.wallet.WalletApi
import quivr.models.VerificationKey

import java.io.PrintWriter
import java.io.File
import co.topl.brambl.builders.TransactionBuilderApi

class WalletController[F[_]: Sync](
    transactionBuilderApi: TransactionBuilderApi[F],
    walletStateAlgebra: dataApi.WalletStateAlgebra[F],
    walletManagementUtils: WalletManagementUtils[F],
    walletApi: WalletApi[F],
    walletAlgebra: WalletAlgebra[F]
) {

  def importVk(
      inputVks: Seq[File],
      keyfile: String,
      password: String,
      contractName: String,
      partyName: String
  ): F[String] = {
    import cats.implicits._
    import TransactionBuilderApi.implicits._
    for {
      keyAndEncodedKeys <- (inputVks
        .map { file =>
          Resource
            .make(Sync[F].delay(scala.io.Source.fromFile(file)))(file =>
              Sync[F].delay(file.close())
            )
            .use { file =>
              Sync[F].blocking(file.getLines().toList.mkString)
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
        .getLockTemplate(contractName)
        .map(_.get) // it exists because of the validation
      // we need to get the corresponding vk
      indices <- walletStateAlgebra.getCurrentIndicesForFunds(
        partyName,
        contractName,
        None
      )
      keypair <- walletManagementUtils.loadKeys(keyfile, password)
      deriveChildKey <- walletApi.deriveChildKeys(keypair, indices.get)
      deriveChildKeyString = Encoding.encodeToBase58Check(
        deriveChildKey.vk.toByteArray
      )
      errorOrLock <- lockTempl.build(
        deriveChildKey.vk :: keyAndEncodedKeys.toList.map(_._1)
      )
      // TODO: double check that this lock is validated
      lockAddress <- transactionBuilderApi.lockAddress(
        errorOrLock.toOption.get
      )
      _ <- walletStateAlgebra.updateWalletState(
        Encoding.encodeToBase58Check(
          errorOrLock.toOption.get.getPredicate.toByteArray
        ), // lockPredicate
        lockAddress.toBase58(), // lockAddress
        Some("ExtendedEd25519"),
        Some(Encoding.encodeToBase58Check(deriveChildKey.vk.toByteArray)),
        indices.get
      )
      _ <- walletStateAlgebra.addEntityVks(
        partyName,
        contractName,
        deriveChildKeyString :: keyAndEncodedKeys.toList.map(_._2)
      )
      _ <- lockTempl.build(keyAndEncodedKeys.toList.map(_._1))
    } yield "Successfully imported verification keys"
  }

  def exportVk(
      params: BramblCliValidatedParams
  ): F[String] = {
    import cats.implicits._
    (for {
      indices <- OptionT(
        walletStateAlgebra.getCurrentIndicesForFunds(
          params.partyName,
          params.contractName,
          None
        )
      )
      keypair <- OptionT(
        walletManagementUtils
          .loadKeys(params.someKeyFile.get, params.password)
          .map(x => Option(x))
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
        .make(Sync[F].delay(new PrintWriter(params.someOutputFile.get)))(file =>
          Sync[F].delay(file.close())
        )
        .use { file =>
          for {
            _ <- Sync[F].blocking(
              file.write(Encoding.encodeToBase58(deriveChildKey.vk.toByteArray))
            )
          } yield ()
        }
    }).value.map(_.get).flatten.map(_ => "Verification key exported")
  }

  def createWalletFromParams(
      params: BramblCliValidatedParams
  ): F[String] = {
    import cats.implicits._
    walletAlgebra.createWalletFromParams(params).map(_ => "Wallet created")
  }

  def currentaddress(): F[String] =
    walletStateAlgebra.getCurrentAddress

}
