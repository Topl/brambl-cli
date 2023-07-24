package co.topl.brambl.cli.modules

import co.topl.brambl.cli.BramblCliValidatedParams
import cats.effect.IO

trait TxModeModule {

  def txModeSubcmds(
      validateParams: BramblCliValidatedParams
  ): IO[Either[String, String]] = ???

}
