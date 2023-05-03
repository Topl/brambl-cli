import scala.util.Random
import scala.annotation.tailrec
import scala.math.BigInt

import co.topl.brambl.cli.utils.EncodingUtils._

((116) / 58) % 58

257 / 58

257 % 58

57 + 256

313 % 58

313 / 58

java.lang.Byte.toUnsignedInt(0.toByte)

toBase58(Array(59, 1, 1))

toBase58("Hello World!".getBytes())

fromBase58(toBase58(Array(-59.toByte))).toOption.get.toList

new String(fromBase58(toBase58("Hello World!".getBytes())).toOption.get)

toBase58Check("Hello world!".getBytes())

val yyy = "801e99423a4ed27608a15a2616a2b0e9e52ced330ac530edcc32c8ffc6a526aedd"

toBase58Check(fromHex(yyy).toOption.get)

def nextHex(c: Char) = {
  if (c == 'F') '0'
  else if (c == '9') 'A'
  else (c + 1).toChar
}

def incHex(s: String): String = {
  val (prefix, suffix) = s.splitAt(s.length - 1)
  (if (nextHex(suffix.head) == '0') incHex(prefix) else prefix) + nextHex(
    suffix.head
  )
}
def incHexN(s: String, n: Int): String = {
  if (n == 0) s
  else incHexN(incHex(s), n - 1)
}

incHex("05B76e")

@tailrec
private def findFirstmprv(n: String, iterations: Int): String = {
  val address = fromHex(
    n ++ "E7B07A00" ++ List.fill(32 * 2)("0").mkString
  ).toOption.get
  val hash = toBase58Check(address)
  if (iterations == 0)
    s"by iteration: ${hash.take(8)}, ${toHex(fromBase58Check(hash).toOption.get.take(9))}"
  else if (hash.startsWith("vtetmain"))
    s"${hash.take(8)}, ${toHex(fromBase58Check(hash).toOption.get.take(8))}"
  else if (hash.startsWith("vtetmaio"))
    s"failed, ${toHex(fromBase58Check(hash).toOption.get.take(8))}"
  else {
    findFirstmprv(
      incHexN(
        n,
        1
      ),
      iterations - 1
    )
  }

}

// "p115, 20c02730"
//  "pt1C, 215d0400"
// "pte2, 215ef7e8"

List.fill(32 * 2)("F").mkString

val hexString = Random.nextBytes(32)

val hexStringAsString = "fcd1d7d5495063990f02e7106afe9217707781adda1dc8b966c68546e92de62e"

val address = fromHex(
  "934B1900" ++ "E7B07A00" ++ hexStringAsString
).toOption.get

toBase58Check(address)

// "msQAwoBy, 8a00000000000000"
// "mt13tnpX, 8a0829d800000000"
// "mte1qiJr, 8a10d0b0"
// "mtet3GyC, 8a11048c"
// "p9QWN1cu, 91000000"
//  "pt131NoL, 93423d08"
// "pte4jcHQ, 934ae7c8"
// "ptetNvEB, 934b1900"
// "v6NFyXYy, a34ae7c8"
// "vt147DxN, a5b66660"
//  "vte249Sh, a5bf0d38"
// "vtetLyPH, a5bf412c"
// "mtetmPD5, 8a11054c"
// "mtetmaLc, 8a11054ce0000000"
// "mtetmai1, 8a11054ce76ba100"
// "mtetmain, 8a11054ce7b07a00"
// "vtetD5Tv, a5bf410800000000"


findFirstmprv("A5BF4108", 190)

var n = BigInt("00FFFFFF", 16)
// while (n < BigInt("FFFFFFFF", 10) ) {

// }
