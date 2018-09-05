package com.gu.identity.frontend.errors

import com.gu.identity.frontend.errors.ErrorIDs.{RepermissionTokenGatewayErrorID, SignInInvalidCredentialsErrorID, UnauthorizedRepermissionTokenErrorID}
import com.gu.identity.service.client._

sealed trait RepermissionTokenAppException extends AppException

object RepermissionTokenAppException {
  def apply(clientError: IdentityClientError): RepermissionTokenAppException =
    clientError match {
      case ClientInvalidTokenError => RepermissionTokenUnauthorizedException
      case err: ClientBadRequestError => RepermissionTokenBadRequestAppException(err)
      case _: ClientGatewayError | _: ClientUnauthorizedError => RepermissionTokenGatewayAppException(clientError)
    }
}

case class RepermissionTokenBadRequestAppException(clientError: IdentityClientError)
  extends ServiceBadRequestAppException(clientError)
    with RepermissionTokenAppException {

  val id = SignInInvalidCredentialsErrorID
}

case class RepermissionTokenGatewayAppException(clientError: IdentityClientError)
    extends ServiceGatewayAppException(clientError)
    with RepermissionTokenAppException {
  override val id = RepermissionTokenGatewayErrorID
}

case object RepermissionTokenUnauthorizedException
    extends ServiceBadRequestAppException(ClientInvalidTokenError)
    with RepermissionTokenAppException {
  override val id = UnauthorizedRepermissionTokenErrorID
}
