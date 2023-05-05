package co.topl.brambl.cli.impl

import cats.effect.kernel.Resource
import cats.effect.kernel.Sync
import co.topl.brambl.cli.BramblCliValidatedParams
import co.topl.brambl.constants.NetworkConstants
import co.topl.brambl.dataApi.DataApi
import co.topl.brambl.models.TransactionId
import co.topl.brambl.models.TransactionOutputAddress
import co.topl.brambl.models.transaction.IoTransaction
import co.topl.brambl.wallet.WalletApi
import co.topl.crypto.encryption.VaultStore
import co.topl.genus.services.Txo
import com.google.protobuf.ByteString
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import quivr.models.Int128
import quivr.models.KeyPair
import java.io.FileOutputStream

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
          params: BramblCliValidatedParams,
          channel: ManagedChannel
      ) = {
        import cats.implicits._
        for {
          // blockingStub <- Sync[F].point(
          //   TransactionServiceGrpc.blockingStub(channel)
          // )
          // response <- Sync[F].blocking(
          //   blockingStub
          //     .getTxosByAddress(
          //       QueryByAddressRequest(params.toAddress.get, None)
          //     )
          // )
          keyPair <- loadKeysFromParam(params)
          currentIndices <- walletStateApi.getCurrentIndicesForDefaultFunds()
          vkOfFundsToUnlock <- walletApi
            .deriveChildKeys(keyPair, currentIndices)
            .map(_.vk)
          lockPredicate <- transactionBuilderApi.lockPredicate(
            vkOfFundsToUnlock
          )
          output <- transactionBuilderApi.lvlOuput(
            lockPredicate,
            Int128(ByteString.copyFrom(BigInt(params.amount).toByteArray))
          )
          mockReturnValue <- Sync[F].point(
            Seq(
              Txo(
                output,
                co.topl.genus.services.TxoState.SPENT,
                TransactionOutputAddress(
                  NetworkConstants.MAIN_NETWORK_ID,
                  NetworkConstants.MAIN_LEDGER_ID,
                  0,
                  TransactionId(ByteString.copyFrom(Array.fill(32)(0.toByte)))
                )
              )
            )
          )
        } yield {
          // response.txos
          mockReturnValue
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
            response <- getTxos(params, channel)
            lvlTxos = response.filter(
              _.transactionOutput.value.value.isLvl
            )
            _ <- Sync[F].raiseWhen(lvlTxos.isEmpty)(
              new Throwable("No LVL txos found")
            )
            keyPair <- loadKeysFromParam(params)
            currentIndices <- walletStateApi.getCurrentIndicesForDefaultFunds()
            vkOfFundsToUnlock <- walletApi
              .deriveChildKeys(keyPair, currentIndices)
              .map(_.vk)
            nextIndices <- walletStateApi.getNextIndicesForDefaultFunds()
            vkFundsToLock <- walletApi
              .deriveChildKeys(keyPair, nextIndices)
              .map(_.vk)
            lockAddress <- transactionBuilderApi.lockAddress(
              vkFundsToLock
            )
            ioTransaction <- transactionBuilderApi.buildSimpleLvlTransaction(
              lvlTxos,
              vkOfFundsToUnlock,
              vkFundsToLock,
              params.toAddress.get,
              params.amount
            )
            _ <- walletStateApi.updateWalletState(
              lockAddress.toBase58(),
              nextIndices
            )
            _ <- Resource
              .make(Sync[F].delay(new FileOutputStream("transaction.pbuf")))(
                fos => Sync[F].delay(fos.close())
              )
              .use(fos => Sync[F].delay(ioTransaction.writeTo(fos)))
          } yield ioTransaction
        }
      }
    }
}
