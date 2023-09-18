package co.topl.brambl.cli.impl

import cats.effect.kernel.Resource
import cats.effect.kernel.Sync
import co.topl.brambl.builders.TransactionBuilderApi
import co.topl.brambl.codecs.AddressCodecs
import co.topl.brambl.dataApi.GenusQueryAlgebra
import co.topl.brambl.dataApi.WalletStateAlgebra
import co.topl.brambl.models.Indices
import co.topl.brambl.models.LockAddress
import co.topl.brambl.models.box.Lock
import co.topl.brambl.utils.Encoding
import co.topl.brambl.wallet.CredentiallerInterpreter
import co.topl.brambl.wallet.WalletApi
import co.topl.genus.services.Txo
import co.topl.node.services.BroadcastTransactionReq
import co.topl.node.services.NodeRpcGrpc
import io.grpc.ManagedChannel
import quivr.models.KeyPair

import java.io.FileInputStream
import java.io.FileOutputStream

trait SimpleTransactionAlgebra[F[_]] {

  def proveSimpleTransactionFromParams(
      inputRes: Resource[F, FileInputStream],
      keyFile: String,
      password: String,
      outputRes: Resource[F, FileOutputStream]
  ): F[Either[SimpleTransactionAlgebraError, Unit]]

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
  ): F[Either[SimpleTransactionAlgebraError, Unit]]

  def broadcastSimpleTransactionFromParams(
      provedTxFile: String
  ): F[Either[SimpleTransactionAlgebraError, Unit]]

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
      ): F[Either[SimpleTransactionAlgebraError, Unit]] = {
        import co.topl.brambl.models.transaction.IoTransaction
        import cats.implicits._
        val inputRes = Resource
          .make {
            Sync[F]
              .delay(new FileInputStream(provedTxFile))
          }(fos => Sync[F].delay(fos.close()))

        (for {
          provedTransaction <-
            inputRes.use(fis =>
              Sync[F]
                .blocking(IoTransaction.parseFrom(fis))
                .adaptErr({ case _ =>
                  InvalidProtobufFile("Invalid protobuf file")
                })
            )
          response <- channelResource.use { channel =>
            (for {
              blockingStub <- Sync[F]
                .point(NodeRpcGrpc.blockingStub(channel))
                .adaptErr(_ => CannotInitializeProtobuf("Cannot obtain stub"))
              response <- Sync[F]
                .blocking(
                  blockingStub
                    .broadcastTransaction(
                      BroadcastTransactionReq(provedTransaction)
                    )
                )
                .adaptErr(_ => NetworkProblem("Problem connecting to node"))
            } yield response)
          }
        } yield response).attempt.map(e =>
          e match {
            case Right(_)                               => ().asRight
            case Left(e: SimpleTransactionAlgebraError) => e.asLeft
            case Left(e) => UnexpectedError(e.getMessage()).asLeft
          }
        )
      }

      override def proveSimpleTransactionFromParams(
          inputRes: Resource[F, FileInputStream],
          keyFile: String,
          password: String,
          outputRes: Resource[F, FileOutputStream]
      ): F[Either[SimpleTransactionAlgebraError, Unit]] = {
        import co.topl.brambl.models.transaction.IoTransaction
        import cats.implicits._

        (for {
          ioTransaction <- inputRes.use(fis =>
            Sync[F]
              .blocking(IoTransaction.parseFrom(fis))
              .adaptErr(_ => InvalidProtobufFile("Invalid protobuf file"))
          )
          keyPair <- walletManagementUtils
            .loadKeys(
              keyFile,
              password
            )
          credentialer <- Sync[F]
            .delay(
              CredentiallerInterpreter
                .make[F](walletApi, walletStateApi, keyPair)
            )
          provedTransaction <- credentialer.prove(ioTransaction)
          _ <- outputRes.use(fos =>
            Sync[F]
              .delay(provedTransaction.writeTo(fos))
              .adaptErr(_ =>
                CannotSerializeProtobufFile("Cannot write to file")
              )
          )
        } yield ()).attempt.map(e =>
          e match {
            case Right(_)                               => ().asRight
            case Left(e: SimpleTransactionAlgebraError) => e.asLeft
            case Left(e) => UnexpectedError(e.getMessage()).asLeft
          }
        )
      }

      private def buildTransaction(
          lvlTxos: Seq[Txo],
          predicateFundsToUnlock: Lock.Predicate,
          lockForChange: Lock,
          recipientLockAddress: LockAddress,
          amount: Long,
          someNextIndices: Option[Indices],
          keyPair: KeyPair,
          outputFile: String
      ) = {
        import cats.implicits._
        import TransactionBuilderApi.implicits._

        for {
          ioTransaction <- transactionBuilderApi
            .buildSimpleLvlTransaction(
              lvlTxos,
              predicateFundsToUnlock,
              lockForChange.getPredicate,
              recipientLockAddress,
              amount
            )
          // Only save to wallet state if there is a change output in the transaction
          _ <-
            if (ioTransaction.outputs.length >= 2) for {
              lockAddress <-
                transactionBuilderApi.lockAddress(
                  lockForChange
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
                  lockForChange.getPredicate.toByteArray
                ),
                lockAddress.toBase58(),
                vk.map(_ => "ExtendedEd25519"),
                vk.map(x => Encoding.encodeToBase58(x.toByteArray)),
                someNextIndices.get
              )
            } yield ()
            else {
              Sync[F].delay(())
            }
          _ <- Resource
            .make(
              Sync[F]
                .delay(
                  new FileOutputStream(outputFile)
                )
            )(fos => Sync[F].delay(fos.close()))
            .use { fos =>
              Sync[F]
                .delay(ioTransaction.writeTo(fos))
                .adaptErr(_ =>
                  CannotSerializeProtobufFile(
                    "Cannot write to file"
                  ): SimpleTransactionAlgebraError
                )
            }
        } yield ()
      }

      override def createSimpleTransactionFromParams(
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
      ): F[Either[SimpleTransactionAlgebraError, Unit]] = {
        import cats.implicits._

        (for {
          keyPair <- walletManagementUtils
            .loadKeys(
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
          _ <-
            (if (lvlTxos.isEmpty) {
               Sync[F].raiseError(CreateTxError("No LVL txos found"))
             } else {
               (changeLock, toAddressOpt) match {
                 case (Some(lockPredicateForChange), Some(toAddress)) =>
                   buildTransaction(
                     lvlTxos,
                     predicateFundsToUnlock.get.getPredicate,
                     lockPredicateForChange,
                     toAddress,
                     amount,
                     someNextIndices,
                     keyPair,
                     outputFile
                   )
                 case (None, _) =>
                   Sync[F].raiseError(
                     CreateTxError("Unable to generate change lock")
                   )
                 case (_, _) =>
                   Sync[F].raiseError(
                     CreateTxError("Unable to derive recipient address")
                   )
               }
             })
        } yield ()).attempt.map(e =>
          e match {
            case Right(_)                               => ().asRight
            case Left(e: SimpleTransactionAlgebraError) => e.asLeft
            case Left(e) => UnexpectedError(e.getMessage()).asLeft
          }
        )

      }
    }
}
