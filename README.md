# Brambl CLI

The Brambl CLI is a command line interface to the Topl platform. It is a simple tool to interact with the Topl platform.

## System Requirements

Your system needs to have the following software installed.

- A Java Virtual Machine (JVM).- The bifrost-daml-broker is a Java application, and thus Java is needed.
- [Coursier](https://get-coursier.io/docs/cli-installation).- A simple command line tool (CLI) to run Java applications without any setup. It is very easy to install.
- check coursier install step
 
```shell
$ cd csInstallPath
$ ./cs version  
2.1.6
```

## Using the CLI

- There is no need to clone this repository for CLI usages, you can launch a release version of the CLI providing the repository.
- You can check releases on [sonatype](https://s01.oss.sonatype.org/content/repositories/releases/co/topl/brambl-cli_2.13/)
- Then just launch your coursier application

```shell
$ cd csInstallPath
$ ./cs launch -r https://s01.oss.sonatype.org/content/repositories/releases co.topl:brambl-cli_2.13:2.0.0-alpha3 -- bifrost-query block-by-height --height 1 -h localhost --bifrost-port 9084  
```

### Usage mode

```
Usage:  [contracts|parties|genus-query|bifrost-query|wallet|tx|simpletransaction]
```

### Contract mode

```
Command: contracts [list|add] [options]
Contract mode
Command: contracts list
List existing contracts
  --walletdb <value>       Wallet DB file. (mandatory)
Command: contracts add
Add a new contracts
  --walletdb <value>       Wallet DB file. (mandatory)
  --contract-name <value>  Name of the contract. (mandatory)
  --contract-template <value>
                           Contract template. (mandatory)
```

### Entity mode

```
Command: parties [list|add] [options]
Entity mode
Command: parties list
List existing parties
  --walletdb <value>       Wallet DB file. (mandatory)
Command: parties add
Add a new parties
  -n, --network <value>    Network name: Possible values: mainnet, testnet, private. (mandatory)
  -h, --host <value>       The host of the node. (mandatory)
  --bifrost-port <value>   Port Bifrost node. (mandatory)
  --walletdb <value>       Wallet DB file. (mandatory)
  --party-name <value>     Name of the party. (mandatory)

```

### Genus query mode

```
Command: genus-query [utxo-by-address] [options]
Genus query mode
Command: genus-query utxo-by-address
Query utxo
  --from-party <value>     Party where we are sending the funds from
  --from-contract <value>  Contract where we are sending the funds from
  --from-state <value>     State from where we are sending the funds from
  -h, --host <value>       The host of the node. (mandatory)
  --bifrost-port <value>   Port Bifrost node. (mandatory)
  --walletdb <value>       Wallet DB file. (mandatory)
  --token <value>          The token type. (optional). The valid token types are 'lvl', 'topl', 'asset' and 'all'
```

### Bifrost query mode

```  
Command: bifrost-query [block-by-height|block-by-id|transaction-by-id] [options]
Bifrost query mode
Command: bifrost-query block-by-height
Get the block at a given height
  -h, --host <value>       The host of the node. (mandatory)
  --bifrost-port <value>   Port Bifrost node. (mandatory)
  --height <value>         The height of the block. (mandatory)
Command: bifrost-query block-by-id
Get the block with a given id
  -h, --host <value>       The host of the node. (mandatory)
  --bifrost-port <value>   Port Bifrost node. (mandatory)
  --block-id <value>       The id of the block in base 58. (mandatory)
Command: bifrost-query transaction-by-id
Get the transaction with a given id
  -h, --host <value>       The host of the node. (mandatory)
  --bifrost-port <value>   Port Bifrost node. (mandatory)
  --transaction-id <value>
                           The id of the transaction in base 58. (mandatory)
                           
```

### Wallet mode

```                           
Command: wallet [sync|init|recover-keys|current-address|export-vk|import-vks] [options]
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
  --newwallet <value>      Wallet DB file. (mandatory)
  --mnemonicfile <value>   Mnemonic output file. (mandatory)
  -P, --passphrase <value>
                           Passphrase for the encrypted key. (optional))
Command: wallet recover-keys
Recover Wallet Main Key
  -n, --network <value>    Network name: Possible values: mainnet, testnet, private. (mandatory)
  -m, --mnemonic <value>   Mnemonic for the key. (mandatory)
  -w, --password <value>   Password for the encrypted key. (mandatory)
  -o, --output <value>     The output file. (optional)
  --walletdb <value>       Wallet DB file. (mandatory)
  -P, --passphrase <value>
                           Passphrase for the encrypted key. (optional))
Command: wallet current-address
Obtain current address
  --walletdb <value>       Wallet DB file. (mandatory)
Command: wallet export-vk
Export verification key
  -k, --keyfile <value>    The key file.
  -w, --password <value>   Password for the encrypted key. (mandatory)
  --walletdb <value>       Wallet DB file. (mandatory)
  -o, --output <value>     The output file.
  --walletdb <value>       Wallet DB file. (mandatory)
  --party-name <value>     Name of the party. (mandatory)
  --contract-name <value>  Name of the contract. (mandatory)
  --state <value>          State from where we are sending the funds from
Command: wallet import-vks
Import verification key
  --walletdb <value>       Wallet DB file. (mandatory)
  --party-name <value>     Name of the party. (mandatory)
  --contract-name <value>  Name of the contract. (mandatory)
  --input-vks <value>      The keys to import. (mandatory)
  
```

### Transaction mode

```  
Command: tx [create] [options]
Transaction mode
Command: tx create
Create transaction
  -n, --network <value>    Network name: Possible values: mainnet, testnet, private. (mandatory)
  -h, --host <value>       The host of the node. (mandatory)
  --bifrost-port <value>   Port Bifrost node. (mandatory)
  -o, --output <value>     The output file. (mandatory)
  -i, --input <value>      The input file. (mandatory)
  
```

### Simple transaction mode

```  
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
  -k, --keyfile <value>    The key file.
  -w, --password <value>   Password for the encrypted key. (mandatory)
  --walletdb <value>       Wallet DB file. (mandatory)
  -o, --output <value>     The output file. (mandatory)
  -i, --input <value>      The input file. (mandatory)
```


### Simple Minting Mode

```
Command: simple-minting [create]
Simple minting mode
Command: simple-minting create [options]
Create minting transaction
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
  -i, --input <value>      The input file. (mandatory)
  -a, --amount <value>     Amount to send or mint
  --fee <value>            Fee paid for the transaction
  --token <value>          The token type. The valid token types are 'lvl', 'topl', 'asset', 'group', 'series', and 'all'
```


## Launch Examples
 
The below examples assume:

- Coursier is installed.
- That the command to lauch has been aliased to `brambl-cli`.

```
alias brambl-cli="cs launch -r https://s01.oss.sonatype.org/content/repositories/releases co.topl:brambl-cli_2.13:2.0.0-alpha3 --"
```

### Initialize a wallet

To create a keyfile for the valhalla network and a new mnemonic, with password `test` and passphrase `passphrase`, and to store the keyfile in the file `mainkey.json` and the mnemonic in the file `mnemonic.txt`, and initialize a `wallet.db` file run the following command:

```bash
brambl-cli wallet init -w test --passphrase passphrase -n private -o mainkey.json --newwalletdb wallet.db --mnemonicfile mnemonic.txt
```

### Recover a wallet keyfile

To recover a keyfile for the valhalla network using a mnemonic, with password `test`, passphrase `passphrase` and an existing mnemonic consisting of a comma-separated list of words, and to store the recovered keyfile in the file `mainkey.json` run the following command:

```bash
brambl-cli wallet recover-keys -w test --passphrase passphrase -n private -o mainkey.json --walletdb wallet.db --mnemonic this,is,an,example,of,a,mnemonic,string,that,contains,12,words
```

Note that the passphrase **must** be the same passphrase used to initially generate the mnemonic. The password can be different.

### Get the current address

To get the current address of the wallet run the following command:

```bash
brambl-cli wallet current-address --walletdb $WALLET
```

This will output the current address of the wallet.

### Create a simple transaction

To create a simple transaction to spend the genesis block run the following command:

```bash
brambl-cli simpletransaction create --from-party noparty --from-contract genesis --from-state 1 -t ptetP7jshHVRuMURLWzn5RNsEBPth1CParqz5Rug99R4m1pjFN9BrChgbHCY -w test -p 9091 -o $TX_FILE -n private -a 100 -h localhost -i $MAIN_KEY --walletdb $WALLET
```

This will create a transaction that spends the genesis block and sends 100 polys to the address `ptetP7jshHVRuMURLWzn5RNsEBPth1CParqz5Rug99R4m1pjFN9BrChgbHCY`. The transaction will be stored in the file `$TX_FILE`.

Alternatively, instead of providing an output address, the party and contract of the output can be used instead. To do this, run the following command:

```bash
brambl-cli simpletransaction create --from-party noparty --from-contract genesis --from-state 1 --from-party self --from-contract default -w test -p 9091 -o $TX_FILE -n private -a 100 -h localhost -i $MAIN_KEY --walletdb $WALLET
```

### Prove a simple transaction

To prove a simple transaction run the following command:

```bash
brambl-cli simpletransaction prove -w test --keyfile $MAIN_KEY -n private -i $TX_FILE -o $TX_PROVED_FILE --walletdb $WALLET
```

This will prove the transaction in the file `$TX_FILE` and store the result in the file `$TX_PROVED_FILE`. The right indexes to derive the keys are taken from the wallet database.

### Broadcast a simple transaction

To broadcast a simple transaction run the following command:

```bash
brambl-cli broadcast -n private -i $TX_PROVED_FILE -h localhost --bifrost-port 9084 --walletdb $WALLET
```

This will broadcast the transaction in the file `$TX_PROVED_FILE` to the network.

### Query a block by id

To query a block by id run the following command:

```bash
brambl-cli bifrost-query block-by-id --block-id $BLOCK_ID -h localhost --bifrost-port 9084
```

This will query the block with id `$BLOCK_ID` from the bifrost node running on `localhost` on port `9084`.

### Query a block by height

To query a block by height run the following command:

```bash
brambl-cli bifrost-query block-by-height --height $HEIGHT -h localhost --bifrost-port 9084
```

This will query the block with height `$HEIGHT` from the bifrost node running on `localhost` on port `9084`.

### Query a transaction by id

To query a transaction by id run the following command:

```bash
brambl-cli bifrost-query transaction-by-id --transaction-id $TX_ID -h localhost --bifrost-port 9084
```

This will query the transaction with id `$TX_ID` from the bifrost node running on `localhost` on port `9084`.

### Query UXTO by address

To query UXTOs by address run the following command:

```bash
brambl-cli genus-query utxo-by-address --from-party self --from-contract default -h localhost --bifrost-port 9084 --walletdb $WALLET
```

This will query the UXTOs for the address in the genus node. It uses the wallet to derive the right address to query.

### Add a new party

To add a new party to the wallet run the following command:

```bash
brambl-cli parties add --party-name $PARTY_NAME --walletdb $WALLET
```

### List all parties

To list all parties in the wallet run the following command:

```bash
brambl-cli parties list --walletdb $WALLET
```

### Add a new contract

To add a new contract to the wallet run the following command:

```bash
brambl-cli contracts add --walletdb $WALLET --contract-name $CONTRACT_NAME --contract-template $CONTRACT_TEMPLATE
```

### List all contracts

To list all contracts in the wallet run the following command:

```bash
brambl-cli contracts list --walletdb $WALLET
```

### Export a base verification key

To export a base verification key run the following command:

```bash
brambl-cli wallet export-vk -w test -o $OUTPUT_FILE --walletdb $WALLET --party-name $PARTY_NAME --contract-name $CONTRACT_NAME --keyfile $KEYFILE -n private
```

This will export the base verification key for the party `$PARTY_NAME` and contract `$CONTRACT_NAME` to the file `$OUTPUT_FILE`. The keyfile `$KEYFILE` is used to derive the exported key.

This command is also used to export a final verification key. To do this, use the `--state` option to specify the state from which to export the key.

### Import a base verification key

To import one or many base verification keys run the following command:

```bash
brambl-cli wallet import-vks --input-vks $BASE_VK_1,$BASE_VK_2 --party-name $PARTY_NAME --contract-name $CONTRACT_NAME -n private --walletdb $WALLET
```

### Sync the wallet

To sync the wallet run the following command:

```bash
brambl-cli wallet sync --contract-name $CONTRACT_NAME --party-name $PARTY_NAME --walletdb $WALLET -n private -h localhost --bifrost-port 9084 --keyfile $KEYFILE -w $PASSWORD
```

This will sync the wallet for the party `$PARTY_NAME` and contract `$CONTRACT_NAME` with the bifrost node running on `localhost` on port `9084`. The keyfile `$KEYFILE` is used to derive keys. The password for the wallet is `$PASSWORD`.

### Create a transaction from a file

To create a transaction from a file run the following command:

```bash
brambl-cli tx create -i $INPUT_FILE --bifrost-port 9084 -o $OUTPUT_FILE -n private -h localhost
```

This will create a transaction from the file `$INPUT_FILE` and store the result in the file `$OUTPUT_FILE`.

#### Example of format

A file to move the input from a height lock contract to a new address would look like this:

```yaml
network: private

keys: []

inputs:
  - address: 7exK7vSMd6aCYqiiZ1VjWSYLif98zHxsQgtqaRM3WvAc#1
    keyMap: []
    proposition: threshold(1, height(1, 9223372036854775807))
    value: 10000000
outputs:
  - address: ptetP7jshHUxEn3noNHnfU5AhV8AcifVAWkhYYvXvrjfErEsey686BBukpQm
    value: 10000000
```

A file to move the input from a single signature lock contract to multiple addresses would look like this:

```yaml
network: private

keys: 
  - id: alice
    vk: GeMD3jTdwPEpABPksjFYGgU9tLebpTbqiEvwF7Yyi5jHUBUXhtfsMRUVc5zE6fbL8FYrTDVNRt7eWwrbQMZuwswVP1zpWq8X8r

inputs:
  - address: 6YKBJePhf48a2kCdrov69v9NoiFAuLT7isthazhVBuu1#0
    keyMap:
     - index: 0
       identifier: alice
    proposition: threshold(1, sign(0))
    value: 10000000
outputs:
  - address: ptetP7jshHUxEn3noNHnfU5AhV8AcifVAWkhYYvXvrjfErEsey686BBukpQm 
    value: 9998000
  - address: ptetP7jshHV4h1qe2uSoaKYiM618wKHbpjwadc7HxEGiDQ85MCMzoFF6oh6R 
    value: 1000
  - address: ptetP7jshHUiwiZvm48Z1ebrARr36cxAKTKfhETfRKXomouZafiRadz2m7jp
    value: 1000
```

A file to move two different UTXOs protected by different locks and different
signatures to a single address would look like this:

```yaml
network: private

keys: 
  - id: aliceAnd
    vk: GeMD3jTedVsewW98Cin4Ksgtce784bwpnpcGZDw9wT9WxCaq3PFKo7zRGzaY6ycCcZeB7Sibdsi8DYdYR3u8go9C6W8Wq6K2TS
  - id: bobAnd
    vk: GeMD3jTdf9P5qLDw8PLzSJR77jXVcPBZfZVKYMWdZfr9urzDPvCemfdLfRHSNPUqL9hVokQTK4eYfVki5bLAtfoEFbeTU61zAY
  - id: aliceOr
    vk: GeMD3jTejkvMtBxhrZo3cgDv7g8tUaJ8QaXJocaz9jS5Re4faHQYhU6RDoimtXUwuFGcMccp4jPdJHY6R3GeKpZ5VvHF25cin3

inputs:
  - address: 3WiAub289RrnFA5rdr5wouTdEbqoef2rHWe6edygXeUL#1
    keyMap:
     - index: 0
       identifier: aliceAnd
     - index: 1
       identifier: bobAnd
    proposition: threshold(1, sign(0) and sign(1))
    value: 1000
  - address: 3WiAub289RrnFA5rdr5wouTdEbqoef2rHWe6edygXeUL#2
    keyMap:
     - index: 0
       identifier: aliceOr
     - index: 1
       identifier: aliceOr
    proposition: threshold(1, sign(0) or sign(1))
    value: 1000
outputs:
  - address: ptetP7jshHUaBVrsqnn3bhtWQ1kugVGJVYsVS4WZ47AthWeL4ZX9B9ZJNTaw 
    value: 2000
```
