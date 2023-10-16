#!/usr/bin/bash

TEMPDIR="temp"

rm -rf $TEMPDIR
mkdir -p $TEMPDIR
WALLET="$TEMPDIR/wallet.db"
KEYFILE="$TEMPDIR/keyfile.json"
MNEMONIC="$TEMPDIR/mnemonic.txt"

BRAMBLCLI="cs launch -r https://s01.oss.sonatype.org/content/repositories/releases co.topl:brambl-cli_2.13:2.0.0-alpha4+12-82ad31ff+20231016-0905-SNAPSHOT -- "

$BRAMBLCLI wallet init -w test -n private -o $KEYFILE --newwalletdb $WALLET  --mnemonicfile $MNEMONIC
ADDRESS=$($BRAMBLCLI wallet current-address  --walletdb $WALLET)
echo "Address " $ADDRESS

GENESIS_TX_FILE="$TEMPDIR/genesisTx.pbuf"
GENESIS_TX_PROVED_FILE="$TEMPDIR/genesisTxProved.pbuf"

$BRAMBLCLI simple-transaction create --from-party noparty --from-contract genesis --from-state 1 -t  $ADDRESS -w test -o $GENESIS_TX_FILE -n private -a 1000 --fee 100 -h localhost --port 9084 --keyfile $KEYFILE --walletdb $WALLET  --transfer-token lvl
$BRAMBLCLI tx prove -i $GENESIS_TX_FILE --walletdb $WALLET --keyfile $KEYFILE -w test -o $GENESIS_TX_PROVED_FILE 
TXID=$($BRAMBLCLI tx broadcast -i $GENESIS_TX_PROVED_FILE -h localhost --port 9084)
echo "TXID " $TXID	
$BRAMBLCLI bifrost-query transaction-by-id --transaction-id $TXID --port 9084 --host localhost
while [ $? -ne 0 ]
do
  echo "Waiting for transaction to be processed"
  sleep 1
  $BRAMBLCLI bifrost-query transaction-by-id --transaction-id $TXID --port 9084 --host localhost
done
UTXO=$($BRAMBLCLI bifrost-query transaction-by-id --transaction-id $TXID --port 9084 --host localhost | grep -ie "#" | tail -2 | head -1 | cut -c15-)
echo "UTXO " $UTXO

SERIESPOLICY="$TEMPDIR/seriesPolicy.yaml"

cat > $SERIESPOLICY << EOD
label: Alice Series
registrationUtxo: $UTXO

fungibility: group-and-series
quantityDescriptor: liquid
permanentMetadata:
  type: object
  properties:
    name:
      type: string
    tickerName:
      type: string
    description:
      type: string
ephemeralMetadata:
  type: object
  properties:
    url:
      type: string
    image:
      type: string
EOD

SERIES_MINT_TX_FILE="$TEMPDIR/seriesMintTx.pbuf"
SERIES_MINT_TX_PROVED_FILE="$TEMPDIR/seriesMintTxProved.pbuf"

$BRAMBLCLI simple-minting create -h localhost --port 9084 --walletdb $WALLET --keyfile $KEYFILE --mint-token series --fee 100 -w test -o $SERIES_MINT_TX_FILE -i $SERIESPOLICY --mint-amount 1 --from-party self --from-contract default

$BRAMBLCLI tx prove -i $SERIES_MINT_TX_FILE --walletdb $WALLET --keyfile $KEYFILE -w test -o $SERIES_MINT_TX_PROVED_FILE

TXID=$($BRAMBLCLI tx broadcast -i $SERIES_MINT_TX_FILE -h localhost --port 9084)

echo "TXID " $TXID	
$BRAMBLCLI bifrost-query transaction-by-id --transaction-id $TXID --port 9084 --host localhost
while [ $? -ne 0 ]
do
  echo "Waiting for transaction to be processed"
  sleep 1
  $BRAMBLCLI bifrost-query transaction-by-id --transaction-id $TXID --port 9084 --host localhost
done