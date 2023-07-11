package co.topl.brambl.cli.modules

import cats.effect.IO
import cats.effect.kernel.Resource

import java.sql.DriverManager

trait WalletResourceModule {
  
  def walletResource(name: String) = Resource
    .make(
      IO.delay(
        DriverManager.getConnection(
          s"jdbc:sqlite:${name}"
        )
      )
    )(conn => IO.delay(conn.close()))
}
