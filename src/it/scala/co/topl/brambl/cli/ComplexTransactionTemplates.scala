package co.topl.brambl.cli

import cats.effect.kernel.Resource
import cats.effect.IO
import java.io.PrintWriter

trait ComplexTransactionTemplates {

  def utxos(genesisBlockAddresses: List[(String, Long)]) =
    genesisBlockAddresses.map(e => s"""
        |  - address: ${e._1}
        |    keyMap: []
        |    proposition: threshold(1, height(1, 9223372036854775807))
        |    value: ${e._2}
        """)

  def genesisToAddressTxTemplate(
      genesisBlockAddresses: List[(String, Long)],
      genesisAmount: Long,
      address: String
  ) =
    s"""|network: private
        |
        |keys: []
        |
        |inputs:
        |${utxos(genesisBlockAddresses).mkString("\n")}
        |outputs:
        |  - address: $address
        |    value: $genesisAmount""".stripMargin

  def createComplexTxFileFromGenesisToAlice(
      fileName: String,
      genesisBlockAddresses: List[(String, Long)],
      genesisAmount: Long,
      address: String
  ) = {
    Resource.make(IO(new PrintWriter(fileName)))(f => IO(f.close)).use { file =>
      IO(
        file.write(
          genesisToAddressTxTemplate(
            genesisBlockAddresses,
            genesisAmount,
            address
          )
        )
      )
    }
  }

  def createComplexTxFileFromAliceToAliceBobAndOr(
      fileName: String,
      aliceUtxoAddress: String,
      aliceKey: String,
      initialAmount: Long,
      addressAliceChange: String,
      addressAliceBobAnd: String,
      addressAliceEveAnd: String
  ) = {
    Resource.make(IO(new PrintWriter(fileName)))(f => IO(f.close)).use { file =>
      IO(
        file.write(
          aliceToSharedTxTemplate(
            aliceUtxoAddress,
            aliceKey,
            initialAmount,
            addressAliceChange,
            addressAliceBobAnd,
            addressAliceEveAnd
          )
        )
      )
    }
  }

  def aliceToSharedTxTemplate(
      aliceUtxoAddress: String,
      aliceKey: String,
      genesisAmount: Long,
      addressAliceChange: String,
      addressAliceBobOr: String,
      addressAliceBobAnd: String
  ) =
    s"""|network: private
        |
        |keys: 
        |  - id: alice
        |    vk: $aliceKey
        |
        |inputs:
        |  - address: $aliceUtxoAddress
        |    keyMap:
        |     - index: 0
        |       identifier: alice
        |    proposition: threshold(1, sign(0))
        |    value: $genesisAmount
        |outputs:
        |  - address: $addressAliceChange 
        |    value: ${genesisAmount - 2000}
        |  - address: $addressAliceBobAnd 
        |    value: 1000
        |  - address: $addressAliceBobOr
        |    value: 1000""".stripMargin

  def createSharedTemplatesToBob(
      fileName: String,
      andUtxoAddress: String,
      orUtxoAddress: String,
      aliceAndKey: String,
      bobAndKey: String,
      aliceOrKey: String,
      andAmount: Long,
      orAmount: Long,
      addressBob: String
  ) = {
    Resource.make(IO(new PrintWriter(fileName)))(f => IO(f.close)).use { file =>
      IO(
        file.write(
          sharedTemplatesToBob(
            andUtxoAddress,
            orUtxoAddress,
            aliceAndKey,
            bobAndKey,
            aliceOrKey,
            andAmount,
            orAmount,
            addressBob
          )
        )
      )
    }
  }

  def sharedTemplatesToBob(
      andUtxoAddress: String,
      orUtxoAddress: String,
      aliceAndKey: String,
      bobAndKey: String,
      aliceOrKey: String,
      andAmount: Long,
      orAmount: Long,
      addressBob: String
  ) =
    s"""|network: private
        |
        |keys: 
        |  - id: aliceAnd
        |    vk: $aliceAndKey
        |  - id: bobAnd
        |    vk: $bobAndKey
        |  - id: aliceOr
        |    vk: $aliceOrKey
        |
        |inputs:
        |  - address: $andUtxoAddress
        |    keyMap:
        |     - index: 0
        |       identifier: aliceAnd
        |     - index: 1
        |       identifier: bobAnd
        |    proposition: threshold(1, sign(0) and sign(1))
        |    value: $andAmount
        |  - address: $orUtxoAddress
        |    keyMap:
        |     - index: 0
        |       identifier: aliceOr
        |     - index: 1
        |       identifier: aliceOr
        |    proposition: threshold(1, sign(0) or sign(1))
        |    value: $orAmount
        |outputs:
        |  - address: $addressBob 
        |    value: ${andAmount + orAmount}""".stripMargin

}
