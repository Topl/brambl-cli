---
sidebar_position: 7
---

# Simple Transaction Mode

```  
Command: simple-transaction [create]
Simple transaction mode
Command: simple-transaction create [options]
Create transaction
  --from-party <value>     Party where we are sending the funds from
  --from-contract <value>  Contract where we are sending the funds from
  --from-state <value>     State from where we are sending the funds from
  -n, --network <value>    Network name: Possible values: mainnet, testnet, private. (mandatory)
  -h, --host <value>       The host of the node. (mandatory)
  --port <value>           Port Bifrost node. (mandatory)
  -k, --keyfile <value>    The key file.
  -w, --password <value>   Password for the encrypted key. (mandatory)
  --walletdb <value>       Wallet DB file. (mandatory)
  -o, --output <value>     The output file. (mandatory)
  --fee <value>            Fee paid for the transaction
  -t, --to <value>         Address to send LVLs to. (mandatory if to-party and to-contract are not provided)
  --to-party <value>       Party to send LVLs to. (mandatory if to is not provided)
  --to-contract <value>    Contract to send LVLs to. (mandatory if to is not provided)
  -a, --amount <value>     Amount to send
  --transfer-token <value>
                           The token type. The valid token types are 'lvl', 'asset', 'group', 'series'.
  --group-id <value>       Group id.
  --series-id <value>      Series id.
```
