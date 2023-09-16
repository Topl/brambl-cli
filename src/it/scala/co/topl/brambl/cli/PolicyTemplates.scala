package co.topl.brambl.cli

import cats.effect.kernel.Resource
import java.io.PrintWriter
import cats.effect.IO

trait PolicyTemplates {

  def basicGroupPolicyTemplate(
      label: String,
      registrationUtxo: String
  ) =
    s"""|label: $label
        |registrationUtxo: $registrationUtxo
    """.stripMargin

  def createAliceGroupPolicy(
      fileName: String,
      label: String,
      utxo: String
  ) = {
    Resource.make(IO(new PrintWriter(fileName)))(f => IO(f.close)).use { file =>
      IO(
        file.write(
          basicGroupPolicyTemplate(
            label,
            utxo
          )
        )
      )
    }
  }

}
