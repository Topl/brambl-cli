#!/bin/bash

WALLET=tmp/wallet.db
MAIN_KEY=tmp/mainkey.json
TO_ADDRESS=ptetP7jshHVnMuaLETgEvQdncWQrr4hCojtmU9q195Q2RHs7pkU6mdycQ2oc
TX_FILE=tmp/tx.pbuf
TX_PROVED_FILE=tmp/tx_proved.pbuf
MNEMONIC_FILE=tmp/mnemonic.txt
rm -rf ./tmp
mkdir ./tmp
echo "Creating wallet"

sbt "run wallet init -w test -n private -o $MAIN_KEY --walletdb $WALLET --mnemonicfile $MNEMONIC_FILE"
echo "Creating transaction"
sbt "run simpletransaction create --from-party noparty --from-contract genesis --from-state 1 -t $TO_ADDRESS -w test --bifrost-port 9084 -o $TX_FILE -n private -a 100 -h localhost --keyfile $MAIN_KEY --walletdb $WALLET"

echo "Proving transaction"

sbt "run simpletransaction prove --from-party noparty --from-contract genesis --from-state 1 -w test --keyfile $MAIN_KEY -n private -i $TX_FILE -o $TX_PROVED_FILE --walletdb $WALLET"

echo "Broadcasting transaction"

sbt "run simpletransaction broadcast -n private -i $TX_PROVED_FILE -h localhost --bifrost-port 9084 --walletdb $WALLET"

echo "Sleeping for 30 seconds"

sleep 30

TO_ADDRESS=ptetP7jshHVrEKqDRdKAZtuybPZoMWTKKM2ngaJ7L5iZnxP5BprDB3hGJEFr
TX1_FILE=tmp/tx1.pbuf
TX1_PROVED_FILE=tmp/tx1_proved.pbuf

echo "Creating transaction"
sbt "run simpletransaction create --from-party self --from-contract default -t $TO_ADDRESS -w test --bifrost-port 9084 -o $TX1_FILE -n private -a 100 -h localhost --keyfile $MAIN_KEY --walletdb $WALLET"

echo "Proving transaction"

sbt "run simpletransaction prove --from-party self --from-contract default -w test --keyfile $MAIN_KEY -n private -i $TX1_FILE -o $TX1_PROVED_FILE --walletdb $WALLET"

echo "Broadcasting transaction"

sbt "run simpletransaction broadcast -n private -i $TX1_PROVED_FILE -h localhost --bifrost-port 9084 --walletdb $WALLET"

echo "Sleeping for 30 seconds"

sleep 30

TO_PARTY=noparty
TO_CONTRACT=genesis
TX2_FILE=tmp/tx2.pbuf
TX2_PROVED_FILE=tmp/tx2_proved.pbuf

echo "Creating transaction"

sbt "run simpletransaction create --from-party self --from-contract default --to-party $TO_PARTY --to-contract $TO_CONTRACT -w test --bifrost-port 9084 -o $TX2_FILE -n private -a 100 -h localhost --keyfile $MAIN_KEY --walletdb $WALLET"

echo "Proving transaction"

sbt "run simpletransaction prove --from-party self --from-contract default -w test --keyfile $MAIN_KEY -n private -i $TX2_FILE -o $TX2_PROVED_FILE --walletdb $WALLET"

echo "Broadcasting transaction"

sbt "run simpletransaction broadcast -n private -i $TX2_PROVED_FILE -h localhost --bifrost-port 9084 --walletdb $WALLET"

echo "Sleeping for 30 seconds"

sleep 30

TO_PARTY=noparty
TO_CONTRACT=genesis
TX3_FILE=tmp/tx3.pbuf
TX3_PROVED_FILE=tmp/tx3_proved.pbuf

echo "Creating transaction"

sbt "run simpletransaction create --from-party self --from-contract default --to-party $TO_PARTY --to-contract $TO_CONTRACT -w test --bifrost-port 9084 -o $TX3_FILE -n private -a 9999700 -h localhost --keyfile $MAIN_KEY --walletdb $WALLET"

echo "Proving transaction"

sbt "run simpletransaction prove --from-party self --from-contract default -w test --keyfile $MAIN_KEY -n private -i $TX3_FILE -o $TX3_PROVED_FILE --walletdb $WALLET"

echo "Broadcasting transaction"

sbt "run simpletransaction broadcast -n private -i $TX3_PROVED_FILE -h localhost --bifrost-port 9084 --walletdb $WALLET"