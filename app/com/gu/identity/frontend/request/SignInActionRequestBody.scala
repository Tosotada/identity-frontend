package com.gu.identity.frontend.request

import com.gu.identity.frontend.errors._
import com.gu.identity.frontend.models.{ClientID, GroupCode, ReturnUrl}
import com.gu.identity.frontend.request.RequestParameters._
import play.api.data.{Mapping, FormError, Form}
import play.api.mvc.RequestHeader
import play.api.http.HeaderNames


case class SignInActionRequestBody private(
    email: String,
    password: String,
    rememberMe: Boolean,
    returnUrl: Option[ReturnUrl],
    skipConfirmation: Option[Boolean],
    clientId: Option[ClientID],
    groupCode: Option[GroupCode],
    csrfToken: String,
    gaClientId: Option[String],
    skipValidationReturn: Option[Boolean])
  extends SignInRequestParameters
  with ReturnUrlRequestParameter
  with SkipConfirmationRequestParameter
  with ClientIdRequestParameter
  with GroupRequestParameter
  with CSRFTokenRequestParameter
  with GaClientIdRequestParameter


class SignInActionRequestBodyParser(formRequestBodyParser: FormRequestBodyParser) {

  val bodyParser =
    formRequestBodyParser.form("SignInActionRequestBody")(signInForm)(handleFormErrors)


  private def signInForm(requestHeader: RequestHeader): Form[SignInActionRequestBody] =
    signInForm(requestHeader.headers.get(HeaderNames.REFERER))

  private def signInForm(refererHeader: Option[String]): Form[SignInActionRequestBody] =
    Form {
      FormMapping.signInFormMapping(refererHeader)
    }


  private def handleFormErrors(formError: FormError): AppException = formError match {
    case FormError("email", _, _) => SignInInvalidCredentialsAppException
    case FormError("password", _, _) => SignInInvalidCredentialsAppException
    case FormError("csrfToken", _, _) => ForgeryTokenAppException("Missing csrfToken on request")
    case e => SignInActionBadRequestAppException(s"Unexpected error: ${e.message}")
  }


  private object FormMapping {
    import play.api.data.Forms.{boolean, default, mapping, optional, text}
    import ClientID.FormMapping.clientId
    import GroupCode.FormMappings.groupCode
    import ReturnUrl.FormMapping.returnUrl

    def signInFormMapping(refererHeader: Option[String]): Mapping[SignInActionRequestBody] =
      mapping(
        "email" -> text,
        "password" -> text,
        "rememberMe" -> default(boolean, false),
        "returnUrl" -> returnUrl(refererHeader),
        "skipConfirmation" -> optional(boolean),
        "clientId" -> optional(clientId),
        "groupCode" -> optional(groupCode),
        "csrfToken" -> text,
        "gaClientId" -> optional(text),
        "skipValidationReturn" -> optional(boolean)
      )(SignInActionRequestBody.apply)(SignInActionRequestBody.unapply)

  }
}
