package com.gu.identity.frontend.errors

import com.gu.identity.frontend.errors.ErrorIDs._
import com.gu.identity.service.client.{ClientBadRequestError, ClientGatewayError, ClientUnauthorizedError, IdentityClientError}

sealed trait ResendTokenException extends AppException

object ResendTokenException {
  def apply(clientError: IdentityClientError): ResendTokenException =
    clientError match {
      case _: ClientBadRequestError => ResendTokenBadRequestException(clientError)
      case _: ClientGatewayError | _: ClientUnauthorizedError  => ResendTokenServiceGatewayExeption(clientError)
    }
}


case class ResendTokenServiceGatewayExeption(clientError: IdentityClientError)
  extends ServiceGatewayAppException(clientError)
    with ResendTokenException {

  val id = ResendTokenEmailGatewayErrorID
}

case class ResendTokenBadRequestException(clientError: IdentityClientError)
  extends BadRequestAppException
    with ResendTokenException {

  val id = ResendTokenEmailBadRequestID
}

case class ResendTokenBadTokenException(message: String)
  extends AbstractAppException(message)
    with BadRequestAppException
    with ResendTokenException {

  val id = ResendTokenEmailBadTokenRequestId
}

case class ResendTokenBadRequestAppException(message: String)
  extends AbstractAppException(message)
    with BadRequestAppException
    with ResendTokenException {

  val id = ResendTokenEmailBadRequestID
}
