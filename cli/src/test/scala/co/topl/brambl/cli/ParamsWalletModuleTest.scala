package co.topl.brambl.cli

import munit.FunSuite
import scopt.OParser

import java.nio.file.Files
import java.nio.file.Paths

class ParamsWalletModuleTest extends FunSuite {

  import BramblCliParamsParserModule._

  val tmpWallet = FunFixture[(String, String)](
    setup = { _ =>
      val initialWalletDb = Paths.get("wallet.db")
      val initialMnemonic = Paths.get("mnemonic.txt")
      if (Files.exists(initialWalletDb))
        Files.delete(initialWalletDb)
      if (Files.exists(initialMnemonic))
        Files.delete(initialMnemonic)
      (
        initialWalletDb.toAbsolutePath().toString(),
        initialMnemonic.toAbsolutePath().toString()
      )
    },
    teardown = { _ => () }
  )

  tmpWallet.test("Test valid wallet create") { walletAndMnemonic =>
    val (wallet, mnemonic) = walletAndMnemonic
    val args0 = List(
      "wallet",
      "init",
      "-w",
      "test",
      "--newwalletdb",
      wallet,
      "-n",
      "private",
      "--mnemonicfile",
      mnemonic
    )
    assert(OParser.parse(paramParser, args0, BramblCliParams()).isDefined)
    val args1 = List(
      "wallet",
      "init",
      "-w",
      "test",
      "-P",
      "myPassphrase",
      "--newwalletdb",
      "wallet.db",
      "-n",
      "private",
      "--mnemonicfile",
      "mnemonic.txt"
    )
    assert(OParser.parse(paramParser, args1, BramblCliParams()).isDefined)
    val args2 = List(
      "wallet",
      "init",
      "-w",
      "test",
      "-o",
      "outputFile.json",
      "--newwalletdb",
      "wallet.db",
      "-n",
      "private",
      "--mnemonicfile",
      "mnemonic.txt"
    )
    assert(OParser.parse(paramParser, args2, BramblCliParams()).isDefined)
  }

  test("Test invalid key create") {
    val args0 = List("wallet", "init")
    assert(
      OParser
        .parse(paramParser, args0, BramblCliParams())
        .isEmpty
    )
  }
  tmpWallet.test("Test valid key recovery") { _ =>
    val args0 = List(
      "wallet",
      "recover-keys",
      "-w",
      "test",
      "-o",
      "outputFile.json",
      "--newwalletdb",
      "wallet.db",
      "-n",
      "private",
      "--passphrase",
      "test-passphrase",
      "--mnemonic",
      "this,is,an,example,of,a,mnemonic,string,that,contains,12,words"
    )
    assert(
      OParser
        .parse(paramParser, args0, BramblCliParams())
        .isDefined
    )
  }
  tmpWallet.test("Test export-vk (TSDK-760)") { _ =>
    val args0 = List(
      "wallet",
      "export-vk",
      "--walletdb",
      "wallet.db",
    )
    assert(
      OParser
        .parse(paramParser, args0, BramblCliParams())
        .isEmpty
    )
  }
  tmpWallet.test("Test fellowships add (TSDK-760)") { _ =>
    val args0 = List(
      "fellowships",
      "add",
      "--walletdb",
      "wallet.db",
    )
    assert(
      OParser
        .parse(paramParser, args0, BramblCliParams())
        .isEmpty
    )
  }

}
