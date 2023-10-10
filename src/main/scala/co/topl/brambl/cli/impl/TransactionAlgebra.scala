package co.topl.brambl.cli.impl

import cats.effect.kernel.Resource
import cats.effect.kernel.Sync
import co.topl.brambl.dataApi.WalletStateAlgebra
import co.topl.brambl.wallet.CredentiallerInterpreter
import co.topl.brambl.wallet.WalletApi
import co.topl.node.services.BroadcastTransactionReq
import co.topl.node.services.NodeRpcGrpc
import io.grpc.ManagedChannel

import java.io.FileInputStream
import java.io.FileOutputStream

trait TransactionAlgebra[F[_]] {
  def proveSimpleTransactionFromParams(
      inputRes: Resource[F, FileInputStream],
      keyFile: String,
      password: String,
      outputRes: Resource[F, FileOutputStream]
  ): F[Either[SimpleTransactionAlgebraError, Unit]]

  def broadcastSimpleTransactionFromParams(
      provedTxFile: String
  ): F[Either[SimpleTransactionAlgebraError, Unit]]

}

object TransactionAlgebra {

  def make[F[_]: Sync](
      walletApi: WalletApi[F],
      walletStateApi: WalletStateAlgebra[F],
      walletManagementUtils: WalletManagementUtils[F],
      channelResource: Resource[F, ManagedChannel]
  ) =
    new TransactionAlgebra[F] {

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
                .adaptErr { e =>
                  e.printStackTrace()
                  NetworkProblem("Problem connecting to node")
                }
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

    }
}
