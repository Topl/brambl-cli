package co.topl.brambl.cli

trait WalletConstants extends BaseConstants {

  val EMPTY_FILE = s"$TMP_DIR/empty.txt"

  val WALLET = s"$TMP_DIR/wallet_wallet.db"

  val WALLET_MAIN_KEY = s"$TMP_DIR/wallet_mainkey.json"

  val WALLET_MAIN_KEY_RECOVERED = s"$TMP_DIR/wallet_mainkey_2.json"

  val WALLET_PASSWORD = "test"

  val WALLET_FIRST_TX_RAW = s"$TMP_DIR/wallet_first_tx.pbuf"

  val WALLET_FIRST_TX_PROVED = s"$TMP_DIR/wallet_first_tx_proved.pbuf"

  val WALLET_SECOND_TX_RAW = s"$TMP_DIR/wallet_second_tx.pbuf"

  val WALLET_SECOND_TX_PROVED = s"$TMP_DIR/wallet_second_tx_proved.pbuf"

  val WALLET_MNEMONIC = s"$TMP_DIR/wallet_mnemonic.txt"

  val walletContext = WalletKeyConfig(
    WALLET,
    WALLET_MAIN_KEY,
    WALLET_PASSWORD,
    WALLET_MNEMONIC
  )
}
