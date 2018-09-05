package com.gu.identity.frontend.errors

import com.gu.identity.frontend.errors.ErrorIDs.{ResendTokenGatewayErrorID, SignInInvalidCredentialsErrorID, UnauthorizedConsentTokenErrorID}
import com.gu.identity.service.client._

sealed trait ConsentTokenAppException extends AppException

object ConsentTokenAppException {
  def apply(clientError: IdentityClientError): ConsentTokenAppException =
    clientError match {
      case ClientTokenExpiredError => ConsentTokenUnauthorizedException
      case err: ClientBadRequestError => ConsentTokenBadRequestAppException(err)
      case _: ClientGatewayError | _: ClientUnauthorizedError  => ConsentTokenGatewayAppException(clientError)
    }
}

case class ConsentTokenBadRequestAppException(clientError: IdentityClientError)
  extends ServiceBadRequestAppException(clientError)
    with ConsentTokenAppException {

  val id = SignInInvalidCredentialsErrorID
}

case class ConsentTokenGatewayAppException(clientError: IdentityClientError)
    extends ServiceGatewayAppException(clientError)
    with ConsentTokenAppException {
  override val id = ResendTokenGatewayErrorID
}

case object ConsentTokenUnauthorizedException
    extends ServiceBadRequestAppException(ClientTokenExpiredError)
    with ConsentTokenAppException {
  override val id = UnauthorizedConsentTokenErrorID
}
