package commons

import java.time.format.DateTimeFormatter

sealed abstract class TransactionType(val name: String, val value: Short) {
  def isDeposit: Boolean = {
    value == 1 || value == 3
  }
  def isTransferFrom: Boolean = {
    value == 2
  }
  def isTransferTo: Boolean = {
    value == 3
  }
}
case object Withdrawal extends TransactionType("withdrawal", 0)
case object Deposit extends TransactionType("deposit", 1)
case object TransferFrom extends TransactionType("transform_from", 2)
case object TransferTo extends TransactionType("transform_to", 3)

sealed abstract class TimeZoneId(val id: String)
case object TimeZoneUTC extends TimeZoneId("UTC")
case object TimeZoneJST extends TimeZoneId("JST")

sealed abstract class Deleted(val value: Int)
case object NotDeleted extends Deleted(0)
case object isDeleted extends Deleted(1)


object DateFormat {
  val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  val DATETIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
  val DATETIME_NO_UNDERSCORE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd")
  val DATETIME_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss"
}
