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

  test("Test invalid key create") {
    val args0 = List("key", "generate")
    assertEquals(OParser.parse(paramParser, args0, BramblCliParams()).isEmpty, true)
    val args1 = List("key", "invalidCommand", "-p", "test")
    assertEquals(OParser.parse(paramParser, args1, BramblCliParams()).isEmpty, true)
  }

  test("Test valid key derive") {
    val args0 = List("key", "derive", "-p", "test", "-i", "src/test/resources/keyfile.json", "-o", "unencrypted.json", "-C", "44,0,0")
    val params0 = OParser.parse(paramParser, args0, BramblCliParams()).get
    assertEquals(validateParams(params0).fold(_.toList, _ =>List[String]()), Nil)
    assertEquals(validateParams(params0).isValid, true)
  }
  test("Test invalid key derive") {
    val args0 = List("key", "derive", "-P", "passphrase", "-p", "test", "-i", "src/test/resources/keyfile.json", "-o", "unencrypted.json", "-C", "44,0,0")
    val params0 = OParser.parse(paramParser, args0, BramblCliParams()).get
    assertEquals(validateParams(params0).isValid, false)
  }

}
