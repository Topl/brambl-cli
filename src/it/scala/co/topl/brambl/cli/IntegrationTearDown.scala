package co.topl.brambl.cli

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.Sync
import co.topl.brambl.cli.modules.GenusQueryAlgebraModule
import co.topl.brambl.cli.modules.TransactionBuilderApiModule
import co.topl.brambl.cli.modules.WalletStateAlgebraModule
import co.topl.brambl.codecs.AddressCodecs
import co.topl.brambl.dataApi.GenusQueryAlgebra
import co.topl.brambl.dataApi.WalletStateAlgebra
import munit.CatsEffectAssertions.assertIO

import scala.concurrent.duration.DurationInt

trait IntegrationTearDown
    extends BaseConstants
    with GenusQueryAlgebraModule
    with WalletStateAlgebraModule
    with TransactionBuilderApiModule
    with CommonTxOperations {

  val TO_PARTY = "noparty"
  val TO_CONTRACT = "genesis"
  val FROM_PARTY = "self"
  val FROM_CONTRACT = "default"

  private def txFileLocation(label: String): String =
    s"$TMP_DIR/teardown_$label.pbuf"

  def tearDown(walletKeyConfig: WalletKeyConfig): IO[ExitCode] = {
    val genus = genusQueryAlgebra(HOST, BIFROST_PORT)
    val walletState = walletStateAlgebra(walletKeyConfig.walletFile)
    for {
      changeAmount <- getChangeAmount(genus, walletState)
      _ <- IO.println(
        s"Creating teardown transaction: Moving change ($changeAmount LVLs) back to genesis"
      )
      _ <- assertIO(
        createSimpleTransactionToCartesianIdx(
          FROM_PARTY,
          FROM_CONTRACT,
          None,
          TO_PARTY,
          TO_CONTRACT,
          changeAmount.toInt - BASE_FEE,
          BASE_FEE,
          txFileLocation("raw"),
          TokenType.lvl,
          None,
          None
        ).run(walletKeyConfig),
        ExitCode.Success
      )
      _ <- IO.println(s"Proving teardown transaction")
      _ <- assertIO(
        proveSimpleTransaction(
          txFileLocation("raw"),
          txFileLocation("proved")
        ).run(walletKeyConfig),
        ExitCode.Success
      )
      _ <- IO.println(s"Broadcasting teardown transaction")
      _ <- assertIO(
        broadcastSimpleTx(txFileLocation("proved")),
        ExitCode.Success
      )
      _ <- IO.println(s"Check $TO_PARTY & $TO_CONTRACT Transaction on the node")
      _ <- IO.asyncForIO.timeout(
        (for {
          queryRes <- getGenesisAmount(genus, walletState)
          _ <- IO.sleep(5.seconds)
          _ <- queryAccount(TO_PARTY, TO_CONTRACT, Some(1))
            .run(walletKeyConfig)
        } yield queryRes)
          .iterateUntil(_ > 10000),
        60.seconds
      )
    } yield ExitCode.Success
  }

  private def getChangeAmount(
      genus: GenusQueryAlgebra[IO],
      walletState: WalletStateAlgebra[IO]
  ): IO[BigInt] = for {
    address <- walletState.getCurrentAddress
    txos <- genus.queryUtxo(AddressCodecs.decodeAddress(address).toOption.get)
    lvlTxos <- Sync[IO].delay(
      txos.filter(_.transactionOutput.value.value.isLvl)
    )
  } yield lvlTxos
    .foldLeft(BigInt(0))((acc, x) =>
      acc + x.transactionOutput.value.value.lvl
        .map(y => BigInt(y.quantity.value.toByteArray))
        .getOrElse(BigInt(0))
    )

  private def getGenesisAmount(
      genus: GenusQueryAlgebra[IO],
      walletState: WalletStateAlgebra[IO]
  ): IO[BigInt] = for {
    address <- walletState.getAddress("noparty", "genesis", Some(1))
    txos <- genus.queryUtxo(AddressCodecs.decodeAddress(address.get).toOption.get)
    lvlTxos <- Sync[IO].delay(
      txos.filter(_.transactionOutput.value.value.isLvl)
    )
  } yield lvlTxos
    .foldLeft(BigInt(0))((acc, x) =>
      acc + x.transactionOutput.value.value.lvl
        .map(y => BigInt(y.quantity.value.toByteArray))
        .getOrElse(BigInt(0))
    )
}
