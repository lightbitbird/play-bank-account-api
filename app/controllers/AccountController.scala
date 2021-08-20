package controllers

import io.circe.syntax.EncoderOps
import models.{Account, Meta, Response}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.circe.Circe
import play.api.mvc.{AbstractController, ControllerComponents, RequestHeader}
import repositories.AccountRepository
import services.AccountService

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

class AccountController @Inject()(cc: ControllerComponents, override val messagesApi: MessagesApi, accountService: AccountService) extends AbstractController(cc) with I18nSupport with Circe {
  override val logger = play.api.Logger("play")

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

        BadRequest(Response(Meta(400), Some(errors.asJson)).asJson)
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

        accountService.add(account)
        Ok(Response(Meta(200), Some(account.asJson)).asJson)
      }
    )
  }

}
