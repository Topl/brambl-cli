package co.topl.brambl.cli

trait BobConstants extends BaseConstants {

  val BOB_WALLET = s"$TMP_DIR/bob_wallet.db"

  val BOB_MAIN_KEY = s"$TMP_DIR/bob_mainkey.json"

  val BOB_PASSWORD = "test"

  val BOB_FIRST_TX_RAW = s"$TMP_DIR/bob_first_tx.pbuf"

  val BOB_FIRST_TX_PROVED = s"$TMP_DIR/bob_first_tx_proved.pbuf"

  val BOB_SECOND_TX_RAW = s"$TMP_DIR/bob_second_tx.pbuf"

  val BOB_SECOND_TX_PROVED = s"$TMP_DIR/bob_second_tx_proved.pbuf"

  val BOB_THIRD_TX_RAW = s"$TMP_DIR/bob_third_tx.pbuf"

  val BOB_THIRD_TX_PROVED = s"$TMP_DIR/bob_third_tx_proved.pbuf"

  val BOB_VK = s"$TMP_DIR/bob_vk.json"

  val BOB_AND_VK = s"$TMP_DIR/bob_and_vk.json"

  val BOB_COMPLEX_VK_OR = s"$TMP_DIR/bob_complex_vk_or.json"

  val BOB_COMPLEX_VK_AND = s"$TMP_DIR/bob_complex_vk_and.json"

  val BOB_VK_AND = s"$TMP_DIR/bob_vk_and.json"

  val BOB_MNEMONIC = s"$TMP_DIR/bob_mnemonic.txt"

  val bobContext = WalletKeyConfig(
    BOB_WALLET,
    BOB_MAIN_KEY,
    BOB_PASSWORD,
    BOB_MNEMONIC
  )

}
