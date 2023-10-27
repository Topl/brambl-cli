---
sidebar_position: 11
---

# Manage Keys

To interact with other fellowships and templates you need to import their public
keys into your wallet. This allows to derive the right keys to derive the next
address where to send the funds.

## Export a base verification key

To export a base verification key run the following command:

```bash
brambl-cli wallet export-vk -w $PASSWORD -o $OUTPUT_FILE --walletdb $WALLET --fellowship-name $FELLOWSHIP_NAME --template-name $LOCK_TEMPLATE_NAME --keyfile $KEYFILE -n $NETWORK
```

This will export the base verification key for the fellowship `$FELLOWSHIP_NAME` and template `$LOCK_TEMPLATE_NAME` to the file `$OUTPUT_FILE`. The keyfile `$KEYFILE` is used to derive the exported key.

This command is also used to export a final verification key. To do this, use the `--interaction` option to specify the 
interaction from which to export the key.

## Import a base verification key

To import one or many base verification keys run the following command:

```bash
brambl-cli wallet import-vks --input-vks $BASE_VK_1,$BASE_VK_2 --fellowship-name $FELLOWSHIP_NAME --template-name $LOCK_TEMPLATE_NAME -n $NETWORK --walletdb $WALLET
```

## Sync the wallet

To sync the wallet run the following command:

```bash
brambl-cli wallet sync --template-name $LOCK_TEMPLATE_NAME --fellowship-name $FELLOWSHIP_NAME --walletdb $WALLET -n $NETWORK -h $HOST --port $PORT --keyfile $KEYFILE -w $PASSWORD
```

This will sync the wallet for the fellowship `$FELLOWSHIP_NAME` and template `$LOCK_TEMPLATE_NAME` with the bifrost node running on `$HOST` on port `$PORT`. The keyfile `$KEYFILE` is used to derive keys. The password for the wallet is `$PASSWORD`. 

The procedure for the sync is the following:

- we first derive the next address for the fellowship and template
- we query the node to see if the UTXOs in that address are spent
- if the UTXOs are spent, then we derive the next address and repeat the process