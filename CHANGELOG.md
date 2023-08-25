# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Dependency to monocle optics library.

### Changed

- Update bifrost node for integration tests to `bifrost-node:2.0.0-alpha7`.
- The query-utxo command now supports a new parameter to filter UTXOs by token
types. The valid token types are "lvl", "topl", "asset" and "all". Documentation
was also updated.
- Refactor validation for: walletdb parameter
- The walletdb parameter for wallet init is now `--newwallet` instead of `--walletdb`.
This is because of a problem with the scopt library that forced that change.

### Removed

- Removed unnecessary validations. Sometimes we were valiting that a parameter 
was not there. This was because of a previous architectural choice that does
not apply anymore, hence the removal of the validations.