package com.gu.identity.frontend.views.models

import com.gu.identity.frontend.errors.{ErrorID, ErrorIDs}

case class ErrorViewModel(id: String, message: String) extends ViewModel

object ErrorViewModel {

  import ErrorIDs._

  def apply(id: String): ErrorViewModel = {
    ErrorViewModel(id, getErrorMessage(id))
  }

  val errorMessages: Map[ErrorID, String] = Map(
    SignInGatewayErrorID -> "There was a problem signing in; please try again.",
    SignInBadRequestErrorID -> "There was a problem signing in; please try again.",
    SignInInvalidCredentialsErrorID -> "Incorrect email or password; please try again or reset your password.",
    SignInInvalidCaptchaErrorID -> "Problem with the captcha process; please try again.",
    SignInActionBadRequestErrorID -> "There was a problem with your request; please try again.",

    RegisterGatewayErrorID -> "There was a problem creating your account; please try again.",
    RegisterBadRequestErrorID -> "One or more inputs was not valid; please try again.",
    RegisterActionBadRequestErrorID -> "One or more inputs was not valid; please try again.",
    RegisterEmailConflictErrorID -> "Looks like you may already have a Guardian account. Please <a href=\"/signin\">sign in</a>, <a href=\"/reset\">set or reset your password</a> or use a different email address.",
    RegisterEmailReservedErrorID -> "This email is not available. Please use another email address.",
    RegisterActionInvalidFirstNameErrorID -> (minMaxLength("First name", 1, 25) + " " + invalidNameCharacters("First name")),
    RegisterActionInvalidLastNameErrorID ->(minMaxLength("Last name", 1, 25) + " " + invalidNameCharacters("Last name")),
    RegisterActionInvalidEmailErrorID -> "Invalid email address; please try again.",
    RegisterActionInvalidDisplayNameErrorID -> (minMaxLength("Display name", 2, 50) + " " + invalidNameCharacters("Display name")),
    RegisterActionInvalidPasswordErrorID -> "Invalid password; your password must be between 6 and 72 characters long.",

    SocialRegistrationFacebookEmailErrorID -> "We need your email address when you sign in with Facebook so that we can keep in touch (you can choose which emails you receive in your account settings). Try again and allow access to your email address or provide it manually below.",

    ResetPasswordGatewayErrorID -> "There was a problem resetting your password; please try again",
    ResetPasswordBadRequestErrorID -> "There was a problem resetting your password; please try again.",
    ResetPasswordNoAccountErrorID -> "There is no account for that email address, please check for typos or create an account.",
    ResetPasswordInvalidEmailErrorID -> nonEmptyField("email"),

    RateLimitedErrorID -> "Too many sign in attempts. Please try again shortly.",

    UnauthorizedRepermissionTokenErrorID -> "Token is invalid",

    ForgeryTokenErrorID -> "Forgery token invalid; please try again.",
    UnexpectedErrorID -> "There was an unexpected problem; please try again."
  )

  lazy val errorMessagesByKey: Map[String, String] =
    errorMessages.map {
      case (k, v) => k.key -> v
    }

  private def nonEmptyField(fieldName: String) = s"${fieldName.capitalize} field must not be blank."

  private def minMaxLength(fieldName: String, min: Int, max: Int) = s"${fieldName.capitalize} field must be between $min and $max characters long."

  private def invalidNameCharacters(fieldName: String) = s"${fieldName.capitalize} field may not contain the characters : or /."

  val default: String = "There was an unexpected problem; please try again."

  private def getErrorMessage(id: String) = errorMessagesByKey.getOrElse(id, default)
}
