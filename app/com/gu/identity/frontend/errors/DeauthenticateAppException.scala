package com.gu.identity.frontend.errors

import com.gu.identity.frontend.errors.ErrorIDs._
import com.gu.identity.service.client.{ClientBadRequestError, ClientGatewayError, ClientUnauthorizedError, IdentityClientError}

sealed trait DeauthenticateAppException extends AppException

object DeauthenticateAppException {
  def apply(clientError: IdentityClientError): DeauthenticateAppException =
    clientError match {
      case _: ClientBadRequestError => DeauthenticateServiceBadRequestException(clientError)
      case _: ClientGatewayError | _: ClientUnauthorizedError => DeauthenticateServiceGatewayAppException(clientError)
    }
}

case class DeauthenticateServiceGatewayAppException(
    clientError: IdentityClientError)
  extends ServiceGatewayAppException(clientError)
  with DeauthenticateAppException {

  val id = DeauthenticateGatewayErrorID
}

case class DeauthenticateServiceBadRequestException(
    clientError: IdentityClientError)
  extends ServiceBadRequestAppException(clientError)
  with DeauthenticateAppException {

  val id = DeauthenticateBadRequestErrorID
}
