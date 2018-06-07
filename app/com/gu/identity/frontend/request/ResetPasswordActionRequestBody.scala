package com.gu.identity.frontend.request

import com.gu.identity.frontend.errors.{AppException, ForgeryTokenAppException, ResetPasswordActionBadRequestAppException, ResetPasswordInvalidEmailAppException}
import com.gu.identity.frontend.request.RequestParameters.{CSRFTokenRequestParameter, GaClientIdRequestParameter}
import play.api.data.{Form, FormError}


case class ResetPasswordActionRequestBody(
    email: String,
    csrfToken: String,
    gaClientId: Option[String])
  extends CSRFTokenRequestParameter
  with GaClientIdRequestParameter

class ResetPasswordActionRequestBodyParser(formRequestBodyParser: FormRequestBodyParser) {

  lazy val bodyParser =
    formRequestBodyParser.form("ResetPasswordActionRequestBody")(_ => resetPasswordForm)(handleFormErrors)

  private lazy val resetPasswordForm = Form(FormMapping.resetPasswordMapping)

  private def handleFormErrors(formError: FormError): AppException = formError match {
    case FormError("email", messages, _) => ResetPasswordInvalidEmailAppException(messages.headOption.getOrElse("Unknown"))
    case FormError("csrfToken", _, _) => ForgeryTokenAppException("Missing csrfToken on request")
    case e => ResetPasswordActionBadRequestAppException(e.message)
  }

  private object FormMapping {
    import play.api.data.Forms._

    val resetPasswordMapping =
      mapping(
        "email" -> email,
        "csrfToken" -> text,
        "gaClientId" -> optional(text)
      )(ResetPasswordActionRequestBody.apply)(ResetPasswordActionRequestBody.unapply)
  }
}
