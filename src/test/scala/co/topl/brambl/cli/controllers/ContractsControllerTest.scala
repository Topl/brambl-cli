package co.topl.brambl.cli.controllers

import cats.effect.IO
import co.topl.brambl.cli.impl.ContractStorageAlgebra
import co.topl.brambl.cli.model.WalletContract
import munit.CatsEffectSuite

class ContractsControllerTest extends CatsEffectSuite {

  test("Add signature contract") {
    var addedContract = ""
    val simpleController = new ContractsController[IO](
      new ContractStorageAlgebra[IO] {
        override def addContract(
            walletContract: WalletContract
        ): IO[Int] = IO { addedContract = walletContract.lockTemplate } *> IO(1)

        override def findContracts(): IO[List[WalletContract]] =
          IO(List.empty)
      }
    )
    simpleController
      .addContract(
        "myNewContract",
        """threshold(1, sign(0))"""
      )
      .assertEquals(
        "Contract added successfully"
      )
    simpleController
      .addContract(
        "myNewContract",
        """threshold(1, sign(0))"""
      )
      .map(_ => addedContract)
      .assertEquals(
        """{"threshold":1,"innerTemplates":[{"routine":"ExtendedEd25519","entityIdx":0,"type":"signature"}],"type":"predicate"}"""
      )
  }
  test("Add and contract") {
    var addedContract = ""
    val simpleController = new ContractsController[IO](
      new ContractStorageAlgebra[IO] {
        override def addContract(
            walletContract: WalletContract
        ): IO[Int] = IO { addedContract = walletContract.lockTemplate } *> IO(1)

        override def findContracts(): IO[List[WalletContract]] =
          IO(List.empty)
      }
    )
    simpleController
      .addContract(
        "myNewContract",
        """threshold(1, sign(0) and sign(1))"""
      )
      .assertEquals(
        "Contract added successfully"
      )
    simpleController
      .addContract(
        "myNewContract",
        """threshold(1, sign(0) and sign(1))"""
      )
      .map(_ => addedContract)
      .assertEquals(
        """{"threshold":1,"innerTemplates":[{"left":{"routine":"ExtendedEd25519","entityIdx":0,"type":"signature"},"right":{"routine":"ExtendedEd25519","entityIdx":1,"type":"signature"},"type":"and"}],"type":"predicate"}"""
      )
  }
  test("Add or contract") {
    var addedContract = ""
    val simpleController = new ContractsController[IO](
      new ContractStorageAlgebra[IO] {
        override def addContract(
            walletContract: WalletContract
        ): IO[Int] = IO { addedContract = walletContract.lockTemplate } *> IO(1)

        override def findContracts(): IO[List[WalletContract]] =
          IO(List.empty)
      }
    )
    simpleController
      .addContract(
        "myNewContract",
        """threshold(1, sign(0) or sign(1))"""
      )
      .assertEquals(
        "Contract added successfully"
      )
    simpleController
      .addContract(
        "myNewContract",
        """threshold(1, sign(0) or sign(1))"""
      )
      .map(_ => addedContract)
      .assertEquals(
        """{"threshold":1,"innerTemplates":[{"left":{"routine":"ExtendedEd25519","entityIdx":0,"type":"signature"},"right":{"routine":"ExtendedEd25519","entityIdx":1,"type":"signature"},"type":"or"}],"type":"predicate"}"""
      )

  }

  test("List contracts") {
    val simpleController = new ContractsController[IO](
      new ContractStorageAlgebra[IO] {
        override def addContract(
            walletContract: WalletContract
        ): IO[Int] = IO(1)

        override def findContracts(): IO[List[WalletContract]] =
          IO(
            List(
              WalletContract(
                1,
                "sign",
                """{"threshold":1,"innerTemplates":[{"routine":"ExtendedEd25519","entityIdx":0,"type":"signature"}],"type":"predicate"}"""
              ),
              WalletContract(
                2,
                "or",
                """{"threshold":1,"innerTemplates":[{"left":{"routine":"ExtendedEd25519","entityIdx":0,"type":"signature"},"right":{"routine":"ExtendedEd25519","entityIdx":1,"type":"signature"},"type":"or"}],"type":"predicate"}"""
              ),
              WalletContract(
                3,
                "and",
                """{"threshold":1,"innerTemplates":[{"left":{"routine":"ExtendedEd25519","entityIdx":0,"type":"signature"},"right":{"routine":"ExtendedEd25519","entityIdx":1,"type":"signature"},"type":"and"}],"type":"predicate"}"""
              )
            )
          )
      }
    )
    simpleController
      .listContracts()
      .assertEquals(
        "Y Coordinate\tContract Name\tLock Template\n" +
          "1\tsign\t" + """{"threshold":1,"innerTemplates":[{"routine":"ExtendedEd25519","entityIdx":0,"type":"signature"}],"type":"predicate"}""" + "\n" +
          "2\tor\t" + """{"threshold":1,"innerTemplates":[{"left":{"routine":"ExtendedEd25519","entityIdx":0,"type":"signature"},"right":{"routine":"ExtendedEd25519","entityIdx":1,"type":"signature"},"type":"or"}],"type":"predicate"}""" + "\n" +
          "3\tand\t" + """{"threshold":1,"innerTemplates":[{"left":{"routine":"ExtendedEd25519","entityIdx":0,"type":"signature"},"right":{"routine":"ExtendedEd25519","entityIdx":1,"type":"signature"},"type":"and"}],"type":"predicate"}"""
      )
  }

}
