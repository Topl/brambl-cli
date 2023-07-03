package co.topl.brambl.cli.controllers

import cats.Id
import co.topl.brambl.cli.impl.PartyStorageAlgebra
import co.topl.brambl.cli.model.WalletEntity
import munit.FunSuite

class PartiesControllerTest extends FunSuite {

  test("Party controller allows adding parties") {
    val controller = new PartiesController[Id](
      new PartyStorageAlgebra[Id] {
        override def addParty(
            walletEntity: WalletEntity
        ): Id[Int] = 1

        override def findParties(): Id[List[WalletEntity]] =
          List.empty
      }
    )
    assertEquals(
      controller.addParty("myNewParty"),
      "Party myNewParty added successfully"
    )
  }

  test("List party allows listing parties") {
    val controller = new PartiesController[Id](
      new PartyStorageAlgebra[Id] {
        override def addParty(
            walletEntity: WalletEntity
        ): Id[Int] = 1

        override def findParties(): Id[List[WalletEntity]] =
          List(
            WalletEntity(1, "party1"),
            WalletEntity(2, "party2")
          )
      }
    )
    assertEquals(
      controller.listParties(),
      "X Coordinate\tParty Name\n" +
        "1\tparty1\n" +
        "2\tparty2".stripMargin
    )
  }

}
