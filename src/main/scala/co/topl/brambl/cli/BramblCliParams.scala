package co.topl.brambl.cli

import java.io.File
import co.topl.client.Provider

final case class BramblCliParams(
    mode: String = "",
    subcmd: String = "",
    somePassword: Option[String] = None,
    networkType: String = "",
    someNetworkUri: Option[String] = None,
    someOutputFile: Option[String] = None,
    someApiKey: Option[String] = None,
    someKeyfile: Option[File] = None
)
final case class BramblCliValidatedParams(
    mode: BramblCliMode.Value,
    subcmd: BramblCliSubCmd.Value,
    provider: Provider,
    password: String,
    outputFile: String,
    someKeyfile: Option[File]
) 