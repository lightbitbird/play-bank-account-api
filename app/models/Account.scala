package models

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder, Json}
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
//  implicit val writes: Writes[Customer] = Json.writes[Customer]
  implicit def encode: Encoder[Customer] = deriveEncoder
  implicit def decode: Decoder[Customer] = deriveDecoder

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
//  implicit val writes: Writes[Account] = Json.writes[Account]
  implicit def encode: Encoder[Account] = deriveEncoder
  implicit def decode: Decoder[Account] = deriveDecoder

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
//  implicit def encode: Encoder[Transaction] = deriveEncoder
  implicit def decode: Decoder[Transaction] = deriveDecoder
  implicit val encodeTransaction: Encoder[Transaction] = new Encoder[Transaction] {
    override def apply(t: Transaction): Json = Json.obj(
      ("id", Json.fromLong(t.id.get)),
      ("account_number", Json.fromString(t.accountNumber)),
      ("account_from", Json.fromString(t.accountFrom)),
      ("account_to", Json.fromString(t.accountTo)),
      ("type", Json.fromInt(t.`type`)),
      ("currency", Json.fromString(t.currency)),
      ("status", Json.fromInt(t.status)),
      ("amount", Json.fromLong(t.amount)),
      ("transaction_at", Json.fromString(t.transactionAt.get.toString)),
      ("transaction_month", Json.fromString(t.transactionMonth.getOrElse("")))
    )
  }

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

case class TransactionHistory(name: String, balance: Balance, month: Month, totals: Map[String, Map[String, Long]], today: Seq[Transaction], yesterday: Seq[Transaction])

object TransactionHistory extends SQLSyntaxSupport[TransactionHistory] {
  implicit def encode: Encoder[TransactionHistory] = deriveEncoder
  implicit def decode: Decoder[TransactionHistory] = deriveDecoder

  def apply(name: String, balance: Balance, month: Month, totals: Map[String, Map[String, Long]], today: Seq[Transaction] = Seq(), yesterday: Seq[Transaction] = Seq()): TransactionHistory
  = new TransactionHistory(name, balance, month, totals, today, yesterday)
}

case class Balance(deposit: Long = 0L, withdrawal: Long = 0L, total: Long = 0L)

object Balance extends SQLSyntaxSupport[Balance] {
  implicit def encode: Encoder[Balance] = deriveEncoder
  implicit def decode: Decoder[Balance] = deriveDecoder

  def apply(deposit: Long = 0L, withdrawal: Long = 0L, total: Long = 0L): Balance
  = new Balance(deposit, withdrawal, total)
}

case class Month(name: String, transactions: Seq[Transaction])

object Month extends SQLSyntaxSupport[Month] {
  implicit def encode: Encoder[Month] = deriveEncoder
  implicit def decode: Decoder[Month] = deriveDecoder

  def apply(name: String, transactions: Seq[Transaction]): Month
  = new Month(name, transactions)
}
