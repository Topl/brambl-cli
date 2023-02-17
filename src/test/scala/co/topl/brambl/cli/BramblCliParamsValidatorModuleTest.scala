package co.topl.brambl.cli

import munit.FunSuite
import scopt.OParser

import co.topl.brambl.cli.validation.BramblCliParamsValidatorModule

class BramblCliParamsValidatorModuleTest extends FunSuite {

  import BramblCliParamsValidatorModule._

  import BramblCliParamsParserModule._

  test("Test valid wallet create") {
    val args0 = List("wallet", "create", "-n", "private", "-p", "test")
    val params0 = OParser.parse(paramParser, args0, BramblCliParams()).get
    assertEquals(validateParams(params0).isValid, true)
    val args1 = List("wallet", "create", "-n", "valhalla", "-p", "test")
    val params1 = OParser.parse(paramParser, args1, BramblCliParams()).get
    assertEquals(validateParams(params1).isValid, true)
    val args2 = List("wallet", "create", "-n", "main", "-p", "test")
    val params2 = OParser.parse(paramParser, args2, BramblCliParams()).get
    assertEquals(validateParams(params2).isValid, true)
  }

  test("Test valid wallet sign") {
    val args0 = List(
      "wallet",
      "sign",
      "-k",
      "src/test/resources/keyfile.json",
      "-n",
      "private",
      "-p",
      "test",
      "-o",
      "output",
      "--token",
      "poly"
    )
    val params0 = OParser.parse(paramParser, args0, BramblCliParams()).get
    assertEquals(validateParams(params0).isValid, true)
    val args1 = List(
      "wallet",
      "sign",
      "-k",
      "src/test/resources/keyfile.json",
      "-n",
      "valhalla",
      "-p",
      "test",
      "-i",
      "input",
      "-o",
      "output",
      "--token",
      "poly"
    )
    val params1 = OParser.parse(paramParser, args1, BramblCliParams()).get
    assertEquals(validateParams(params1).isValid, true)
    val args2 = List(
      "wallet",
      "sign",
      "-k",
      "src/test/resources/keyfile.json",
      "-n",
      "main",
      "-p",
      "test",
      "-i",
      "input",
      "-o",
      "output",
      "--token",
      "poly"
    )
    val params2 = OParser.parse(paramParser, args2, BramblCliParams()).get
    assertEquals(validateParams(params2).isValid, true)
  }

  test("Test valid transaction create") {
    val args0 = List(
      "transaction",
      "create",
      "-n",
      "private",
      "-u",
      "http://localhost:8065",
      "-f",
      "AUAFAWju3tDYw1jeGX7zbT4oUdUgHzim8E2dVxuGg3HLpPdohrGB",
      "-t",
      "AUAFAWju3tDYw1jeGX7zbT4oUdUgHzim8E2dVxuGg3HLpPdohrGB=100",
      "-c",
      "AUAFAWju3tDYw1jeGX7zbT4oUdUgHzim8E2dVxuGg3HLpPdohrGB",
      "-o",
      "output",
      "--token",
      "poly"
    )
    val params0 = OParser.parse(paramParser, args0, BramblCliParams()).get
    assertEquals(validateParams(params0).isValid, true)
    val args1 = List(
      "transaction",
      "create",
      "-n",
      "valhalla",
      "-u",
      "http://localhost:8095",
      "-f",
      "3NLiSix8R4zKDYHL8bTSrVe1Y6E4sg6W7L89VpPY4Zdvz2KJRK3z",
      "-t",
      "3NLiSix8R4zKDYHL8bTSrVe1Y6E4sg6W7L89VpPY4Zdvz2KJRK3z=100",
      "-c",
      "3NLiSix8R4zKDYHL8bTSrVe1Y6E4sg6W7L89VpPY4Zdvz2KJRK3z",
      "-o",
      "output",
      "--token",
      "poly"
    )
    val params1 = OParser.parse(paramParser, args1, BramblCliParams()).get
    assertEquals(validateParams(params1).isValid, true)
    val args2 = List(
      "transaction",
      "create",
      "-n",
      "main",
      "-u",
      "http://localhost:8065",
      "-f",
      "9dC1pQbrZP8jNLmWkWz9WvX6KmTfthpk51P2xf5165KEGYitjL5",
      "-c",
      "9dC1pQbrZP8jNLmWkWz9WvX6KmTfthpk51P2xf5165KEGYitjL5",
      "-t",
      "9dC1pQbrZP8jNLmWkWz9WvX6KmTfthpk51P2xf5165KEGYitjL5=100",
      "-o",
      "output",
      "--token",
      "poly"
    )
    val params2 = OParser.parse(paramParser, args2, BramblCliParams()).get
    assertEquals(validateParams(params2).isValid, true)
  }

  test("Test valid transaction broadcast") {
    val args0 = List(
      "transaction",
      "broadcast",
      "--token", 
      "poly",
      "-n",
      "private",
      "-u",
      "http://localhost:9085"
    )
    val params0 = OParser.parse(paramParser, args0, BramblCliParams()).get
    assertEquals(validateParams(params0).isValid, true)

    val args1 = List(
      "transaction",
      "broadcast",
      "--token", 
      "poly",
      "-n",
      "valhalla",
      "-u",
      "http://localhost:9085"
    )
    val params1 = OParser.parse(paramParser, args1, BramblCliParams()).get
    assertEquals(validateParams(params1).isValid, true)

    val args2 = List(
      "transaction",
      "broadcast",
      "--token", 
      "poly",
      "-n",
      "main",
      "-u",
      "http://localhost:9085"
    )
    val params2 = OParser.parse(paramParser, args2, BramblCliParams()).get
    assertEquals(validateParams(params2).isValid, true)
  }

}
