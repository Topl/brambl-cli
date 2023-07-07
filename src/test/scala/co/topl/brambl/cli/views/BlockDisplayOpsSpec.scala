package co.topl.brambl.cli.views

import co.topl.brambl.cli.modules.DummyObjects

class BlockDisplayOpsSpec extends munit.FunSuite with DummyObjects {

  test("Txo display should show the correct data") {
    val display = BlockDisplayOps.display(txo01)
    assertEquals(
      display,
      """
TxoAddress : 11111111111111111111111111111111#1
LockAddress: ptetP7jshHVrEKqDRdKAZtuybPZoMWTKKM2ngaJ7L5iZnxP5BprDB3hGJEFr
Type       : LVL
Value      : 100
"""
    )
  }

  test("Block and transaction display should show the correct data") {
    val display = BlockDisplayOps.display(blockId01, Seq(iotransaction01))
    assertEquals(display.trim(), """
BlockId: 11111111111111111111111111111111

Block Body:

TransactionId: 11111111111111111111111111111111

Inputs       : 
TxoAddress   : 11111111111111111111111111111111#1
Attestation  : Not implemented
Value        : 100

Outputs      :

LockAddress  : ptetP7jshHVrEKqDRdKAZtuybPZoMWTKKM2ngaJ7L5iZnxP5BprDB3hGJEFr
Type         : LVL
Value        : 100

Datum        :

Value: 
""".trim())
  }

}
