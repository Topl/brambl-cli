package co.topl.brambl.cli.impl

import cats.Id
import co.topl.brambl.cli.views.WalletModelDisplayOps

class QuivrFastParserSpec extends munit.FunSuite {

  test("Parser should parse a simple predicate") {
    val input = "threshold(1, sign(0))"
    val actual =
      WalletModelDisplayOps.serialize(
        QuivrFastParser.make[Id].parseQuivr(input).toOption.get
      )
    assertEquals(actual, input)
  }

  test("Parser should parse the genesis predicate") {
    val input = "threshold(1, height(1, 9223372036854775807))"
    val actual =
      WalletModelDisplayOps.serialize(
        QuivrFastParser.make[Id].parseQuivr(input).toOption.get
      )
    assertEquals(actual, input)
  }

  test("Parser should parse and") {
    val input = "threshold(1, sign(0) and sign(1))"
    val actual =
      WalletModelDisplayOps.serialize(
        QuivrFastParser.make[Id].parseQuivr(input).toOption.get
      )
    assertEquals(actual, input)
  }

  test("Parser should parse or") {
    val input = "threshold(1, sign(0) or sign(1))"
    val actual =
      WalletModelDisplayOps.serialize(
        QuivrFastParser.make[Id].parseQuivr(input).toOption.get
      )
    assertEquals(actual, input)
  }
  test("Parser should parse tick") {
    val input = "threshold(1, tick(1, 100))"
    val actual =
      WalletModelDisplayOps.serialize(
        QuivrFastParser.make[Id].parseQuivr(input).toOption.get
      )
    assertEquals(actual, input)
  }
  test("Parser should parse locked no data") {
    val input = "threshold(1, locked())"
    val actual =
      WalletModelDisplayOps.serialize(
        QuivrFastParser.make[Id].parseQuivr(input).toOption.get
      )
    assertEquals(actual, input)
  }
  test("Parser should parse locked with data") {
    val input = "threshold(1, locked(72k1xXWG59fYdzSNoA))"
    val actual =
      WalletModelDisplayOps.serialize(
        QuivrFastParser.make[Id].parseQuivr(input).toOption.get
      )
    assertEquals(actual, input)
  }
  test("Parser should parse locked digest") {
    val input = "threshold(1, digest(6TcbSYWweHnZgEY2oVopiUue6xbZAE1NTkq77u8uFvD8))"
    val actual =
      WalletModelDisplayOps.serialize(
        QuivrFastParser.make[Id].parseQuivr(input).toOption.get
      )
    assertEquals(actual, input)
  }
  test("Parser should parse and and or") {
    val input = "threshold(1, sign(0) or sign(1) and sign(2))"
    val expected = "threshold(1, (sign(0) or sign(1)) and sign(2))"
    val actual =
      WalletModelDisplayOps.serialize(
        QuivrFastParser.make[Id].parseQuivr(input).toOption.get
      )
    assertEquals(actual, expected)
  }
  test("Parser should parse threshold with several params") {
    val input = "threshold(3, sign(0), sign(1), sign(2))"
    val actual =
      WalletModelDisplayOps.serialize(
        QuivrFastParser.make[Id].parseQuivr(input).toOption.get
      )
    assertEquals(actual, input)
  }
  test("Parser should parse and and or with parens") {
    val input = "threshold(1, sign(0) or (sign(1) and height(300, 400)))"
    val actual =
      WalletModelDisplayOps.serialize(
        QuivrFastParser.make[Id].parseQuivr(input).toOption.get
      )
    assertEquals(actual, input)
  }

}
