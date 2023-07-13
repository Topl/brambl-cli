package co.topl.brambl.cli

case class WalletKeyConfig(
    walletFile: String,
    keyFile: String,
    password: String
)

trait BaseConstants {

  val TMP_DIR = "./tmp"

  val BASE_AMOUNT = 1000

}
