package co.topl.brambl.cli
import munit.FunSuite
import scopt.OParser

class ParamsSimpleTransactionTest extends FunSuite {

  import BramblCliParamsParserModule._

  test("Test valid transaction create using toAddress") {
    val args0 = List(
      "simple-transaction",
      "create",
      "-t",
      "ptetP7jshHVrEKqDRdKAZtuybPZoMWTKKM2ngaJ7L5iZnxP5BprDB3hGJEFr",
      "-w",
      "test",
      "-o",
      "newTransaction.pbuf",
      "--port",
      "9084",
      "-h",
      "localhost",
      "-n",
      "private",
      "-a",
      "100",
      "--fee",
      "10",
      "--transfer-token",
      "lvl",	
      "--keyfile",
      "src/test/resources/keyfile.json",
      "--walletdb",
      "src/test/resources/wallet.db"
    )
    assert(OParser.parse(paramParser, args0, BramblCliParams()).isDefined)
  }

  test("Test noparty transactions require index") {
    val args0 = List(
      "simple-transaction",
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
      "--port",
      "9084",
      "-h",
      "localhost",
      "-n",
      "private",
      "-a",
      "100",
      "--fee",
      "10",
      "--transfer-token",
      "lvl",	
      "--keyfile",
      "src/test/resources/keyfile.json",
      "--walletdb",
      "src/test/resources/wallet.db"
    )
    assert(OParser.parse(paramParser, args0, BramblCliParams()).isEmpty)
  }

  test("Test from-party transactions require index") {
    val args0 = List(
      "simple-transaction",
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
      "--port",
      "9084",
      "-h",
      "localhost",
      "-n",
      "private",
      "-a",
      "100",
      "--fee",
      "10",
      "--transfer-token",
      "lvl",	
      "--keyfile",
      "src/test/resources/keyfile.json",
      "--walletdb",
      "src/test/resources/wallet.db"
    )
    assert(OParser.parse(paramParser, args0, BramblCliParams()).isDefined)
  }

  test("Test valid transaction create using toParty and toContract") {
    val args0 = List(
      "simple-transaction",
      "create",
      "--to-party",
      "self",
      "--to-contract",
      "default",
      "-w",
      "test",
      "-o",
      "newTransaction.pbuf",
      "--port",
      "9084",
      "-h",
      "localhost",
      "-n",
      "private",
      "-a",
      "100",
      "--fee",
      "10",
      "--transfer-token",
      "lvl",	
      "--keyfile",
      "src/test/resources/keyfile.json",
      "--walletdb",
      "src/test/resources/wallet.db"
    )
    assert(OParser.parse(paramParser, args0, BramblCliParams()).isDefined)
  }

  test(
    "Test invalid transaction create with all toAddress, toParty and toContract"
  ) {
    val args0 = List(
      "simple-transaction",
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
      "--port",
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
      "src/test/resources/wallet.db"
    )
    assert(OParser.parse(paramParser, args0, BramblCliParams()).isEmpty)
  }
  test(
    "Test invalid transaction create with no toAddress, toParty or toContract"
  ) {
    val args0 = List(
      "simple-transaction",
      "create",
      "-w",
      "test",
      "-o",
      "newTransaction.pbuf",
      "--port",
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
      "src/test/resources/wallet.db"
    )
    assert(OParser.parse(paramParser, args0, BramblCliParams()).isEmpty)

  }
  test(
    "Test invalid transaction create with only one of toParty or toContract"
  ) {
    val args0 = List(
      "simple-transaction",
      "create",
      "--to-contract",
      "default",
      "-w",
      "test",
      "-o",
      "newTransaction.pbuf",
      "--port",
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
      "src/test/resources/wallet.db"
    )
    assert(OParser.parse(paramParser, args0, BramblCliParams()).isEmpty)
  }
}
