package co.topl.brambl.cli

import munit.FunSuite
import scopt.OParser

import co.topl.brambl.cli.validation.BramblCliParamsValidatorModule

class ParamsWalletModuleTest extends FunSuite {

  import BramblCliParamsValidatorModule._

  import BramblCliParamsParserModule._

  test("Test valid wallet create") {
    val args0 = List(
      "wallet",
      "init",
      "-w",
      "test",
      "--walletdb",
      "wallet.db",
      "-n",
      "private"
    )
    val params0 = OParser.parse(paramParser, args0, BramblCliParams()).get
    assertEquals(validateParams(params0).isValid, true)
    val args1 = List(
      "wallet",
      "init",
      "-w",
      "test",
      "-P",
      "myPassphrase",
      "--walletdb",
      "wallet.db",
      "-n",
      "private"
    )
    val params1 = OParser.parse(paramParser, args1, BramblCliParams()).get
    assertEquals(validateParams(params1).isValid, true)
    val args2 = List(
      "wallet",
      "init",
      "-w",
      "test",
      "-o",
      "outputFile.json",
      "--walletdb",
      "wallet.db",
      "-n",
      "private"
    )
    val params2 = OParser.parse(paramParser, args2, BramblCliParams()).get
    assertEquals(validateParams(params2).isValid, true)
  }

  test("Test invalid key create") {
    val args0 = List("wallet", "init")
    assertEquals(
      OParser
        .parse(paramParser, args0, BramblCliParams())
        .map(validateKeyGenerationParams)
        .get
        .isInvalid,
      true
    )
    val args1 = List("wallet", "invalidCommand", "-p", "test")
    assertEquals(
      OParser.parse(paramParser, args1, BramblCliParams()).isEmpty,
      true
    )
  }
}
