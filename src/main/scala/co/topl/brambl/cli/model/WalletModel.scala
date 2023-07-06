package co.topl.brambl.cli.model

case class WalletEntity(
    idx: Int,
    name: String
)

case class WalletContract(
    idx: Int,
    name: String,
    lockTemplate: String
)
