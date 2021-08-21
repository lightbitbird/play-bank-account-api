package repositories

import com.google.inject.ImplementedBy
import commons.NotDeleted
import models.Account
import play.api.Configuration
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import javax.inject.{Inject, Singleton}

@ImplementedBy(classOf[AccountRepositoryImpl])
trait AccountRepository {
  def findByAccountNumber(accountNumber: String): Option[Account]

  def findAll: Seq[Account]

  def add(user: Account): Unit

  def updateBalance(accountNumber: String, amount: Long): Int
}

@Singleton
class AccountRepositoryImpl @Inject()(config: Configuration) extends AccountRepository {

  override def findByAccountNumber(accountNumber: String): Option[Account] = DB readOnly { implicit session =>
    sql"""SELECT id, account_number, customer_id, `type`, bank_id, balance, interest_rate, status FROM account
         WHERE account_number = $accountNumber""".map { rs =>
      Account(Option(rs.long("id")),
        rs.string("account_number"),
        rs.long("customer_id"),
        rs.short("type"),
        rs.long("bank_id"),
        rs.long("balance"),
        rs.bigDecimal("interest_rate"),
        rs.short("status")
      )
    }.single().apply()
  }

  override def findAll: Seq[Account] = DB readOnly { implicit session =>
    sql"SELECT id, account_number, customer_id, `type`, bank_id, balance, interest_rate, status FROM account".map { rs =>
      Account(Option(rs.long("id")),
        rs.string("account_number"),
        rs.long("customer_id"),
        rs.short("type"),
        rs.long("bank_id"),
        rs.long("balance"),
        rs.bigDecimal("interest_rate"),
        rs.short("status")
      )
    }.list().apply()
  }

  override def add(user: Account): Unit = DB localTx { implicit session =>
    sql"""INSERT INTO account (account_number, customer_id, `type`, bank_id, balance, interest_rate, status)
         VALUES(${user.accountNumber}, ${user.customerId}, ${user.`type`}, ${user.bankId}, ${user.balance}, ${user.interestRate}, ${user.status})""".update().apply()
  }

  override def updateBalance(accountNumber: String, amount: Long): Int = DB localTx { implicit session =>
    sql"""UPDATE account SET balance = balance + $amount, updated_at = NOW() WHERE account_number = $accountNumber AND delete_flg = ${NotDeleted.value}""".update().apply()
  }
}
