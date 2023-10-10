---
sidebar_position: 11
---

# Manage Keys

To interact with other parties and contracts you need to import their public
keys into your wallet. This allows to derive the right keys to derive the next
address where to send the funds.

## Export a base verification key

To export a base verification key run the following command:

```bash
brambl-cli wallet export-vk -w $PASSWORD -o $OUTPUT_FILE --walletdb $WALLET --party-name $PARTY_NAME --contract-name $CONTRACT_NAME --keyfile $KEYFILE -n $NETWORK
```

This will export the base verification key for the party `$PARTY_NAME` and contract `$CONTRACT_NAME` to the file `$OUTPUT_FILE`. The keyfile `$KEYFILE` is used to derive the exported key.

This command is also used to export a final verification key. To do this, use the `--state` option to specify the 
state from which to export the key.

## Import a base verification key

To import one or many base verification keys run the following command:

```bash
brambl-cli wallet import-vks --input-vks $BASE_VK_1,$BASE_VK_2 --party-name $PARTY_NAME --contract-name $CONTRACT_NAME -n $NETWORK --walletdb $WALLET
```

## Sync the wallet

To sync the wallet run the following command:

```bash
brambl-cli wallet sync --contract-name $CONTRACT_NAME --party-name $PARTY_NAME --walletdb $WALLET -n $NETWORK -h $HOST --port $PORT --keyfile $KEYFILE -w $PASSWORD
```

This will sync the wallet for the party `$PARTY_NAME` and contract `$CONTRACT_NAME` with the bifrost node running on `$HOST` on port `$PORT`. The keyfile `$KEYFILE` is used to derive keys. The password for the wallet is `$PASSWORD`. 

The procedure for the sync is the following:

- we first derive the next address for the party and contract
- we query the node to see if the UTXOs in that address are spent
- if the UTXOs are spent, then we derive the next address and repeat the process