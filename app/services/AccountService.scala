package services

import com.google.inject.ImplementedBy
import models.Account
import repositories.AccountRepository

import javax.inject.{Inject, Singleton}

@ImplementedBy(classOf[AccountServiceImpl])
trait AccountService {
  def findByAccountNumber(accountNumber: String): Option[Account]

  def findAll: Seq[Account]

  def add(user: Account): Unit
}

@Singleton
class AccountServiceImpl @Inject()(repository: AccountRepository) extends AccountService {
  override def findByAccountNumber(accountNumber: String): Option[Account] = repository.findByAccountNumber(accountNumber)

  override def findAll: Seq[Account] = repository.findAll

  override def add(user: Account): Unit = repository.add(user)
}
