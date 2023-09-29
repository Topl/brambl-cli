package co.topl.brambl.cli.impl

import cats.effect.IO

trait SeriesPolicyParserModule {

  def seriesPolicyParserAlgebra(networkId: Int) =
    SeriesPolicyParser.make[IO](networkId)

}
