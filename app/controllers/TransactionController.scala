package controllers

import models.{Meta, Response, Transaction}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
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
  extends AbstractController(cc) with I18nSupport {
  val logger = play.api.Logger("play")

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
      "start_at" -> sqlTimestamp("yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("JST")),
      "end_at" -> sqlTimestamp("yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("JST"))
      //      "end_at" -> localDateTime("yyyy-MM-dd HH:mm:ss")
    )(HistoryRequest.apply)(HistoryRequest.unapply))

  private[this] val monthHistoryForm = Form(
    mapping(
      "account_number" -> text,
      "target_timestamp" -> sqlTimestamp("yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("JST"))
//      "end_at" -> localDateTime("yyyy-MM-dd HH:mm:ss")
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
        logger.info(s"errors: $errors")

        BadRequest(Json.toJson(Response(Meta(400), Some(Json.toJson(errors)))))
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

        logger.info(s"transaction: $transaction")

        transactionService.add(transaction)
        Ok(Json.toJson(Response(Meta(200), Some(Json.toJson(transaction)))))
      }
    )
  }

  def histories = Action { implicit request =>
    historyForm.bindFromRequest().fold(
      error => {
        val errors = errorJson[HistoryRequest](error, request)
        BadRequest(Json.toJson(Response(Meta(400), Some(Json.toJson(errors)))))
      },
      historyReq => {
        logger.info(s"historyRequest: $historyReq")
        val histories = transactionService.getHistories(historyReq.accountNumber, historyReq.startAt, historyReq.endAt)
        Ok(Json.toJson(Response(Meta(200), Some(Json.toJson(histories)))))
      }
    )
  }

  def monthHistories = Action { implicit request =>
    monthHistoryForm.bindFromRequest().fold(
      error => {
        val errors = errorJson[MonthHistoryRequest](error, request)
        BadRequest(Json.toJson(Response(Meta(400), Some(Json.toJson(errors)))))
      },
      historyReq => {
        logger.info(s"historyRequest: $historyReq")
        val histories = transactionService.getMonthHistories(historyReq.accountNumber, historyReq.targetTimestamp)
        Ok(Json.toJson(Response(Meta(200), Some(Json.toJson(histories)))))
      }
    )
  }
}
