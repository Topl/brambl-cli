package co.topl.brambl.cli.controllers

import cats.data.OptionT
import cats.effect.kernel.Resource
import cats.effect.kernel.Sync
import co.topl.brambl.builders.TransactionBuilderApi
import co.topl.brambl.cli.BramblCliParams
import co.topl.brambl.cli.impl.WalletAlgebra
import co.topl.brambl.cli.impl.WalletManagementUtils
import co.topl.brambl.codecs.AddressCodecs
import co.topl.brambl.constants.NetworkConstants
import co.topl.brambl.dataApi
import co.topl.brambl.models.Indices
import co.topl.brambl.models.LockAddress
import co.topl.brambl.models.LockId
import co.topl.brambl.models.box.QuantityDescriptorType
import co.topl.brambl.syntax.GroupAndSeriesFungible
import co.topl.brambl.syntax.GroupType
import co.topl.brambl.syntax.LvlType
import co.topl.brambl.syntax.SeriesType
import co.topl.brambl.utils.Encoding
import co.topl.brambl.wallet.WalletApi
import co.topl.genus.services.Txo
import co.topl.genus.services.TxoState
import quivr.models.VerificationKey

import java.io.File
import java.io.PrintWriter

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
      fellowshipName: String
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
        fellowshipName,
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
        fellowshipName,
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
      fellowshipName: String,
      contractName: String,
      state: Int
  ): F[Either[String, String]] = {
    import cats.implicits._
    (for {
      indices <- OptionT(
        walletStateAlgebra.getCurrentIndicesForFunds(
          fellowshipName,
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
          Sync[F].delay(file.flush()) >> Sync[F].delay(file.close())
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
      fellowshipName: String,
      contractName: String
  ): F[Either[String, String]] = {
    import cats.implicits._
    (for {
      indices <- OptionT(
        walletStateAlgebra.getCurrentIndicesForFunds(
          fellowshipName,
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
          Sync[F].delay(file.flush()) >> Sync[F].delay(file.close())
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
      params: BramblCliParams
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
      params: BramblCliParams
  ): F[Either[String, String]] = {
    import cats.implicits._
    walletAlgebra
      .recoverKeysFromParams(
        params.mnemonic.toIndexedSeq,
        params.password,
        params.network.networkId,
        NetworkConstants.MAIN_LEDGER_ID,
        params.somePassphrase,
        params.someOutputFile
      )
      .map(_ => Right("Wallet Main Key Recovered"))
  }

  def currentaddress(params: BramblCliParams): F[Either[String, String]] = {
    import cats.implicits._
    params.fromAddress
      .map(x => Sync[F].point(Some(x)))
      .getOrElse(
        walletStateAlgebra
          .getAddress(
            params.fromFellowship,
            params.fromContract,
            params.someFromState
          )
      )
      .map(_ match {
        case Some(address) => Right(address)
        case None          => Left("No address found")
      })
  }

  def sync(
      networkId: Int,
      fellowship: String,
      contract: String
  ): F[Either[String, String]] = {
    import cats.implicits._
    import TransactionBuilderApi.implicits._
    import co.topl.brambl.common.ContainsEvidence.Ops
    import co.topl.brambl.common.ContainsImmutable.instances._
    (for {
      // current indices
      someIndices <- walletStateAlgebra.getCurrentIndicesForFunds(
        fellowship,
        contract,
        None
      )
      // current address
      someAddress <- walletStateAlgebra.getAddress(
        fellowship,
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
          fellowship,
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
        lock <- walletStateAlgebra.getLock(fellowship, contract, indices.z)
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
      fellowship: String,
      contract: String,
      someState: Option[Int]
  ): F[Option[String]] =
    walletStateAlgebra.getAddress(fellowship, contract, someState)

  def getBalance(
      someAddress: Option[String],
      someFellowship: Option[String],
      someContract: Option[String],
      someState: Option[Int]
  ): F[Either[String, String]] = {

    import cats.implicits._
    val addressGetter = (someAddress, someFellowship, someContract) match {
      case (Some(address), None, None) =>
        Sync[F].point(Some(address))
      case (None, Some(fellowship), Some(contract)) =>
        walletStateAlgebra.getAddress(
          fellowship,
          contract,
          someState
        )
      case (_, _, _) =>
        Sync[F].raiseError(
          new Exception("Invalid arguments (should not happen)")
        )
    }
    for {
      someAddress <- addressGetter
      balance <- someAddress
        .map(address =>
          genusQueryAlgebra
            .queryUtxo(
              AddressCodecs.decodeAddress(address).toOption.get,
              TxoState.UNSPENT
            )
        )
        .getOrElse(Sync[F].pure(Seq.empty[Txo]))
    } yield {
      val assetMap = balance.groupBy(x =>
        if (x.transactionOutput.value.value.isLvl)
          LvlType
        else if (x.transactionOutput.value.value.isGroup)
          GroupType(x.transactionOutput.value.value.group.get.groupId)
        else if (x.transactionOutput.value.value.isSeries)
          SeriesType(x.transactionOutput.value.value.series.get.seriesId)
        else if (x.transactionOutput.value.value.isAsset)
          GroupAndSeriesFungible(
            x.transactionOutput.value.value.asset.get.groupId.get,
            x.transactionOutput.value.value.asset.get.seriesId.get,
            QuantityDescriptorType.LIQUID
          )
        else ()
      )
      val res = assetMap.map { e =>
        val (key, value) = e
        val result = value.foldl(BigInt(0))((a, c) => {
          a + (if (c.transactionOutput.value.value.isLvl)
                 BigInt(
                   c.transactionOutput.value.value.lvl.get.quantity.value.toByteArray
                 )
               else if (c.transactionOutput.value.value.isGroup)
                 BigInt(
                   c.transactionOutput.value.value.group.get.quantity.value.toByteArray
                 )
               else if (c.transactionOutput.value.value.isSeries)
                 BigInt(
                   c.transactionOutput.value.value.series.get.quantity.value.toByteArray
                 )
               else if (c.transactionOutput.value.value.isAsset)
                 BigInt(
                   c.transactionOutput.value.value.asset.get.quantity.value.toByteArray
                 )
               else BigInt(0))
        })
        val keyIdentifier = key match {
          case LvlType => "LVL"
          case GroupType(groupId) =>
            "Group(" + Encoding.encodeToHex(groupId.toByteArray) + ")"
          case SeriesType(seriesId) =>
            "Series(" + Encoding.encodeToHex(seriesId.toByteArray) + ")"
          case GroupAndSeriesFungible(groupId, seriesId, _) =>
            "Asset(" + Encoding.encodeToHex(
              groupId.toByteArray
            ) + ", " + Encoding
              .encodeToHex(seriesId.toByteArray) + ")"
          case _ => "Unknown"
        }
        (keyIdentifier -> result)
      }
      Right(
        res
          .filterNot(_._1 == "Unknown")
          .map(x => x._1 + ": " + x._2.toString)
          .mkString("\n")
      )
    }

  }

}
