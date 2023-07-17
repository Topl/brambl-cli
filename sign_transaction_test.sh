#!/bin/bash

TMP_DIR=./tmp

rm -rf $TMP_DIR
mkdir $TMP_DIR

echo "Creating wallet for Edmundo"

BRAMBL_CLI="cs launch -r https://s01.oss.sonatype.org/content/repositories/snapshots -V co.topl:protobuf-fs2_2.13:2.0.0-alpha2 co.topl:brambl-cli_2.13:2.0.0-demo-2023-07-07+8-0e9522b4+20230711-1512-SNAPSHOT --"

BASE_AMOUNT=1000
SHARED_AMOUNT=500

EDMUNDO_WALLET=$TMP_DIR/edmundo_wallet.db
EDMUNDO_MAIN_KEY=$TMP_DIR/edmundo_mainkey.json
EDMUNDO_PASSWORD=test
EDMUNDO_FIRST_TX_RAW=$TMP_DIR/edmundo_first_tx.pbuf
EDMUNDO_FIRST_TX_PROVED=$TMP_DIR/edmundo_first_tx_proved.pbuf

$BRAMBL_CLI wallet init -w $EDMUNDO_PASSWORD \
                  -n private -o $EDMUNDO_MAIN_KEY --walletdb $EDMUNDO_WALLET

echo "Creating transaction for Edmundo"

EDMUNDO_TO_ADDRESS=$($BRAMBL_CLI wallet current-address -w $EDMUNDO_PASSWORD -n private --walletdb $EDMUNDO_WALLET)

$BRAMBL_CLI simpletransaction create  \
                  --from-party noparty \
                  --from-contract genesis \
                  --from-state 1 \
                  -t $EDMUNDO_TO_ADDRESS -w $EDMUNDO_PASSWORD \
                  --bifrost-port 9084 \
                  -o $EDMUNDO_FIRST_TX_RAW -n private -a $BASE_AMOUNT -h localhost \
                  --keyfile $EDMUNDO_MAIN_KEY --walletdb $EDMUNDO_WALLET

sleep 10

$BRAMBL_CLI simpletransaction prove --from-party noparty \
                                     --from-contract genesis \
                                     --from-state 1 \
                                     -w test --keyfile $EDMUNDO_MAIN_KEY \
                                     -n private -i $EDMUNDO_FIRST_TX_RAW \
                                     -o $EDMUNDO_FIRST_TX_PROVED \
                                     --walletdb $EDMUNDO_WALLET

$BRAMBL_CLI simpletransaction broadcast -n private -i $EDMUNDO_FIRST_TX_PROVED \
       -h localhost --bifrost-port 9084 --walletdb $EDMUNDO_WALLET

echo "Creating wallet for Daniela"

DANIELA_WALLET=$TMP_DIR/daniela_wallet.db
DANIELA_MAIN_KEY=$TMP_DIR/daniela_mainkey.json
DANIELA_PASSWORD=test
DANIELA_FIRST_TX_RAW=$TMP_DIR/daniela_first_tx.pbuf
DANIELA_FIRST_TX_PROVED=$TMP_DIR/daniela_first_tx_proved.pbuf

$BRAMBL_CLI wallet init -w $DANIELA_PASSWORD \
                  -n private -o $DANIELA_MAIN_KEY --walletdb $DANIELA_WALLET

# echo "Creating transaction for Daniela"

# DANIELA_TO_ADDRESS=$($BRAMBL_CLI wallet current-address -w $DANIELA_PASSWORD -n private --walletdb $DANIELA_WALLET)

# $BRAMBL_CLI simpletransaction create  \
#                   --from-party noparty \
#                   --from-contract genesis \
#                   --from-state 1 \
#                   -t $DANIELA_TO_ADDRESS -w $DANIELA_PASSWORD \
#                   --bifrost-port 9084 \
#                   -o $DANIELA_FIRST_TX_RAW -n private -a $BASE_AMOUNT -h localhost \
#                   --keyfile $DANIELA_MAIN_KEY --walletdb $DANIELA_WALLET

# $BRAMBL_CLI simpletransaction prove --from-party noparty \
#                                      --from-contract genesis \
#                                      --from-state 1 \
#                                      -w test --keyfile $DANIELA_MAIN_KEY \
#                                      -n private -i $DANIELA_FIRST_TX_RAW \
#                                      -o $DANIELA_FIRST_TX_PROVED \
#                                      --walletdb $DANIELA_WALLET

# $BRAMBL_CLI simpletransaction broadcast -n private -i $DANIELA_FIRST_TX_PROVED \
#        -h localhost --bifrost-port 9084 --walletdb $DANIELA_WALLET

$BRAMBL_CLI parties add --party-name "edmundo_daniela_0" --walletdb $EDMUNDO_WALLET -n private

$BRAMBL_CLI parties add --party-name "edmundo_daniela_0" --walletdb $DANIELA_WALLET -n private

CONTRACT_NAME=or_sign

$BRAMBL_CLI contracts add --walletdb $EDMUNDO_WALLET  -n private \
          --contract-name $CONTRACT_NAME \
          --contract-template "threshold(1, sign(0) or sign(1))"

$BRAMBL_CLI contracts add --walletdb $DANIELA_WALLET  -n private \
          --contract-name $CONTRACT_NAME \
          --contract-template "threshold(1, sign(1) or sign(0))"

EDMUNDO_VK=$TMP_DIR/edmundo_vk.txt
DANIELA_VK=$TMP_DIR/daniela_vk.txt

echo "Exporting verification keys"

$BRAMBL_CLI wallet export-vk -w test -o $EDMUNDO_VK -n private \
              --walletdb $EDMUNDO_WALLET --party-name edmundo_daniela_0 \
              --contract-name $CONTRACT_NAME \
              --keyfile $EDMUNDO_MAIN_KEY 

$BRAMBL_CLI wallet export-vk -w test -o $DANIELA_VK -n private \
              --walletdb $DANIELA_WALLET --party-name edmundo_daniela_0 \
              --contract-name $CONTRACT_NAME \
              --keyfile $DANIELA_MAIN_KEY 

echo "Importing verification keys to Edmundo's wallet"

$BRAMBL_CLI wallet import-vks -n private \
          --input-vks $DANIELA_VK --party-name edmundo_daniela_0 \
          --contract-name $CONTRACT_NAME  --walletdb $EDMUNDO_WALLET\
           --keyfile $EDMUNDO_MAIN_KEY -w $EDMUNDO_PASSWORD

echo "Sending 500 to shared account"

EDMUNDO_SECOND_TX_RAW=$TMP_DIR/edmundo_second_tx.pbuf
EDMUNDO_SECOND_TX_PROVED=$TMP_DIR/edmundo_second_tx_proved.pbuf

$BRAMBL_CLI simpletransaction create  \
                  --from-party self \
                  --from-contract default \
                  --to-party edmundo_daniela_0 \
                  --to-contract $CONTRACT_NAME \
                  -w $EDMUNDO_PASSWORD \
                  --bifrost-port 9084 \
                  -o $EDMUNDO_SECOND_TX_RAW -n private -a $SHARED_AMOUNT -h localhost \
                  --keyfile $EDMUNDO_MAIN_KEY --walletdb $EDMUNDO_WALLET

sleep 5

$BRAMBL_CLI simpletransaction prove --from-party self \
                                     --from-contract default \
                                     -w test --keyfile $EDMUNDO_MAIN_KEY \
                                     -n private -i $EDMUNDO_SECOND_TX_RAW \
                                     -o $EDMUNDO_SECOND_TX_PROVED \
                                     --walletdb $EDMUNDO_WALLET

$BRAMBL_CLI simpletransaction broadcast -n private -i $EDMUNDO_SECOND_TX_PROVED \
       -h localhost --bifrost-port 9084 --walletdb $EDMUNDO_WALLET

sleep 10

echo "Checking balance of shared account"

$BRAMBL_CLI genus-query utxo-by-address \
            --from-party edmundo_daniela_0 \
            --from-contract $CONTRACT_NAME -n private \
            -h localhost --bifrost-port 9084 \
             --walletdb $EDMUNDO_WALLET

echo "Spending 200 from shared account by Daniela"

echo "Importing verification keys to Daniela's wallet"

$BRAMBL_CLI wallet import-vks -n private \
          --input-vks $EDMUNDO_VK --party-name edmundo_daniela_0 \
          --contract-name $CONTRACT_NAME  --walletdb $DANIELA_WALLET \
           --keyfile $DANIELA_MAIN_KEY -w $DANIELA_PASSWORD

EDMUNDO_TO_ADDRESS_2=$($BRAMBL_CLI wallet current-address -w $EDMUNDO_PASSWORD -n private --walletdb $EDMUNDO_WALLET)

DANIELA_SECOND_TX_RAW=$TMP_DIR/daniela_second_tx.pbuf
DANIELA_SECOND_TX_PROVED=$TMP_DIR/daniela_second_tx_proved.pbuf


$BRAMBL_CLI simpletransaction create  \
                  --from-party edmundo_daniela_0 \
                  --from-contract $CONTRACT_NAME \
                  -t $EDMUNDO_TO_ADDRESS_2  \
                  -w $EDMUNDO_PASSWORD \
                  --bifrost-port 9084 \
                  -o $DANIELA_SECOND_TX_RAW -n private -a 200 -h localhost \
                  --keyfile $DANIELA_MAIN_KEY --walletdb $DANIELA_WALLET

sleep 5

$BRAMBL_CLI simpletransaction prove --from-party edmundo_daniela_0 \
                                     --from-contract $CONTRACT_NAME \
                                     -w test --keyfile $DANIELA_MAIN_KEY \
                                     -n private -i $DANIELA_SECOND_TX_RAW \
                                     -o $DANIELA_SECOND_TX_PROVED \
                                     --walletdb $DANIELA_WALLET

$BRAMBL_CLI simpletransaction broadcast -n private -i $DANIELA_SECOND_TX_PROVED \
       -h localhost --bifrost-port 9084 --walletdb $DANIELA_WALLET
