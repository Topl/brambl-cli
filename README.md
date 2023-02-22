# Brambl CLI

The Brambl CLI is a command line interface to the Topl platform. It is a simple tool to interact with the Topl platform.

## System Requirements

Your system needs to have the following software installed.

- A Java Virtual Machine (JVM).- The bifrost-daml-broker is a Java application, and thus Java is needed.
- [Coursier](https://get-coursier.io/docs/cli-installation).- A simple command line tool (CLI) to
to run Java applications without any setup. It is very easy to install.

## Using the CLI

```
Usage:  [transaction|wallet] [options]

  -n, --network <value>    the Topl network to connect to, one of: main, valhalla, and private
  -u, --network-uri <value>
                           the URI of the network
  -a, --topl-api-key <value>
                           the API key for the Topl network
Command: transaction [broadcast|create] [options]
Transaction mode
Command: transaction broadcast
Broadcast a transaction
  -i, --input-file <value>
                           the input file
Command: transaction create
Create a new transaction
  -o, --output-file <value>
                           the output file
  -f, --from-addresses <value>
                           the address(es) to send from
  --token <value>          the token that we are sending, possible values: poly
  -t, --to-addresses <value>
                           the address(es) to send to
  -c, --change-address <value>
                           the address to send change to
  -e, --fee <value>        the fee to pay
Command: wallet [sign|create|balance] [options]
Wallet mode
Command: wallet sign
Sign transaction
  --token <value>          the token that we are sending, possible values: poly
  -o, --output-file <value>
                           the outputfile
  -i, --input-file <value>
                           the outputfile
  -p, --password <value>   the password for the keyfile
  -k, --keyfile <value>    the file that contains the operator key, for example keyfile.json
Command: wallet create
Create a new wallet
  -o, --output-file <value>
                           the outputfile
  -p, --password <value>   the password for the keyfile
  -k, --keyfile <value>    the file that contains the operator key, for example keyfile.json
Command: wallet balance
Check balance of a wallet
  -f, --from-addresses <value>
                           the address(es) from which we get the balances
```

### Create a keyfile

To create a keyfile for the valhalla network, with password `test` and to store it in the file `the_new_keyfile.json`, run the following command:

```bash
cs launch co.topl:brambl-cli_2.13:1.0.0.beta-1 -- wallet create -n valhalla  -o the_new_keyfile.json -p test
```

### Create an unsigned transaction

To create a transaction from address `3NKUzcXsbZfZmCQnxMoMMTafDqhvCv3m8pYAFTiARSZdgBg6iohL` to address `3NKm1dopH5z9ECdpy2SuuKx6rrSdA9XpJ4EXA199MgcxFPvhEW96` for 100 polys, and 200 polys to `3NK75Pqk5uEWS17om3AQAsS6ctvNoFphx2QFzbPbPVm8YavaQN8g`, storing the rest of the funds in address `3NLNWXD7thdQbPxusw1otDeacUEa1gCB2GpWKRD8BARLYzr39HSA` for the valhalla network:

```bash
cs launch co.topl:brambl-cli_2.13:1.0.0.beta-1 -- transaction create --token poly -n valhalla -f 3NKUzcXsbZfZmCQnxMoMMTafDqhvCv3m8pYAFTiARSZdgBg6iohL -t 3NKm1dopH5z9ECdpy2SuuKx6rrSdA9XpJ4EXA199MgcxFPvhEW96=100,3NK75Pqk5uEWS17om3AQAsS6ctvNoFphx2QFzbPbPVm8YavaQN8g=200 -c 3NLNWXD7thdQbPxusw1otDeacUEa1gCB2GpWKRD8BARLYzr39HSA -e 100 -u https://vertx.topl.services/valhalla/<projectId> -a $API_TOKEN
```

The command outputs the unsigned transaction in JSON format to the console. You can pipe the output to a file, or use the `-o` flag to specify the output file.

### Sign a poly transaction

To sign a poly transaction, you need to have a keyfile. To sign the transaction created in the previous step, run the following command:

```bash
cs launch co.topl:brambl-cli_2.13:1.0.0.beta-1 -- wallet sign --token poly -n valhalla  -k keyfile.json -p $WALLET_PASSWORD
```

The input is taken from stdin, and the output is written to stdout. You can pipe the output of the previous command to the input of this command. You can also use the `-i` flag to specify the input file and the `-o` flag to specify the output file.

### Broadcast a poly transaction

To broadcast the transaction created in the previous step, run the following command:

```bash
cs launch co.topl:brambl-cli_2.13:1.0.0.beta-1 transaction broadcast --token poly -n private -u http://localhost:9085 -i test_transaction_signed.json
```

### Get the poly balance for a set of addresses

To get the poly balance for a set of addresses, run the following command:

```bash
cs launch co.topl:brambl-cli_2.13:1.0.0.beta-1 wallet balance -n private -f AUAFAWju3tDYw1jeGX7zbT4oUdUgHzim8E2dVxuGg3HLpPdohrGB
```