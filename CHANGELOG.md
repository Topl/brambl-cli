# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
## [v2.0.0-alpha5] - 2023-mm-dd (this date should be changed on release)
## [v2.0.0-alpha4](https://github.com/Topl/brambl-cli/releases/tag/v2.0.0-alpha4) - 2023-10-10
### Added

- Dependency to monocle optics library.
- Support for minting group tokens.
- Support for minting series tokens.
- Support for minting asset tokens.
- New command to inspect transactions on disk
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