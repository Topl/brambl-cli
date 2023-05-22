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
  --genus-port <value>     Port Genus node. (mandatory)
  --bifrost-port <value>   Port Bifrost node. (mandatory)
  --walletdb <value>       Wallet DB file. (mandatory)
Command: bifrost-query [block-by-height|block-by-id|transaction-by-id] [options]
Bifrost query mode
Command: bifrost-query block-by-height
Get the block at a given height
  -n, --network <value>    Network name: Possible values: mainnet, testnet, private. (mandatory)
  -h, --host <value>       The host of the Genus node. (mandatory)
  --genus-port <value>     Port Genus node. (mandatory)
  --bifrost-port <value>   Port Bifrost node. (mandatory)
  --height <value>         The height of the block. (mandatory)
Command: bifrost-query block-by-id
Get the block with a given id
  -n, --network <value>    Network name: Possible values: mainnet, testnet, private. (mandatory)
  -h, --host <value>       The host of the Genus node. (mandatory)
  --genus-port <value>     Port Genus node. (mandatory)
  --bifrost-port <value>   Port Bifrost node. (mandatory)
  --block-id <value>       The id of the block in base 58. (mandatory)
Command: bifrost-query transaction-by-id
Get the transaction with a given id
  -n, --network <value>    Network name: Possible values: mainnet, testnet, private. (mandatory)
  -h, --host <value>       The host of the Genus node. (mandatory)
  --genus-port <value>     Port Genus node. (mandatory)
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
  --genus-port <value>     Port Genus node. (mandatory)
  --bifrost-port <value>   Port Bifrost node. (mandatory)
  -k, --keyfile <value>    The key file.
  -w, --password <value>   Password for the encrypted key. (mandatory)
  --walletdb <value>       Wallet DB file. (mandatory)
  -o, --output <value>     The output file. (mandatory)
  -t, --to <value>         Address to send polys to. (mandatory)
  -a, --amount <value>     Amount to send simple transaction
Command: simpletransaction broadcast
Broadcast transaction
  -n, --network <value>    Network name: Possible values: mainnet, testnet, private. (mandatory)
  -h, --host <value>       The host of the Genus node. (mandatory)
  --genus-port <value>     Port Genus node. (mandatory)
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