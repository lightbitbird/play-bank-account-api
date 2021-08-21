package services

import com.google.inject.ImplementedBy
import commons.{Deposit, TransferFrom, TransferTo, Withdrawal}
import models.{Balance, Month, Transaction, TransactionHistory}
import repositories.{AccountRepository, TransactionRepository}

import java.sql.Timestamp
import java.time.temporal.TemporalAdjusters
import java.time.{OffsetDateTime, ZoneOffset}
import javax.inject.{Inject, Singleton}

@ImplementedBy(classOf[TransactionServiceImpl])
trait TransactionService {
  def getHistories(accountNumber: String, startAt: Timestamp, endAt: Timestamp): Map[String, Seq[Transaction]]

  def getMonthHistories(accountNumber: String, transactionAt: Timestamp): TransactionHistory

  def getAmountsByMonth(accountNumber: String, startAt: Timestamp, endAt: Timestamp, types: Seq[Short]): Map[String, Map[Short, Long]]

  def getAmountsByMonth(accountNumber: String, startAt: Timestamp, endAt: Timestamp): Map[String, Map[Short, Long]]

  def getMonthlyAmountsByType(accountNumber: String, transactionAt: Timestamp, types: Seq[Short]): Map[String, Map[String, Long]]

  def getMonthlyAmountsByType(accountNumber: String, transactionAt: Timestamp): Map[String, Map[String, Long]]

  def findByAccountNumber(accountNumber: String): Option[Transaction]

  def findAll: Seq[Transaction]

  def add(t: Transaction): Int

  def updateBalance(accountNumber: String, amount: Long): Int
}

@Singleton
class TransactionServiceImpl @Inject()(repository: TransactionRepository, accountRepository: AccountRepository) extends TransactionService {
  override def getHistories(accountNumber: String, startAt: Timestamp, endAt: Timestamp): Map[String, Seq[Transaction]] = repository.getHistories(accountNumber, startAt, endAt)

  override def getMonthHistories(accountNumber: String, transactionAt: Timestamp): TransactionHistory = {
    val histories = repository.getMonthHistories(accountNumber, transactionAt)
    val totals = getMonthlyAmountsByType(accountNumber, transactionAt)
    val localDt = transactionAt.toLocalDateTime
    val yearMonth = s"${localDt.getYear}${String.format("%02d", localDt.getMonthValue)}"
    var withdrawal = 0L
    for {
      w <- totals.filter(t => t._1 == Withdrawal.name || t._1 == TransferFrom.name)
      a <- w._2 if a._1 == yearMonth
    } yield withdrawal += a._2

    var deposit = 0L
    for {
      d <- totals.filter(t => t._1 == Deposit.name || t._1 == TransferTo.name)
      a <- d._2 if a._1 == yearMonth
    } yield deposit += a._2

    val month = Month(transactionAt.toLocalDateTime.getMonth.name(), histories._1)
    TransactionHistory("Monthly", Balance(deposit = deposit, withdrawal = withdrawal, total = histories._2), month, totals, histories._3, histories._4)
  }

  override def getAmountsByMonth(accountNumber: String, startAt: Timestamp, endAt: Timestamp, types: Seq[Short]): Map[String, Map[Short, Long]] =
    repository.getAmountsByMonth(accountNumber, startAt, endAt, types)

  override def getAmountsByMonth(accountNumber: String, startAt: Timestamp, endAt: Timestamp): Map[String, Map[Short, Long]] =
    getAmountsByMonth(accountNumber, startAt, endAt, Seq[Short](Withdrawal.value, Deposit.value, TransferFrom.value, TransferTo.value))

  override def getMonthlyAmountsByType(accountNumber: String, transactionAt: Timestamp, types: Seq[Short]): Map[String, Map[String, Long]] = {
    val temp = TemporalAdjusters.firstDayOfMonth()
    val offsetDatetime = OffsetDateTime.now().plusMonths(1).`with`(temp)
    val before12Months = offsetDatetime.minusMonths(12).`with`(temp)
    val timestamp = Timestamp.valueOf(offsetDatetime.atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime
      .withDayOfMonth(1).withHour(1).withMinute(1).withSecond(1).withNano(0))
    val tmBefore12Months = Timestamp.valueOf(before12Months.atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime
      .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0))

    repository.getMonthlyAmountsByType(accountNumber, tmBefore12Months, timestamp, Seq[Short](Withdrawal.value, Deposit.value, TransferFrom.value, TransferTo.value))
  }

  override def getMonthlyAmountsByType(accountNumber: String, transactionAt: Timestamp): Map[String, Map[String, Long]] =
    getMonthlyAmountsByType(accountNumber, transactionAt, Seq[Short](Withdrawal.value, Deposit.value, TransferFrom.value, TransferTo.value))

  override def findByAccountNumber(accountNumber: String): Option[Transaction] = repository.findByAccountNumber(accountNumber)

  override def findAll: Seq[Transaction] = repository.findAll

  override def add(t: Transaction): Int = {
    val info = repository.add(t)
    updateBalance(t.accountNumber, info._2)
  }

  override def updateBalance(accountNumber: String, amount: Long): Int = {
    accountRepository.updateBalance(accountNumber, amount)
  }
}
