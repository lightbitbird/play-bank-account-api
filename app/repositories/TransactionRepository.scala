package repositories

import com.google.inject.ImplementedBy
import models.Transaction
import play.api.Configuration
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}

@ImplementedBy(classOf[TransactionRepositoryImpl])
trait TransactionRepository {
  def findAll: Seq[Transaction]
  def findByAccountNumber(accountNumber: String): Option[Transaction]
  def getHistories(accountNumber: String, startAt: Timestamp, endAt: Timestamp): Map[String, Seq[Transaction]]
  def getMonthHistories(accountNumber: String, transactionAt: Timestamp): Seq[Transaction]
  def add(t: Transaction): Unit
}

@Singleton
class TransactionRepositoryImpl @Inject()(config: Configuration) extends TransactionRepository {
  override def findAll: Seq[Transaction] = DB readOnly { implicit session =>
    sql"SELECT id, account_number, account_from, account_to, `type`, currency, status, amount, transaction_at FROM transaction".map { rs =>
      Transaction(Option(rs.long("id")),
        rs.string("account_number"),
        rs.string("account_from"),
        rs.string("account_to"),
        rs.short("type"),
        rs.string("currency"),
        rs.short("status"),
        rs.long("amount"),
        Option(rs.offsetDateTime("transaction_at"))
      )
    }.list().apply()
  }

  override def findByAccountNumber(accountNumber: String): Option[Transaction] = DB readOnly { implicit session =>
      sql"""SELECT id, account_number, account_from, account_to, `type`, currency, status, amount, transaction_at FROM transaction
        WHERE account_number = $accountNumber""".map { rs =>
        Transaction(Option(rs.long("id")),
          rs.string("account_number"),
          rs.string("account_from"),
          rs.string("account_to"),
          rs.short("type"),
          rs.string("currency"),
          rs.short("status"),
          rs.long("amount"),
          Option(rs.offsetDateTime("transaction_at"))
        )
    }.single().apply()
  }

  override def getHistories(accountNumber: String, startAt: Timestamp, endAt: Timestamp): Map[String, Seq[Transaction]] = DB readOnly { implicit session =>
    val list = sql"""SELECT id, account_number, account_from, account_to, `type`, currency, status, amount, transaction_at, DATE_FORMAT(transaction_at, '%Y%m') AS transaction_month FROM transaction
         WHERE account_number = $accountNumber AND transaction_at >= '2021-01-01 00:00:00' AND transaction_at < '2022-01-01 00:00:00' order by transaction_at""".map { rs =>
      Transaction(Option(rs.long("id")),
        rs.string("account_number"),
        rs.string("account_from"),
        rs.string("account_to"),
        rs.short("type"),
        rs.string("currency"),
        rs.short("status"),
        rs.long("amount"),
        Option(rs.offsetDateTime("transaction_at")),
        Option(rs.string("transaction_month"))
      )
    }.list().apply()

    Map[String, Seq[Transaction]]("monthly" -> list)
  }


  override def getMonthHistories(accountNumber: String, transactionAt: Timestamp): Seq[Transaction] = DB readOnly { implicit session =>
    val month = transactionAt.toLocalDateTime.getMonthValue
    sql"""SELECT id, account_number, account_from, account_to, `type`, currency, status, amount, transaction_at, MONTH(transaction_at) AS transaction_month FROM transaction
         WHERE account_number = $accountNumber AND MONTH(transaction_at) = $month AND YEAR(transaction_at) = YEAR(transaction_at) order by transaction_at""".map { rs =>
      Transaction(Option(rs.long("id")),
        rs.string("account_number"),
        rs.string("account_from"),
        rs.string("account_to"),
        rs.short("type"),
        rs.string("currency"),
        rs.short("status"),
        rs.long("amount"),
        Option(rs.offsetDateTime("transaction_at")),
        Option(rs.string("transaction_month"))
      )
    }.list().apply()
  }

  override def add(t: Transaction): Unit = DB localTx { implicit session =>
    sql"""INSERT INTO transaction (account_number, account_from, account_to, `type`, currency, status, amount)
         VALUES(${t.accountNumber}, ${t.accountFrom}, ${t.accountTo}, ${t.`type`}, ${t.currency}, ${t.status}, ${t.amount})""".update().apply()
  }
}
