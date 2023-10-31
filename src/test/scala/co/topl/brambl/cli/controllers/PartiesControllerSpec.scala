package co.topl.brambl.cli.controllers

import cats.Id
import co.topl.brambl.dataApi.{PartyStorageAlgebra, WalletEntity}
import munit.FunSuite

class FellowshipsControllerSpec extends FunSuite {

  test("Fellowship controller allows adding fellowships") {
    val controller = new FellowshipsController[Id](
      new PartyStorageAlgebra[Id] {
        override def addParty(
            walletEntity: WalletEntity
        ): Id[Int] = 1

        override def findParties(): Id[List[WalletEntity]] =
          List.empty
      }
    )
    assertEquals(
      controller.addParty("myNewFellowship"),
      Right("Fellowship myNewFellowship added successfully")
    )
  }

  test("List fellowship allows listing fellowships") {
    val controller = new FellowshipsController[Id](
      new PartyStorageAlgebra[Id] {
        override def addParty(
            walletEntity: WalletEntity
        ): Id[Int] = 1

        override def findParties(): Id[List[WalletEntity]] =
          List(
            WalletEntity(1, "fellowship1"),
            WalletEntity(2, "fellowship2")
          )
      }
    )
    assertEquals(
      controller.listFellowships(),
      Right(
        "X Coordinate\tFellowship Name\n" +
          "1\tfellowship1\n" +
          "2\tfellowship2".stripMargin
      )
    )
  }

}
