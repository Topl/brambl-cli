package co.topl.brambl.cli

import munit.CatsEffectSuite

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import cats.effect.ExitCode

class WalletDigestPropositionTest
    extends CatsEffectSuite
    with WalletConstants
    with CommonTxOperations {

  val tmpDirectory = FunFixture[Path](
    setup = { _ =>
      val tmpDir = Paths.get(TMP_DIR).toFile()
      if (tmpDir.exists()) {
        Paths.get(TMP_DIR).toFile().listFiles().map(_.delete()).mkString("\n")
        Files.deleteIfExists(Paths.get(TMP_DIR))
      }
      Files.createDirectory(Paths.get(TMP_DIR))
      Files.createFile(Paths.get(EMPTY_FILE))
    },
    teardown = { _ => () }
  )

  tmpDirectory.test("Initialize wallet and add digest (sha256) template") { _ =>
    for {
      _ <- createWallet().run(walletContext)
      _ <- assertIO(
        addTemplate(
          "sha256Template",
          "threshold(1, sha256(b39f7e1305cd9107ed9af824fcb0729ce9888bbb7f219cc0b6731332105675dc))"
        ).run(walletContext),
        ExitCode.Success
      )
      _ <- importVk("nofellowship", "sha256Template", EMPTY_FILE).run(
        walletContext
      )
      _ <- assertIO(
        walletController(WALLET)
          .currentaddress("nofellowship", "sha256Template", None),
        Some("ptetP7jshHUxpB3Aep7erqzXRtSpcYnpxwcyCVQCpktktNhrUeSWgQH1hmvP")
      )
    } yield ()
  }

  tmpDirectory.test("Initialize wallet and add digest (blake2b) template") {
    _ =>
      for {
        _ <- createWallet().run(walletContext)
        _ <- assertIO(
          addTemplate(
            "blake2bTemplate",
            "threshold(1, blake2b(b39f7e1305cd9107ed9af824fcb0729ce9888bbb7f219cc0b6731332105675dc))"
          ).run(walletContext),
          ExitCode.Success
        )
        _ <- importVk("nofellowship", "blake2bTemplate", EMPTY_FILE).run(
          walletContext
        )
        _ <- assertIO(
          walletController(WALLET)
            .currentaddress("nofellowship", "blake2bTemplate", None),
          Some("ptetP7jshHUzFqDR9cjYFnRt5caJAYmUVToDkmjSzvXUjVMFZDtDoEc7tRfT")
        )
      } yield ()
  }
}
