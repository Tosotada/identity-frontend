package com.gu.identity.service.client.request

import com.gu.identity.frontend.models.ClientIp
import com.gu.identity.frontend.request.ResetPasswordActionRequestBody
import com.gu.identity.service.client._

case class SendResetPasswordEmailRequestBody(
  `email-address`: String,
  returnUrl: String
) extends ApiRequestBody

case class SendResetPasswordEmailApiRequest(data: ResetPasswordActionRequestBody, clientIp: ClientIp)
                                           (implicit configuration: IdentityClientConfiguration) extends ApiRequest {
  override val method = POST
  override val headers = Iterable(
    ApiRequest.apiKeyHeader,
    ApiRequest.ipHeader(clientIp),
    "Content-Type" -> "application/json")
  override val url = ApiRequest.apiEndpoint("pwd-reset/send-password-reset-email")
  override val body = Some(SendResetPasswordEmailRequestBody(
    `email-address` = data.email,
    returnUrl = data.returnUrl.getOrElse("")
  ))
}

