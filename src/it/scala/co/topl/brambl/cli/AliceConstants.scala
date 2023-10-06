package co.topl.brambl.cli

trait AliceConstants extends BaseConstants {

  val ALICE_WALLET = s"$TMP_DIR/alice_wallet.db"

  val ALICE_MAIN_KEY = s"$TMP_DIR/alice_mainkey.json"

  val ALICE_PASSWORD = "test"

  val ALICE_FIRST_TX_RAW = s"$TMP_DIR/alice_first_tx.pbuf"

  val ALICE_FIRST_COMPLEX_TX = s"$TMP_DIR/alice_first_complex_tx.yaml"

  val ALICE_FIRST_GROUP_POLICY = s"$TMP_DIR/alice_first_group_policy.yaml"

  val ALICE_FIRST_SERIES_POLICY = s"$TMP_DIR/alice_first_series_policy.yaml"

  val ALICE_FIRST_ASSET_MINTING_STATEMENT = s"$TMP_DIR/alice_first_am_statement.yaml"

  val ALICE_FIRST_GROUP_MINTING_TX = s"$TMP_DIR/alice_first_group_minting_tx.pbuf"

  val ALICE_FIRST_SERIES_MINTING_TX = s"$TMP_DIR/alice_first_series_minting_tx.pbuf"

  val ALICE_FIRST_ASSET_MINTING_TX = s"$TMP_DIR/alice_first_asset_minting_tx.pbuf"

  val ALICE_FIRST_ASSET_MINTING_METADATA = s"$TMP_DIR/alice_first_asset_minting_metadata.json"

  val ALICE_FIRST_GROUP_MINTING_TX_PROVED = s"$TMP_DIR/alice_first_group_minting_tx_proved.pbuf"

  val ALICE_FIRST_SERIES_MINTING_TX_PROVED = s"$TMP_DIR/alice_first_series_minting_tx_proved.pbuf"

  val ALICE_FIRST_ASSET_MINTING_TX_PROVED = s"$TMP_DIR/alice_first_asset_minting_tx_proved.pbuf"

  val ALICE_SECOND_COMPLEX_TX = s"$TMP_DIR/alice_second_complex_tx.yaml"

  val ALICE_THIRD_COMPLEX_TX = s"$TMP_DIR/alice_third_complex_tx.yaml"

  val ALICE_FIRST_COMPLEX_TX_RAW = s"$TMP_DIR/alice_first_complex_tx.pbuf"
  
  val ALICE_SECOND_COMPLEX_TX_RAW = s"$TMP_DIR/alice_second_complex_tx.pbuf"

  val ALICE_THIRD_COMPLEX_TX_RAW = s"$TMP_DIR/alice_third_complex_tx.pbuf"

  val ALICE_FIRST_COMPLEX_TX_PROVED = s"$TMP_DIR/alice_first_complex_tx_proved.pbuf"

  val ALICE_FIRST_TX_PROVED = s"$TMP_DIR/alice_first_tx_proved.pbuf"

  val ALICE_VK = s"$TMP_DIR/alice_vk.json"

  val ALICE_COMPLEX_VK_OR = s"$TMP_DIR/alice_complex_vk_or.json"

  val ALICE_COMPLEX_VK_AND = s"$TMP_DIR/alice_complex_vk_and.json"

  val ALICE_FINAL_VK = s"$TMP_DIR/alice_final_vk.json"

  val ALICE_AND_VK = s"$TMP_DIR/alice_and_vk.json"

  val ALICE_OR_VK = s"$TMP_DIR/alice_or_vk.json"

  val ALICE_VK_AND = s"$TMP_DIR/alice_vk_and.json"

  val ALICE_SECOND_TX_RAW = s"$TMP_DIR/alice_second_tx.pbuf"

  val ALICE_THIRD_TX_RAW = s"$TMP_DIR/alice_third_tx.pbuf"

  val ALICE_SECOND_TX_PROVED = s"$TMP_DIR/alice_second_tx_proved.pbuf"

  val ALICE_SECOND_COMPLEX_TX_PROVED = s"$TMP_DIR/alice_second_complex_tx_proved.pbuf"

  val ALICE_THIRD_COMPLEX_TX_PROVED = s"$TMP_DIR/alice_third_complex_tx_proved.pbuf"

  val ALICE_THIRD_COMPLEX_TX_PROVED_BY_BOTH = s"$TMP_DIR/alice_third_complex_tx_proved_by_both.pbuf"

  val ALICE_THIRD_TX_PROVED = s"$TMP_DIR/alice_third_tx_proved.pbuf"

  val ALICE_FOURTH_TX_PROVED = s"$TMP_DIR/alice_fourth_tx_proved.pbuf"

  val ALICE_MNEMONIC = s"$TMP_DIR/alice_mnemonic.txt"

  val aliceContext = WalletKeyConfig(
    ALICE_WALLET,
    ALICE_MAIN_KEY,
    ALICE_PASSWORD,
    ALICE_MNEMONIC
  )
}
