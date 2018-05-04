package com.gu.identity.frontend.request

import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.errors._
import com.gu.identity.frontend.models.{ClientID, GroupCode, ReturnUrl}
import com.gu.identity.frontend.request.RequestParameters._
import play.api.data.Forms._
import play.api.data.validation._
import play.api.data.{Form, FormError, Mapping}
import play.api.http.HeaderNames
import play.api.mvc.{BodyParser, BodyParsers, RequestHeader, Result}
import com.gu.identity.model.{Consent, ConsentUnapply}

import scala.util.matching.Regex


case class RegisterActionRequestBody private(
    firstName: String,
    lastName: String,
    email: String,
    displayName: String,
    password: String,
    countryCode: Option[String],
    localNumber: Option[String],
    receiveGnmMarketing: Boolean,
    receive3rdPartyMarketing: Boolean,
    consents: List[Consent],
    returnUrl: Option[ReturnUrl],
    skipConfirmation: Option[Boolean],
    skipValidationReturn: Option[Boolean],
    groupCode: Option[GroupCode],
    clientId: Option[ClientID],
    csrfToken: String,
    gaClientId: Option[String])
  extends SignInRequestParameters
  with ReturnUrlRequestParameter
  with SkipConfirmationRequestParameter
  with ClientIdRequestParameter
  with GroupRequestParameter
  with CSRFTokenRequestParameter
  with GaClientIdRequestParameter {

  // activate "rememberMe" on registrations
  val rememberMe = true
}

class RegisterActionRequestBodyParser(formRequestBodyParser: FormRequestBodyParser) {

  lazy val bodyParser: BodyParser[RegisterActionRequestBody] =
    formRequestBodyParser.form("RegisterActionRequestBody")(registerForm)(handleFormErrors)

  private def registerForm(requestHeader: RequestHeader): Form[RegisterActionRequestBody] =
    registerForm(requestHeader.headers.get(HeaderNames.REFERER))

  private def registerForm(refererHeader: Option[String]): Form[RegisterActionRequestBody] =
    Form(RegisterActionRequestBodyFormMapping.registerFormMapping(refererHeader))


  private def handleFormErrors(formError: FormError): AppException = formError match {
    case FormError("csrfToken", _, _) => ForgeryTokenAppException("Missing csrfToken on request")
    case FormError("firstName", msg, _) => RegisterActionInvalidFirstNameAppException(msg.headOption.getOrElse("unknown"))
    case FormError("lastName", msg, _) => RegisterActionInvalidLastNameAppException(msg.headOption.getOrElse("unknown"))
    case FormError("email", msg, _) => RegisterActionInvalidEmailAppException(msg.headOption.getOrElse("unknown"))
    case FormError("displayName", msg, _) => RegisterActionInvalidDisplayNameAppException(msg.headOption.getOrElse("unknown"))
    case FormError("password", msg, _) => RegisterActionInvalidPasswordAppException(msg.headOption.getOrElse("unknown"))
    case FormError("groupCode", msg, _) => RegisterActionInvalidGroupAppException(msg.headOption.getOrElse("unknown"))
    case e => RegisterActionBadRequestAppException(s"Unexpected error: ${e.message}")
  }


}

object RegisterActionRequestBodyFormMapping {
    import ClientID.FormMapping.clientId
    import GroupCode.FormMappings.groupCode
    import ReturnUrl.FormMapping.returnUrl

    // This regex is based on the one used by WebKit for html email validation, documented here:
    // https://html.spec.whatwg.org/#valid-e-mail-address
    // But with the additional constraint that the domain must not be dotless
    // RFC spec here: https://tools.ietf.org/html/rfc2822#section-3.4.1
    val atext = """a-zA-Z0-9!#$%&'*+\/=?^_`{|}~-"""
    val dotlessDomainEmailRegex: Regex = s"""^([$atext]|[$atext][.$atext]*[$atext])@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\\.(?:[a-zA-Z0-9\\.](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$$""".r

    def dotlessDomainEmail: Mapping[String] = email.verifying(
      Constraints.pattern(dotlessDomainEmailRegex)
    )
    def validNameText(min : Int, max: Int): Mapping[String] = text.verifying(
      Constraints.minLength(min),
      Constraints.maxLength(max),
      Constraints.pattern("^[^/:]*$".r)
    )

    private val password: Mapping[String] = text.verifying(
      "error.password", pwd => pwd.length > 5 && pwd.length < 73
    )

    def registerFormMapping(refererHeader: Option[String]): Mapping[RegisterActionRequestBody] =
      mapping(
        "firstName" -> validNameText(1, 25),
        "lastName" -> validNameText(1, 25),
        "email" -> dotlessDomainEmail,
        "displayName" -> validNameText(2, 50),
        "password" -> password,
        "countryCode" -> optional(text),
        "localNumber" -> optional(text),
        "receiveGnmMarketing" -> boolean,
        "receive3rdPartyMarketing" -> boolean,
        "consents" -> list(
          mapping(
            "id" -> text,
            "actor" -> text,
            "consented" -> boolean
          )(Consent.apply)(ConsentUnapply.unapply)
        ),
        "returnUrl" -> returnUrl(refererHeader),
        "skipConfirmation" -> optional(boolean),
        "skipValidationReturn" -> optional(boolean),
        "groupCode" -> optional(groupCode),
        "clientId" -> optional(clientId),
        "csrfToken" -> text,
        "gaClientId" -> optional(text)
      )(RegisterActionRequestBody.apply)(RegisterActionRequestBody.unapply)
  }
