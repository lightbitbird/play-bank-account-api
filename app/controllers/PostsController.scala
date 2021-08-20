package controllers

import io.circe.syntax.EncoderOps
import models.{Meta, Post, Response}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.circe.Circe
import play.api.mvc.{AbstractController, ControllerComponents}
import repositories.PostDBRepository

import java.time.OffsetDateTime
import javax.inject._


case class PostRequest(body: String)

class PostsController @Inject()(cc: ControllerComponents, override val messagesApi: MessagesApi) extends AbstractController(cc) with I18nSupport with Circe {

  private[this] val form = Form(
    mapping(
      "post" -> text(minLength = 1, maxLength = 10)
    )(PostRequest.apply)(PostRequest.unapply)
  )

  def get = Action { implicit request =>

    Ok(Response(Meta(200), Some(Map("posts" -> PostDBRepository.findAll).asJson)).asJson)
  }

  def post = Action { implicit request =>
    form.bindFromRequest().fold(
      error => {
        val errorMessage = Messages(error.errors("post")(0).message)
        BadRequest(Response(Meta(400, Some(errorMessage))).asJson)
      },
      postRequest => {
        val post = Post(postRequest.body, OffsetDateTime.now)
        PostDBRepository.add(post)
        Ok(Response(Meta(200)).asJson)
      }
    )
  }

}
