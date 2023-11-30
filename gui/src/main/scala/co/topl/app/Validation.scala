package co.topl.app

import scala.util.Try
import scala.util.Success
import scala.util.Failure

sealed abstract trait EncodingError extends Exception
case object InvalidInputString extends EncodingError
case object InvalidNetwork extends EncodingError
case object InvalidLeger extends EncodingError

object Validation {

  val base58alphabet =
    "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".zipWithIndex

  val idxToCharBase58 = Map(base58alphabet.map(_.swap): _*)

  val charToIdxBase58 = Map(base58alphabet: _*)

  def decodeFromHex(hex: String): Either[EncodingError, Array[Byte]] =
    Try(hex.grouped(2).map(Integer.parseInt(_, 16).toByte).toArray) match {
      case Success(value) => Right(value)
      case Failure(_)     => Left(InvalidInputString)
    }

  def decodeAddress(
      address: String,
      expectedNetwork: Int
  ): Either[EncodingError, String] =
    for {
      byteArray <- decodeFromBase58Check(address)
      (network, ledgerAndId) = byteArray.splitAt(4)
      (ledger, id) = ledgerAndId.splitAt(4)
      _ <- Either.cond(
        BigInt(network).toInt == expectedNetwork,
        (),
        InvalidNetwork
      )
      _ <- Either.cond(
        BigInt(ledger).toInt == 0xe7b07a00,
        (),
        InvalidLeger
      )
    } yield address

  def decodeFromBase58Check(b58: String): Either[EncodingError, Array[Byte]] =
    for {
      _ <- Either.cond(b58.length > 0, (), InvalidInputString)
      decoded <- decodeFromBase58(b58)
      (payload, _) = decoded.splitAt(decoded.length - 4)
    } yield payload

  def decodeFromBase58(b58: String): Either[EncodingError, Array[Byte]] =
    Try({
      val zeroCount = b58.takeWhile(_ == '1').length
      Array.fill(zeroCount)(0.toByte) ++
        b58
          .drop(zeroCount)
          .map(charToIdxBase58)
          .toList
          .foldLeft(BigInt(0))((acc, x) => acc * 58 + x)
          .toByteArray
          .dropWhile(_ == 0.toByte)
    }) match {
      case Success(value) => Right(value)
      case Failure(_)     => Left(InvalidInputString)
    }
}
