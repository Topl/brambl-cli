package co.topl.brambl.cli

import munit.FunSuite

import co.topl.brambl.cli.validation.BramblCliParamsValidatorModule
import scopt.OParser

class ParamsBifrostQueryTest extends FunSuite {

  import BramblCliParamsValidatorModule._

  import BramblCliParamsParserModule._

  test("Block by height") {
    val args0 = List(
      "bifrost-query",
      "block-by-height",
      "--height",
      "1",
      "--bifrost-port",
      "9084",
      "-h",
      "localhost",
      "-n",
      "private"
    )
    val params0 = OParser.parse(paramParser, args0, BramblCliParams()).get
    assertEquals(validateParams(params0).isValid, true)
  }
  test("Block by id") {
    val args0 = List(
      "bifrost-query",
      "block-by-id",
      "--block-id",
      "8PrjN9RtFK44nmR1dTo1jG2ggaRHaGNYhePEhnWY1TTM",
      "--bifrost-port",
      "9084",
      "-h",
      "localhost",
      "-n",
      "private"
    )
    val params0 = OParser.parse(paramParser, args0, BramblCliParams()).get
    assertEquals(validateParams(params0).isValid, true)
  }
  test("Transaction by id") {
    val args0 = List(
      "bifrost-query",
      "transaction-by-id",
      "--transaction-id",
      "8PrjN9RtFK44nmR1dTo1jG2ggaRHaGNYhePEhnWY1TTM",
      "--bifrost-port",
      "9084",
      "-h",
      "localhost",
      "-n",
      "private"
    )
    val params0 = OParser.parse(paramParser, args0, BramblCliParams()).get
    assertEquals(validateParams(params0).isValid, true)
  }

}
