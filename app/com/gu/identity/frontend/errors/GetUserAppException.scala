package com.gu.identity.frontend.errors

import com.gu.identity.frontend.errors.ErrorIDs.{GetUserBadRequestErrorID, GetUserGatewayErrorID, GetUserUnauthorizedErrorID}
import com.gu.identity.service.client.{ClientBadRequestError, ClientGatewayError, ClientUnauthorizedError, IdentityClientError}


sealed trait GetUserAppException extends AppException

object GetUserAppException {
  def apply(clientError: IdentityClientError): GetUserAppException =
    clientError match {
      case _: ClientBadRequestError => GetUserServiceBadRequestException(clientError)
      case _: ClientGatewayError => GetUserServiceGatewayAppException(clientError)
      case _: ClientUnauthorizedError => GetUserServiceUnauthorizedException(clientError)
    }
}

case class GetUserServiceGatewayAppException(
    clientError: IdentityClientError)
  extends ServiceGatewayAppException(clientError)
  with GetUserAppException {

  val id = GetUserGatewayErrorID
}


case class GetUserServiceBadRequestException(
    clientError: IdentityClientError)
  extends ServiceBadRequestAppException(clientError)
  with GetUserAppException {

  val id = GetUserBadRequestErrorID
}

case class GetUserServiceUnauthorizedException(
  clientError: IdentityClientError)
  extends ServiceUnauthorizedAppException(clientError)
  with GetUserAppException {

    val id = GetUserUnauthorizedErrorID
  }
