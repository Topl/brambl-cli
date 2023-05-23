package co.topl.brambl.cli

import munit.FunSuite
import scopt.OParser

import co.topl.brambl.cli.validation.BramblCliParamsValidatorModule

class ParamsGenusQueryTest extends FunSuite {

  import BramblCliParamsValidatorModule._

  import BramblCliParamsParserModule._


  test("Test from-party transactions require index (UTXO query)") {
    val args0 = List(
      "genus-query",
      "utxo-by-address",
      "--from-party",
      "noparty",
      "--from-contract",
      "genesis",
      "--from-state",
      "0",
      "--genus-port",
      "9091",
      "--bifrost-port",
      "9084",
      "-h",
      "localhost",
      "-n",
      "private",
      "--walletdb",
      "wallet.db"
    )
    val params0 = OParser.parse(paramParser, args0, BramblCliParams()).get
    assertEquals(validateParams(params0).isValid, true)
  }

}
