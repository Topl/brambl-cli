package co.topl.brambl.cli.impl

import cats.effect.kernel.Resource
import cats.effect.kernel.Sync
import co.topl.brambl.cli.BramblCliValidatedParams
import co.topl.brambl.dataApi.DataApi
import co.topl.brambl.models.LockAddress
import co.topl.brambl.models.transaction.IoTransaction
import co.topl.brambl.wallet.WalletApi
import co.topl.crypto.encryption.VaultStore
import co.topl.genus.services.QueryByAddressRequest
import co.topl.genus.services.TransactionServiceGrpc
import co.topl.genus.services.TxoState
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import quivr.models.KeyPair

import java.io.FileOutputStream
import co.topl.brambl.utils.Encoding

trait SimpleTransactionOps[F[_]] {

  def createSimpleTransactionFromParams(
      params: BramblCliValidatedParams
  ): F[IoTransaction]

}
object SimpleTransactionOps {

  def make[F[_]: Sync](
      dataApi: DataApi[F],
      walletApi: WalletApi[F],
      walletStateApi: WalletStateApi[F],
      transactionBuilderApi: TransactionBuilderApi[F]
  ) =
    new SimpleTransactionOps[F] {

      def channelResource(address: String, port: Int) = {
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

      def readInputFile(
          someInputFile: Option[String]
      ): F[VaultStore[F]] = {
        someInputFile match {
          case Some(inputFile) =>
            import cats.implicits._
            dataApi
              .getMainKeyVaultStore(inputFile)
              .flatMap(
                _.fold(
                  x =>
                    Sync[F].raiseError[VaultStore[F]](
                      new Throwable("Error reading input file: " + x)
                    ),
                  Sync[F].point(_)
                )
              )

          case None =>
            Sync[F].raiseError(
              (new Throwable("No input file (should not happen)"))
            )
        }
      }

      def loadKeysFromParam(params: BramblCliValidatedParams) = {
        import cats.implicits._
        for {
          wallet <- readInputFile(params.someInputFile)
          keyPair <-
            walletApi
              .extractMainKey(wallet, params.password.getBytes())
              .flatMap(
                _.fold(
                  _ =>
                    Sync[F].raiseError[KeyPair](
                      new Throwable("No input file (should not happen)")
                    ),
                  Sync[F].point(_)
                )
              )
        } yield keyPair
      }

      def getTxos(
          fromAddress: LockAddress,
          channel: ManagedChannel
      ) = {
        import cats.implicits._
        for {
          blockingStub <- Sync[F].point(
            TransactionServiceGrpc.blockingStub(channel)
          )
          response <- Sync[F].blocking(
            blockingStub
              .getTxosByAddress(
                QueryByAddressRequest(fromAddress, None, TxoState.UNSPENT)
              )
          )
        } yield {
          response.txos
        }
      }

      def createSimpleTransactionFromParams(
          params: BramblCliValidatedParams
      ): F[IoTransaction] = {
        import TransactionBuilderApi.implicits._
        (for {
          channel <- channelResource(params.host, params.port)
        } yield (channel)).use { case (channel) =>
          import cats.implicits._
          for {
            keyPair <- loadKeysFromParam(params)
            someCurrentIndices <- walletStateApi.getCurrentIndicesForFunds(
              params.fromParty,
              params.fromContract,
              params.someFromState
            )
            predicateFundsToUnlock <- someCurrentIndices
              .map(currentIndices =>
                walletStateApi
                  .getLockByIndex(currentIndices)
              )
              .sequence
              .map(_.get)
            someNextIndices <- walletStateApi.getNextIndicesForFunds(
              "self", // default party to send funds to
              "default" // default contract to send funds to
            )
            lockPredicateForChange <- someNextIndices
              .map(nextIndices =>
                walletApi
                  .deriveChildKeys(keyPair, nextIndices)
                  .map(_.vk)
                  .flatMap(x => transactionBuilderApi.lockPredicateSignature(x))
              )
              .sequence
              .map(
                _.get
              ) // the deault party to send funds to should always be present
            fromAddress <- transactionBuilderApi.lockAddress(
              predicateFundsToUnlock.get
            )
            response <- getTxos(fromAddress, channel)
            lvlTxos = response.filter(
              _.transactionOutput.value.value.isLvl
            )
            _ <- Sync[F].raiseWhen(lvlTxos.isEmpty)(
              new Throwable("No LVL txos found")
            )
            lockAddress <- transactionBuilderApi.lockAddress(
              lockPredicateForChange
            )
            ioTransaction <- transactionBuilderApi.buildSimpleLvlTransaction(
              lvlTxos,
              predicateFundsToUnlock.get,
              lockPredicateForChange,
              params.toAddress.get,
              params.amount
            )
            _ <- walletStateApi.updateWalletState(
              Encoding.encodeToBase58Check(lockPredicateForChange.toByteArray),
              lockAddress.toBase58(),
              someNextIndices.get
            )
            _ <- Resource
              .make(
                Sync[F].delay(new FileOutputStream(params.someOutputFile.get))
              )(fos => Sync[F].delay(fos.close()))
              .use(fos => Sync[F].delay(ioTransaction.writeTo(fos)))
          } yield ioTransaction
        }
      }
    }
}
