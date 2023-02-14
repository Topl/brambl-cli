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
cs launch co.topl:brambl-cli_2.13:1.0.0 -- wallet create -n valhalla  -o the_new_keyfile.json -p test
```

