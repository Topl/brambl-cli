package co.topl.brambl.cli.controllers

import cats.Id
import co.topl.brambl.dataApi.{PartyStorageAlgebra, WalletEntity}
import munit.FunSuite

class PartiesControllerSpec extends FunSuite {

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
      Right("Party myNewParty added successfully")
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
      Right(
        "X Coordinate\tParty Name\n" +
          "1\tparty1\n" +
          "2\tparty2".stripMargin
      )
    )
  }

}
