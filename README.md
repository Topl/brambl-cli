# Brambl CLI

The Brambl CLI is a command line interface to the Topl platform. It is a simple tool to interact with the Topl platform.

## System Requirements

Your system needs to have the following software installed.

- A Java Virtual Machine (JVM).- The bifrost-daml-broker is a Java application, and thus Java is needed.
- [Coursier](https://get-coursier.io/docs/cli-installation).- A simple command line tool (CLI) to
to run Java applications without any setup. It is very easy to install.

## Using the CLI

```
Usage:  [contracts|parties|genus-query|bifrost-query|wallet|simpletransaction]
Command: contracts [list|add] [options]
Contract mode
Command: contracts list
List existing contracts
  -n, --network <value>    Network name: Possible values: mainnet, testnet, private. (mandatory)
  -h, --host <value>       The host of the node. (mandatory)
  --bifrost-port <value>   Port Bifrost node. (mandatory)
  --walletdb <value>       Wallet DB file. (mandatory)
Command: contracts add
Add a new contracts
  -n, --network <value>    Network name: Possible values: mainnet, testnet, private. (mandatory)
  -h, --host <value>       The host of the node. (mandatory)
  --bifrost-port <value>   Port Bifrost node. (mandatory)
  --walletdb <value>       Wallet DB file. (mandatory)
  --contract-name <value>  Name of the contract. (mandatory)
  --contract-template <value>
                           Contract template. (mandatory)
Command: parties [list|add] [options]
Entity mode
Command: parties list
List existing parties
  -n, --network <value>    Network name: Possible values: mainnet, testnet, private. (mandatory)
  -h, --host <value>       The host of the node. (mandatory)
  --bifrost-port <value>   Port Bifrost node. (mandatory)
  --walletdb <value>       Wallet DB file. (mandatory)
Command: parties add
Add a new parties
  -n, --network <value>    Network name: Possible values: mainnet, testnet, private. (mandatory)
  -h, --host <value>       The host of the node. (mandatory)
  --bifrost-port <value>   Port Bifrost node. (mandatory)
  --walletdb <value>       Wallet DB file. (mandatory)
  --party-name <value>     Name of the party. (mandatory)
Command: genus-query [utxo-by-address] [options]
Genus query mode
Command: genus-query utxo-by-address
Query utxo
  --from-party <value>     Party where we are sending the funds from
  --from-contract <value>  Contract where we are sending the funds from
  --from-state <value>     State from where we are sending the funds from
  -n, --network <value>    Network name: Possible values: mainnet, testnet, private. (mandatory)
  -h, --host <value>       The host of the node. (mandatory)
  --bifrost-port <value>   Port Bifrost node. (mandatory)
  --walletdb <value>       Wallet DB file. (mandatory)
Command: bifrost-query [block-by-height|block-by-id|transaction-by-id] [options]
Bifrost query mode
Command: bifrost-query block-by-height
Get the block at a given height
  -n, --network <value>    Network name: Possible values: mainnet, testnet, private. (mandatory)
  -h, --host <value>       The host of the node. (mandatory)
  --bifrost-port <value>   Port Bifrost node. (mandatory)
  --height <value>         The height of the block. (mandatory)
Command: bifrost-query block-by-id
Get the block with a given id
  -n, --network <value>    Network name: Possible values: mainnet, testnet, private. (mandatory)
  -h, --host <value>       The host of the node. (mandatory)
  --bifrost-port <value>   Port Bifrost node. (mandatory)
  --block-id <value>       The id of the block in base 58. (mandatory)
Command: bifrost-query transaction-by-id
Get the transaction with a given id
  -n, --network <value>    Network name: Possible values: mainnet, testnet, private. (mandatory)
  -h, --host <value>       The host of the node. (mandatory)
  --bifrost-port <value>   Port Bifrost node. (mandatory)
  --transaction-id <value>
                           The id of the transaction in base 58. (mandatory)
Command: wallet [sync|init|current-address|export-vk|import-vks] [options]
Wallet mode
Command: wallet sync
Sync wallet
  -n, --network <value>    Network name: Possible values: mainnet, testnet, private. (mandatory)
  -h, --host <value>       The host of the node. (mandatory)
  --bifrost-port <value>   Port Bifrost node. (mandatory)
  --party-name <value>     Name of the party. (mandatory)
  --contract-name <value>  Name of the contract. (mandatory)
  --walletdb <value>       Wallet DB file. (mandatory)
Command: wallet init
Initialize wallet
  -n, --network <value>    Network name: Possible values: mainnet, testnet, private. (mandatory)
  -w, --password <value>   Password for the encrypted key. (mandatory)
  -o, --output <value>     The output file. (optional)
  --walletdb <value>       Wallet DB file. (mandatory)
  -P, --passphrase <value>
                           Passphrase for the encrypted key. (optional))
  --mnemonicfile <value>   Mnemonic output file. (mandatory)
Command: wallet recover-keys
Recover the wallet main key using a mnemonic. 
  -n, --network <value>    Network name: Possible values: mainnet, testnet, private. (mandatory)
  -w, --password <value>   Password for the encrypted key. (mandatory)
  -o, --output <value>     The output file. (optional)
  --walletdb <value>       Wallet DB file. (mandatory)
  -P, --passphrase <value>
                           Passphrase for the encrypted key. (optional)
  -m, --mnemonic <value>   Mnemonic for the key. (mandatory)
Command: wallet current-address
Obtain current address
Command: wallet export-vk
Export verification key
  -k, --keyfile <value>    The key file.
  -w, --password <value>   Password for the encrypted key. (mandatory)
  --walletdb <value>       Wallet DB file. (mandatory)
  -o, --output <value>     The output file.
  --walletdb <value>       Wallet DB file. (mandatory)
  --party-name <value>     Name of the party. (mandatory)
  --contract-name <value>  Name of the contract. (mandatory)
Command: wallet import-vks
Import verification key
  --walletdb <value>       Wallet DB file. (mandatory)
  --party-name <value>     Name of the party. (mandatory)
  --contract-name <value>  Name of the contract. (mandatory)
  --input-vks <value>      The keys to import. (mandatory)
Command: simpletransaction [create|broadcast|prove] [options]
Simple transaction mode
Command: simpletransaction create
Create transaction
  --from-party <value>     Party where we are sending the funds from
  --from-contract <value>  Contract where we are sending the funds from
  --from-state <value>     State from where we are sending the funds from
  -n, --network <value>    Network name: Possible values: mainnet, testnet, private. (mandatory)
  -h, --host <value>       The host of the node. (mandatory)
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
  -h, --host <value>       The host of the node. (mandatory)
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

### Add a new party

To add a new party to the wallet run the following command:

```bash
cs launch co.topl:brambl-cli_2.13:2.0.0.beta-1 -- parties add --party-name $PARTY_NAME --walletdb $WALLET -n private
```

### List all parties

To list all parties in the wallet run the following command:

```bash
cs launch co.topl:brambl-cli_2.13:2.0.0.beta-1 -- parties list --walletdb $WALLET -n private
```

### Add a new contract

To add a new contract to the wallet run the following command:

```bash
cs launch co.topl:brambl-cli_2.13:2.0.0.beta-1 -- contracts add --walletdb $WALLET --contract-name $CONTRACT_NAME --contract-template $CONTRACT_TEMPLATE -n private
```

### List all contracts

To list all contracts in the wallet run the following command:

```bash
cs launch co.topl:brambl-cli_2.13:2.0.0.beta-1 -- contracts list --walletdb $WALLET -n private
```

### Export a base verification key

To export a base verification key run the following command:

```bash
cs launch co.topl:brambl-cli_2.13:2.0.0.beta-1 -- wallet export-vk -w test -o $OUTPUT_FILE --walletdb $WALLET --party-name $PARTY_NAME --contract-name $CONTRACT_NAME --keyfile $KEYFILE -n private
```

This will export the base verification key for the party `$PARTY_NAME` and contract `$CONTRACT_NAME` to the file `$OUTPUT_FILE`. The keyfile `$KEYFILE` is used to derive the exported key.

### Import a base verification key

To import one or many base verification keys run the following command:

```bash
cs launch co.topl:brambl-cli_2.13:2.0.0.beta-1 -- wallet import-vks --input-vks $BASE_VK_1,$BASE_VK_2 --party-name $PARTY_NAME --contract-name $CONTRACT_NAME -n private --walletdb $WALLET
```

### Sync the wallet

To sync the wallet run the following command:

```bash
cs launch co.topl:brambl-cli_2.13:2.0.0.beta-1 -- wallet sync --contract-name $CONTRACT_NAME --party-name $PARTY_NAME --walletdb $WALLET -n private -h localhost --bifrost-port 9084 --keyfile $KEYFILE -w $PASSWORD
```

This will sync the wallet for the party `$PARTY_NAME` and contract `$CONTRACT_NAME` with the bifrost node running on `localhost` on port `9084`. The keyfile `$KEYFILE` is used to derive keys. The password for the wallet is `$PASSWORD`.