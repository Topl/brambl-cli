package co.topl.brambl.cli.impl

import cats.effect.kernel.Resource
import cats.effect.kernel.Sync
import co.topl.brambl.cli.BramblCliValidatedParams
import co.topl.brambl.dataApi.DataApi
import co.topl.brambl.utils.Encoding
import co.topl.brambl.wallet.CredentiallerInterpreter
import co.topl.brambl.wallet.WalletApi
import co.topl.crypto.encryption.VaultStore
import quivr.models.KeyPair

import java.io.FileInputStream
import java.io.FileOutputStream
import co.topl.node.services.NodeRpcGrpc
import co.topl.node.services.BroadcastTransactionReq
import io.grpc.ManagedChannel
import co.topl.brambl.models.box.Attestation
import quivr.models.Proof
import co.topl.brambl.cli.impl.GenusQueryAlgebra
import co.topl.brambl.codecs.AddressCodecs
import co.topl.brambl.Context
import cats.Id
import co.topl.brambl.validation.TransactionAuthorizationInterpreter

trait SimpleTransactionAlgebra[F[_]] {

  def proveSimpleTransactionFromParams(
      params: BramblCliValidatedParams
  ): F[Unit]

  def createSimpleTransactionFromParams(
      params: BramblCliValidatedParams
  ): F[Unit]

  def broadcastSimpleTransactionFromParams(
      params: BramblCliValidatedParams
  ): F[Unit]

}
object SimpleTransactionAlgebra {

  def make[F[_]: Sync](
      dataApi: DataApi[F],
      walletApi: WalletApi[F],
      walletStateApi: WalletStateAlgebra[F],
      utxoAlgebra: GenusQueryAlgebra[F],
      transactionBuilderApi: TransactionBuilderApi[F],
      channelResource: Resource[F, ManagedChannel]
  ) =
    new SimpleTransactionAlgebra[F] {

      override def broadcastSimpleTransactionFromParams(
          params: BramblCliValidatedParams
      ): F[Unit] = {
        import co.topl.brambl.models.transaction.IoTransaction
        import cats.implicits._
        (for {
          provedTransaction <- Resource
            .make {
              Sync[F]
                .delay(new FileInputStream(params.someInputFile.get))
            }(fos => Sync[F].delay(fos.close()))
            .use(fis => Sync[F].blocking(IoTransaction.parseFrom(fis)))

        } yield (for {
          channel <- channelResource
        } yield channel).use { channel =>
          for {
            blockingStub <- Sync[F].point(
              NodeRpcGrpc.blockingStub(channel)
            )
            response <- Sync[F].blocking(
              blockingStub
                .broadcastTransaction(
                  BroadcastTransactionReq(provedTransaction)
                )
            )
          } yield {
            response
          }
        }).flatten.map(_ => ())
      }

      override def proveSimpleTransactionFromParams(
          params: BramblCliValidatedParams
      ): F[Unit] = {
        import co.topl.brambl.models.transaction.IoTransaction
        import cats.implicits._
        import co.topl.quivr.api.Verifier.instances.verifierInstance
        for {
          ioTransaction <- Resource
            .make {
              Sync[F]
                .delay(new FileInputStream(params.someInputFile.get))
            }(fos => Sync[F].delay(fos.close()))
            .use(fis => Sync[F].blocking(IoTransaction.parseFrom(fis)))
          keyPair <- loadKeysFromParam(params)
          credentialer <- Sync[F].delay(
            CredentiallerInterpreter.make(dataApi, keyPair)
          )
          unprovenTransaction = ioTransaction.copy(
            inputs = ioTransaction.inputs.map(x =>
              x.copy(attestation =
                x.attestation.copy(value =
                  Attestation.Value.Predicate(
                    x.attestation.value.predicate
                      .map(_.copy(responses = List(Proof(Proof.Value.Empty))))
                      .get
                  )
                )
              )
            )
          )
          provedTransaction <- credentialer.prove(unprovenTransaction)
          errors <- TransactionAuthorizationInterpreter
            .make[F]()
            .validate(Context[F](provedTransaction, 500, _ => None))(
              provedTransaction
            )
          _ <- Sync[F].delay(
            println("Errors: " + errors.left.map(_.toString()))
          )
          _ <- Resource
            .make(
              Sync[F]
                .delay(new FileOutputStream(params.someOutputFile.get))
            )(fos => Sync[F].delay(fos.close()))
            .use(fos => Sync[F].delay(provedTransaction.writeTo(fos)))
        } yield ()
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
          wallet <- readInputFile(params.someKeyFile)
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

      def createSimpleTransactionFromParams(
          params: BramblCliValidatedParams
      ): F[Unit] = {
        import TransactionBuilderApi.implicits._
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
          _ <- Sync[F].delay(
            println("From address: " + AddressCodecs.encodeAddress(fromAddress))
          )
          response <- utxoAlgebra.queryUtxo(fromAddress)
          lvlTxos = response.filter(
            _.transactionOutput.value.value.isLvl
          )
          _ <-
            if (lvlTxos.isEmpty) {
              Sync[F].delay(println("No LVL txos found"))
            } else
              for {
                ioTransaction <- transactionBuilderApi
                  .buildSimpleLvlTransaction(
                    lvlTxos,
                    predicateFundsToUnlock.get,
                    lockPredicateForChange,
                    params.toAddress.get,
                    params.amount
                  )
                lockAddress <- transactionBuilderApi.lockAddress(
                  lockPredicateForChange
                )
                vk <- someNextIndices
                  .map(nextIndices =>
                    walletApi
                      .deriveChildKeys(keyPair, nextIndices)
                      .map(_.vk)
                  )
                  .sequence
                _ <- walletStateApi.updateWalletState(
                  lockAddress.toBase58(),
                  Encoding.encodeToBase58Check(
                    lockPredicateForChange.toByteArray
                  ),
                  vk.map(_ => "ExtendedEd25519"),
                  vk.map(x => Encoding.encodeToBase58(x.toByteArray)),
                  someNextIndices.get
                )
                _ <- Resource
                  .make(
                    Sync[F]
                      .delay(new FileOutputStream(params.someOutputFile.get))
                  )(fos => Sync[F].delay(fos.close()))
                  .use { fos =>
                    Sync[F].delay(ioTransaction.writeTo(fos))
                  }
              } yield ()
        } yield ()
      }
    }
}
