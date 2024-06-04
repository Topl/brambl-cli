---
sidebar_position: 1
---
# Getting Started

The Brambl CLI is a command line interface to the Apparatus platform. It is a simple tool to interact with the platform.

## System Requirements

Your system needs to have the following software installed.

- A Java Virtual Machine (JVM).- The brambl-cli is a Java application, and thus Java is needed.
- [Coursier](https://get-coursier.io/docs/cli-installation).- A simple command line tool (CLI) to run Java applications without any setup. It is very easy to install.
- Just go to the [Coursier installation page](https://get-coursier.io/docs/cli-installation) and follow the instructions for your operating system.
- Once you have it, you can check it like this:
 
```shell
$ cd csInstallPath
$ ./cs version  
2.1.6
```

## Using the CLI

- There is no need to clone a repository to use the CLI, you can launch a release version of the CLI using coursier directly.
- You can check releases on [sonatype](https://s01.oss.sonatype.org/content/repositories/releases/co/topl/brambl-cli_2.13/) or at the [releases page](https://github.com/Topl/brambl-cli/releases) on Github.
- Then just launch your coursier application

```shell
$ cd csInstallPath
$ ./cs launch -r https://s01.oss.sonatype.org/content/repositories/releases co.topl:brambl-cli_2.13:2.0.0-beta0 -- bifrost-query block-by-height --height 1 -h localhost --port 9084  
```
