package services

import com.google.inject.ImplementedBy
import models.Transaction
import repositories.TransactionRepository

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}

@ImplementedBy(classOf[TransactionServiceImpl])
trait TransactionService {
  def getHistories(accountNumber: String, startAt: Timestamp, endAt: Timestamp): Map[String, Seq[Transaction]]

  def getMonthHistories(accountNumber: String, transactionAt: Timestamp): Seq[Transaction]

  def findByAccountNumber(accountNumber: String): Option[Transaction]

  def findAll: Seq[Transaction]

  def add(t: Transaction): Unit
}

@Singleton
class TransactionServiceImpl @Inject()(repository: TransactionRepository) extends TransactionService {
  override def getHistories(accountNumber: String, startAt: Timestamp, endAt: Timestamp): Map[String, Seq[Transaction]] = repository.getHistories(accountNumber, startAt, endAt)

  override def getMonthHistories(accountNumber: String, transactionAt: Timestamp): Seq[Transaction] = repository.getMonthHistories(accountNumber, transactionAt)

  override def findByAccountNumber(accountNumber: String): Option[Transaction] = repository.findByAccountNumber(accountNumber)

  override def findAll: Seq[Transaction] = repository.findAll

  override def add(t: Transaction): Unit = repository.add(t)

}


//{
//  month: {
//  name: 'Jan',
//  transactions: [
//  {
//    id: 'abc12345678',
//    activity: 'deposit',
//    place: 'London',
//    amount: 99000,
//    timestamp: `${year}/01/03 10:10:20`,
//    operation: 'Deposit',
//  },
//  {
//    id: 'abc12345678',
//    activity: 'withdrawal',
//    place: 'London',
//    amount: 99000,
//    timestamp: `${year}/01/16 19:42:36`,
//    operation: 'Withdrawal',
//  },
//  {
//    id: 'abc12345678',
//    activity: 'deposit',
//    place: 'London',
//    amount: 12550,
//    timestamp: `${year}/01/22 17:48:54`,
//    operation: 'Deposit',
//  },
//  {
//    id: 'abc12345678',
//    activity: 'withdrawal',
//    place: 'London',
//    amount: 30480,
//    timestamp: `${year}/01/25 11:23:36`,
//    operation: 'Withdrawal',
//  },
//  {
//    id: 'abc12345678',
//    activity: 'deposit',
//    place: 'Amazon',
//    amount: 30480,
//    timestamp: `${year}/01/25 11:23:36`,
//    operation: 'Transfer',
//  }]
//  }
//}
