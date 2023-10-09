---
sidebar_position: 6
---

# Broadcast a Transaction

To broadcast a simple transaction run the following command:

```bash
brambl-cli simple-transaction broadcast -n $NETWORK -i $TX_PROVED_FILE -h $HOST --port $PORT --walletdb $WALLET
```

This will broadcast the transaction in the file `$TX_PROVED_FILE` to the network.