package co.topl.brambl.cli.impl

import co.topl.brambl.codecs.AddressCodecs._

trait UtxoOps {

  def getUTXOsByLockAddress(address: String): Unit = {
    // first step is to create the LockAddress object
    val lockAddress = decodeAddress(address)
    // sha256.hash(address)
    // then use the LockAddress object to get the UTXOs
  }

}
