# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Removed

- Removed unnecessary validations. Sometimes we were valiting that a parameter 
was not there. This was because of a previous architectural choice that does
not apply anymore, hence the removal of the validations.