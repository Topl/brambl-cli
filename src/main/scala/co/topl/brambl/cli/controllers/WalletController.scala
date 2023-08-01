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
import co.topl.brambl.codecs.AddressCodecs
import co.topl.genus.services.TxoState
import co.topl.genus.services.Txo
import co.topl.brambl.models.Indices
import co.topl.brambl.constants.NetworkConstants
import co.topl.brambl.models.LockAddress
import co.topl.brambl.models.LockId

class WalletController[F[_]: Sync](
    walletStateAlgebra: dataApi.WalletStateAlgebra[F],
    walletManagementUtils: WalletManagementUtils[F],
    walletApi: WalletApi[F],
    walletAlgebra: WalletAlgebra[F],
    genusQueryAlgebra: dataApi.GenusQueryAlgebra[F]
) {

  def importVk(
      networkId: Int,
      inputVks: Seq[File],
      keyfile: String,
      password: String,
      contractName: String,
      partyName: String
  ): F[Either[String, String]] = {
    import cats.implicits._
    import TransactionBuilderApi.implicits._
    import co.topl.brambl.common.ContainsEvidence.Ops
    import co.topl.brambl.common.ContainsImmutable.instances._
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
        .flatMap(
          _.map(
            // TODO: replace with proper serialization in TSDK-476
            vk =>
              // we derive the key once
              walletApi
                .deriveChildVerificationKey(
                  VerificationKey.parseFrom(
                    Encoding.decodeFromBase58(vk).toOption.get
                  ),
                  1
                )
                .map(x => (x, vk))
          ).sequence
        )
      lockTempl <- walletStateAlgebra
        .getLockTemplate(contractName)
        .map(_.get) // it exists because of the validation
      // we need to get the corresponding vk
      indices <- walletStateAlgebra.getNextIndicesForFunds(
        partyName,
        contractName
      )
      keypair <- walletManagementUtils.loadKeys(keyfile, password)
      deriveChildKey <- walletApi.deriveChildKeys(keypair, indices.get)
      deriveChildKeyBase <- walletApi.deriveChildKeysPartial(
        keypair,
        indices.get.x,
        indices.get.y
      )
      deriveChildKeyString = Encoding.encodeToBase58(
        deriveChildKeyBase.vk.toByteArray
      )
      errorOrLock <- lockTempl.build(
        deriveChildKey.vk :: keyAndEncodedKeys.toList.map(x => x._1)
      )
      lockAddress = LockAddress(
        networkId,
        NetworkConstants.MAIN_LEDGER_ID,
        LockId(errorOrLock.toOption.get.sizedEvidence.digest.value)
      )
      _ <- walletStateAlgebra.updateWalletState(
        Encoding.encodeToBase58Check(
          errorOrLock.toOption.get.getPredicate.toByteArray
        ), // lockPredicate
        lockAddress.toBase58(), // lockAddress
        Some("ExtendedEd25519"),
        Some(Encoding.encodeToBase58(deriveChildKey.vk.toByteArray)),
        indices.get
      )
      _ <- walletStateAlgebra.addEntityVks(
        partyName,
        contractName,
        deriveChildKeyString :: keyAndEncodedKeys.toList.map(_._2)
      )
      _ <- lockTempl.build(keyAndEncodedKeys.toList.map(_._1))
    } yield Right("Successfully imported verification keys")
  }

  def exportFinalVk(
      keyFile: String,
      password: String,
      outputFile: String,
      partyName: String,
      contractName: String,
      state: Int
  ): F[Either[String, String]] = {
    import cats.implicits._
    (for {
      indices <- OptionT(
        walletStateAlgebra.getCurrentIndicesForFunds(
          partyName,
          contractName,
          None
        )
      )
      keypair <- OptionT(
        walletManagementUtils
          .loadKeys(keyFile, password)
          .map(x => Option(x))
      )
      deriveChildKey <- OptionT(
        walletApi
          .deriveChildKeys(keypair, indices.copy(z = state))
          .map(
            Option(_)
          )
      )
    } yield {
      Resource
        .make(Sync[F].delay(new PrintWriter(outputFile)))(file =>
          Sync[F].delay(file.close())
        )
        .use { file =>
          for {
            _ <- Sync[F].blocking(
              file.write(Encoding.encodeToBase58(deriveChildKey.vk.toByteArray))
            )
          } yield ()
        }
    }).value.map(_.get).flatten.map(_ => Right("Verification key exported"))
  }

  def exportVk(
      keyFile: String,
      password: String,
      outputFile: String,
      partyName: String,
      contractName: String
  ): F[Either[String, String]] = {
    import cats.implicits._
    (for {
      indices <- OptionT(
        walletStateAlgebra.getCurrentIndicesForFunds(
          partyName,
          contractName,
          None
        )
      )
      keypair <- OptionT(
        walletManagementUtils
          .loadKeys(keyFile, password)
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
        .make(Sync[F].delay(new PrintWriter(outputFile)))(file =>
          Sync[F].delay(file.close())
        )
        .use { file =>
          for {
            _ <- Sync[F].blocking(
              file.write(Encoding.encodeToBase58(deriveChildKey.vk.toByteArray))
            )
          } yield ()
        }
    }).value.map(_.get).flatten.map(_ => Right("Verification key exported"))
  }

  def createWalletFromParams(
      params: BramblCliValidatedParams
  ): F[Either[String, String]] = {
    import cats.implicits._
    walletAlgebra
      .createWalletFromParams(
        params.network.networkId,
        NetworkConstants.MAIN_LEDGER_ID,
        params.password,
        params.somePassphrase,
        params.someOutputFile,
        params.someMnemonicFile
      )
      .map(_ => Right("Wallet created"))
  }

  def recoverKeysFromParams(
      params: BramblCliValidatedParams
  ): F[Either[String, String]] = {
    import cats.implicits._
    walletAlgebra
      .recoverKeysFromParams(
        params.mnemonic,
        params.password,
        params.somePassphrase,
        params.someOutputFile
      )
      .map(_ => Right("Wallet Main Key Recovered"))
  }

  def currentaddress(): F[Either[String, String]] = {
    import cats.implicits._
    walletStateAlgebra.getCurrentAddress.map(Right(_))
  }

  def sync(
      networkId: Int,
      party: String,
      contract: String
  ): F[Either[String, String]] = {
    import cats.implicits._
    import TransactionBuilderApi.implicits._
    import co.topl.brambl.common.ContainsEvidence.Ops
    import co.topl.brambl.common.ContainsImmutable.instances._
    (for {
      // current indices
      someIndices <- walletStateAlgebra.getCurrentIndicesForFunds(
        party,
        contract,
        None
      )
      // current address
      someAddress <- walletStateAlgebra.getAddress(
        party,
        contract,
        someIndices.map(_.z)
      )
      // txos that are spent at current address
      txos <- someAddress
        .map(address =>
          genusQueryAlgebra
            .queryUtxo(
              AddressCodecs.decodeAddress(address).toOption.get,
              TxoState.SPENT
            )
        )
        .getOrElse(Sync[F].pure(Seq.empty[Txo]))
    } yield
    // we have indices AND txos at current address are spent
    if (someIndices.isDefined && !txos.isEmpty) {
      // we need to update the wallet state with the next indices
      val indices = someIndices.map(idx => Indices(idx.x, idx.y, idx.z + 1)).get
      for {
        vks <- walletStateAlgebra.getEntityVks(
          party,
          contract
        )
        vksDerived <- vks.get
          .map(x =>
            walletApi.deriveChildVerificationKey(
              VerificationKey.parseFrom(
                Encoding.decodeFromBase58(x).toOption.get
              ),
              indices.z
            )
          )
          .sequence
        lock <- walletStateAlgebra.getLock(party, contract, indices.z)
        lockAddress = LockAddress(
          networkId,
          NetworkConstants.MAIN_LEDGER_ID,
          LockId(lock.get.getPredicate.sizedEvidence.digest.value)
        )
        _ <- walletStateAlgebra.updateWalletState(
          Encoding.encodeToBase58Check(
            lock.get.getPredicate.toByteArray
          ), // lockPredicate
          lockAddress.toBase58(), // lockAddress
          Some("ExtendedEd25519"),
          Some(Encoding.encodeToBase58(vksDerived.head.toByteArray)),
          indices
        )
      } yield txos
    } else {
      Sync[F].delay(txos)
    }).flatten.iterateUntil(x => x.isEmpty).map(_ => Right("Wallet synced"))
  }

  def currentaddress(
      party: String,
      contract: String,
      someState: Option[Int]
  ): F[Option[String]] =
    walletStateAlgebra.getAddress(party, contract, someState)

}
