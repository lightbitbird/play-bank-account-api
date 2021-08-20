package controllers

import commons.{DateFormat, TimeZoneJST}
import io.circe.syntax.EncoderOps
import models.{Meta, Response, Transaction}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.circe.Circe
import play.api.mvc.{AbstractController, ControllerComponents, RequestHeader}
import services.TransactionService

import java.sql.Timestamp
import java.util.TimeZone
import javax.inject.Inject

case class TransactionRequest(accountNumber: String,
                              accountFrom: String,
                              accountTo: String,
                              `type`: Short,
                              currency: String,
                              status: Short,
                              amount: Long
                             )

case class HistoryRequest(accountNumber: String, startAt: Timestamp, endAt: Timestamp)

case class MonthHistoryRequest(accountNumber: String, targetTimestamp: Timestamp)

class TransactionController @Inject()(cc: ControllerComponents, override val messagesApi: MessagesApi, transactionService: TransactionService)
  extends AbstractController(cc) with I18nSupport with Circe {
  override val logger = play.api.Logger("play")

  private[this] val transactionForm = Form(
    mapping(
      "account_number" -> text,
      "account_from" -> text,
      "account_to" -> text,
      "type" -> shortNumber,
      "currency" -> text,
      "status" -> shortNumber,
      "amount" -> longNumber
    )(TransactionRequest.apply)(TransactionRequest.unapply))

  private[this] val historyForm = Form(
    mapping(
      "account_number" -> text,
      "start_at" -> sqlTimestamp(DateFormat.DATETIME_FORMAT_STRING, TimeZone.getTimeZone("JST")),
      "end_at" -> sqlTimestamp(DateFormat.DATETIME_FORMAT_STRING, TimeZone.getTimeZone("JST"))
    )(HistoryRequest.apply)(HistoryRequest.unapply))

  private[this] val monthHistoryForm = Form(
    mapping(
      "account_number" -> text,
      "target_timestamp" -> sqlTimestamp("yyyyMMddHHmmss", TimeZone.getTimeZone(TimeZoneJST.id))
    )(MonthHistoryRequest.apply)(MonthHistoryRequest.unapply))

  def errorJson[T](error: Form[T], request: RequestHeader): Seq[Map[String, String]] = {
    implicit val lan = messagesApi.preferred(request).lang
    error.errors.map { e =>
      Map(
        "key" -> e.key,
        "message" -> messagesApi(e.key)
      )
    }
  }

  def add = Action { implicit request =>
    transactionForm.bindFromRequest().fold(
      error => {
        val errors = errorJson[TransactionRequest](error, request)
        BadRequest(Response(Meta(400), Some(errors.asJson)).asJson)
      },
      transactionReq => {
        val transaction = Transaction(
          accountNumber = transactionReq.accountNumber,
          accountFrom = transactionReq.accountFrom,
          accountTo = transactionReq.accountTo,
          `type` = transactionReq.`type`,
          currency = transactionReq.currency,
          status = transactionReq.status,
          amount = transactionReq.amount
        )
        transactionService.add(transaction)
        Ok(Response(Meta(200)).asJson)
      }
    )
  }

  def histories = Action { implicit request =>
    historyForm.bindFromRequest().fold(
      error => {
        val errors = errorJson[HistoryRequest](error, request)
        BadRequest(Response(Meta(400), Some(errors.asJson)).asJson)
      },
      historyReq => {
        val histories = transactionService.getHistories(historyReq.accountNumber, historyReq.startAt, historyReq.endAt)
        Ok(Response(Meta(200), Some(histories.asJson)).asJson)
      }
    )
  }

  def monthHistories = Action { implicit request =>
    monthHistoryForm.bindFromRequest().fold(
      error => {
        val errors = errorJson[MonthHistoryRequest](error, request)
        BadRequest(Response(Meta(400), Some(errors.asJson)).asJson)
      },
      historyReq => {
        val histories = transactionService.getMonthHistories(historyReq.accountNumber, historyReq.targetTimestamp)
        Ok(Response(Meta(200), Some(histories.asJson)).asJson)
      }
    )
  }

  def monthlyAmounts = Action { implicit request =>
    monthHistoryForm.bindFromRequest().fold(
      error => {
        val errors = errorJson[MonthHistoryRequest](error, request)
        BadRequest(Response(Meta(400), Some(errors.asJson)).asJson)
      },
      historyReq => {
        val histories = transactionService.getMonthlyAmountsByType(historyReq.accountNumber, historyReq.targetTimestamp)
        Ok(Response(Meta(200), Some(histories.asJson)).asJson)
      }
    )
  }
}
