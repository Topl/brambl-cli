package co.topl.brambl.cli.controllers

import munit.CatsEffectSuite
import co.topl.brambl.cli.impl.GroupPolicyParserModule
import co.topl.brambl.cli.modules.SimpleMintingAlgebraModule
import co.topl.brambl.constants.NetworkConstants
import co.topl.brambl.cli.modules.DummyObjects
import co.topl.brambl.cli.impl.SimpleMintingAlgebra
import cats.effect.IO

class SimpleMintingControllerSpec
    extends CatsEffectSuite
    with GroupPolicyParserModule
    with SimpleMintingAlgebraModule
    with DummyObjects {

  val controllerUnderTest = new SimpleMintingController(
    groupPolicyParserAlgebra(NetworkConstants.PRIVATE_NETWORK_ID),
    simpleMintingAlgebra()
  )

  def simpleMintingAlgebra(
  ) = SimpleMintingAlgebra.make[IO](
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
      Left("Error parsing group policy: DecodingFailure(Attempt to decode value on failed cursor, List(DownField(registrationUtxo)))")
    )
  }
}
