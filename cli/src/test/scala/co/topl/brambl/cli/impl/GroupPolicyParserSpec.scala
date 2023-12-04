package co.topl.brambl.cli.impl

import munit.CatsEffectSuite
import co.topl.brambl.constants.NetworkConstants
import cats.effect.kernel.Resource
import cats.effect.IO

class GroupPolicyParserSpec
    extends CatsEffectSuite
    with GroupPolicyParserModule {

  test(
    "parseGroupPolicy should support transactions with no fixed series"
  ) {
    val parser = groupPolicyParserAlgebra(NetworkConstants.PRIVATE_NETWORK_ID)
    assertIO(
      parser
        .parseGroupPolicy(
          Resource.make(
            IO.delay(
              scala.io.Source.fromFile(
                "src/test/resources/valid_group_policy.yaml"
              )
            )
          )(source => IO.delay(source.close()))
        )
        .map(policy => policy.toOption.get.fixedSeries),
      None
    )
  }

  test(
    "parseGroupPolicy should support transactions with fixed series"
  ) {
    val parser = groupPolicyParserAlgebra(NetworkConstants.PRIVATE_NETWORK_ID)
    assertIO(
      parser
        .parseGroupPolicy(
          Resource.make(
            IO.delay(
              scala.io.Source.fromFile(
                "src/test/resources/valid_group_policy_fixed_series.yaml"
              )
            )
          )(source => IO.delay(source.close()))
        )
        .map(policy => policy.toOption.isDefined),
      true
    )
  }
  test(
    "parseGroupPolicy should fail if fixed seriesl too large"
  ) {
    val parser = groupPolicyParserAlgebra(NetworkConstants.PRIVATE_NETWORK_ID)
    assertIO(
      parser
        .parseGroupPolicy(
          Resource.make(
            IO.delay(
              scala.io.Source.fromFile(
                "src/test/resources/invalid_group_policy_fixed_series.yaml"
              )
            )
          )(source => IO.delay(source.close()))
        )
        .map(policy => {
          policy
        }),
      Left(
        InvalidHex(
          "The hex string for the series must be 32 bytes long"
        )
      )
    )
  }

}
