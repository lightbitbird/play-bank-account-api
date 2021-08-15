package repositories

import models.{Customer, Post}
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

object CustomerRepository {
  def findAuthUser(email: String, password: String): Option[Customer] = DB readOnly { implicit session =>
    sql"""SELECT id, first_name, last_name, postal_code, address, phone_number, email, password, date_of_birth, gender FROM customer
         WHERE email = $email AND password = $password""".map { rs =>
      Customer(Option(rs.long("id")),
        rs.string("first_name"),
        rs.string("last_name"),
        rs.int("postal_code"),
        rs.string("address"),
        rs.int("phone_number"),
        rs.string("email"),
        rs.string("password"),
        rs.localDate("date_of_birth"),
        rs.int("gender")
      )
    }.single().apply()
  }

  def findAll: Seq[Customer] = DB readOnly { implicit session =>
    sql"SELECT id, first_name, last_name, postal_code, address, phone_number, email, password, date_of_birth, gender FROM customer".map { rs =>
      Customer(Option(rs.long("id")),
        rs.string("first_name"),
        rs.string("last_name"),
        rs.int("postal_code"),
        rs.string("address"),
        rs.int("phone_number"),
        rs.string("email"),
        rs.string("password"),
        rs.localDate("date_of_birth"),
        rs.int("gender")
      )
    }.list().apply()
  }

  def add(user: Customer): Unit = DB localTx { implicit session =>
    sql"""INSERT INTO customer (first_name, last_name, postal_code, address, phone_number, email, password, date_of_birth, gender)
         VALUES(${user.firstName}, ${user.lastName}, ${user.postalCode}, ${user.address}, ${user.phoneNumber}, ${user.email}, ${user.password}, ${user.dateOfBirth}, ${user.gender})""".update().apply()
  }
}
