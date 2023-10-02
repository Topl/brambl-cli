package co.topl.brambl.cli.controllers

import cats.effect.IO
import cats.effect.kernel.Sync
import co.topl.brambl.cli.impl.AssetStatementParserModule
import co.topl.brambl.cli.impl.GroupPolicyParserModule
import co.topl.brambl.cli.impl.SeriesPolicyParserModule
import co.topl.brambl.cli.impl.SimpleMintingAlgebra
import co.topl.brambl.cli.modules.DummyObjects
import co.topl.brambl.cli.modules.SimpleMintingAlgebraModule
import co.topl.brambl.constants.NetworkConstants
import munit.CatsEffectSuite

class SimpleMintingControllerSpec
    extends CatsEffectSuite
    with GroupPolicyParserModule
    with SeriesPolicyParserModule
    with AssetStatementParserModule
    with SimpleMintingAlgebraModule
    with DummyObjects {

  val controllerUnderTest = new SimpleMintingController(
    groupPolicyParserAlgebra(NetworkConstants.PRIVATE_NETWORK_ID),
    seriesPolicyParserAlgebra(NetworkConstants.PRIVATE_NETWORK_ID),
    assetMintingStatementParserAlgebra(NetworkConstants.PRIVATE_NETWORK_ID),
    simpleMintingAlgebra()
  )

  def simpleMintingAlgebra(
  ) = SimpleMintingAlgebra.make[IO](
    Sync[IO],
    walletApi,
    walletStateAlgebra("src/test/resources/wallet.db"),
    walletManagementUtils,
    transactionBuilderApi(
      NetworkConstants.PRIVATE_NETWORK_ID,
      NetworkConstants.MAIN_LEDGER_ID
    ),
    makeGenusQueryAlgebraMockWithAddress
  )

  test(
    "createSimpleGroupMintingTransactionFromParams should create a minting transaction"
  ) {
    assertIO(
      controllerUnderTest.createSimpleGroupMintingTransactionFromParams(
        "src/test/resources/valid_group_policy.yaml",
        "src/test/resources/keyfile.json",
        "test",
        "self",
        "default",
        None,
        1L,
        100,
        "target/transaction.pbuf"
      ),
      Right("Transaction successfully created")
    )
  }

  test(
    "createSimpleSeriesMintingTransactionFromParams should create a minting transaction"
  ) {
    assertIO(
      controllerUnderTest.createSimpleSeriesMintingTransactionFromParams(
        "src/test/resources/valid_series_policy.yaml",
        "src/test/resources/keyfile.json",
        "test",
        "self",
        "default",
        None,
        1L,
        100,
        "target/transaction_series_mint.pbuf"
      ),
      Right("Transaction successfully created")
    )
  }

  test(
    "createSimpleSeriesMintingTransactionFromParams should elegantly fail if the policy file is invalid: quantity descriptor"
  ) {
    assertIO(
      controllerUnderTest.createSimpleSeriesMintingTransactionFromParams(
        "src/test/resources/invalid_series_policy_quantity_descriptor.yaml",
        "src/test/resources/keyfile.json",
        "test",
        "self",
        "default",
        None,
        1L,
        100,
        "target/transaction_series_mint.pbuf"
      ),
      Left("Error parsing series policy: Invalid quantity descriptor: standard")
    )
  }

  test(
    "createSimpleSeriesMintingTransactionFromParams should elegantly fail if the policy file is invalid: fungibility"
  ) {
    assertIO(
      controllerUnderTest.createSimpleSeriesMintingTransactionFromParams(
        "src/test/resources/invalid_series_policy_fungibility.yaml",
        "src/test/resources/keyfile.json",
        "test",
        "self",
        "default",
        None,
        1L,
        100,
        "target/transaction_series_mint.pbuf"
      ),
      Left("Error parsing series policy: Invalid fungibility: fungus")
    )
  }

  test(
    "createSimpleGroupMintingTransactionFromParams should elegantly fail if the policy file is invalid"
  ) {
    assertIO(
      controllerUnderTest.createSimpleGroupMintingTransactionFromParams(
        "src/test/resources/invalid_group_policy.yaml",
        "src/test/resources/keyfile.json",
        "test",
        "self",
        "default",
        None,
        1L,
        100,
        "target/transaction.pbuf"
      ),
      Left(
        "Error parsing group policy: DecodingFailure(Attempt to decode value on failed cursor, List(DownField(registrationUtxo)))"
      )
    )
  }
}
