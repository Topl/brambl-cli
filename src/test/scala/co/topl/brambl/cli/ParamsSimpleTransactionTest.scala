package co.topl.brambl.cli
import munit.FunSuite
import scopt.OParser

class ParamsSimpleTransactionTest extends FunSuite {

  import BramblCliParamsParserModule._

  test("Test valid group transaction create using toAddress") {
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
      "group",
      "--group-id",
      "0"*64,	
      "--keyfile",
      "src/test/resources/keyfile.json",
      "--walletdb",
      "src/test/resources/wallet.db"
    )
    assert(OParser.parse(paramParser, args0, BramblCliParams()).isDefined)
  }

  test("Test invalid transaction omitting port") {
    val args0 = List(
      "simple-transaction",
      "create",
      "-t",
      "ptetP7jshHVrEKqDRdKAZtuybPZoMWTKKM2ngaJ7L5iZnxP5BprDB3hGJEFr",
      "-w",
      "test",
      "-o",
      "newTransaction.pbuf",
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
    assert(!OParser.parse(paramParser, args0, BramblCliParams()).isDefined)
  }

  test("Test invalid transaction omitting host") {
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
    assert(!OParser.parse(paramParser, args0, BramblCliParams()).isDefined)
  }

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

  test("Test nofellowship transactions require index") {
    val args0 = List(
      "simple-transaction",
      "create",
      "--from-fellowship",
      "nofellowship",
      "--from-template",
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

  test("Test nofellowship transactions require change") {
    val args0 = List(
      "simple-transaction",
      "create",
      "--from-fellowship",
      "nofellowship",
      "--from-template",
      "genesis",
      "--from-interaction",
      "1",
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

  test("Test from-fellowship transactions require index") {
    val args0 = List(
      "simple-transaction",
      "create",
      "--from-fellowship",
      "nofellowship",
      "--from-template",
      "genesis",
      "--from-interaction",
      "1",
      "--change-fellowship",
      "nofellowship",
      "--change-template",
      "genesis",
      "--change-interaction",
      "1",
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

  test("Test valid transaction create using toFellowship and toTemplate") {
    val args0 = List(
      "simple-transaction",
      "create",
      "--to-fellowship",
      "self",
      "--to-template",
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
    "Test invalid transaction create with all toAddress, toFellowship and toTemplate"
  ) {
    val args0 = List(
      "simple-transaction",
      "create",
      "-t",
      "ptetP7jshHVrEKqDRdKAZtuybPZoMWTKKM2ngaJ7L5iZnxP5BprDB3hGJEFr",
      "--to-fellowship",
      "self",
      "--to-template",
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
    "Test invalid transaction create with no toAddress, toFellowship or toTemplate"
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
    "Test invalid transaction create with only one of toFellowship or toTemplate"
  ) {
    val args0 = List(
      "simple-transaction",
      "create",
      "--to-template",
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
