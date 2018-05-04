package com.gu.identity.frontend.request

import com.gu.identity.frontend.errors._
import com.gu.identity.frontend.request.RequestParameters.CSRFTokenRequestParameter
import play.api.data.{Form, FormError}


case class ResendTokenActionRequestBody(
  token: String,
  csrfToken: String)
  extends CSRFTokenRequestParameter


class ResendTokenActionRequestBodyParser(formRequestBodyParser: FormRequestBodyParser) {

  lazy val bodyParser =
    formRequestBodyParser.form("ResendTokenActionRequestBody")(_ => resendTokenForm)(handleFormErrors)

  private lazy val resendTokenForm = Form(FormMapping.resetMapping)

  private def handleFormErrors(formError: FormError): AppException = formError match {
    case FormError("token", messages, _) => ResendTokenBadTokenException(messages.headOption.getOrElse("Unknown"))
    case FormError("csrfToken", _, _) => ForgeryTokenAppException("Missing csrfToken on request")
    case e => ResendTokenBadRequestAppException(e.message)
  }

  private object FormMapping {
    import play.api.data.Forms._

    val resetMapping =
      mapping(
        "token" -> text,
        "csrfToken" -> text
      )(ResendTokenActionRequestBody.apply)(ResendTokenActionRequestBody.unapply)
  }
}
