package co.topl.brambl.cli

import cats.effect.kernel.Resource
import cats.effect.IO
import java.io.PrintWriter

trait ComplexTransactionTemplates {

  def genesisToAddressTxTemplate(
      genesisBlockAddress: String,
      genesisAmount: Long,
      address: String
  ) =
    s"""|network: private
        |
        |keys: []
        |
        |inputs:
        |  - address: $genesisBlockAddress
        |    keyMap: []
        |    proposition: threshold(1, height(1, 9223372036854775807))
        |    value: $genesisAmount
        |outputs:
        |  - address: $address
        |    value: $genesisAmount""".stripMargin

  def createComplexTxFileFromGenesisToAlice(
      fileName: String,
      genesisBlockAddress: String,
      genesisAmount: Long,
      address: String
  ) = {
    Resource.make(IO(new PrintWriter(fileName)))(f => IO(f.close)).use { file =>
      IO(
        file.write(
          genesisToAddressTxTemplate(
            genesisBlockAddress,
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

}
