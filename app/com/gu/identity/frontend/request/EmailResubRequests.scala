package com.gu.identity.frontend.request

import com.gu.identity.frontend.errors.SignInInvalidCredentialsAppException
import com.gu.identity.frontend.models.ClientID
import com.gu.identity.frontend.request.RequestParameters.GaClientIdRequestParameter
import play.api.data.{Form, Forms, Mapping}
import play.api.data.Forms.{email, nonEmptyText, optional}
import play.api.mvc.BodyParser

case class EmailResubscribeRequest(email: String, returnUrl: Option[String], clientId: Option[ClientID], gaClientId: Option[String]) extends GaClientIdRequestParameter

class EmailResubRequestsParser(formRequestBodyParser: FormRequestBodyParser) {

  import ClientID.FormMapping.clientId

  private val emailResubFormMapping: Mapping[EmailResubscribeRequest] = Forms.mapping(
    "email" -> email,
    "returnUrl" -> optional(nonEmptyText),
    "clientId" -> optional(clientId),
    "gaClientId" -> optional(nonEmptyText)
  )(EmailResubscribeRequest.apply)(EmailResubscribeRequest.unapply)

  private val emailResubForm: Form[EmailResubscribeRequest] = Form(emailResubFormMapping)

  val bodyParser: BodyParser[EmailResubscribeRequest] =
    formRequestBodyParser.form("email_signin")(_ => emailResubForm)(e => SignInInvalidCredentialsAppException)

}

