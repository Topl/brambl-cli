package co.topl.brambl.cli

case class WalletKeyConfig(
    walletFile: String,
    keyFile: String,
    password: String,
    mnemonicFile: String
)

trait BaseConstants {

  val TMP_DIR = "./tmp"

  val BASE_AMOUNT = 1000

  val HOST = "localhost"

  val BIFROST_PORT = 9084

}
