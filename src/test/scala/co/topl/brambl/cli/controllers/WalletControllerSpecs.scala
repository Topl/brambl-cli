package co.topl.brambl.cli.controllers

import cats.data.EitherT
import cats.effect.IO
import co.topl.brambl.cli.mockbase.BaseGenusQueryAlgebra
import co.topl.brambl.cli.mockbase.BaseTransactionBuilderApi
import co.topl.brambl.cli.mockbase.BaseWalletAlgebra
import co.topl.brambl.cli.mockbase.BaseWalletApi
import co.topl.brambl.cli.mockbase.BaseWalletManagementUtils
import co.topl.brambl.cli.mockbase.BaseWalletStateAlgebra
import co.topl.brambl.cli.modules.DataApiModule
import co.topl.brambl.models.Indices
import co.topl.brambl.utils.Encoding
import co.topl.brambl.wallet.WalletApi
import munit.CatsEffectSuite
import quivr.models.KeyPair

import java.nio.file.Files
import java.nio.file.Paths
import scala.io.Source

class WalletControllerSpecs extends CatsEffectSuite with DataApiModule {

  val walletApi = WalletApi.make[IO](dataApi)

  val keyPair = (for {
    w <- EitherT(walletApi.createNewWallet("test".getBytes(), None))
    keyPair <- EitherT(
      walletApi.extractMainKey(
        w.mainKeyVaultStore,
        "test".getBytes()
      )
    )
  } yield keyPair).value.map(_.toOption.get)

  val tmpDirectory = FunFixture[Boolean](
    setup = { _ =>
      if (Files.exists(Paths.get("test.vk"))) {
        Files.deleteIfExists(Paths.get("test.vk"))
      } else true
    },
    teardown = { _ => Files.deleteIfExists(Paths.get("test.vk")) }
  )

  tmpDirectory.test("exportFinalVk should export the key at the right index") {
    _ =>
      val controller = new WalletController[IO](
        new BaseTransactionBuilderApi[IO], // : TransactionBuilderApi[F],
        new BaseWalletStateAlgebra[IO] {
          override def getCurrentIndicesForFunds(
              party: String,
              contract: String,
              someState: Option[Int]
          ): IO[Option[Indices]] = IO.pure(Some(Indices(1, 2, 3)))
        }, // : dataApi.WalletStateAlgebra[F],
        new BaseWalletManagementUtils[IO] {
          override def loadKeys(keyfile: String, password: String) = keyPair
        }, // : WalletManagementUtils[F],
        new BaseWalletApi[IO] {
          override def deriveChildKeys(
              vk: KeyPair,
              idx: Indices
          ): IO[KeyPair] = walletApi.deriveChildKeys(vk, idx)
        }, // : WalletApi[F],
        new BaseWalletAlgebra[IO], // : WalletAlgebra[F],
        new BaseGenusQueryAlgebra[IO] // : dataApi.GenusQueryAlgebra[F]
      )
      import cats.implicits._
      for {
        res <- controller.exportFinalVk(
          "keyfile.json",
          "test",
          "test.vk",
          "self",
          "default",
          3
        )
        _ <- assertIO(
          IO("Verification key exported".asRight[String]),
          res
        )
        kp <- keyPair
        vk <- walletApi.deriveChildKeys(
          kp,
          Indices(1, 2, 3)
        )
        res <- IO(Encoding.encodeToBase58(vk.vk.toByteArray))
        _ <- assertIO(
          IO(Source.fromFile("test.vk").getLines().toList.mkString),
          res
        )
      } yield ()
  }

}
