# Brambl CLI

The Brambl CLI is a command line interface to the Topl platform. It is a simple tool to interact with the Topl platform.

## System Requirements

Your system needs to have the following software installed.

- A Java Virtual Machine (JVM).- The bifrost-daml-broker is a Java application, and thus Java is needed.
- [Coursier](https://get-coursier.io/docs/cli-installation).- A simple command line tool (CLI) to
to run Java applications without any setup. It is very easy to install.

## Using the CLI

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