package controllers

import io.circe.syntax._
import models.{Customer, Meta, Response}
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints.nonEmpty
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.circe.Circe
import play.api.mvc.{AbstractController, ControllerComponents, RequestHeader}
import repositories.CustomerRepository

import java.time.LocalDate
import javax.inject.Inject

case class LoginRequest(email: String, password: String)

case class RegisterRequest(
                            firstName: String,
                            lastName: String,
                            postalCode: Int,
                            address: String,
                            phoneNumber: Int,
                            email: String,
                            password: String,
                            dateOfBirth: LocalDate,
                            gender: Int
                          )

class AuthController @Inject()(cc: ControllerComponents, override val messagesApi: MessagesApi) extends AbstractController(cc) with I18nSupport with Circe {
  override val logger = play.api.Logger("play")

  private[this] val registerForm = Form(
    mapping(
      "first_name" -> text,
      "last_name" -> text,
      "postal_code" -> number,
      "address" -> text,
      "phone_number" -> number,
      "email" -> email,
      "password" -> text,
      "date_of_birth" -> localDate("yyyy-MM-dd"),
      "gender" -> number
    )(RegisterRequest.apply)(RegisterRequest.unapply))

  private[this] val loginForm = Form(
    mapping(
//      "email" -> text.verifying(nonEmpty),
      "email" -> email,
      "password" -> text.verifying(nonEmpty)
//      "password" -> text(minLength = 1, maxLength = 10)
  )(LoginRequest.apply)(LoginRequest.unapply))

  def errorJson[T](error: Form[T], request: RequestHeader): Seq[Map[String, String]] = {
//    implicit val lang = cc.langs.preferred(Seq(Lang(Locale.ENGLISH)))
//    lazy val messages: Messages = messagesApi.preferred(messagesApi)
//    import play.api.i18n.Messages.implicitMessagesProviderToMessages
//    implicit val provider = new Messages("en")

    implicit val lan = messagesApi.preferred(request).lang
    error.errors.map { e =>
      Map(
        "key" -> e.key,
        "message" -> messagesApi(e.key)
//        "message" -> Messages(e.message)
      )
    }
  }

  def register = Action { implicit request =>
    registerForm.bindFromRequest().fold(
      error => {
        val errors = errorJson[RegisterRequest](error, request)
        logger.info(s"errors: $errors")

        BadRequest(Response(Meta(400), Some(errors.asJson)).asJson)
      },
      register => {
        val user = Customer(
          firstName = register.firstName,
          lastName = register.lastName,
          postalCode = register.postalCode,
          address = register.address,
          phoneNumber = register.phoneNumber,
          email = register.email,
          password = register.password,
          dateOfBirth = register.dateOfBirth,
          gender = register.gender
        )

        logger.info(s"user: $user")

        CustomerRepository.add(user)
//        val user = Customer(email = register.email, password = register.password)
        Ok(Response(Meta(200), Some(user.asJson)).asJson)
      }
    )
  }

  def login = Action { implicit request =>
    loginForm.bindFromRequest().fold(
      error => {
//        BadRequest(views.html.index(formWithErrors))
//        BadRequest(Json.toJson(formWithErrors.data))

//        val errors = error.errors.map { e =>
//          Map(
//            "key" -> e.key,
//            "message" -> Messages(e.message)
//          )
//        }
        val errors = errorJson[LoginRequest](error, request)
        BadRequest(Response(Meta(400), Some(errors.asJson)).asJson)
      },
      loginRequest => {
        logger.info(s"loginRequest: ${loginRequest.email}, ${loginRequest.password}")
        CustomerRepository.findAuthUser(loginRequest.email, loginRequest.password) match {
          case Some(u) => Ok(Response(Meta(200), Some(u.asJson)).asJson)
          case _ => {
            val error = Map(
              "key" -> "Unauthorized",
              "message" -> Messages("error.unauthorized")
            )
            BadRequest(Response(Meta(401), Some(error.asJson)).asJson)
          }
        }
//        val user = Customer(email = Some(loginRequest.email), password = Some(loginRequest.password))
//        PostDBRepository.add(user)
//        Redirect("/"
//        Ok(Json.toJson(Response(Meta(200))))
//        Ok(Json.toJson(Response(Meta(200), Some(Json.toJson(user)))))
      }
    )
  }

}
