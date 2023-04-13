package co.topl.brambl.cli

import munit.FunSuite
import scopt.OParser

import co.topl.brambl.cli.validation.BramblCliParamsValidatorModule

class BramblCliParamsValidatorModuleTest extends FunSuite {

  import BramblCliParamsValidatorModule._

  import BramblCliParamsParserModule._

  test("Test valid key create") {
    val args0 = List("key", "generate", "-p", "test")
    val params0 = OParser.parse(paramParser, args0, BramblCliParams()).get
    assertEquals(validateParams(params0).isValid, true)
    val args1 = List("key", "generate", "-p", "test", "-P","myPassphrase")
    val params1 = OParser.parse(paramParser, args1, BramblCliParams()).get
    assertEquals(validateParams(params1).isValid, true)
    val args2 = List("key", "generate", "-p", "test", "-o","outputFile.json")
    val params2 = OParser.parse(paramParser, args2, BramblCliParams()).get
    assertEquals(validateParams(params2).isValid, true)
  }

}
