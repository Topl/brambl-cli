package co.topl.brambl.cli.controllers

import cats.data.EitherT
import cats.effect.IO
import co.topl.brambl.cli.mockbase.BaseGenusQueryAlgebra
import co.topl.brambl.cli.mockbase.BaseWalletAlgebra
import co.topl.brambl.cli.mockbase.BaseWalletApi
import co.topl.brambl.cli.mockbase.BaseWalletManagementUtils
import co.topl.brambl.cli.mockbase.BaseWalletStateAlgebra
import co.topl.brambl.cli.modules.WalletKeyApiModule
import co.topl.brambl.models.Indices
import co.topl.brambl.utils.Encoding
import co.topl.brambl.wallet.WalletApi
import munit.CatsEffectSuite
import quivr.models.KeyPair

import java.nio.file.Files
import java.nio.file.Paths
import scala.io.Source
import co.topl.brambl.cli.Sha256
import quivr.models.Proposition
import quivr.models.Preimage
import com.google.protobuf.ByteString
import co.topl.brambl.cli.Blake2b

class WalletControllerSpecs extends CatsEffectSuite with WalletKeyApiModule {

  val walletApi = WalletApi.make[IO](walletKeyApi)

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
        new BaseWalletStateAlgebra[IO] {
          override def getCurrentIndicesForFunds(
              fellowship: String,
              template: String,
              someInteraction: Option[Int]
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
          IO({
            val src = Source.fromFile("test.vk")
            val vks = src.getLines().toList.mkString
            src.close()
            vks
          }),
          res
        )
      } yield ()
  }

  test("setCurrentInteraction fails with none") {
    val controller = new WalletController[IO](
      new BaseWalletStateAlgebra[IO] {
        override def getCurrentIndicesForFunds(
            fellowship: String,
            template: String,
            someInteraction: Option[Int]
        ): IO[Option[Indices]] = IO.pure(Some(Indices(1, 2, 3)))

        override def setCurrentIndices(
            fellowship: String,
            template: String,
            interaction: Int
        ): IO[Option[Indices]] = IO.pure(None)
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
    assertIO(
      for {
        res <- controller.setCurrentInteraction(
          "self",
          "default",
          3
        )
      } yield res,
      Left("Error setting current interaction")
    )
  }
  test("listInteractions succeeds with valid result") {
    val controller = new WalletController[IO](
      new BaseWalletStateAlgebra[IO] {
        override def getInteractionList(
            fellowship: String,
            template: String
        ): IO[Option[List[(Indices, String)]]] =
          IO.pure(Some(List((Indices(1, 2, 3), "test"))))

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
    assertIO(
      for {
        res <- controller.listInteractions(
          "self",
          "default"
        )
      } yield res,
      Right("1\t2\t3\ttest")
    )
  }
  test("listInteractions fails with none") {
    val controller = new WalletController[IO](
      new BaseWalletStateAlgebra[IO] {
        override def getInteractionList(
            fellowship: String,
            template: String
        ): IO[Option[List[(Indices, String)]]] =
          IO.pure(None)

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
    assertIO(
      for {
        res <- controller.listInteractions(
          "self",
          "default"
        )
      } yield res,
      Left(s"The fellowship or template does not exist.")
    )
  }
  test("setCurrentInteraction succeeds with valid result") {
    val controller = new WalletController[IO](
      new BaseWalletStateAlgebra[IO] {
        override def getCurrentIndicesForFunds(
            fellowship: String,
            template: String,
            someInteraction: Option[Int]
        ): IO[Option[Indices]] = IO.pure(Some(Indices(1, 2, 3)))

        override def setCurrentIndices(
            fellowship: String,
            template: String,
            interaction: Int
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
    assertIO(
      for {
        res <- controller.setCurrentInteraction(
          "self",
          "default",
          3
        )
      } yield res,
      Right("Current interaction set")
    )
  }

  test("addSecret fails when a secret already exists (sha256)") {
    val controller = new WalletController[IO](
      new BaseWalletStateAlgebra[IO] {
        override def getPreimage(
            digestProposition: Proposition.Digest
        ): IO[Option[Preimage]] =
          IO.pure(Some(Preimage(ByteString.copyFrom("topl-secret".getBytes()))))
      },
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
    assertIO(
      for {
        res <- controller.addSecret(
          "topl-secret",
          Sha256
        )
      } yield res,
      Left("Secret already exists")
    )
  }

  test("addSecret succeeds when a secret does not exists (sha256)") {
    val controller = new WalletController[IO](
      new BaseWalletStateAlgebra[IO] {
        override def getPreimage(
            digestProposition: Proposition.Digest
        ): IO[Option[Preimage]] =
          IO.pure(None)

        override def addPreimage(
            preimage: Preimage,
            digest: Proposition.Digest
        ): IO[Unit] = IO.unit
      },
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
    assertIO(
      for {
        res <- controller.addSecret(
          "topl-secret",
          Sha256
        )
      } yield res,
      Right("Secret added. Hash: b39f7e1305cd9107ed9af824fcb0729ce9888bbb7f219cc0b6731332105675dc")
    )
  }

  test("addSecret fails when a secret already exists (Blake2b)") {
    val controller = new WalletController[IO](
      new BaseWalletStateAlgebra[IO] {
        override def getPreimage(
            digestProposition: Proposition.Digest
        ): IO[Option[Preimage]] =
          IO.pure(Some(Preimage(ByteString.copyFrom("topl-secret".getBytes()))))
      },
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
    assertIO(
      for {
        res <- controller.addSecret(
          "topl-secret",
          Blake2b
        )
      } yield res,
      Left("Secret already exists")
    )
  }

  test("addSecret succeeds when a secret does not exists (blake2b)") {
    val controller = new WalletController[IO](
      new BaseWalletStateAlgebra[IO] {
        override def getPreimage(
            digestProposition: Proposition.Digest
        ): IO[Option[Preimage]] =
          IO.pure(None)

        override def addPreimage(
            preimage: Preimage,
            digest: Proposition.Digest
        ): IO[Unit] = IO.unit

      },
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
    assertIO(
      for {
        res <- controller.addSecret(
          "topl-secret",
          Blake2b
        )
      } yield res,
      Right("Secret added. Hash: a28f43e7ba06f79b31b189cfee16e160fba1c0ea8f2c4cc8ca38fa567fbca2e3")
    )
  }
}
