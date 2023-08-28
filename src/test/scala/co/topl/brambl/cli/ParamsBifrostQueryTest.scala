package co.topl.brambl.cli

import munit.FunSuite

import scopt.OParser

class ParamsBifrostQueryTest extends FunSuite {

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
      "localhost"
    )
    assert(OParser.parse(paramParser, args0, BramblCliParams()).isDefined)
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
      "localhost"
    )
    assert(OParser.parse(paramParser, args0, BramblCliParams()).isDefined)
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
      "localhost"
    )
    assert(OParser.parse(paramParser, args0, BramblCliParams()).isDefined)
  }

}
