package co.topl.brambl.cli.views

import co.topl.brambl.cli.modules.DummyObjects
import co.topl.brambl.display.DisplayOps.DisplayTOps

class BlockDisplayOpsSpec extends munit.FunSuite with DummyObjects {

  test("Txo display should show the correct data") {
    assertNoDiff(
      txo01.display,
      """
TxoAddress                 : DAas2fmY1dfpVkTYSJXp3U1CD7yTMEonum2xG9BJmNtQ#1
LockAddress                : ptetP7jshHUqDhjMhP88yhtQhhvrnBUVJkSvEo5xZvHE4UDL9FShTf1YBqSU
Type                       : LVL
Value                      : 100
"""
    )
  }

  test("Block and transaction display should show the correct data") {
    val display = BlockDisplayOps.display(blockId01, Seq(iotransaction01))
    assertNoDiff(display.trim(), """
BlockId: 11111111111111111111111111111111

Block Body:

TransactionId              : DAas2fmY1dfpVkTYSJXp3U1CD7yTMEonum2xG9BJmNtQ

Group Policies
==============


Series Policies
===============


Asset Minting Statements
========================


Inputs
======
TxoAddress                 : DAas2fmY1dfpVkTYSJXp3U1CD7yTMEonum2xG9BJmNtQ#1
Attestation                : Not implemented
Type                       : LVL
Value                      : 100

Outputs
=======
LockAddress                : ptetP7jshHUqDhjMhP88yhtQhhvrnBUVJkSvEo5xZvHE4UDL9FShTf1YBqSU
Type                       : LVL
Value                      : 100

Datum
=====
Value                      :
""".trim())
  }

}
