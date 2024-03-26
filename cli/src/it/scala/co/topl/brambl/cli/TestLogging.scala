package co.topl.brambl.cli

import cats.effect.IO
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

trait TestLogging {

  protected implicit val logger: Logger[IO] =
    Slf4jLogger.getLoggerFromClass(this.getClass)

}
