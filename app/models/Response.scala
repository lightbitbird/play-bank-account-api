package models

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder, Json}

case class Meta(status: Int, errorMessage: Option[String] = None)

//object Meta {
//  implicit val writes: Writes[Meta] = Json.writes[Meta]
//}

case class Response(meta: Meta, data: Option[Json] = None)

object Response {
  implicit def encodeMeta: Encoder[Meta] = deriveEncoder
  implicit def encodeResponse: Encoder[Response] = deriveEncoder
  implicit def decodeMeta: Decoder[Meta] = deriveDecoder
  implicit def decodeResponse: Decoder[Response] = deriveDecoder
//  implicit def writes: Writes[Response] = play.api.libs.json.Json.writes[Response]
}

//trait WebJsonSupport {
//  implicit def encode: Encoder[Response] = deriveEncoder
//  implicit def decode: Decoder[Response] = deriveDecoder
//}