package controllers

import models.{Account, Customer, Meta, Response}
import play.api.data.Form
import play.api.data.Forms.{bigDecimal, date, email, localDate, localDateTime, longNumber, mapping, number, shortNumber, sqlDate, text}
import play.api.data.validation.Constraints.nonEmpty
import play.api.i18n.{I18nSupport, Lang, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents, RequestHeader}
import repositories.{AccountRepository, CustomerRepository}

import java.time.{LocalDate, LocalDateTime, OffsetDateTime, ZonedDateTime}
import java.util.Locale
import javax.inject.Inject

case class AccountRequest(
                            accountNumber: String,
                            customerId: Long,
                            `type`: Short,
                            bankId: Long,
                            balance: Long,
                            interestRate: BigDecimal,
                            status: Short
                          )

class AccountController @Inject()(cc: ControllerComponents, override val messagesApi: MessagesApi) extends AbstractController(cc) with I18nSupport {
  val logger = play.api.Logger("play")

  private[this] val registerForm = Form(
    mapping(
      "account_number" -> text,
      "customer_id" -> longNumber,
      "type" -> shortNumber,
      "bank_id" -> longNumber,
      "balance" -> longNumber,
      "interest_rate" -> bigDecimal,
      "status" -> shortNumber
    )(AccountRequest.apply)(AccountRequest.unapply))

  def errorJson[T](error: Form[T], request: RequestHeader): Seq[Map[String, String]] = {
    implicit val lan = messagesApi.preferred(request).lang
    error.errors.map { e =>
      Map(
        "key" -> e.key,
        "message" -> messagesApi(e.key)
      )
    }
  }

  def create = Action { implicit request =>
    registerForm.bindFromRequest().fold(
      error => {
        val errors = errorJson[AccountRequest](error, request)
        logger.info(s"errors: $errors")

        BadRequest(Json.toJson(Response(Meta(400), Some(Json.toJson(errors)))))
      },
      accountReq => {
        val account = Account(
          accountNumber = accountReq.accountNumber,
          customerId = accountReq.customerId,
          `type` = accountReq.`type`,
          bankId = accountReq.bankId,
          balance = accountReq.balance,
          interestRate = accountReq.interestRate,
          status = accountReq.status
        )

        logger.info(s"account: $account")

        AccountRepository.add(account)
        Ok(Json.toJson(Response(Meta(200), Some(Json.toJson(account)))))
      }
    )
  }

}
