package com.gu.identity.frontend.errors

import com.gu.identity.service.client._
import ErrorIDs._

/**
 * Errors which may occur during Registration process.
 */
sealed trait RegisterAppException extends AppException

// Errors when Registering
sealed trait RegisterServiceAppException extends RegisterAppException

object RegisterServiceAppException {
  def apply(clientError: IdentityClientError): RegisterServiceAppException =
    clientError match {
      case ClientRegistrationEmailValidationError => RegisterEmailInvalidConflictAppException
      case ClientRegistrationEmailConflictError => RegisterEmailConflictAppException
      case ClientRegistrationEmailReservedError => RegisterEmailReservedAppException
      case err: ClientBadRequestError => RegisterServiceBadRequestException(clientError)
      case err: ClientGatewayError => RegisterServiceGatewayAppException(clientError)
    }
}

case class RegisterServiceGatewayAppException(
    clientError: IdentityClientError)
  extends ServiceGatewayAppException(clientError)
  with RegisterServiceAppException {

  val id = RegisterGatewayErrorID
}

case class RegisterServiceBadRequestException(
    clientError: IdentityClientError)
  extends ServiceBadRequestAppException(clientError)
  with RegisterServiceAppException {

  val id = RegisterBadRequestErrorID
}

case object RegisterEmailConflictAppException
  extends ServiceBadRequestAppException(ClientRegistrationEmailConflictError)
  with RegisterServiceAppException {

  val id = RegisterEmailConflictErrorID
}

case object RegisterEmailReservedAppException
  extends ServiceBadRequestAppException(ClientRegistrationEmailReservedError)
    with RegisterServiceAppException {

  val id = RegisterEmailReservedErrorID
}

case object RegisterEmailInvalidConflictAppException
  extends ServiceBadRequestAppException(ClientRegistrationEmailValidationError)
  with RegisterServiceAppException {

  val id = RegisterActionInvalidEmailErrorID
}

case class RegisterActionInvalidFirstNameAppException(message: String)
  extends AbstractAppException(message)
  with BadRequestAppException
  with RegisterAppException {

  val id = RegisterActionInvalidFirstNameErrorID
}

case class RegisterActionInvalidLastNameAppException(message: String)
  extends AbstractAppException(message)
  with BadRequestAppException
  with RegisterAppException {

  val id = RegisterActionInvalidLastNameErrorID
}

case class RegisterActionInvalidEmailAppException(message: String)
  extends AbstractAppException(message)
  with BadRequestAppException
  with RegisterAppException {

  val id = RegisterActionInvalidEmailErrorID
}

case class RegisterActionInvalidPasswordAppException(message: String)
  extends AbstractAppException(message)
  with BadRequestAppException
  with RegisterAppException {

  val id = RegisterActionInvalidPasswordErrorID
}

case class RegisterActionInvalidGroupAppException(message: String)
  extends AbstractAppException(message)
  with BadRequestAppException
  with RegisterAppException {

  val id = RegisterActionInvalidGroupErrorID
}

case class RegisterActionBadRequestAppException(message: String)
  extends AbstractAppException(message)
  with BadRequestAppException
  with RegisterAppException {

  val id = RegisterActionBadRequestErrorID
}
