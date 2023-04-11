package co.topl.brambl.cli



object BramlblCli {
  trait BramblCliAlgebra[F[_]] {

    def createWallet(password: String, someOutputFile: Option[String]): F[Unit]

    def createUnsignedPolyTransfer(
        someOutputFile: Option[String],
        fromAddresses: Seq[String],
        toAddresses: Seq[(String, Int)],
        changeAddress: String,
        fee: Int
    ): F[Unit]

    def signTransferPoly(
        keyFile: String,
        password: String,
        someOutputFile: Option[String],
        someInputFile: Option[String]
    ): F[Unit]

    def broadcastPolyTransfer(
        someInputFile: Option[String]
    ): F[Unit]

    def balancePolys(
        addresses: Seq[String],
        someOutputFile: Option[String]
    ): F[Unit]

  }

  object BramblCliInterpreter

  // def makeIO(
  //     provider: Provider,
  //     keyRing: KeyRing[PrivateKeyCurve25519, KeyfileCurve25519]
  // ): BramlblCli.BramblCliAlgebra[IO] =
  //   new BramlblCli.BramblCliAlgebra[IO] {
  //     import provider._

}
