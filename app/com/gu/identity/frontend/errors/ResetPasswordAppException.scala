package com.gu.identity.frontend.errors

import com.gu.identity.frontend.errors.ErrorIDs.{ResetPasswordBadRequestErrorID, ResetPasswordGatewayErrorID, ResetPasswordInvalidEmailErrorID, ResetPasswordNoAccountErrorID}
import com.gu.identity.service.client._

sealed trait ResetPasswordAppException extends AppException

object ResetPasswordAppException {
  def apply(clientError: IdentityClientError): ResetPasswordAppException =
    clientError match {
      case ResourceNotFoundError => ResetPasswordServiceNoAccountAppException(ResourceNotFoundError)
      case _: ClientBadRequestError => ResetPasswordServiceBadRequestAppException(clientError)
      case _: ClientGatewayError | _: ClientUnauthorizedError  => ResetPasswordServiceGatewayAppException(clientError)
    }
}

case class ResetPasswordServiceGatewayAppException(
    clientError: IdentityClientError)
  extends ServiceGatewayAppException(clientError)
  with ResetPasswordAppException {

  val id = ResetPasswordGatewayErrorID
}

case class ResetPasswordServiceNoAccountAppException(
  clientError: IdentityClientError)
  extends ServiceBadRequestAppException(clientError)
    with ResetPasswordAppException {

  val id = ResetPasswordNoAccountErrorID
}

case class ResetPasswordServiceBadRequestAppException(
    clientError: IdentityClientError)
  extends ServiceBadRequestAppException(clientError)
  with ResetPasswordAppException {

  val id = ResetPasswordBadRequestErrorID
}

case class ResetPasswordActionBadRequestAppException(message: String)
  extends AbstractAppException(message)
  with BadRequestAppException
  with ResetPasswordAppException {

  val id = ResetPasswordBadRequestErrorID
}

case class ResetPasswordInvalidEmailAppException(message: String)
  extends AbstractAppException(message)
  with BadRequestAppException
  with ResetPasswordAppException {

  val id = ResetPasswordInvalidEmailErrorID
}
