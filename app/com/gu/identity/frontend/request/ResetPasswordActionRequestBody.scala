package com.gu.identity.frontend.request

import com.gu.identity.frontend.errors.{ResetPasswordActionBadRequestAppException, ForgeryTokenAppException, ResetPasswordInvalidEmailAppException, AppException}
import com.gu.identity.frontend.request.RequestParameters.CSRFTokenRequestParameter
import play.api.data.{FormError, Form}


case class ResetPasswordActionRequestBody(
    email: String,
    csrfToken: String)
  extends CSRFTokenRequestParameter


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
        "csrfToken" -> text
      )(ResetPasswordActionRequestBody.apply)(ResetPasswordActionRequestBody.unapply)
  }
}
