---
sidebar_position: 16
---

# Inspecting a Transaction

To inspect a transaction run the following command:

```bash
brambl-cli tx inspect -i $TX_FILE
```

This will inspect the transaction in the file `$TX_FILE` and output the result to the console. The output will look something like this:

```
TransactionId : No transaction id
Group Policies
==============
Series Policies
===============
Asset Minting Statements
========================
  
Group-Token-Utxo: FYX4xtEh9vvXjSwKvXczqa9TCjgyTCawvfnL6L5M2P5N#2
Series-Token-Utxo: FYX4xtEh9vvXjSwKvXczqa9TCjgyTCawvfnL6L5M2P5N#1
Quantity: 1000
Permanent-Metadata:
No permanent metadata
      
Inputs
======
TxoAddress   : FYX4xtEh9vvXjSwKvXczqa9TCjgyTCawvfnL6L5M2P5N#0
Attestation  : Not implemented
Value        : 9998800
-----------
TxoAddress   : FYX4xtEh9vvXjSwKvXczqa9TCjgyTCawvfnL6L5M2P5N#2
Attestation  : Not implemented
Value        : 1
-----------
TxoAddress   : FYX4xtEh9vvXjSwKvXczqa9TCjgyTCawvfnL6L5M2P5N#1
Attestation  : Not implemented
Value        : 1
Outputs
=======
LockAddress  : ptetP7jshHUHx1621p51SSQekgpXzKLaYudhmz5FKMSUDThccGj274Y1P89n
Type         : LVL
Value        : 9998700
-----------
LockAddress  : ptetP7jshHUHx1621p51SSQekgpXzKLaYudhmz5FKMSUDThccGj274Y1P89n
Type         : Group Constructor
Id           : 072c5e13d6b72888ee4d77f347d15a12f21627f5fa5ddd8e59c7f279d9663380
Fixed-Series : NO FIXED SERIES
Value        : 1
-----------
LockAddress  : ptetP7jshHUHx1621p51SSQekgpXzKLaYudhmz5FKMSUDThccGj274Y1P89n
Type         : Series Constructor
Id           : 416ff145cafc2369063684847abf06ec03a5ef16e8d95a7d66f2a4f062e001c9
Fungibility  : group-and-series
Token-Supply : UNLIMITED
Quant-Descr. : liquid
Value        : 1
-----------
LockAddress  : ptetP7jshHUHx1621p51SSQekgpXzKLaYudhmz5FKMSUDThccGj274Y1P89n
Type         : Asset
GroupId      : 072c5e13d6b72888ee4d77f347d15a12f21627f5fa5ddd8e59c7f279d9663380
SeriesId     : 416ff145cafc2369063684847abf06ec03a5ef16e8d95a7d66f2a4f062e001c9
Commitment   : 3e8fd1ed52e0c8107f3265da13a42b323a492d334b6da23b0f1ef279b988a225
Ephemeral-Metadata: 
  url: http://topl.co
  image: http://topl.co/image.png
  number: 42.0
Value        : 1000
Datum        :
Value      : 
```