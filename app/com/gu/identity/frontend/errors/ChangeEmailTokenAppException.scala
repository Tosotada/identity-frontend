package com.gu.identity.frontend.errors

import com.gu.identity.frontend.errors.ErrorIDs._
import com.gu.identity.service.client._

sealed trait ChangeEmailTokenAppException extends AppException

object ChangeEmailTokenAppException {
  def apply(clientError: IdentityClientError): ChangeEmailTokenAppException =
    clientError match {
      case ClientInvalidTokenError => ChangeEmailTokenUnauthorizedException
      case err: ClientBadRequestError => ChangeEmailTokenBadRequestAppException(err)
      case err: ClientGatewayError => ChangeEmailTokenGatewayAppException(err)
    }
}

case class ChangeEmailTokenBadRequestAppException(clientError: IdentityClientError)
  extends ServiceBadRequestAppException(clientError)
    with ChangeEmailTokenAppException {

  val id = SignInInvalidCredentialsErrorID
}

case class ChangeEmailTokenGatewayAppException(clientError: IdentityClientError)
  extends ServiceGatewayAppException(clientError)
    with ChangeEmailTokenAppException {
  override val id = ChangeEmailTokenGatewayErrorID
}

case object ChangeEmailTokenUnauthorizedException
  extends ServiceBadRequestAppException(ClientInvalidTokenError)
    with ChangeEmailTokenAppException {
  override val id = UnauthorizedChangeEmailTokenErrorID
}
