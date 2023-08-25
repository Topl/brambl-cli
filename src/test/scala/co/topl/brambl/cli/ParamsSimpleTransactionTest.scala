package co.topl.brambl.cli
import munit.FunSuite
import scopt.OParser

import co.topl.brambl.cli.validation.BramblCliParamsValidatorModule
class ParamsSimpleTransactionTest extends FunSuite {
  import BramblCliParamsValidatorModule._

  import BramblCliParamsParserModule._

  test("Test valid transaction create using toAddress") {
    val args0 = List(
      "simpletransaction",
      "create",
      "-t",
      "ptetP7jshHVrEKqDRdKAZtuybPZoMWTKKM2ngaJ7L5iZnxP5BprDB3hGJEFr",
      "-w",
      "test",
      "-o",
      "newTransaction.pbuf",
      "--bifrost-port",
      "9084",
      "-h",
      "localhost",
      "-n",
      "private",
      "-a",
      "100",
      "--keyfile",
      "src/test/resources/keyfile.json",
      "--walletdb",
      "wallet.db"
    )
    val params0 = OParser.parse(paramParser, args0, BramblCliParams()).get
    assertEquals(validateParams(params0).isValid, true)
  }

  test("Test noparty transactions require index") {
    val args0 = List(
      "simpletransaction",
      "create",
      "--from-party",
      "noparty",
      "--from-contract",
      "genesis",
      "-t",
      "ptetP7jshHVrEKqDRdKAZtuybPZoMWTKKM2ngaJ7L5iZnxP5BprDB3hGJEFr",
      "-w",
      "test",
      "-o",
      "newTransaction.pbuf",
      "--bifrost-port",
      "9084",
      "-h",
      "localhost",
      "-n",
      "private",
      "-a",
      "100",
      "-w",
      "test",
      "--keyfile",
      "src/test/resources/keyfile.json",
      "--walletdb",
      "wallet.db"
    )
    val params0 = OParser.parse(paramParser, args0, BramblCliParams()).get
    assertEquals(validateParams(params0).isInvalid, true)
  }

  test("Test from-party transactions require index") {
    val args0 = List(
      "simpletransaction",
      "create",
      "--from-party",
      "noparty",
      "--from-contract",
      "genesis",
      "--from-state",
      "0",
      "-t",
      "ptetP7jshHVrEKqDRdKAZtuybPZoMWTKKM2ngaJ7L5iZnxP5BprDB3hGJEFr",
      "-w",
      "test",
      "-o",
      "newTransaction.pbuf",
      "--bifrost-port",
      "9084",
      "-h",
      "localhost",
      "-n",
      "private",
      "-a",
      "100",
      "-w",
      "test",
      "--keyfile",
      "src/test/resources/keyfile.json",
      "--walletdb",
      "wallet.db"
    )
    val params0 = OParser.parse(paramParser, args0, BramblCliParams()).get
    assertEquals(validateParams(params0).isValid, true)
  }

  test("Test valid transaction create using toParty and toContract") {
    val args0 = List(
      "simpletransaction",
      "create",
      "--to-party",
      "self",
      "--to-contract",
      "default",
      "-w",
      "test",
      "-o",
      "newTransaction.pbuf",
      "--bifrost-port",
      "9084",
      "-h",
      "localhost",
      "-n",
      "private",
      "-a",
      "100",
      "--keyfile",
      "src/test/resources/keyfile.json",
      "--walletdb",
      "wallet.db"
    )
    val params0 = OParser.parse(paramParser, args0, BramblCliParams()).get
    assert(validateParams(params0).isValid)
  }

  test("Test invalid transaction create with all toAddress, toParty and toContract") {
    val args0 = List(
      "simpletransaction",
      "create",
      "-t",
      "ptetP7jshHVrEKqDRdKAZtuybPZoMWTKKM2ngaJ7L5iZnxP5BprDB3hGJEFr",
      "--to-party",
      "self",
      "--to-contract",
      "default",
      "-w",
      "test",
      "-o",
      "newTransaction.pbuf",
      "--bifrost-port",
      "9084",
      "-h",
      "localhost",
      "-n",
      "private",
      "-a",
      "100",
      "--keyfile",
      "src/test/resources/keyfile.json",
      "--walletdb",
      "wallet.db"
    )
    val params0 = OParser.parse(paramParser, args0, BramblCliParams()).get
    assert(validateParams(params0).isInvalid)
  }
  test(
    "Test invalid transaction create with no toAddress, toParty or toContract"
  ) {
    val args0 = List(
      "simpletransaction",
      "create",
      "-w",
      "test",
      "-o",
      "newTransaction.pbuf",
      "--bifrost-port",
      "9084",
      "-h",
      "localhost",
      "-n",
      "private",
      "-a",
      "100",
      "--keyfile",
      "src/test/resources/keyfile.json",
      "--walletdb",
      "wallet.db",
      "-w",
      "test"
    )
    val params0 = OParser.parse(paramParser, args0, BramblCliParams()).get
    assert(validateParams(params0).isInvalid)

  }
  test(
    "Test invalid transaction create with only one of toParty or toContract"
  ) {
    val args0 = List(
      "simpletransaction",
      "create",
      "--to-contract",
      "default",
      "-w",
      "test",
      "-o",
      "newTransaction.pbuf",
      "--bifrost-port",
      "9084",
      "-h",
      "localhost",
      "-n",
      "private",
      "-a",
      "100",
      "--keyfile",
      "src/test/resources/keyfile.json",
      "--walletdb",
      "wallet.db"
    )
    val params0 = OParser.parse(paramParser, args0, BramblCliParams()).get
    assert(validateParams(params0).isInvalid)

  }
}
