---
sidebar_position: 6
---

# Transaction Mode

```  
Command: tx [inspect|broadcast|prove|create]
Transaction mode
Command: tx inspect [options]
Inspect transaction
  -i, --input <value>      The input file. (mandatory)
Command: tx broadcast [options]
Broadcast transaction
  -n, --network <value>    Network name: Possible values: mainnet, testnet, private. (mandatory)
  -h, --host <value>       The host of the node. (mandatory)
  --port <value>           Port Bifrost node. (mandatory)
  -s, --secure <value>     Enables the secure connection to the node. (optional)
  -i, --input <value>      The input file. (mandatory)
Command: tx prove [options]
Prove transaction
  -k, --keyfile <value>    The key file.
  -w, --password <value>   Password for the encrypted key. (mandatory)
  --walletdb <value>       Wallet DB file. (mandatory)
  -o, --output <value>     The output file. (mandatory)
  -i, --input <value>      The input file. (mandatory)
Command: tx create [options]
Create transaction
  -n, --network <value>    Network name: Possible values: mainnet, testnet, private. (mandatory)
  -h, --host <value>       The host of the node. (mandatory)
  --port <value>           Port Bifrost node. (mandatory)
  -s, --secure <value>     Enables the secure connection to the node. (optional)
  -o, --output <value>     The output file. (mandatory)
  -i, --input <value>      The input file. (mandatory)
```
