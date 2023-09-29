package co.topl.brambl.cli.impl

import cats.Monad
import cats.effect.kernel.Sync
import co.topl.brambl.builders.TransactionBuilderApi
import co.topl.brambl.dataApi.GenusQueryAlgebra
import co.topl.brambl.dataApi.WalletStateAlgebra
import co.topl.brambl.models.Event
import co.topl.brambl.wallet.WalletApi

trait SimpleMintingAlgebra[F[_]] {
  def createSimpleGroupMintingTransactionFromParams(
      keyFile: String,
      password: String,
      fromParty: String,
      fromContract: String,
      someFromState: Option[Int],
      amount: Long,
      fee: Long,
      outputFile: String,
      groupPolicy: Event.GroupPolicy
  ): F[Unit]

  def createSimpleSeriesMintingTransactionFromParams(
      keyFile: String,
      password: String,
      fromParty: String,
      fromContract: String,
      someFromState: Option[Int],
      amount: Long,
      fee: Long,
      outputFile: String,
      seriesPolicy: Event.SeriesPolicy
  ): F[Unit]

  def createSimpleAssetMintingTransactionFromParams(
      keyFile: String,
      password: String,
      fromParty: String,
      fromContract: String,
      someFromState: Option[Int],
      amount: Long,
      fee: Long,
      outputFile: String,
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
    with SeriesMintingOps[F] {

    override implicit val sync = psync

    implicit val m: Monad[F] = sync

    val wsa: WalletStateAlgebra[F] = walletStateApi

    val tba = transactionBuilderApi

    val wa = walletApi

    import co.topl.brambl.syntax._

    override def createSimpleGroupMintingTransactionFromParams(
        keyfile: String,
        password: String,
        fromParty: String,
        fromContract: String,
        someFromState: Option[Int],
        amount: Long,
        fee: Long,
        outputFile: String,
        groupPolicy: Event.GroupPolicy
    ): F[Unit] = for {
      keyPair <-
        walletManagementUtils
          .loadKeys(
            keyfile,
            password
          )
      someCurrentIndices <- getCurrentIndices(
        fromParty,
        fromContract,
        someFromState
      )
      predicateFundsToUnlock <- getPredicateFundsToUnlock(someCurrentIndices)
      someNextIndices <- getNextIndices(fromParty, fromContract)
      changeLock <- getChangeLockPredicate(
        someNextIndices,
        fromParty,
        fromContract
      )
      fromAddress <- transactionBuilderApi.lockAddress(
        predicateFundsToUnlock.get
      )
      response <- utxoAlgebra.queryUtxo(fromAddress)
      lvlTxos = response.filter(
        _.transactionOutput.value.value.isLvl
      )
      _ <- buildGroupTxAux(
        lvlTxos,
        predicateFundsToUnlock.get.getPredicate,
        amount,
        fee,
        someNextIndices,
        keyPair,
        outputFile,
        groupPolicy.computeId,
        groupPolicy.label,
        groupPolicy.registrationUtxo,
        groupPolicy.fixedSeries,
        changeLock
      )
    } yield ()

    override def createSimpleSeriesMintingTransactionFromParams(
        keyfile: String,
        password: String,
        fromParty: String,
        fromContract: String,
        someFromState: Option[Int],
        amount: Long,
        fee: Long,
        outputFile: String,
        seriesPolicy: Event.SeriesPolicy
    ): F[Unit] = for {
      keyPair <-
        walletManagementUtils
          .loadKeys(
            keyfile,
            password
          )
      someCurrentIndices <- getCurrentIndices(
        fromParty,
        fromContract,
        someFromState
      )
      predicateFundsToUnlock <- getPredicateFundsToUnlock(someCurrentIndices)
      someNextIndices <- getNextIndices(fromParty, fromContract)
      changeLock <- getChangeLockPredicate(
        someNextIndices,
        fromParty,
        fromContract
      )
      fromAddress <- transactionBuilderApi.lockAddress(
        predicateFundsToUnlock.get
      )
      response <- utxoAlgebra.queryUtxo(fromAddress)
      lvlTxos = response.filter(
        _.transactionOutput.value.value.isLvl
      )
      _ <- buildSeriesTxAux(
        lvlTxos,
        predicateFundsToUnlock.get.getPredicate,
        amount,
        fee,
        someNextIndices,
        keyPair,
        outputFile,
        seriesPolicy.computeId,
        seriesPolicy.label,
        seriesPolicy.tokenSupply,
        seriesPolicy.quantityDescriptor,
        seriesPolicy.fungibility,
        seriesPolicy.registrationUtxo,
        seriesPolicy.ephemeralMetadataScheme,
        seriesPolicy.permanentMetadataScheme,
        changeLock
      )
    } yield ()

    def createSimpleAssetMintingTransactionFromParams(
        keyFile: String,
        password: String,
        fromParty: String,
        fromContract: String,
        someFromState: Option[Int],
        amount: Long,
        fee: Long,
        outputFile: String,
        assetMintingStatement: AssetMintingStatement
    ): F[Unit] = ???

  }

}
