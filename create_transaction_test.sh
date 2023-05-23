#!/bin/bash

WALLET=tmp/wallet.db
MAIN_KEY=tmp/mainkey.json
TO_ADDRESS=ptetP7jshHVrEKqDRdKAZtuybPZoMWTKKM2ngaJ7L5iZnxP5BprDB3hGJEFr
TX_FILE=tmp/tx.pbuf
TX_PROVED_FILE=tmp/tx_proved.pbuf
rm -rf ./tmp
mkdir ./tmp
echo "Creating wallet"

sbt "run wallet init -w test -n private -o $MAIN_KEY --walletdb $WALLET"
echo "Creating transaction"
sbt "run simpletransaction create --from-party noparty --from-contract genesis --from-state 1 -t $TO_ADDRESS -w test --genus-port 9091 -o $TX_FILE -n private -a 100 -h localhost --keyfile $MAIN_KEY --walletdb $WALLET"

echo "Proving transaction"

sbt "run simpletransaction prove --from-party noparty --from-contract genesis --from-state 1 -w test --keyfile $MAIN_KEY -n private -i $TX_FILE -o $TX_PROVED_FILE --walletdb $WALLET"

echo "Broadcasting transaction"

sbt "run simpletransaction broadcast -n private -i $TX_PROVED_FILE -h localhost --genus-port 9091 --bifrost-port 9084 --walletdb $WALLET"

echo "Sleeping for 40 seconds"

sleep 40

TO_ADDRESS=ptetP7jshHVrEKqDRdKAZtuybPZoMWTKKM2ngaJ7L5iZnxP5BprDB3hGJEFr
TX1_FILE=tmp/tx1.pbuf
TX1_PROVED_FILE=tmp/tx1_proved.pbuf

echo "Creating transaction"
sbt "run simpletransaction create --from-party self --from-contract default -t $TO_ADDRESS -w test --genus-port 9091 -o $TX1_FILE -n private -a 100 -h localhost --keyfile $MAIN_KEY --walletdb $WALLET"

echo "Proving transaction"

sbt "run simpletransaction prove --from-party self --from-contract default -w test --keyfile $MAIN_KEY -n private -i $TX1_FILE -o $TX1_PROVED_FILE --walletdb $WALLET"

echo "Broadcasting transaction"

sbt "run simpletransaction broadcast -n private -i $TX1_PROVED_FILE -h localhost --genus-port 9091 --bifrost-port 9084 --walletdb $WALLET"
