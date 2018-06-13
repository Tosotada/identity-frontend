package com.gu.identity.service.client.request

import com.gu.identity.frontend.models.{ClientIp, TrackingData}
import com.gu.identity.frontend.request.RegisterActionRequestBody
import com.gu.identity.service.client._
import com.gu.identity.model.Consent


case class RegisterApiRequest(url: String, extraHeaders: HttpParameters = Nil, trackingData: TrackingData, override val body: Option[ApiRequestBody]) extends ApiRequest {
  override val method = POST
  override val headers = Seq("Content-Type" -> "application/json") ++ extraHeaders
  override val parameters = trackingData.parameters
}

object RegisterApiRequest {
  def apply(request: RegisterActionRequestBody, clientIp: ClientIp, trackingData: TrackingData)(implicit configuration: IdentityClientConfiguration): RegisterApiRequest = {
    RegisterApiRequest(
      ApiRequest.apiEndpoint("user"),
      body = Some(RegisterRequestBody(
        request.email,
        request.password,
        RegisterRequestBodyPublicFields(request.displayName),
        RegisterRequestBodyPrivateFields(
          firstName = request.firstName,
          secondName = request.lastName,
          registrationIp = clientIp.ip,
          telephoneNumber = getPhoneNumber(request)
        ),
        RegisterRequestBodyStatusFields(
          receiveGnmMarketing = false,
          receive3rdPartyMarketing = false
        ),
        request.consents
      )),
      extraHeaders = ApiRequest.commonApiHeaders(trackingData),
      trackingData = trackingData
    )
  }

  private def getPhoneNumber(request: RegisterActionRequestBody) : Option[RegisterRequestTelephoneNumber] =
    (request.countryCode, request.localNumber) match {
      case(Some(countryCode), Some(localNumber)) => Option(RegisterRequestTelephoneNumber(countryCode, localNumber))
      case(None, Some(localNumber)) => Option(RegisterRequestTelephoneNumber("", localNumber))
      case _ => None
    }
}

case class RegisterRequestBody(
    primaryEmailAddress: String,
    password: String,
    publicFields: RegisterRequestBodyPublicFields,
    privateFields: RegisterRequestBodyPrivateFields,
    statusFields: RegisterRequestBodyStatusFields,
    consents: List[Consent])
  extends ApiRequestBody

case class RegisterRequestBodyPublicFields(displayName: String)

case class RegisterRequestBodyPrivateFields(
    firstName: String,
    secondName: String,
    registrationIp: String,
    telephoneNumber: Option[RegisterRequestTelephoneNumber] = None)

case class RegisterRequestBodyStatusFields(
    receiveGnmMarketing: Boolean = false,
    receive3rdPartyMarketing: Boolean = false)

case class RegisterRequestTelephoneNumber(countryCode: String, localNumber: String)
