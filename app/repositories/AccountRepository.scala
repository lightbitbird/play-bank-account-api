package repositories

import models.{Account, Customer}
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

object AccountRepository {
  def findByAccountNumber(accountNumber: String): Option[Account] = DB readOnly { implicit session =>
    sql"""SELECT id, account_number, customer_id, `type`, bank_id, balance, interest_rate, status FROM account
         WHERE account_number = $accountNumber""".map { rs =>
      Account(Option(rs.long("id")),
        rs.string("account_number"),
        rs.long("customer_id"),
        rs.short("type"),
        rs.long("bank_id"),
        rs.long("balance"),
        rs.bigDecimal("interestRate"),
        rs.short("status")
      )
    }.single().apply()
  }

  def findAll: Seq[Account] = DB readOnly { implicit session =>
    sql"SELECT id, account_number, customer_id, `type`, bank_id, balance, interest_rate, status FROM account".map { rs =>
      Account(Option(rs.long("id")),
        rs.string("account_number"),
        rs.long("customer_id"),
        rs.short("type"),
        rs.long("bank_id"),
        rs.long("balance"),
        rs.bigDecimal("interestRate"),
        rs.short("status")
      )
    }.list().apply()
  }

  def add(user: Account): Unit = DB localTx { implicit session =>
    sql"""INSERT INTO account (account_number, customer_id, `type`, bank_id, balance, interest_rate, status)
         VALUES(${user.accountNumber}, ${user.customerId}, ${user.`type`}, ${user.bankId}, ${user.balance}, ${user.interestRate}, ${user.status})""".update().apply()
  }
}
