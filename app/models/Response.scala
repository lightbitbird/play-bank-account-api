package models

import play.api.libs.json.{JsValue, Json, Writes}

case class Meta(status: Int, errorMessage: Option[String] = None)

object Meta {
  implicit val writes: Writes[Meta] = Json.writes[Meta]
}

case class Response(meta: Meta, data: Option[JsValue] = None)

object Response {
  implicit def writes: Writes[Response] = Json.writes[Response]
}
