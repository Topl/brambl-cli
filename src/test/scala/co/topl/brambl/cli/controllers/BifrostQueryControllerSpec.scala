package co.topl.brambl.cli.controllers

import cats.effect.IO
import co.topl.brambl.cli.mockbase.BaseBifrostQueryAlgebra
import co.topl.brambl.models.transaction.IoTransaction
import co.topl.consensus.models.BlockId
import co.topl.node.models.BlockBody
import munit.CatsEffectSuite
import co.topl.brambl.cli.modules.DummyObjects
import co.topl.brambl.cli.views.BlockDisplayOps

class BifrostQueryControllerSpec extends CatsEffectSuite with DummyObjects {

  test("blockByHeight should return error when block is not there") {
    val bifrostQueryController = new BifrostQueryController[IO](
      new BaseBifrostQueryAlgebra[IO] {

        override def blockByHeight(
            height: Long
        ): IO[Option[(BlockId, BlockBody, Seq[IoTransaction])]] = IO(None)

      }
    )
    bifrostQueryController
      .blockByHeight(1)
      .assertEquals(
        "No blocks found at that height"
      )
  }
  test("blockByHeight should display a block when it is there") {
    val bifrostQueryController = new BifrostQueryController[IO](
      new BaseBifrostQueryAlgebra[IO] {

        override def blockByHeight(
            height: Long
        ): IO[Option[(BlockId, BlockBody, Seq[IoTransaction])]] =
          IO(Some((blockId01, blockBody01, Seq(iotransaction01))))

      }
    )
    bifrostQueryController
      .blockByHeight(1)
      .assertEquals(
        BlockDisplayOps.display(blockId01, Seq(iotransaction01))
      )
  }

}
