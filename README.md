# Brambl CLI

The Brambl CLI is a command line interface to the Topl platform. It is a simple tool to interact with the Topl platform.

## System Requirements

Your system needs to have the following software installed.

- A Java Virtual Machine (JVM).- The bifrost-daml-broker is a Java application, and thus Java is needed.
- [Coursier](https://get-coursier.io/docs/cli-installation).- A simple command line tool (CLI) to
to run Java applications without any setup. It is very easy to install.

## Using the CLI

```
Usage:  [genus-query|bifrost-query|wallet|simpletransaction]

Command: genus-query [utxo-by-address] [options]
Genus query mode
Command: genus-query utxo-by-address
Query utxo
  --from-party <value>     Party where we are sending the funds from
  --from-contract <value>  Contract where we are sending the funds from
  --from-state <value>     State from where we are sending the funds from
  -n, --network <value>    Network name: Possible values: mainnet, testnet, private. (mandatory)
  -h, --host <value>       The host of the Genus node. (mandatory)
  --bifrost-port <value>   Port Bifrost node. (mandatory)
  --walletdb <value>       Wallet DB file. (mandatory)
Command: bifrost-query [block-by-height|block-by-id|transaction-by-id] [options]
Bifrost query mode
Command: bifrost-query block-by-height
Get the block at a given height
  -n, --network <value>    Network name: Possible values: mainnet, testnet, private. (mandatory)
  -h, --host <value>       The host of the Genus node. (mandatory)
  --bifrost-port <value>   Port Bifrost node. (mandatory)
  --height <value>         The height of the block. (mandatory)
Command: bifrost-query block-by-id
Get the block with a given id
  -n, --network <value>    Network name: Possible values: mainnet, testnet, private. (mandatory)
  -h, --host <value>       The host of the Genus node. (mandatory)
  --bifrost-port <value>   Port Bifrost node. (mandatory)
  --block-id <value>       The id of the block in base 58. (mandatory)
Command: bifrost-query transaction-by-id
Get the transaction with a given id
  -n, --network <value>    Network name: Possible values: mainnet, testnet, private. (mandatory)
  -h, --host <value>       The host of the Genus node. (mandatory)
  --bifrost-port <value>   Port Bifrost node. (mandatory)
  --transaction-id <value>
                           The id of the transaction in base 58. (mandatory)
Command: wallet [init|current-address] [options]
Wallet mode
Command: wallet init
Initialize wallet
  -n, --network <value>    Network name: Possible values: mainnet, testnet, private. (mandatory)
  -w, --password <value>   Password for the encrypted key. (mandatory)
  -o, --output <value>     The output file. (optional)
  --walletdb <value>       Wallet DB file. (mandatory)
  -P, --passphrase <value>
                           Passphrase for the encrypted key. (optional))
Command: wallet current-address
Initialize wallet
Command: simpletransaction [create|broadcast|prove] [options]
Simple transaction mode
Command: simpletransaction create
Create transaction
  --from-party <value>     Party where we are sending the funds from
  --from-contract <value>  Contract where we are sending the funds from
  --from-state <value>     State from where we are sending the funds from
  -n, --network <value>    Network name: Possible values: mainnet, testnet, private. (mandatory)
  -h, --host <value>       The host of the Genus node. (mandatory)
  --bifrost-port <value>   Port Bifrost node. (mandatory)
  -k, --keyfile <value>    The key file.
  -w, --password <value>   Password for the encrypted key. (mandatory)
  --walletdb <value>       Wallet DB file. (mandatory)
  -o, --output <value>     The output file. (mandatory)
  -t, --to <value>         Address to send LVLs to. (mandatory if to-party and to-contract are not provided)
  --to-party <value>       Party to send LVLs to. (mandatory if to is not provided)
  --to-contract <value>    Contract to send LVLs to. (mandatory if to is not provided)
  -a, --amount <value>     Amount to send simple transaction
Command: simpletransaction broadcast
Broadcast transaction
  -n, --network <value>    Network name: Possible values: mainnet, testnet, private. (mandatory)
  -h, --host <value>       The host of the Genus node. (mandatory)
  --bifrost-port <value>   Port Bifrost node. (mandatory)
  -i, --input <value>      The input file. (mandatory)
Command: simpletransaction prove
Prove transaction
  --from-party <value>     Party where we are sending the funds from
  --from-contract <value>  Contract where we are sending the funds from
  --from-state <value>     State from where we are sending the funds from
  -k, --keyfile <value>    The key file.
  -w, --password <value>   Password for the encrypted key. (mandatory)
  --walletdb <value>       Wallet DB file. (mandatory)
  -o, --output <value>     The output file. (mandatory)
  -i, --input <value>      The input file. (mandatory)
```

### Initialize a wallet

To create a keyfile for the valhalla network, with password `test` and to store it in the file `mainkey.json`, and initialize a `wallet.db` file run the following command:

```bash
cs launch co.topl:brambl-cli_2.13:2.0.0.beta-1 -- wallet init -w test -n private -o mainkey.json --walletdb wallet.db
```

### Get the current address

To get the current address of the wallet run the following command:

```bash
cs launch co.topl:brambl-cli_2.13:2.0.0.beta-1 -- wallet current-address -w test -n private -o $MAIN_KEY --walletdb $WALLET
```

This will output the current address of the wallet.

### Create a simple transaction

To create a simple transaction to spend the genesis block run the following command:

```bash
cs launch co.topl:brambl-cli_2.13:2.0.0.beta-1 --  simpletransaction create --from-party noparty --from-contract genesis --from-state 1 -t ptetP7jshHVRuMURLWzn5RNsEBPth1CParqz5Rug99R4m1pjFN9BrChgbHCY -w test -p 9091 -o $TX_FILE -n private -a 100 -h localhost -i $MAIN_KEY --walletdb $WALLET
```

This will create a transaction that spends the genesis block and sends 100 polys to the address `ptetP7jshHVRuMURLWzn5RNsEBPth1CParqz5Rug99R4m1pjFN9BrChgbHCY`. The transaction will be stored in the file `$TX_FILE`.

Alternatively, instead of providing an output address, the party and contract of the output can be used instead. To do this, run the following command:

```bash
cs launch co.topl:brambl-cli_2.13:2.0.0.beta-1 --  simpletransaction create --from-party noparty --from-contract genesis --from-state 1 --from-party self --from-contract default -w test -p 9091 -o $TX_FILE -n private -a 100 -h localhost -i $MAIN_KEY --walletdb $WALLET
```


```bash 

### Prove a simple transaction

To prove a simple transaction run the following command:

```bash
cs launch co.topl:brambl-cli_2.13:2.0.0.beta-1 --  simpletransaction prove --from-party self --from-contract default -w test --keyfile $MAIN_KEY -n private -i $TX_FILE -o $TX_PROVED_FILE --walletdb $WALLET
```

This will prove the transaction in the file `$TX_FILE` and store the result in the file `$TX_PROVED_FILE`.

### Broadcast a simple transaction

To broadcast a simple transaction run the following command:

```bash
cs launch co.topl:brambl-cli_2.13:2.0.0.beta-1 -- broadcast -n private -i $TX_PROVED_FILE -h localhost --bifrost-port 9084 --walletdb $WALLET
```

This will broadcast the transaction in the file `$TX_PROVED_FILE` to the network.

### Query a block by id

To query a block by id run the following command:

```bash
cs launch co.topl:brambl-cli_2.13:2.0.0.beta-1 -- bifrost-query block-by-id --block-id $BLOCK_ID -n private -h localhost --bifrost-port 9084
```

This will query the block with id `$BLOCK_ID` from the bifrost node running on `localhost` on port `9084`.

### Query a block by height

To query a block by height run the following command:

```bash
cs launch co.topl:brambl-cli_2.13:2.0.0.beta-1 -- bifrost-query block-by-height --height $HEIGHT -n private -h localhost --bifrost-port 9084
```

This will query the block with height `$HEIGHT` from the bifrost node running on `localhost` on port `9084`.

### Query a transaction by id

To query a transaction by id run the following command:

```bash
cs launch co.topl:brambl-cli_2.13:2.0.0.beta-1 -- bifrost-query transaction-by-id --transaction-id $TX_ID -n private -h localhost --bifrost-port 9084
```

This will query the transaction with id `$TX_ID` from the bifrost node running on `localhost` on port `9084`.

### Query UXTO by address

To query UXTOs by address run the following command:

```bash
cs launch co.topl:brambl-cli_2.13:2.0.0.beta-1 -- genus-query utxo-by-address --from-party self --from-contract default -n private -h localhost --bifrost-port 9084 --walletdb $WALLET
```

This will query the UXTOs for the address in the genus node. It uses the wallet to derive the right address to query.

