package co.topl.brambl.cli.impl

import cats.Monad
import cats.effect.kernel.Sync
import co.topl.brambl.builders.TransactionBuilderApi
import co.topl.brambl.dataApi.GenusQueryAlgebra
import co.topl.brambl.dataApi.WalletStateAlgebra
import co.topl.brambl.models.Event
import co.topl.brambl.models.box.AssetMintingStatement
import co.topl.brambl.wallet.WalletApi
import com.google.protobuf.ByteString
import io.circe.Json
import co.topl.genus.services.Txo
import co.topl.genus.services.TokenServiceFs2Grpc

trait SimpleMintingAlgebra[F[_]] {
  def createSimpleGroupMintingTransactionFromParams(
      keyFile: String,
      password: String,
      fromFellowship: String,
      fromTemplate: String,
      someFromInteraction: Option[Int],
      amount: Long,
      fee: Long,
      outputFile: String,
      groupPolicy: Event.GroupPolicy
  ): F[Unit]

  def createSimpleSeriesMintingTransactionFromParams(
      keyFile: String,
      password: String,
      fromFellowship: String,
      fromTemplate: String,
      someFromInteraction: Option[Int],
      amount: Long,
      fee: Long,
      outputFile: String,
      seriesPolicy: Event.SeriesPolicy
  ): F[Unit]

  def createSimpleAssetMintingTransactionFromParams(
      keyfile: String,
      password: String,
      fromFellowship: String,
      fromTemplate: String,
      someFromInteraction: Option[Int],
      fee: Long,
      outputFile: String,
      ephemeralMetadata: Option[Json],
      commitment: Option[ByteString],
      assetMintingStatement: AssetMintingStatement
  ): F[Unit]
}

object SimpleMintingAlgebra {

  import cats.implicits._

  def make[F[_]](
      psync: Sync[F],
      walletApi: WalletApi[F],
      walletStateApi: WalletStateAlgebra[F],
      walletManagementUtils: WalletManagementUtils[F],
      transactionBuilderApi: TransactionBuilderApi[F],
      utxoAlgebra: GenusQueryAlgebra[F]
  ): SimpleMintingAlgebra[F] = new SimpleMintingAlgebra[F]
    with WalletApiHelpers[F]
    with GroupMintingOps[F]
    with SeriesMintingOps[F]
    with AssetMintingOps[F] {

    override implicit val sync: cats.effect.kernel.Sync[F]= psync

    implicit val m: Monad[F] = sync

    val wsa: WalletStateAlgebra[F] = walletStateApi

    val tba = transactionBuilderApi

    val wa = walletApi

    private def sharedOps(
        keyfile: String,
        password: String,
        fromFellowship: String,
        fromTemplate: String,
        someFromInteraction: Option[Int]
    ) = for {
      keyPair <-
        walletManagementUtils
        .loadKeys(
          keyfile,
          password
          )
      someCurrentIndices <- getCurrentIndices(
        fromFellowship,
        fromTemplate,
        someFromInteraction
      )
      predicateFundsToUnlock <- getPredicateFundsToUnlock(someCurrentIndices)
      someNextIndices <- getNextIndices(fromFellowship, fromTemplate)
      changeLock <- getChangeLockPredicate(
        someNextIndices,
        fromFellowship,
        fromTemplate
      )
    } yield (
      keyPair,
      predicateFundsToUnlock.get,
      someCurrentIndices,
      someNextIndices,
      changeLock
    )

    override def createSimpleGroupMintingTransactionFromParams(
        keyfile: String,
        password: String,
        fromFellowship: String,
        fromTemplate: String,
        someFromInteraction: Option[Int],
        amount: Long,
        fee: Long,
        outputFile: String,
        groupPolicy: Event.GroupPolicy
    ): F[Unit] = for {
      tuple <- sharedOps(
        keyfile,
        password,
        fromFellowship,
        fromTemplate,
        someFromInteraction
      )
      (
        keyPair,
        predicateFundsToUnlock,
        someCurrentIndices,
        someNextIndices,
        changeLock
      ) = tuple
      fromAddress <- transactionBuilderApi.lockAddress(
        predicateFundsToUnlock
      )
      response <- utxoAlgebra
        .queryUtxo(fromAddress)
        .attempt
        .flatMap {
          _ match {
            case Left(_) =>
              Sync[F].raiseError(
                CreateTxError("Problem contacting network")
              ): F[Seq[Txo]]
            case Right(txos) => Sync[F].pure(txos: Seq[Txo])
          }
        }
      lvlTxos = response.filter(
        _.transactionOutput.value.value.isLvl
      )
      nonlvlTxos = response.filter(x => !x.transactionOutput.value.value.isLvl)
      _ <- buildGroupTxAux(
        lvlTxos,
        nonlvlTxos,
        predicateFundsToUnlock.getPredicate,
        amount,
        fee,
        someNextIndices,
        keyPair,
        outputFile,
        groupPolicy,
        changeLock
      )
    } yield ()

    override def createSimpleSeriesMintingTransactionFromParams(
        keyfile: String,
        password: String,
        fromFellowship: String,
        fromTemplate: String,
        someFromInteraction: Option[Int],
        amount: Long,
        fee: Long,
        outputFile: String,
        seriesPolicy: Event.SeriesPolicy
    ): F[Unit] = for {
      tuple <- sharedOps(
        keyfile,
        password,
        fromFellowship,
        fromTemplate,
        someFromInteraction
      )
      (
        keyPair,
        predicateFundsToUnlock,
        someCurrentIndices,
        someNextIndices,
        changeLock
      ) = tuple
      fromAddress <- transactionBuilderApi.lockAddress(
        predicateFundsToUnlock
      )
      response <- utxoAlgebra
        .queryUtxo(fromAddress)
        .attempt
        .flatMap {
          _ match {
            case Left(_) =>
              Sync[F].raiseError(
                CreateTxError("Problem contacting network")
              ): F[Seq[Txo]]
            case Right(txos) => Sync[F].pure(txos: Seq[Txo])
          }
        }
      lvlTxos = response.filter(
        _.transactionOutput.value.value.isLvl
      )
      nonLvlTxos = response.filter(x => !x.transactionOutput.value.value.isLvl)
      _ <- buildSeriesTxAux(
        lvlTxos,
        nonLvlTxos,
        predicateFundsToUnlock.getPredicate,
        amount,
        fee,
        someNextIndices,
        keyPair,
        outputFile,
        seriesPolicy,
        changeLock
      )
    } yield ()

    def createSimpleAssetMintingTransactionFromParams(
        keyfile: String,
        password: String,
        fromFellowship: String,
        fromTemplate: String,
        someFromInteraction: Option[Int],
        fee: Long,
        outputFile: String,
        ephemeralMetadata: Option[Json],
        commitment: Option[ByteString],
        assetMintingStatement: AssetMintingStatement
    ): F[Unit] = for {
      tuple <- sharedOps(
        keyfile,
        password,
        fromFellowship,
        fromTemplate,
        someFromInteraction
      )
      (
        keyPair,
        predicateFundsToUnlock,
        someCurrentIndices,
        someNextIndices,
        changeLock
      ) = tuple
      fromAddress <- transactionBuilderApi.lockAddress(
        predicateFundsToUnlock
      )
      response <- utxoAlgebra
        .queryUtxo(fromAddress)
        .attempt
        .flatMap {
          _ match {
            case Left(_) =>
              Sync[F].raiseError(
                CreateTxError("Problem contacting network")
              ): F[Seq[Txo]]
            case Right(txos) => Sync[F].pure(txos: Seq[Txo])
          }
        }
      lvlTxos = response.filter(
        _.transactionOutput.value.value.isLvl
      )
      nonLvlTxos = response.filter(x =>
        (
          !x.transactionOutput.value.value.isLvl &&
            x.outputAddress != assetMintingStatement.groupTokenUtxo &&
            x.outputAddress != assetMintingStatement.seriesTokenUtxo
        )
      )
      groupTxo <- response
        .filter(
          _.transactionOutput.value.value.isGroup
        )
        .find(_.outputAddress == assetMintingStatement.groupTokenUtxo)
        .map(Sync[F].delay(_))
        .getOrElse(
          Sync[F].raiseError(
            new Exception(
              "Group token utxo not found"
            )
          )
        )
      seriesTxo <- response
        .filter(
          _.transactionOutput.value.value.isSeries
        )
        .find(_.outputAddress == assetMintingStatement.seriesTokenUtxo)
        .map(Sync[F].delay(_))
        .getOrElse(
          Sync[F].raiseError(
            new Exception(
              "Series token utxo not found"
            )
          )
        )
      _ <- buildAssetTxAux(
        keyPair,
        outputFile,
        lvlTxos,
        nonLvlTxos,
        groupTxo,
        seriesTxo,
        fromAddress,
        predicateFundsToUnlock.getPredicate,
        fee,
        someNextIndices,
        assetMintingStatement,
        ephemeralMetadata,
        commitment,
        changeLock
      )
    } yield ()
  }

}
