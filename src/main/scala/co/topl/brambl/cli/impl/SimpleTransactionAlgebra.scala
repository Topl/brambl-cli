package co.topl.brambl.cli.impl

import cats.effect.kernel.Resource
import cats.effect.kernel.Sync
import co.topl.brambl.builders.TransactionBuilderApi
import co.topl.brambl.codecs.AddressCodecs
import co.topl.brambl.dataApi.GenusQueryAlgebra
import co.topl.brambl.dataApi.WalletStateAlgebra
import co.topl.brambl.models.LockAddress
import co.topl.brambl.models.box.Attestation
import co.topl.brambl.models.box.Lock
import co.topl.brambl.utils.Encoding
import co.topl.brambl.wallet.CredentiallerInterpreter
import co.topl.brambl.wallet.WalletApi
import co.topl.node.services.BroadcastTransactionReq
import co.topl.node.services.NodeRpcGrpc
import io.grpc.ManagedChannel
import quivr.models.Proof

import java.io.FileInputStream
import java.io.FileOutputStream

trait SimpleTransactionAlgebra[F[_]] {

  def proveSimpleTransactionFromParams(
      inputFile: String,
      keyFile: String,
      password: String,
      outputFile: String
  ): F[Either[String, String]]

  def createSimpleTransactionFromParams(
      keyfile: String,
      password: String,
      fromParty: String,
      fromContract: String,
      someFromState: Option[Int],
      someToAddress: Option[LockAddress],
      someToParty: Option[String],
      someToContract: Option[String],
      amount: Long,
      outputFile: String
  ): F[Either[String, String]]

  def broadcastSimpleTransactionFromParams(
      provedTxFile: String
  ): F[Either[String, String]]

}
object SimpleTransactionAlgebra {

  def make[F[_]: Sync](
      walletApi: WalletApi[F],
      walletStateApi: WalletStateAlgebra[F],
      utxoAlgebra: GenusQueryAlgebra[F],
      transactionBuilderApi: TransactionBuilderApi[F],
      walletManagementUtils: WalletManagementUtils[F],
      channelResource: Resource[F, ManagedChannel]
  ) =
    new SimpleTransactionAlgebra[F] {

      override def broadcastSimpleTransactionFromParams(
          provedTxFile: String
      ): F[Either[String, String]] = {
        import co.topl.brambl.models.transaction.IoTransaction
        import cats.implicits._
        (for {
          provedTransaction <- Resource
            .make {
              Sync[F]
                .delay(new FileInputStream(provedTxFile))
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
        }).flatten.map(_ => Right("Transaction broadcasted"))
      }

      override def proveSimpleTransactionFromParams(
          inputFile: String,
          keyFile: String,
          password: String,
          outputFile: String
      ): F[Either[String, String]] = {
        import co.topl.brambl.models.transaction.IoTransaction
        import cats.implicits._
        for {
          ioTransaction <- Resource
            .make {
              Sync[F]
                .delay(new FileInputStream(inputFile))
            }(fos => Sync[F].delay(fos.close()))
            .use(fis => Sync[F].blocking(IoTransaction.parseFrom(fis)))
          keyPair <- walletManagementUtils.loadKeys(
            keyFile,
            password
          )
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
                .delay(new FileOutputStream(outputFile))
            )(fos => Sync[F].delay(fos.close()))
            .use(fos => Sync[F].delay(provedTransaction.writeTo(fos)))
        } yield Right("Transaction proved")
      }

      def createSimpleTransactionFromParams(
          keyfile: String,
          password: String,
          fromParty: String,
          fromContract: String,
          someFromState: Option[Int],
          someToAddress: Option[LockAddress],
          someToParty: Option[String],
          someToContract: Option[String],
          amount: Long,
          outputFile: String
      ): F[Either[String, String]] = {
        import TransactionBuilderApi.implicits._
        import cats.implicits._
        for {
          keyPair <- walletManagementUtils.loadKeys(
            keyfile,
            password
          )
          someCurrentIndices <- walletStateApi.getCurrentIndicesForFunds(
            fromParty,
            fromContract,
            someFromState
          )
          predicateFundsToUnlock <- someCurrentIndices
            .map(currentIndices =>
              walletStateApi.getLockByIndex(currentIndices)
            )
            .sequence
            .map(_.flatten.map(Lock().withPredicate(_)))
          someNextIndices <- walletStateApi.getNextIndicesForFunds(
            if (fromParty == "noparty") "self" else fromParty,
            if (fromParty == "noparty") "default"
            else fromContract
          )
          // Generate a new lock for the change, if possible
          changeLock <- someNextIndices
            .map(idx =>
              walletStateApi.getLock(
                if (fromParty == "noparty") "self" else fromParty,
                if (fromParty == "noparty") "default"
                else fromContract,
                idx.z
              )
            )
            .sequence
            .map(_.flatten)
          fromAddress <- transactionBuilderApi.lockAddress(
            predicateFundsToUnlock.get
          )
          response <- utxoAlgebra.queryUtxo(fromAddress)
          lvlTxos = response.filter(
            _.transactionOutput.value.value.isLvl
          )
          // either toAddress or both toContract and toParty must be defined
          toAddressOpt <- (
            someToAddress,
            someToParty,
            someToContract
          ) match {
            case (Some(address), _, _) => Sync[F].point(Some(address))
            case (None, Some(party), Some(contract)) =>
              walletStateApi
                .getAddress(party, contract, None)
                .map(
                  _.flatMap(addrStr =>
                    AddressCodecs.decodeAddress(addrStr).toOption
                  )
                )
            case _ => Sync[F].point(None)
          }
          res <-
            if (lvlTxos.isEmpty) {
              Sync[F].delay(Left("No LVL txos found"))
            } else {
              (changeLock, toAddressOpt) match {
                case (Some(lockPredicateForChange), Some(toAddress)) =>
                  for {
                    ioTransaction <- transactionBuilderApi
                      .buildSimpleLvlTransaction(
                        lvlTxos,
                        predicateFundsToUnlock.get.getPredicate,
                        lockPredicateForChange.getPredicate,
                        toAddress,
                        amount
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
                      Encoding.encodeToBase58Check(
                        lockPredicateForChange.getPredicate.toByteArray
                      ),
                      lockAddress.toBase58(),
                      vk.map(_ => "ExtendedEd25519"),
                      vk.map(x => Encoding.encodeToBase58(x.toByteArray)),
                      someNextIndices.get
                    )
                    _ <- Resource
                      .make(
                        Sync[F]
                          .delay(
                            new FileOutputStream(outputFile)
                          )
                      )(fos => Sync[F].delay(fos.close()))
                      .use { fos =>
                        Sync[F].delay(ioTransaction.writeTo(fos))
                      }
                  } yield Right("Transaction created successfully")
                case (None, _) =>
                  Sync[F].delay(Left("Unable to generate change lock"))
                case (_, _) =>
                  Sync[F].delay(Left("Unable to derive recipient address"))
              }
            }
        } yield res
      }
    }
}
