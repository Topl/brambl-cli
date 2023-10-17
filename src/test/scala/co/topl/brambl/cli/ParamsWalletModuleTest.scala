package co.topl.brambl.cli

import munit.FunSuite
import scopt.OParser

class ParamsWalletModuleTest extends FunSuite {

  import BramblCliParamsParserModule._

  test("Test valid wallet create".only) {
    val args0 = List(
      "wallet",
      "init",
      "-w",
      "test",
      "--newwalletdb",
      "wallet.db",
      "-n",
      "private",
      "--mnemonicfile",
      "mnemonic.txt"
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
  test("Test valid key recovery") {
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
}
