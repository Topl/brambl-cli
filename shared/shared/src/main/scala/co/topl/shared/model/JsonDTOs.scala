package co.topl.shared.models

case class TxRequest(
    fromFellowship: String,
    fromTemplate: String,
    fromInteraction: Option[String],
    address: String,
    amount: String,
    network: String
)

case class TxResponse(
    value: Either[String, String]
)

case class FellowshipDTO(idx: Int, name: String)

case class TemplateDTO(idx: Int, name: String, lockTemplate: String)

case class BalanceRequestDTO(
    fellowship: String,
    template: String,
    interaction: Option[String]
)

sealed trait BalanceDTO

case class LvlBalance(balance: String) extends BalanceDTO
case class GroupTokenBalanceDTO(id: String, balance: String) extends BalanceDTO
case class SeriesTokenBalanceDTO(id: String, balance: String) extends BalanceDTO
case class AssetTokenBalanceDTO(group: String, series: String, balance: String)
    extends BalanceDTO
case class UnknownBalanceDTO(balance: String) extends BalanceDTO

case class BalanceResponseDTO(
    lvlBalance: String,
    groupBalances: List[GroupTokenBalanceDTO],
    seriesBalances: List[SeriesTokenBalanceDTO],
    assetBalances: List[AssetTokenBalanceDTO]
)

case class SimpleErrorDTO(
    error: String
)