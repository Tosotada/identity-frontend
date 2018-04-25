package com.gu.identity.frontend.request

import com.gu.identity.frontend.errors.SignInInvalidCredentialsAppException
import com.gu.identity.frontend.models.ClientID
import play.api.data.{Form, Forms, Mapping}
import play.api.data.Forms.{email, nonEmptyText, optional}
import play.api.mvc.BodyParser

case class EmailResubscribeRequest(email: String, returnUrl: Option[String], clientId: Option[ClientID])

trait EmailResubRequests {

  import ClientID.FormMapping.clientId

  protected lazy val emailResubFormMapping: Mapping[EmailResubscribeRequest] = Forms.mapping(
    "email" -> email,
    "returnUrl" -> optional(nonEmptyText),
    "clientId" -> optional(clientId)
  )(EmailResubscribeRequest.apply)(EmailResubscribeRequest.unapply)
  protected lazy val emailResubForm: Form[EmailResubscribeRequest] = Form(emailResubFormMapping)
  protected lazy val emailResubFormParser: BodyParser[EmailResubscribeRequest] = FormRequestBodyParser("email_signin")(_ => emailResubForm)(e => SignInInvalidCredentialsAppException)

}

object EmailResubRequests extends EmailResubRequests
