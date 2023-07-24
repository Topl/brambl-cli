package co.topl.brambl.cli

import cats.effect.{ExitCode, IO, Sync}
import cats.implicits.toTraverseOps
import co.topl.brambl.builders.TransactionBuilderApi
import co.topl.brambl.cli.modules.{GenusQueryAlgebraModule, TransactionBuilderApiModule, WalletStateAlgebraModule}
import co.topl.brambl.codecs.AddressCodecs
import co.topl.brambl.dataApi.{GenusQueryAlgebra, WalletStateAlgebra}
import co.topl.brambl.constants.NetworkConstants
import co.topl.brambl.models.LockAddress
import co.topl.brambl.models.box.Lock
import co.topl.brambl.models.transaction.IoTransaction
import co.topl.genus.services.Txo
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

  private def txFileLocation(label: String): String = s"$TMP_DIR/teardown_$label.pbuf"

  def tearDown(walletKeyConfig: WalletKeyConfig): IO[ExitCode] = {
    val genus = genusQueryAlgebra(HOST, BIFROST_PORT)
    val walletState = walletStateAlgebra(walletKeyConfig.walletFile, NetworkConstants.PRIVATE_NETWORK_ID)
    for {
      changeAmount <- getChangeAmount(genus, walletState)
      _ <- IO.println(s"Creating teardown transaction: Moving change ($changeAmount LVLs) back to genesis")
      _ <- assertIO(
        createSimpleTransactionToCartesianIdx(
          FROM_PARTY,
          FROM_CONTRACT,
          None,
          TO_PARTY,
          TO_CONTRACT,
          changeAmount.toInt,
          txFileLocation("raw")
        ).run(walletKeyConfig),
        ExitCode.Success
      )
      _ <- IO.sleep(5.seconds)
      _ <- IO.println(s"Proving teardown transaction")
      _ <- assertIO(
        proveSimpleTransaction(
          FROM_PARTY,
          FROM_CONTRACT,
          None,
          txFileLocation("raw"),
          txFileLocation("proved")
        ).run(walletKeyConfig),
        ExitCode.Success
      )
      _ <- IO.sleep(5.seconds)
      _ <- IO.println(s"Broadcasting teardown transaction")
      _ <- assertIO(
        broadcastSimpleTx(txFileLocation("proved"), walletKeyConfig.walletFile),
        ExitCode.Success
      )
      _ <- IO.sleep(5.seconds)
      _ <- IO.println(s"Check $TO_PARTY & $TO_CONTRACT Transaction on the node")
      res <- IO.asyncForIO.timeout(
        (for {
          queryRes <- queryAccount(TO_PARTY, TO_CONTRACT, Some(1)).run(walletKeyConfig)
          _ <- IO.sleep(5.seconds)
        } yield queryRes)
          .iterateUntil(_ == ExitCode.Success),
        60.seconds
      )
    } yield res
  }

  private def getChangeAmount(
    genus: GenusQueryAlgebra[IO],
    walletState: WalletStateAlgebra[IO]
  ): IO[BigInt] = for {
    address <- walletState.getCurrentAddress
    txos <- genus.queryUtxo(AddressCodecs.decodeAddress(address).toOption.get)
    lvlTxos <- Sync[IO].delay(txos.filter(_.transactionOutput.value.value.isLvl))
  } yield lvlTxos
    .foldLeft(BigInt(0))((acc, x) =>
      acc + x.transactionOutput.value.value.lvl
        .map(y => BigInt(y.quantity.value.toByteArray))
        .getOrElse(BigInt(0))
    )
}
