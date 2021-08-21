package repositories

import com.google.inject.ImplementedBy
import commons._
import models.Transaction
import play.api.Configuration
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.sql.Timestamp
import java.time.LocalDateTime
import javax.inject.{Inject, Singleton}
import scala.collection.immutable.ListMap

@ImplementedBy(classOf[TransactionRepositoryImpl])
trait TransactionRepository {
  def findAll: Seq[Transaction]

  def findByAccountNumber(accountNumber: String): Option[Transaction]

  def getHistories(accountNumber: String, startAt: Timestamp, endAt: Timestamp): Map[String, Seq[Transaction]]

  def getMonthHistories(accountNumber: String, transactionAt: Timestamp): (Seq[Transaction], Long, Seq[Transaction], Seq[Transaction])

  def getAmountsByMonth(accountNumber: String, startAt: Timestamp, endAt: Timestamp, types: Seq[Short]): Map[String, Map[Short, Long]]

  def getMonthlyAmountsByType(accountNumber: String, startAt: Timestamp, endAt: Timestamp, types: Seq[Short]): Map[String, Map[String, Long]]

  def add(t: Transaction): (Long, Long)
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
    val groupBy =
      sql"""SELECT id, account_number, account_from, account_to, `type`, currency, status, amount, transaction_at, DATE_FORMAT(transaction_at, '%Y%m') AS transaction_month FROM transaction
         WHERE account_number = $accountNumber AND transaction_at >= '2021-01-01 00:00:00' AND transaction_at < '2022-01-01 00:00:00' order by transaction_month, transaction_at""".map { rs =>
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
      }.list().apply().groupBy(t => t.transactionMonth).map(m => (m._1.get, m._2))

    ListMap(groupBy.toSeq.sortBy(_._1): _*)
  }

  /**
   * Get total amounts of the account's transactions by month
   *
   * @param accountNumber
   * @param startAt
   * @param endAt
   * @param types
   * @return
   */
  override def getAmountsByMonth(accountNumber: String, startAt: Timestamp, endAt: Timestamp, types: Seq[Short]): Map[String, Map[Short, Long]] = DB readOnly { implicit session =>
    val map =
      sql"""SELECT DATE_FORMAT(transaction_at, '%Y%m') AS transaction_month,
            CASE WHEN `type` = 0 OR `type` = 2 THEN 0
                 WHEN `type` = 1 OR `type` = 3 THEN 1
            END AS `type`, SUM(amount) AS sums FROM transaction
          WHERE account_number = $accountNumber AND transaction_at >= $startAt AND transaction_at < $endAt AND `type` IN ($types)
          GROUP BY DATE_FORMAT(transaction_at, '%Y%m'),
            CASE WHEN `type` = 0 OR `type` = 2 THEN 0
                 WHEN `type` = 1 OR `type` = 3 THEN 1
            END ORDER BY transaction_month, `type`""".map { rs =>
        (rs.string("transaction_month"), rs.short("type"), rs.long("sums"))
      }.list().apply().groupBy(_._1).map(a => {
        val types = (for {
          d <- a._2
        } yield (d._2, d._3)).foldLeft(Map[Short, Long]()) { (m, s) => m.updated(s._1, s._2) }

        (a._1 -> types)
      })

    ListMap(map.toSeq.sortBy(_._1): _*)
  }

  /**
   * Get monthly total amounts of the account's transactions by type
   *
   * @param accountNumber
   * @param startAt
   * @param endAt
   * @param types
   * @return
   */
  override def getMonthlyAmountsByType(accountNumber: String, startAt: Timestamp, endAt: Timestamp, types: Seq[Short]): Map[String, Map[String, Long]] =
    DB readOnly { implicit session =>
      val typeName = (a: Short) => {
        if (Deposit.value == a || TransferFrom.value == a)
          Deposit.name
        else
          Withdrawal.name
      }
      val map =
        sql"""SELECT DATE_FORMAT(transaction_at, '%Y%m') AS transaction_month,
            CASE WHEN `type` = 0 OR `type` = 2 THEN 0
                 WHEN `type` = 1 OR `type` = 3 THEN 1
            END AS `type`, SUM(amount) AS sums FROM transaction
          WHERE account_number = $accountNumber AND transaction_at >= $startAt AND transaction_at < $endAt AND `type` IN ($types)
          GROUP BY DATE_FORMAT(transaction_at, '%Y%m'),
            CASE WHEN `type` = 0 OR `type` = 2 THEN 0
                 WHEN `type` = 1 OR `type` = 3 THEN 1
            END ORDER BY transaction_month, `type`""".map { rs =>
          (rs.string("transaction_month"), rs.short("type"), rs.long("sums"))
        }.list().apply().groupBy(_._2).map(a => {
          val mapByMonth: Map[String, Long] = (for {
            d <- a._2
          } yield (d._1, d._3)).foldLeft(Map[String, Long]())((m, d) => m + (d._1 -> d._2))

          var incrementDate = startAt.toLocalDateTime
          val amountMap = (0 to 11).toList.foldLeft(Map[String, Long]()) { (adds, i) =>
            val yearMonth = s"${incrementDate.getYear}${String.format("%02d", incrementDate.getMonthValue)}"
            val list = mapByMonth.get(yearMonth) match {
              case Some(a) => adds + (yearMonth -> a)
              case None => adds + (yearMonth -> 0L)
            }
            incrementDate = incrementDate.plusMonths(1)
            list
          }
          (typeName(a._1) -> ListMap(amountMap.toSeq.sortBy(_._1): _*))
        })

      ListMap(map.toSeq.sortBy(_._1): _*)
    }

  override def getMonthHistories(accountNumber: String, transactionAt: Timestamp): (Seq[Transaction], Long, Seq[Transaction], Seq[Transaction]) = DB readOnly { implicit session =>
    val year = transactionAt.toLocalDateTime.getYear
    val month = transactionAt.toLocalDateTime.getMonthValue

    val list =
      sql"""SELECT t.id, balance, a.account_number, account_from, account_to, t.`type`, currency, t.status, amount, transaction_at, MONTH(transaction_at) AS transaction_month FROM account AS a
           INNER JOIN transaction AS t ON a.account_number = t.account_number
          WHERE a.account_number = $accountNumber AND MONTH(transaction_at) = $month AND YEAR(transaction_at) = $year order by transaction_at""".map { rs =>
        (Transaction(Option(rs.long("id")),
          rs.string("account_number"),
          rs.string("account_from"),
          rs.string("account_to"),
          rs.short("type"),
          rs.string("currency"),
          rs.short("status"),
          rs.long("amount"),
          Option(rs.offsetDateTime("transaction_at")),
          Option(rs.string("transaction_month"))
        ), rs.long("balance"))
      }.list().apply()

    val balance = list(0)._2

    val localDate = (localDatetime: LocalDateTime) => {
      localDatetime.format(DateFormat.DATE_FORMATTER)
    }
    val todayDate = localDate(LocalDateTime.now())
    val yesterdayDate = localDate(LocalDateTime.now().minusDays(1))

    val today = list.map(t => t._1).filter(t => localDate(t.transactionAt.get.toLocalDateTime) == todayDate)
    val yesterday = list.map(t => t._1).filter(t => localDate(t.transactionAt.get.toLocalDateTime) == yesterdayDate)

    (list.map(_._1), balance, today, yesterday)
  }

  override def add(t: Transaction): (Long, Long) = DB localTx { implicit session =>
    val fix = (t: Transaction) => {
      if (Deposit.value == t.`type` || TransferTo.value == t.`type`)
        t.amount
      else
        0L - t.amount
    }

    val amount = fix(t)
    val id =
      sql"""INSERT INTO transaction (account_number, account_from, account_to, `type`, currency, status, amount)
         VALUES(${t.accountNumber}, ${t.accountFrom}, ${t.accountTo}, ${t.`type`}, ${t.currency}, ${t.status}, $amount)""".updateAndReturnGeneratedKey().apply()
    (id, amount)
  }
}
