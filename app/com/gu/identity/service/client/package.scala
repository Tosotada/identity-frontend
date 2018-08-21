package com.gu.identity.service

import scala.concurrent.Future


package object client {

  type HttpParameters = Iterable[(String, String)]

  type IdentityClientErrors = Seq[IdentityClientError]

  trait IdentityClientRequestHandler {
    def handleRequest(request: ApiRequest): Future[Either[IdentityClientErrors, ApiResponse]]
  }

  case class IdentityClientConfiguration(host: String, apiKey: String, requestHandler: IdentityClientRequestHandler) {
    val hostWithProtocol = s"https://$host"
  }

}
