package co.topl.brambl.cli.controllers

import cats.effect.IO
import co.topl.brambl.cli.mockbase.BaseBifrostQueryAlgebra
import co.topl.brambl.models.transaction.IoTransaction
import co.topl.consensus.models.BlockId
import co.topl.node.models.BlockBody
import munit.CatsEffectSuite
import co.topl.brambl.cli.modules.DummyObjects
import co.topl.brambl.cli.views.BlockDisplayOps
import co.topl.brambl.models.TransactionId
import co.topl.consensus.models.BlockHeader

class BifrostQueryControllerSpec extends CatsEffectSuite with DummyObjects {

  test("blockByHeight should return error when block is not there") {
    val bifrostQueryController = new BifrostQueryController[IO](
      new BaseBifrostQueryAlgebra[IO] {

        override def blockByHeight(
            height: Long
        ): IO[Option[(BlockId, BlockHeader, BlockBody, Seq[IoTransaction])]] =
          IO(None)

      }
    )
    bifrostQueryController
      .blockByHeight(1)
      .assertEquals(
        Left("No blocks found at that height")
      )
  }
  test("blockByHeight should display a block when it is there") {
    val bifrostQueryController = new BifrostQueryController[IO](
      new BaseBifrostQueryAlgebra[IO] {

        override def blockByHeight(
            height: Long
        ): IO[Option[(BlockId, BlockHeader, BlockBody, Seq[IoTransaction])]] =
          IO(
            Some((blockId01, blockHeader01, blockBody01, Seq(iotransaction01)))
          )

      }
    )
    bifrostQueryController
      .blockByHeight(1)
      .assertEquals(
        Right(BlockDisplayOps.display(blockId01, Seq(iotransaction01)))
      )
  }

  test("blockById should return error when block is not there") {
    val bifrostQueryController = new BifrostQueryController[IO](
      new BaseBifrostQueryAlgebra[IO] {

        override def blockById(
            blockId: BlockId
        ): IO[Option[(BlockId, BlockHeader, BlockBody, Seq[IoTransaction])]] =
          IO(None)

      }
    )
    bifrostQueryController
      .blockById("A7k6tpK25N5ZvmjkYn8jN6CnP8u9aNheT9cYb7ZjS3PN")
      .assertEquals(
        Left("No blocks found at that block id")
      )
  }

  test("blockById should display a block when it is there") {
    val bifrostQueryController = new BifrostQueryController[IO](
      new BaseBifrostQueryAlgebra[IO] {

        override def blockById(
            blockId: BlockId
        ): IO[Option[(BlockId, BlockHeader, BlockBody, Seq[IoTransaction])]] =
          IO(
            Some((blockId01, blockHeader01, blockBody01, Seq(iotransaction01)))
          )

      }
    )
    bifrostQueryController
      .blockById("A7k6tpK25N5ZvmjkYn8jN6CnP8u9aNheT9cYb7ZjS3PN")
      .assertEquals(
        Right(BlockDisplayOps.display(blockId01, Seq(iotransaction01)))
      )
  }

  test("fetchTransaction should return error when transaction is not there") {
    val bifrostQueryController = new BifrostQueryController[IO](
      new BaseBifrostQueryAlgebra[IO] {

        override def fetchTransaction(
            txId: TransactionId
        ): IO[Option[IoTransaction]] = IO(None)

      }
    )
    bifrostQueryController
      .fetchTransaction("A7k6tpK25N5ZvmjkYn8jN6CnP8u9aNheT9cYb7ZjS3PN")
      .assertEquals(
        Left(
          "No transaction found with id A7k6tpK25N5ZvmjkYn8jN6CnP8u9aNheT9cYb7ZjS3PN"
        )
      )
  }

  test("fetchTransaction should display a transaction when it is there") {
    val bifrostQueryController = new BifrostQueryController[IO](
      new BaseBifrostQueryAlgebra[IO] {

        override def fetchTransaction(
            txId: TransactionId
        ): IO[Option[IoTransaction]] = IO(Some(iotransaction01))

      }
    )
    bifrostQueryController
      .fetchTransaction("A7k6tpK25N5ZvmjkYn8jN6CnP8u9aNheT9cYb7ZjS3PN")
      .assertEquals(
        Right(BlockDisplayOps.display(iotransaction01))
      )
  }

}
