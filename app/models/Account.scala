package models

import play.api.libs.json.{Json, Writes}
import scalikejdbc._

import java.time.{LocalDate, OffsetDateTime}

case class Customer(
                     id: Option[Long],
                     firstName: String,
                     lastName: String,
                     postalCode: Int,
                     address: String,
                     phoneNumber: Int,
                     email: String,
                     password: String,
                     dateOfBirth: LocalDate,
                     gender: Int,
                     updatedAt: Option[OffsetDateTime] = None
                   )

object Customer extends SQLSyntaxSupport[Customer] {
  implicit val writes: Writes[Customer] = Json.writes[Customer]

  def apply(id: Option[Long] = Some(0),
            firstName: String,
            lastName: String,
            postalCode: Int,
            address: String,
            phoneNumber: Int,
            email: String,
            password: String,
            dateOfBirth: LocalDate,
            gender: Int,
            updatedAt: Option[OffsetDateTime] = None
           ): Customer
  = new Customer(id, firstName, lastName, postalCode, address, phoneNumber, email, password, dateOfBirth, gender, updatedAt)

  def apply(c: ResultName[Customer])(rs: WrappedResultSet): Customer = new Customer(
    id = rs.get(c.id),
    firstName = rs.get(c.firstName),
    lastName = rs.get(c.lastName),
    postalCode = rs.get(c.postalCode),
    address = rs.get(c.address),
    phoneNumber = rs.get(c.phoneNumber),
    email = rs.get(c.email),
    password = rs.get(c.password),
    dateOfBirth = rs.get(c.dateOfBirth),
    gender = rs.get(c.gender)
  )
}

case class Account(
                    id: Option[Long],
                    accountNumber: String,
                    customerId: Long,
                    `type`: Short,
                    bankId: Long,
                    balance: Long,
                    interestRate: BigDecimal,
                    status: Short,
                    updatedAt: Option[OffsetDateTime] = None
                  )

object Account extends SQLSyntaxSupport[Account] {
  implicit val writes: Writes[Account] = Json.writes[Account]

  def apply(id: Option[Long] = Some(0),
            accountNumber: String,
            customerId: Long,
            `type`: Short,
            bankId: Long,
            balance: Long,
            interestRate: BigDecimal,
            status: Short,
            updatedAt: Option[OffsetDateTime] = None
           ): Account
  = new Account(id, accountNumber, customerId, `type`, bankId, balance, interestRate, status, updatedAt)

  def apply(c: ResultName[Account])(rs: WrappedResultSet): Account = new Account(
    id = rs.get(c.id),
    accountNumber = rs.get(c.accountNumber),
    customerId = rs.get(c.customerId),
    `type` = rs.get(c.`type`),
    bankId = rs.get(c.bankId),
    balance = rs.get(c.balance),
    interestRate = rs.get(c.interestRate),
    status = rs.get(c.status),
    updatedAt = rs.get(c.updatedAt)
  )
}

case class Bank(id: Long, name: String, branch: Branch)
case class Branch(id: Long, name: String)

case class Transaction(
                        id: Option[Long],
                        accountNumber: String,
                        accountFrom: String,
                        accountTo: String,
                        `type`: Short,
                        currency: String,
                        status: Short,
                        amount: Long,
                        transactionAt: Option[OffsetDateTime] = None,
                        transactionMonth: Option[String] = None
                      )

object Transaction extends SQLSyntaxSupport[Transaction] {
  implicit val writes: Writes[Transaction] = Json.writes[Transaction]

  def apply(id: Option[Long] = Some(0),
            accountNumber: String,
            accountFrom: String,
            accountTo: String,
            `type`: Short,
            currency: String,
            status: Short,
            amount: Long,
            transactionAt: Option[OffsetDateTime] = None,
            transactionMonth: Option[String] = None
           ): Transaction
  = new Transaction(id, accountNumber, accountFrom, accountTo, `type`, currency, status, amount, transactionAt, transactionMonth)

  def apply(c: ResultName[Transaction])(rs: WrappedResultSet): Transaction = new Transaction(
    id = rs.get(c.id),
    accountNumber = rs.get(c.accountNumber),
    accountFrom = rs.get(c.accountFrom),
    accountTo = rs.get(c.accountTo),
    `type` = rs.get(c.`type`),
    currency = rs.get(c.currency),
    status = rs.get(c.status),
    amount = rs.get(c.amount),
    transactionAt = rs.get(c.transactionAt),
    transactionMonth = rs.get(c.transactionMonth)
  )
}
