---
sidebar_position: 5
---

# Prove a Transaction

To prove a transaction run the following command:

```bash
brambl-cli simple-transaction prove -w $PASSWORD --keyfile $MAIN_KEY -n $NETWORK -i $TX_FILE -o $TX_PROVED_FILE --walletdb $WALLET
```

This will prove the transaction in the file `$TX_FILE` and store the result in the file `$TX_PROVED_FILE`. The right indexes to derive the keys are taken from the wallet database.
