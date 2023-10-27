package co.topl.brambl.cli

import munit.FunSuite
import scopt.OParser

class ParamsGenusQueryTest extends FunSuite {

  import BramblCliParamsParserModule._

  test("Test from-fellowship transactions require index (UTXO query)") {
    val args0 = List(
      "genus-query",
      "utxo-by-address",
      "--from-fellowship",
      "nofellowship",
      "--from-contract",
      "genesis",
      "--from-state",
      "0",
      "--port",
      "9084",
      "-h",
      "localhost",
      "--walletdb",
      "src/test/resources/wallet.db"
    )
    assert(OParser.parse(paramParser, args0, BramblCliParams()).isDefined)
  }

}
