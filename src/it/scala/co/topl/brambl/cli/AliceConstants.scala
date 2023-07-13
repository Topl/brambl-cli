package co.topl.brambl.cli

trait AliceConstants extends BaseConstants {

  val ALICE_WALLET = s"$TMP_DIR/alice_wallet.db"

  val ALICE_MAIN_KEY = s"$TMP_DIR/alice_mainkey.json"

  val ALICE_PASSWORD = "test"

  val ALICE_FIRST_TX_RAW = s"$TMP_DIR/alice_first_tx.pbuf"

  val ALICE_FIRST_TX_PROVED = s"$TMP_DIR/alice_first_tx_proved.pbuf"

  val ALICE_VK = s"$TMP_DIR/alice_vk.json"

  val ALICE_SECOND_TX_RAW = s"$TMP_DIR/alice_second_tx.pbuf"

  val ALICE_SECOND_TX_PROVED = s"$TMP_DIR/alice_second_tx_proved.pbuf"

  val aliceContext = WalletKeyConfig(
    ALICE_WALLET,
    ALICE_MAIN_KEY,
    ALICE_PASSWORD
  )
}
