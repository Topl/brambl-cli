

# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased] - 2024-mm-dd (this date should be changed on release)

### Added

### Changed

### Removed

## [v2.0.0-beta4] - 2024-04-10
### Added

- Added 2 operations `add-secret` and `get-preimage` to the `wallet` mode.

### Changed

- Updated logback-classic to 1.4.14 to fix security vulnerability CVE-2023-6378.
- Updated sqlite-jdbc to 3.45.0.0 to fix security vulnerability CVE-2023-32697.
- The quivr expression supported to add a template now support the keywords: 
`sha256` and `blake2b`. The `digest` keyword is not supported anymore.
- The digests for `sha256` and `blake2b` use the hexadecimal representation of the
digest instead of the base64 representation.
- The `import-vks` command now supports empty files for the verification key when
the fellowship is a `nofellowship`.
- Migrated the `BravlSc` dependency to `2.0.0-beta3`.
- Migrated the `Bifrost` dependency to `2.0.0-beta3`.
- Added support for multiple assets to the GUI.

## [v2.0.0-beta3] - 2024-01-10

### Changed

- Various bug fixes and improvements

## [v2.0.0-beta1] - 2023-12-05

### Added

- Prove transaction validates the transaction before proving it.
- Broadcast transaction validates transaction before broadcasting it and also
checks that the transaction is valid in the node. 
- Tutorial on how to fund the wallet.
- Tutorial on how to recover the wallet
- Default values for `--from-fellowship`, `--from-template`, `--fellowsip-name` 
and `--template-name` parameters. When not provided, the default values are
`self` for the fellowship and `default` for the template.
- Feature to create transactions using a web gui. The command `server init` now
starts a web server that allows to create transactions using a web interface.
- Update to `BramblSc 2.0.0-beta1`.

### Changed

- The errors don't show the usage anymore. We also show the usage only for
a given mode when no subcomand is given.
- Fixed bugs with change address. 1. Change address was not required with
nofellowship. 2. Change address always requires the three fields: party, contract
and state. 
- Broadcast will not let you broadcast a transaction that will not validate in
node.
- Show nicer error when a lock address passed as parameter is invalid
- Check that the address corresponds to the network when creating simple transactions.
- Error messages are now more user friendly.

## [v2.0.0-beta0] - 2023-11-16

### Added

- Support to set the current interaction of the wallet with the command
`wallet set-current-interaction`.
- Support the list of new interactions with the command `wallet list-interactions`.

### Changed

- Change terminology. Now we use `fellowship` instead of `party` and `template` instead of `contract`,
and `interaction` instead of `state`. Both the code and the documentation were updated.
The CLI parameters also reflect this change.
- BramblSc was updated with the new terminology. Now `noparty` became `nofellowship`.
- Update bifrost node for integration tests to `bifrost-node:2.0.0-alpha10`.
- Changed the display of lock templates in the template list command. Now, it
shows a user friendlier template instead of the JSON representation of the
backend.
- Update to `BramblSc 2.0.0-beta0`.
- Update deployment to deploy documentation only when a tag is created.

## [v2.0.0-alpha5] - 2023-10-24

### Added

- Support to transfer group tokens using the `simple-transaction` command.
- Support to transfer series tokens using the `simple-transaction` command.
- Support to transfer asset tokens using the `simple-transaction` command.
- Add `wallet balance` command to check the balance of an address or of a 
given pair of coordinates given `--from-party`, `--from-contract` and `--from-state` parameters. 
- A new parameter `-s` or `--secure` to all commands that require a host address.
This is necessary to use the TLS over the connection. This is particularly useful
for the test net.
- Add tutorial to create transactions.
- Add tutorial to create asset tokens.

### Changed

- Improve `sbt_checkPR` to be split in several jobs instead of one workflow per 
task. This is done to have dependencies among the tasks and to avoid running
all the tasks when only one is needed.
- Update bifrost node for integration tests to `bifrost-node:2.0.0-alpha9`.
- Modified the `simple-transaction` command so that it now requires two extra parameters: `--fee` and `--transfer-token`. We also add
an optional parameter `--group-id` to specify the group token to transfer.
- Modified the `simple-transaction` command so that it allows to specify the series token to transfer using the parameter `--series-id`.
- Made `--host` and `--port` required. Each time these parameters appear they are required. Some commands presented strange behaviours
because we allowed them to be optional.
- Updated documentation for `simple-transaction` command to include the new parameters.
- Updated reference documentation for the CLI.
- Instead of printing the message "Transaction broadcasted", the `broadcast` 
subcommand now prints the transaction id. This makes is easier to check
if the transaction has already been added to the blockchain. 
- Modify `wallet current-address` to require `--from-party`, `--from-contract` and `--from-state` parameters.
Using this the user can get the address of any address on the wallet.
- Modified `simple-transaction` command so that it now requires the user
to provide the change parameters (`--change-party`, `--change-contract`, `--change-state`)
when the from party is a `noparty`. 
- Update `BramblSc` to `2.0.0-alpha7`.

## [v2.0.0-alpha4] - 2023-10-10
### Added

- Dependency to monocle optics library.
- Support for minting group tokens.
- Support for minting series tokens.
- Support for minting asset tokens.
- New command to inspect transactions on disk.
- Added full documentation in docusaurus. Available [here](https://topl.github.io/brambl-cli).

### Changed

- Update bifrost node for integration tests to `bifrost-node:2.0.0-alpha8`.
- The query-utxo command now supports a new parameter to filter UTXOs by token
types. The valid token types are "lvl", "topl", "asset" and "all". Documentation
was also updated.
- The walletdb parameter for wallet init and recover-keys is now `--newwallet` 
instead of `--walletdb`.
This is because of a problem with the scopt library that forced that change.
- Modified the display of tokens (genus-query) to show the group and series 
tokens and include all the information in the token.
- Change the default behavior of minting so that it moves all the assets as
the group, series and asset tokens are minted.
- The `simple-mint` command now uses the parameter `--mint-amount` instead of
`--amount` to avoid confusion with the amount of the asset to mint.
- The `simpletransaction` command has been renamed to `simple-transaction`.
- The subcomands `prove` and `broadcast` were moved from `simple-transaction`
to `tx` mode.
- The parameter `--bifrost-port` has been renamed to `--port` in all commands. 
- Update dependency to BramblSc 2.0.0-alpha6.

### Removed

- Validation layer. All validation was moved to the scopt DSL. This simplifies
the code and makes it easier to add new commands. It also revealed some issues
in validation of parameters.