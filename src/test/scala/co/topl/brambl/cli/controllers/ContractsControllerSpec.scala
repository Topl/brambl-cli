package co.topl.brambl.cli.controllers

import cats.effect.IO
import co.topl.brambl.dataApi.{TemplateStorageAlgebra, WalletTemplate}
import munit.CatsEffectSuite

class TemplatesControllerSpec extends CatsEffectSuite {

  test("Add signature template") {
    var addedTemplate = ""
    val simpleController = new TemplatesController[IO](
      new TemplateStorageAlgebra[IO] {
        override def addTemplate(
            walletTemplate: WalletTemplate
        ): IO[Int] = IO { addedTemplate = walletTemplate.lockTemplate } *> IO(1)

        override def findTemplates(): IO[List[WalletTemplate]] =
          IO(List.empty)
      }
    )
    simpleController
      .addTemplate(
        "myNewTemplate",
        """threshold(1, sign(0))"""
      )
      .assertEquals(
        Right("Template added successfully")
      )
    simpleController
      .addTemplate(
        "myNewTemplate",
        """threshold(1, sign(0))"""
      )
      .map(_ => addedTemplate)
      .assertEquals(
        """{"threshold":1,"innerTemplates":[{"routine":"ExtendedEd25519","entityIdx":0,"type":"signature"}],"type":"predicate"}"""
      )
  }
  test("Add and template") {
    var addedTemplate = ""
    val simpleController = new TemplatesController[IO](
      new TemplateStorageAlgebra[IO] {
        override def addTemplate(
            walletTemplate: WalletTemplate
        ): IO[Int] = IO { addedTemplate = walletTemplate.lockTemplate } *> IO(1)

        override def findTemplates(): IO[List[WalletTemplate]] =
          IO(List.empty)
      }
    )
    simpleController
      .addTemplate(
        "myNewTemplate",
        """threshold(1, sign(0) and sign(1))"""
      )
      .assertEquals(
        Right("Template added successfully")
      )
    simpleController
      .addTemplate(
        "myNewTemplate",
        """threshold(1, sign(0) and sign(1))"""
      )
      .map(_ => addedTemplate)
      .assertEquals(
        """{"threshold":1,"innerTemplates":[{"left":{"routine":"ExtendedEd25519","entityIdx":0,"type":"signature"},"right":{"routine":"ExtendedEd25519","entityIdx":1,"type":"signature"},"type":"and"}],"type":"predicate"}"""
      )
  }

  test("Add or template") {
    var addedTemplate = ""
    val simpleController = new TemplatesController[IO](
      new TemplateStorageAlgebra[IO] {
        override def addTemplate(
            walletTemplate: WalletTemplate
        ): IO[Int] = IO { addedTemplate = walletTemplate.lockTemplate } *> IO(1)

        override def findTemplates(): IO[List[WalletTemplate]] =
          IO(List.empty)
      }
    )
    simpleController
      .addTemplate(
        "myNewTemplate",
        """threshold(1, sign(0) or sign(1))"""
      )
      .assertEquals(
        Right("Template added successfully")
      )
    simpleController
      .addTemplate(
        "myNewTemplate",
        """threshold(1, sign(0) or sign(1))"""
      )
      .map(_ => addedTemplate)
      .assertEquals(
        """{"threshold":1,"innerTemplates":[{"left":{"routine":"ExtendedEd25519","entityIdx":0,"type":"signature"},"right":{"routine":"ExtendedEd25519","entityIdx":1,"type":"signature"},"type":"or"}],"type":"predicate"}"""
      )

  }
  test("Add lock template empty") {
    var addedTemplate = ""
    val simpleController = new TemplatesController[IO](
      new TemplateStorageAlgebra[IO] {
        override def addTemplate(
            walletTemplate: WalletTemplate
        ): IO[Int] = IO { addedTemplate = walletTemplate.lockTemplate } *> IO(1)

        override def findTemplates(): IO[List[WalletTemplate]] =
          IO(List.empty)
      }
    )
    simpleController
      .addTemplate(
        "myNewTemplate",
        """threshold(1, locked())"""
      )
      .assertEquals(
        Right("Template added successfully")
      )
    simpleController
      .addTemplate(
        "myNewTemplate",
        """threshold(1, locked())"""
      )
      .map(_ => addedTemplate)
      .assertEquals(
        """{"threshold":1,"innerTemplates":[{"type":"locked"}],"type":"predicate"}"""
      )
  }

  test("Add lock template with data") {
    var addedTemplate = ""
    val simpleController = new TemplatesController[IO](
      new TemplateStorageAlgebra[IO] {
        override def addTemplate(
            walletTemplate: WalletTemplate
        ): IO[Int] = IO { addedTemplate = walletTemplate.lockTemplate } *> IO(1)

        override def findTemplates(): IO[List[WalletTemplate]] =
          IO(List.empty)
      }
    )
    simpleController
      .addTemplate(
        "myNewTemplate",
        """threshold(1, locked(72k1xXWG59fYdzSNoA))"""
      )
      .assertEquals(
        Right("Template added successfully")
      )
    simpleController
      .addTemplate(
        "myNewTemplate",
        """threshold(1, locked(72k1xXWG59fYdzSNoA))"""
      )
      .map(_ => addedTemplate)
      .assertEquals(
        """{"threshold":1,"innerTemplates":[{"data":"72k1xXWG59fYdzSNoA","type":"locked"}],"type":"predicate"}"""
      )
  }

  test("Add height template") {
    var addedTemplate = ""
    val simpleController = new TemplatesController[IO](
      new TemplateStorageAlgebra[IO] {
        override def addTemplate(
            walletTemplate: WalletTemplate
        ): IO[Int] = IO { addedTemplate = walletTemplate.lockTemplate } *> IO(1)

        override def findTemplates(): IO[List[WalletTemplate]] =
          IO(List.empty)
      }
    )
    simpleController
      .addTemplate(
        "myNewTemplate",
        """threshold(1, height(1, 1000))"""
      )
      .assertEquals(
        Right("Template added successfully")
      )
    simpleController
      .addTemplate(
        "myNewTemplate",
        """threshold(1, height(1, 1000))"""
      )
      .map(_ => addedTemplate)
      .assertEquals(
        """{"threshold":1,"innerTemplates":[{"chain":"header","min":1,"max":1000,"type":"height"}],"type":"predicate"}"""
      )
  }
  test("Add tick template") {
    var addedTemplate = ""
    val simpleController = new TemplatesController[IO](
      new TemplateStorageAlgebra[IO] {
        override def addTemplate(
            walletTemplate: WalletTemplate
        ): IO[Int] = IO { addedTemplate = walletTemplate.lockTemplate } *> IO(1)

        override def findTemplates(): IO[List[WalletTemplate]] =
          IO(List.empty)
      }
    )
    simpleController
      .addTemplate(
        "myNewTemplate",
        """threshold(1, tick(1, 1000))"""
      )
      .assertEquals(
        Right("Template added successfully")
      )
    simpleController
      .addTemplate(
        "myNewTemplate",
        """threshold(1, tick(1, 1000))"""
      )
      .map(_ => addedTemplate)
      .assertEquals(
        """{"threshold":1,"innerTemplates":[{"min":1,"max":1000,"type":"tick"}],"type":"predicate"}"""
      )
  }

  test("Add digest template") {
    var addedTemplate = ""
    val simpleController = new TemplatesController[IO](
      new TemplateStorageAlgebra[IO] {
        override def addTemplate(
            walletTemplate: WalletTemplate
        ): IO[Int] = IO { addedTemplate = walletTemplate.lockTemplate } *> IO(1)

        override def findTemplates(): IO[List[WalletTemplate]] =
          IO(List.empty)
      }
    )
    simpleController
      .addTemplate(
        "myNewTemplate",
        """threshold(1, digest(6TcbSYWweHnZgEY2oVopiUue6xbZAE1NTkq77u8uFvD8))"""
      )
      .assertEquals(
        Right("Template added successfully")
      )
    simpleController
      .addTemplate(
        "myNewTemplate",
        """threshold(1, digest(6TcbSYWweHnZgEY2oVopiUue6xbZAE1NTkq77u8uFvD8))"""
      )
      .map(_ => addedTemplate)
      .assertEquals(
        """{"threshold":1,"innerTemplates":[{"routine":"Blake2b256","digest":"6TcbSYWweHnZgEY2oVopiUue6xbZAE1NTkq77u8uFvD8","type":"digest"}],"type":"predicate"}"""
      )
  }

  test("List templates") {
    val simpleController = new TemplatesController[IO](
      new TemplateStorageAlgebra[IO] {
        override def addTemplate(
            walletTemplate: WalletTemplate
        ): IO[Int] = IO(1)

        override def findTemplates(): IO[List[WalletTemplate]] =
          IO(
            List(
              WalletTemplate(
                1,
                "sign",
                """{"threshold":1,"innerTemplates":[{"routine":"ExtendedEd25519","entityIdx":0,"type":"signature"}],"type":"predicate"}"""
              ),
              WalletTemplate(
                2,
                "or",
                """{"threshold":1,"innerTemplates":[{"left":{"routine":"ExtendedEd25519","entityIdx":0,"type":"signature"},"right":{"routine":"ExtendedEd25519","entityIdx":1,"type":"signature"},"type":"or"}],"type":"predicate"}"""
              ),
              WalletTemplate(
                3,
                "and",
                """{"threshold":1,"innerTemplates":[{"left":{"routine":"ExtendedEd25519","entityIdx":0,"type":"signature"},"right":{"routine":"ExtendedEd25519","entityIdx":1,"type":"signature"},"type":"and"}],"type":"predicate"}"""
              )
            )
          )
      }
    )
    simpleController
      .listTemplates()
      .assertEquals(
        Right(
          "Y Coordinate\tTemplate Name\tLock Template\n" +
            "1\t\tsign\t\t" + """threshold(1, sign(0))""" + "\n" +
            "2\t\tor\t\t" + """threshold(1, sign(0) or sign(1))""" + "\n" +
            "3\t\tand\t\t" + """threshold(1, sign(0) and sign(1))"""
        )
      )
  }

}
