package co.topl.brambl.cli.views

import co.topl.brambl.cli.modules.DummyObjects

class BlockDisplayOpsSpec extends munit.FunSuite with DummyObjects {

  test("Txo display should show the correct data") {
    val display = BlockDisplayOps.display(txo01)
    assertEquals(
      display,
      """
TxoAddress : DAas2fmY1dfpVkTYSJXp3U1CD7yTMEonum2xG9BJmNtQ#1
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

TransactionId : DAas2fmY1dfpVkTYSJXp3U1CD7yTMEonum2xG9BJmNtQ

Group Policies
==============



Series Policies
===============



Asset Minting Statements
========================
  



Inputs
======

TxoAddress   : DAas2fmY1dfpVkTYSJXp3U1CD7yTMEonum2xG9BJmNtQ#1
Attestation  : Not implemented
Value        : 100

Outputs
=======

LockAddress  : ptetP7jshHVrEKqDRdKAZtuybPZoMWTKKM2ngaJ7L5iZnxP5BprDB3hGJEFr
Type         : LVL
Value        : 100

Datum        :

Value      :
""".trim())
  }

}
