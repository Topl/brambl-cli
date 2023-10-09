---
sidebar_position: 8
---

# Simple Minting Mode

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
  --port <value>   Port Bifrost node. (mandatory)
  -k, --keyfile <value>    The key file.
  -w, --password <value>   Password for the encrypted key. (mandatory)
  --walletdb <value>       Wallet DB file. (mandatory)
  -o, --output <value>     The output file. (mandatory)
  -i, --input <value>      The input file. (mandatory)
  -a, --amount <value>     Amount to send or mint
  --fee <value>            Fee paid for the transaction
  --mint-token <value>     The token type. The valid token types are 'asset', 'group', and 'series'
```
