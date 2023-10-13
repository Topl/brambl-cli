package co.topl.brambl.cli

import cats.effect.ExitCode
import munit.CatsEffectSuite

import scala.concurrent.duration.Duration

class MintingTests
    extends CatsEffectSuite
    with CommonFunctions
    with MintingFunctions
    with CommonTxOperations
    with AliceConstants
    with BobConstants
    with IntegrationTearDown
    with PolicyTemplates {

  override val munitTimeout = Duration(180, "s")

  tmpDirectory.test("Move funds from genesis to alice") { _ =>
    assertIO(
      moveFundsFromGenesisToAlice(),
      ExitCode.Success
    )
  }

  test("Use alice's funds to mint a group") {
    assertIO(
      mintGroup(),
      ExitCode.Success
    )
  }

  test("Use alice's funds to mint a series") {
    assertIO(
      mintSeries(),
      ExitCode.Success
    )
  }

  test("Use alice's funds to mint an asset") {
    assertIO(
      mintAsset(),
      ExitCode.Success
    )
  }

  test("Send Wallet Change back to HeightLock") {
    assertIO(
      tearDown(aliceContext),
      ExitCode.Success
    )
  }

}
