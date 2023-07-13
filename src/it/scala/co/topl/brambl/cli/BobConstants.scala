package co.topl.brambl.cli

trait BobConstants extends BaseConstants {

  val BOB_WALLET = s"$TMP_DIR/bob_wallet.db"

  val BOB_MAIN_KEY = s"$TMP_DIR/bob_mainkey.json"

  val BOB_PASSWORD = "test"

  val BOB_FIRST_TX_RAW = s"$TMP_DIR/bob_first_tx.pbuf"

  val BOB_FIRST_TX_PROVED = s"$TMP_DIR/bob_first_tx_proved.pbuf"

  val BOB_SECOND_TX_RAW = s"$TMP_DIR/bob_second_tx.pbuf"

  val BOB_SECOND_TX_PROVED = s"$TMP_DIR/bob_second_tx_proved.pbuf"

  val BOB_VK = s"$TMP_DIR/bob_vk.json"

  val bobContext = WalletKeyConfig(
    BOB_WALLET,
    BOB_MAIN_KEY,
    BOB_PASSWORD
  )

}
