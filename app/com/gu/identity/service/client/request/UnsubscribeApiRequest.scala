package com.gu.identity.service.client.request

import play.api.libs.json.Json

case class UnsubscribeApiRequest(emailType: String, emailId: String, userId: String, timestamp: Long, token: String)

object UnsubscribeApiRequest {
  implicit val format = Json.format[UnsubscribeApiRequest]
}
