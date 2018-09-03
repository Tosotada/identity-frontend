package com.gu.identity.frontend.errors


sealed trait ErrorID {
  val key: String
}

object ErrorIDs {

  case object SignInGatewayErrorID extends ErrorID {
    val key = "signin-error-gateway"
  }

  case object SignInBadRequestErrorID extends ErrorID {
    val key = "signin-error-bad-request"
  }

  case object SignInInvalidCredentialsErrorID extends ErrorID {
    val key = "signin-error-credentials"
  }

  case object SignInInvalidCaptchaErrorID extends ErrorID {
    val key = "signin-error-captcha"
  }

  case object SignInActionBadRequestErrorID extends ErrorID {
    val key = "signin-error-action-bad-request"
  }


  case object RegisterGatewayErrorID extends ErrorID {
    val key = "register-error-gateway"
  }

  case object RegisterBadRequestErrorID extends ErrorID {
    val key = "register-error-bad-request"
  }

  case object RegisterEmailConflictErrorID extends ErrorID {
    val key = "register-error-email-conflict"
  }

  case object RegisterEmailConflictHydratedErrorID extends ErrorID {
    val key = "register-error-email-conflict-hydrated"
  }

  case object RegisterEmailReservedErrorID extends ErrorID {
    val key = "register-error-email-reserved"
  }
  case object RegisterActionInvalidFirstNameErrorID extends ErrorID {
    val key = "register-error-firstname"
  }

  case object RegisterActionInvalidLastNameErrorID extends ErrorID {
    val key = "register-error-lastname"
  }

  case object RegisterActionInvalidEmailErrorID extends ErrorID {
    val key = "register-error-email"
  }

  case object RegisterActionInvalidPasswordErrorID extends ErrorID {
    val key = "register-error-password"
  }

  case object RegisterActionInvalidGroupErrorID extends ErrorID {
    val key = "register-error-groupCode"
  }

  case object RegisterActionBadRequestErrorID extends ErrorID {
    val key = "register-error-bad-request"
  }


  /**
   * Note: externally generated error from Federation API
   */
  case object SocialRegistrationFacebookEmailErrorID extends ErrorID {
    val key = "fbEmail"
  }



  case object ResetPasswordGatewayErrorID extends ErrorID {
    val key = "reset-password-error-gateway"
  }

  case object ResetPasswordBadRequestErrorID extends ErrorID {
    val key = "reset-password-error-bad-request"
  }

  case object ResetPasswordNoAccountErrorID extends ErrorID {
    val key = "reset-password-error-no-account"
  }

  case object ResetPasswordInvalidEmailErrorID extends ErrorID {
    val key = "reset-password-error-email"
  }


  case object DeauthenticateGatewayErrorID extends ErrorID {
    val key = "deauthenticate-error-gateway"
  }

  case object ResendTokenGatewayErrorID extends ErrorID {
    val key = "token-error-gateway"
  }

  case object ResendTokenEmailGatewayErrorID extends ErrorID {
    val key = "token-error-gateway"
  }

  case object ResendTokenEmailBadRequestID extends ErrorID {
    val key = "token-error-bad-request"
  }

  case object ResendTokenEmailBadTokenRequestId extends ErrorID {
    val key = "token-error-bad-token"
  }

  case object UnauthorizedConsentTokenErrorID extends ErrorID {
    val key = "unauthorized-consent-token-error-gateway"
  }

  case object DeauthenticateBadRequestErrorID extends ErrorID {
    val key = "deauthenticate-error-bad-request"
  }

  case object RepermissionTokenGatewayErrorID extends ErrorID {
    val key = "repermission-token-error-gateway"
  }

  case object RepermissionConsentTokenErrorID extends ErrorID {
    val key = "unauthorized-consent-token-error-gateway"
  }

  case object UnauthorizedRepermissionTokenErrorID extends ErrorID {
    val key = "unauthorized-repermission-token-error-gateway"
  }

  case object ChangeEmailTokenGatewayErrorID extends ErrorID {
    val key = "change-email-token-error-gateway"
  }

  case object UnauthorizedChangeEmailTokenErrorID extends ErrorID {
    val key = "unauthorized-change-email-token-error-gateway"
  }

  case object GetUserGatewayErrorID extends ErrorID {
    val key = "getuser-error-gateway"
  }

  case object GetUserBadRequestErrorID extends ErrorID {
    val key = "getuser-error-bad-request"
  }

  case object GetUserUnauthorizedExceptionID extends ErrorID {
    val key = "getuser-unauthorized"
  }


  case object AssignGroupGatewayErrorID extends ErrorID {
    val key = "assigngroup-error-gateway"
  }

  case object AssignGroupBadRequestErrorID extends ErrorID {
    val key = "assigngroup-error-bad-request"
  }


  case object ForgeryTokenErrorID extends ErrorID {
    val key = "error-forgery-token"
  }

  case object RateLimitedErrorID extends ErrorID {
    val key = "rate-limited"
  }

  case object UnexpectedErrorID extends ErrorID {
    val key = "error-unexpected"
  }

}
