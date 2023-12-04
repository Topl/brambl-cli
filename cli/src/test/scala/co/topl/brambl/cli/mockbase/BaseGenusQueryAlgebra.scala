package co.topl.brambl.cli.mockbase

import co.topl.brambl.dataApi.GenusQueryAlgebra
import co.topl.brambl.models.LockAddress
import co.topl.genus.services.{Txo, TxoState}

class BaseGenusQueryAlgebra[F[_]] extends GenusQueryAlgebra[F] {

  override def queryUtxo(
      fromAddress: LockAddress,
      txoState: TxoState
  ): F[Seq[Txo]] = ???

}
