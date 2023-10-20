---
sidebar_position: 1
---

# Creating Transactions

In this tutorial we are creating a transaction using the brambl-cli and
sending it to the network.

The process of creating a transaction is the following:

- First, we need to create the actual transaction.
- Then, we need to prove the transaction.
- Finally, we need to send the transaction to the network.

## Setup

## Setup Wallet and Funds

To create a transaction, you need to have a wallet with some funds. You can 
create a wallet following the instructions in the
[Initialize Wallet](/docs/current/how-tos/initialize-wallet) how to. For people
to send you funds, you need to share your address with them. You can see how
to get your current address using the 
[Get the Current Address](/docs/current/how-tos/current-address). You can check you balance
using the [Check Balance](/docs/current/how-tos/check-balance) how to.

## Launch a Private Node or use a Public Testnet Node

### Launch a Private Node

To launch a private node you need to have Docker installed in your machine. Once
Docker is installed, you can launch a node using the following command:

```
docker run --rm -p 9085:9085 -p 9084:9084 docker.io/toplprotocol/bifrost-node:$NODE_VERSION
```

The latest available version right now is `2.0.0-alpha9`.

### Use a Public Testnet Node

To use the public test net you need to call the different methods using the 
`--host` and `--port` parameters. The host is `testnet.topl.tech` and the port
is `443`. You also need to use the `--secure` or `-s` parameter to use TLS over the
connection.

For example, to check the balance of the address `ptetP7jshHTwEg9Fz9Xa1AmmzhYHDHo1zZRde7mnw3fddcXPjV14RPcgVgy7`
on the testnet you need to run the following command:

```bash
brambl-cli wallet balance --from-address ptetP7jshHTwEg9Fz9Xa1AmmzhYHDHo1zZRde7mnw3fddcXPjV14RPcgVgy7 --walletdb $WALLET --host testnet.topl.tech --port 443 -s true
```

## Create an LVL transaction

To create a simple transaction you need to run the following command:

```bash
brambl-cli simple-transaction create --from-party $PARTY --from-contract $CONTRACT --from-state $STATE -t $TO_ADDRESS -w $PASSWORD --port $PORT -o $TX_FILE -n $NETWORK -a $SEND_AMOUNT -h $HOST -i $MAIN_KEY --walletdb $WALLET --fee $FEE --transfer-token $TOKEN_TYPE
```

This will create a transaction that spends the state `$STATE` of the contract `$CONTRACT` of the party `$PARTY` and sends `$SEND_AMOUNT` polys to the address `$TO_ADDRESS`. The transaction will be stored in the file `$TX_FILE`.

The `--from-state` parameter is only required if the party is `noparty`. If the party is `self`, or any contract where there is at least one party, then the `--from-state` parameter is not required.


## Prove the Transaction

To prove the transaction run the following command:

```bash
brambl-cli tx prove -w $PASSWORD --keyfile $MAIN_KEY -n $NETWORK -i $TX_FILE -o $TX_PROVED_FILE --walletdb $WALLET
```

This will prove the transaction in the file `$TX_FILE` and store the result in the file `$TX_PROVED_FILE`. The right indexes to derive the keys are taken from the wallet database.

## Send the Transaction to the Network

To send the transaction to the network you need to run the following command:

```bash
brambl-cli tx broadcast -n $NETWORK -i $TX_PROVED_FILE -h $HOST --port $PORT --walletdb $WALLET
```

This will broadcast the transaction in the file `$TX_PROVED_FILE` to the network.

Do not forget to use the `--secure` parameter if you are using the testnet.

## Check the Balance

You can check the balance of the address `$TO_ADDRESS` using the following command:

```bash
brambl-cli wallet balance --from-address $TO_ADDRESS --walletdb $WALLET_DB --host $HOST --port $PORT
```

Do not forget to use the `--secure` parameter if you are using the testnet.