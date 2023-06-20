package co.topl.brambl.cli.impl

import cats.effect.kernel.Resource
import cats.effect.kernel.Sync
import co.topl.brambl.cli.BramblCliValidatedParams
import co.topl.brambl.dataApi.{WalletKeyApiAlgebra, WalletStateAlgebra, GenusQueryAlgebra}
import co.topl.brambl.models.box.{Attestation, Lock}
import co.topl.brambl.utils.Encoding
import co.topl.brambl.wallet.{CredentiallerInterpreter, WalletApi}
import co.topl.crypto.encryption.VaultStore
import co.topl.brambl.builders.TransactionBuilderApi
import co.topl.brambl.codecs.AddressCodecs
import co.topl.brambl.models.LockAddress
import co.topl.node.services.BroadcastTransactionReq
import co.topl.node.services.NodeRpcGrpc
import io.grpc.ManagedChannel
import quivr.models.KeyPair
import quivr.models.Proof

import java.io.FileInputStream
import java.io.FileOutputStream

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
      dataApi: WalletKeyApiAlgebra[F],
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
        for {
          ioTransaction <- Resource
            .make {
              Sync[F]
                .delay(new FileInputStream(params.someInputFile.get))
            }(fos => Sync[F].delay(fos.close()))
            .use(fis => Sync[F].blocking(IoTransaction.parseFrom(fis)))
          keyPair <- loadKeysFromParam(params)
          credentialer <- Sync[F].delay(
            CredentiallerInterpreter.make[F](walletApi, walletStateApi, keyPair)
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
            .map(currentIndices => walletStateApi.getLockByIndex(currentIndices))
            .sequence
            .map(_.flatten.map(Lock().withPredicate(_)))
          someNextIndices <- walletStateApi.getNextIndicesForFunds(
            "self",
            "default"
          )
          // Generate a new lock for the change, if possible
          changeLock <- someNextIndices.map(idx =>
            walletStateApi.getLock(
              "self",
              "default",
              idx.z
            )
          ).sequence.map(_.flatten)
          fromAddress <- transactionBuilderApi.lockAddress(
            predicateFundsToUnlock.get
          )
          response <- utxoAlgebra.queryUtxo(fromAddress)
          lvlTxos = response.filter(
            _.transactionOutput.value.value.isLvl
          )
          // either toAddress or both toContract and toParty must be defined
          toAddressOpt <- (params.toAddress, params.someToParty, params.someToContract) match {
            case (Some(address), _, _) =>  Sync[F].point(Some(address))
            case (None, Some(party), Some(contract)) => walletStateApi.getAddress(party, contract, None).map(_.flatMap(
              addrStr => AddressCodecs.decodeAddress(addrStr).toOption
            ))
            case _ => Sync[F].point(None)
          }
          _ <-
            if (lvlTxos.isEmpty) {
              Sync[F].delay(println(fromAddress))
              println(fromAddress)
              println(fromAddress.toBase58)
              Sync[F].delay(println(someCurrentIndices))
              Sync[F].delay(println(fromAddress.toBase58))
              Sync[F].delay(println("No LVL txos found", someCurrentIndices))
            }
             else (changeLock, toAddressOpt) match {
              case (Some(lockPredicateForChange), Some(toAddress)) => for {
                ioTransaction <- transactionBuilderApi
                  .buildSimpleLvlTransaction(
                    lvlTxos,
                    predicateFundsToUnlock.get.getPredicate,
                    lockPredicateForChange.getPredicate,
                    toAddress,
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
              case (None, _) => Sync[F].delay(println("Unable to generate change lock"))
              case (_, _) => Sync[F].delay(println("Unable to derive recipient address"))
            }
        } yield ()
      }
    }
}
