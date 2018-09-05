package com.gu.identity.frontend.errors

import com.gu.identity.frontend.errors.ErrorIDs.{AssignGroupBadRequestErrorID, AssignGroupGatewayErrorID}
import com.gu.identity.service.client.{ClientBadRequestError, ClientGatewayError, ClientUnauthorizedError, IdentityClientError}


sealed trait AssignGroupAppException extends AppException

object AssignGroupAppException {
  def apply(clientError: IdentityClientError): AssignGroupAppException =
    clientError match {
      case _: ClientBadRequestError | _: ClientUnauthorizedError => AssignGroupServiceBadRequestException(clientError)
      case _: ClientGatewayError => AssignGroupServiceGatewayAppException(clientError)
    }
}

case class AssignGroupServiceGatewayAppException(
    clientError: IdentityClientError)
  extends ServiceGatewayAppException(clientError)
  with AssignGroupAppException {

  val id = AssignGroupGatewayErrorID
}


case class AssignGroupServiceBadRequestException(
    clientError: IdentityClientError)
  extends ServiceBadRequestAppException(clientError)
  with AssignGroupAppException {

  val id = AssignGroupBadRequestErrorID
}
