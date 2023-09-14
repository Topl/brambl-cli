package co.topl.brambl.cli.impl

import cats.data.EitherT
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
import co.topl.brambl.wallet.Credentialler
import co.topl.brambl.wallet.CredentiallerInterpreter
import co.topl.brambl.wallet.WalletApi
import co.topl.genus.services.Txo
import co.topl.node.services.BroadcastTransactionReq
import co.topl.node.services.BroadcastTransactionRes
import co.topl.node.services.NodeRpcGrpc
import io.grpc.ManagedChannel
import quivr.models.KeyPair

import java.io.FileInputStream
import java.io.FileOutputStream

sealed trait SimpleTransactionAlgebraError extends Throwable {

  def description: String

}

case class CannotInitializeProtobuf(description: String)
    extends SimpleTransactionAlgebraError

case class InvalidProtobufFile(description: String)
    extends SimpleTransactionAlgebraError

case class CannotSerializeProtobufFile(description: String)
    extends SimpleTransactionAlgebraError

case class NetworkProblem(description: String)
    extends SimpleTransactionAlgebraError

case class UnexpectedError(description: String)
    extends SimpleTransactionAlgebraError

case class CreateTxError(description: String)
    extends SimpleTransactionAlgebraError

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

      import TransactionUtils._

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
          provedTransaction <- lift[F, IoTransaction](
            inputRes.use(fis =>
              Sync[F].blocking(
                Either
                  .catchNonFatal(IoTransaction.parseFrom(fis))
                  .leftMap(_ => InvalidProtobufFile("Invalid protobuf file"))
              )
            )
          )
        } yield EitherT[
          F,
          SimpleTransactionAlgebraError,
          BroadcastTransactionRes
        ]((for {
          channel <- channelResource
        } yield channel).use { channel =>
          (for {
            blockingStub <- lift[F, NodeRpcGrpc.NodeRpcBlockingStub](
              Sync[F]
                .point(
                  Either
                    .catchNonFatal(NodeRpcGrpc.blockingStub(channel))
                    .leftMap(_ =>
                      CannotInitializeProtobuf("Cannot obtain stub")
                    )
                )
            )
            response <- lift[F, BroadcastTransactionRes](
              Sync[F].blocking(
                Either
                  .catchNonFatal(
                    blockingStub
                      .broadcastTransaction(
                        BroadcastTransactionReq(provedTransaction)
                      )
                  )
                  .leftMap(_ => NetworkProblem("Problem connecting to node"))
              )
            )
          } yield {
            response
          }).value
        })).flatten.value.map(_.void)
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
          ioTransaction <- lift[F, IoTransaction](
            inputRes.use(fis =>
              Sync[F].blocking(
                Either
                  .catchNonFatal(IoTransaction.parseFrom(fis))
                  .leftMap(_ => InvalidProtobufFile("Invalid protobuf file"))
              )
            )
          )
          keyPair <- lift[F, KeyPair](
            walletManagementUtils
              .loadKeys(
                keyFile,
                password
              )
              .map(Right(_))
          )
          credentialer <- lift[F, Credentialler[F]](
            Sync[F]
              .delay(
                CredentiallerInterpreter
                  .make[F](walletApi, walletStateApi, keyPair)
              )
              .map(Right(_))
          )
          provedTransaction <- lift[F, IoTransaction](
            credentialer.prove(ioTransaction).map(Right(_))
          )
          _ <- lift[F, Unit](
            outputRes.use(fos =>
              Sync[F]
                .delay(
                  Either
                    .catchNonFatal(provedTransaction.writeTo(fos))
                    .leftMap(_ =>
                      CannotSerializeProtobufFile("Cannot write to file")
                    )
                )
            )
          )
        } yield ()).value
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
          ioTransaction <- liftF(
            transactionBuilderApi
              .buildSimpleLvlTransaction(
                lvlTxos,
                predicateFundsToUnlock,
                lockForChange.getPredicate,
                recipientLockAddress,
                amount
              )
          )
          // Only save to wallet state if there is a change output in the transaction
          _ <-
            if (ioTransaction.outputs.length >= 2) for {
              lockAddress <- liftF(
                transactionBuilderApi.lockAddress(
                  lockForChange
                )
              )
              vk <- liftF(
                someNextIndices
                  .map(nextIndices =>
                    walletApi
                      .deriveChildKeys(keyPair, nextIndices)
                      .map(_.vk)
                  )
                  .sequence
              )
              _ <- liftF(
                walletStateApi.updateWalletState(
                  Encoding.encodeToBase58Check(
                    lockForChange.getPredicate.toByteArray
                  ),
                  lockAddress.toBase58(),
                  vk.map(_ => "ExtendedEd25519"),
                  vk.map(x => Encoding.encodeToBase58(x.toByteArray)),
                  someNextIndices.get
                )
              )
            } yield ()
            else {
              liftF(Sync[F].delay(()))
            }
          _ <- EitherT(
            Resource
              .make(
                Sync[F]
                  .delay(
                    new FileOutputStream(outputFile)
                  )
              )(fos => Sync[F].delay(fos.close()))
              .use { fos =>
                Sync[F].delay(
                  Either
                    .catchNonFatal(ioTransaction.writeTo(fos))
                    .leftMap(_ =>
                      CannotSerializeProtobufFile(
                        "Cannot write to file"
                      ): SimpleTransactionAlgebraError
                    )
                )
              }
          )
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
          keyPair <- liftF(
            walletManagementUtils
              .loadKeys(
                keyfile,
                password
              )
          )
          someCurrentIndices <- liftF(
            walletStateApi.getCurrentIndicesForFunds(
              fromParty,
              fromContract,
              someFromState
            )
          )
          predicateFundsToUnlock <- liftF(
            someCurrentIndices
              .map(currentIndices =>
                walletStateApi.getLockByIndex(currentIndices)
              )
              .sequence
              .map(_.flatten.map(Lock().withPredicate(_)))
          )
          someNextIndices <- liftF(
            walletStateApi.getNextIndicesForFunds(
              if (fromParty == "noparty") "self" else fromParty,
              if (fromParty == "noparty") "default"
              else fromContract
            )
          )
          // Generate a new lock for the change, if possible
          changeLock <- liftF(
            someNextIndices
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
          )
          fromAddress <- liftF(
            transactionBuilderApi.lockAddress(
              predicateFundsToUnlock.get
            )
          )
          response <- liftF(utxoAlgebra.queryUtxo(fromAddress))
          lvlTxos = response.filter(
            _.transactionOutput.value.value.isLvl
          )
          // either toAddress or both toContract and toParty must be defined
          toAddressOpt <- liftF(
            (
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
          )
          _ <-
            (if (lvlTxos.isEmpty) {
               lift[F, Unit](
                 Sync[F].delay(Left(CreateTxError("No LVL txos found")))
               )
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
                   lift[F, Unit](
                     Sync[F].delay(
                       Left(CreateTxError("Unable to generate change lock"))
                     )
                   )
                 case (_, _) =>
                   lift[F, Unit](
                     Sync[F].delay(
                       Left(CreateTxError("Unable to derive recipient address"))
                     )
                   )
               }
             })
        } yield ()).value
      }
    }
}
