#!/bin/bash

WALLET=tmp/wallet.db
MAIN_KEY_1=tmp/mainkey-1.json
MAIN_KEY_2=tmp/mainkey-2.json
MNEMONIC_FILE=tmp/mnemonic.txt
PASSPHRASE=randomtest

rm -rf ./tmp
mkdir ./tmp

echo "Creating wallet"
sbt "run wallet init -w test -n private -o $MAIN_KEY_1 --walletdb $WALLET --passphrase $PASSPHRASE --mnemonicfile $MNEMONIC_FILE"

MNEMONIC=$(cat $MNEMONIC_FILE)


echo "Recover wallet key"
sbt "run wallet recover-keys -w test -n private -o $MAIN_KEY_2 --walletdb $WALLET --passphrase $PASSPHRASE --mnemonic \"$MNEMONIC\""