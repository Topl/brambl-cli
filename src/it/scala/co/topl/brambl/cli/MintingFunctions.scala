package co.topl.brambl.cli

import cats.effect.ExitCode
import cats.effect.IO
import co.topl.brambl.codecs.AddressCodecs.decodeAddress
import co.topl.brambl.utils.Encoding
import munit.CatsEffectSuite

trait MintingFunctions extends PolicyTemplates {

  self: CatsEffectSuite
    with CommonTxOperations
    with AliceConstants
    with BobConstants =>

  import scala.concurrent.duration._

  def mintGroup(secure: Boolean = false) = for {
    _ <- IO.println("Crate a group minting policy")
    ALICE_CURRENT_ADDRESS <- walletController(ALICE_WALLET)
      .currentaddress("self", "default", None)
    utxos <- genusQueryAlgebra
      .queryUtxo(
        decodeAddress(ALICE_CURRENT_ADDRESS.get).toOption.get
      )
      .map(_.filter(_.transactionOutput.value.value.isLvl))
    lvlUtxos = utxos.filter(_.transactionOutput.value.value.isLvl)
    _ <- IO.println(s"Alice's address is $ALICE_CURRENT_ADDRESS")
    aliceUtxoAddress = Encoding.encodeToBase58(
      lvlUtxos.head.outputAddress.id.value.toByteArray
    ) + "#" + lvlUtxos.head.outputAddress.index.toString
    _ <- createAliceGroupPolicy(
      ALICE_FIRST_GROUP_POLICY,
      "Alice Group",
      aliceUtxoAddress
    )
    _ <- assertIO(
      createSimpleGroupMintingTransaction(
        "self",
        "default",
        None,
        1,
        100,
        ALICE_FIRST_GROUP_POLICY,
        ALICE_FIRST_GROUP_MINTING_TX, 
        secure
      ).run(aliceContext),
      ExitCode.Success
    )
    _ <- assertIO(
      proveSimpleTransaction(
        ALICE_FIRST_GROUP_MINTING_TX,
        ALICE_FIRST_GROUP_MINTING_TX_PROVED
      ).run(aliceContext),
      ExitCode.Success
    )
    _ <- assertIO(
      broadcastSimpleTx(ALICE_FIRST_GROUP_MINTING_TX_PROVED, secure),
      ExitCode.Success
    )
    _ <- IO.println(
      "Check change  account for from alice's wallet, group tokens"
    )
    res <- IO.asyncForIO.timeout(
      (for {
        _ <- IO.println("Querying alice's change account")
        queryRes <- queryAccountGroupTokens("self", "default", None, secure).run(
          aliceContext
        )
        _ <- IO.sleep(5.seconds)
      } yield queryRes)
        .iterateUntil(_ == ExitCode.Success),
      60.seconds
    )
  } yield res

  def mintSeries(secure: Boolean = false) = for {
    _ <- IO.println("Crate a series minting policy")
    ALICE_CURRENT_ADDRESS <- walletController(ALICE_WALLET)
      .currentaddress("self", "default", None)
    utxos <- genusQueryAlgebra
      .queryUtxo(
        decodeAddress(ALICE_CURRENT_ADDRESS.get).toOption.get
      )
      .map(_.filter(_.transactionOutput.value.value.isLvl))
    lvlUtxos = utxos.filter(_.transactionOutput.value.value.isLvl)
    _ <- IO.println(s"Alice's address is $ALICE_CURRENT_ADDRESS")
    aliceUtxoAddress = Encoding.encodeToBase58(
      lvlUtxos.head.outputAddress.id.value.toByteArray
    ) + "#" + lvlUtxos.head.outputAddress.index.toString
    _ <- createAliceSeriesPolicy(
      ALICE_FIRST_SERIES_POLICY,
      "Alice Series",
      "group-and-series",
      "liquid",
      aliceUtxoAddress
    )
    _ <- assertIO(
      createSimpleSeriesMintingTransaction(
        "self",
        "default",
        None,
        1,
        100,
        ALICE_FIRST_SERIES_POLICY,
        ALICE_FIRST_SERIES_MINTING_TX,
        secure
      ).run(aliceContext),
      ExitCode.Success
    )
    _ <- assertIO(
      proveSimpleTransaction(
        ALICE_FIRST_SERIES_MINTING_TX,
        ALICE_FIRST_SERIES_MINTING_TX_PROVED
      ).run(aliceContext),
      ExitCode.Success
    )
    _ <- assertIO(
      broadcastSimpleTx(ALICE_FIRST_SERIES_MINTING_TX_PROVED, secure),
      ExitCode.Success
    )
    _ <- IO.println(
      "Check change  account for from alice's wallet, expected group and series tokens"
    )
    res <- IO.asyncForIO.timeout(
      (for {
        _ <- IO.println("Querying alice's change account")
        queryRes <- queryAccountSeriesTokens("self", "default", None, secure).run(
          aliceContext
        )
        _ <- IO.sleep(5.seconds)
      } yield queryRes)
        .iterateUntil(_ == ExitCode.Success),
      60.seconds
    )
  } yield res

  def mintAsset(secure: Boolean = false) = for {
    _ <- IO.println("Crate an asset minting statement")
    ALICE_CURRENT_ADDRESS <- walletController(ALICE_WALLET)
      .currentaddress("self", "default", None)
    utxos <- genusQueryAlgebra
      .queryUtxo(
        decodeAddress(ALICE_CURRENT_ADDRESS.get).toOption.get
      )
    groupTokenUtxo = utxos.filter(_.transactionOutput.value.value.isGroup)
    _ <- IO.println(s"Alice's address is $ALICE_CURRENT_ADDRESS")
    groupTokenUtxoAddress = Encoding.encodeToBase58(
      groupTokenUtxo.head.outputAddress.id.value.toByteArray
    ) + "#" + groupTokenUtxo.head.outputAddress.index.toString
    seriesTokenUtxo = utxos.filter(_.transactionOutput.value.value.isSeries)
    _ <- IO.println(s"Alice's address is $ALICE_CURRENT_ADDRESS")
    seriesTokenUtxoAddress = Encoding.encodeToBase58(
      seriesTokenUtxo.head.outputAddress.id.value.toByteArray
    ) + "#" + seriesTokenUtxo.head.outputAddress.index.toString
    _ <- createAliceAssetMintingStatement(
      ALICE_FIRST_ASSET_MINTING_STATEMENT,
      groupTokenUtxoAddress,
      seriesTokenUtxoAddress,
      1000
    )
    _ <- createAliceEphemeralMetadata(
      ALICE_FIRST_ASSET_MINTING_METADATA,
      "http://topl.co",
      "http://topl.co/image.png",
      42
    )
    _ <- assertIO(
      createSimpleAssetMintingTransaction(
        "self",
        "default",
        None,
        100,
        ALICE_FIRST_ASSET_MINTING_STATEMENT,
        ALICE_FIRST_ASSET_MINTING_TX,
        ALICE_FIRST_ASSET_MINTING_METADATA,
        secure
      ).run(aliceContext),
      ExitCode.Success
    )
    _ <- assertIO(
      proveSimpleTransaction(
        ALICE_FIRST_ASSET_MINTING_TX,
        ALICE_FIRST_ASSET_MINTING_TX_PROVED
      ).run(aliceContext),
      ExitCode.Success
    )
    _ <- assertIO(
      broadcastSimpleTx(ALICE_FIRST_ASSET_MINTING_TX_PROVED, secure),
      ExitCode.Success
    )
    _ <- IO.println(
      "Check change  account for from alice's wallet, expected a new asset"
    )
    res <- IO.asyncForIO.timeout(
      (for {
        _ <- IO.println("Querying alice's change account")
        queryRes <- queryAccountAssetTokens("self", "default", None, secure).run(
          aliceContext
        )
        _ <- IO.sleep(5.seconds)
      } yield queryRes)
        .iterateUntil(_ == ExitCode.Success),
      60.seconds
    )
  } yield res

}
