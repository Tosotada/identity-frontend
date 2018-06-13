package com.gu.identity.service.client.request

import com.gu.identity.service.client._

case class ChangeEmailTokenRequest private(override val url: String) extends ApiRequest
final case class ChangeEmailTokenRequestBody(token: String) extends ApiRequestBody

object ChangeEmailTokenRequest {
  def apply(token: String, config: IdentityClientConfiguration): ChangeEmailTokenRequest = {
    val pathComponents = Seq("auth", "change-email")
    new ChangeEmailTokenRequest(ApiRequest.apiEndpoint(pathComponents: _*)(config)) {
      override val method: HttpMethod = POST
      override val headers = Iterable(ApiRequest.apiKeyHeader(config))
      override val body = Some(ChangeEmailTokenRequestBody(token))
      override val parameters: HttpParameters = List("token" -> token)
    }
  }
}
