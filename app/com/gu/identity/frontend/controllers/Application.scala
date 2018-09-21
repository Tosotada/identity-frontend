package com.gu.identity.frontend.controllers

import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.logging.Logging
import com.gu.identity.frontend.models._
import com.gu.identity.frontend.mvt.MultiVariantTestAction
import com.gu.identity.frontend.views.ViewRenderer._
import com.gu.identity.model.{CurrentUser, GuestUser, NewUser}
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.filters.csrf.CSRF


class Application(
  configuration: Configuration,
  cc: ControllerComponents,
  multiVariantTestAction: MultiVariantTestAction
) extends AbstractController(cc) with Logging with I18nSupport {

  def twoStepSignInStart(error: Seq[String], returnUrl: Option[String], skipConfirmation: Option[Boolean], clientId: Option[String], group: Option[String], skipValidationReturn: Option[Boolean]) =
    multiVariantTestAction { implicit req =>
      val clientIdActual = ClientID(clientId)
      val returnUrlActual = ReturnUrl(returnUrl, req.headers.get("Referer"), configuration, clientIdActual)
      val csrfToken = CSRF.getToken(req)
      val groupCode = GroupCode(group)
      val email : Option[String] = req.getQueryString("email")
      renderTwoStepSignInStart(configuration, req.activeTests, csrfToken, error, returnUrlActual, skipConfirmation, clientIdActual, groupCode, email, skipValidationReturn)
    }

  def twoStepSignInChoices(signInType: String, error: Seq[String], returnUrl: Option[String], skipConfirmation: Option[Boolean], clientId: Option[String], group: Option[String], skipValidationReturn: Option[Boolean]) =
    multiVariantTestAction { implicit req =>
      val _clientId = ClientID(clientId)
      val returnUrlActual = ReturnUrl(returnUrl, req.headers.get("Referer"), configuration, _clientId)
      val csrfToken = CSRF.getToken(req)
      val groupCode = GroupCode(group)
      val email : Option[String] = req.cookies.get("GU_SIGNIN_EMAIL").map(_.value)
      val userType = Seq(CurrentUser, GuestUser, NewUser).find(_.name == signInType)
      val intcmp = req.getQueryString("INTCMP")

      renderTwoStepSignInChoices(configuration, req.activeTests, csrfToken, error, userType, returnUrlActual, skipConfirmation, _clientId, groupCode, email, skipValidationReturn, intcmp)
    }

  def sendResubLink(error: Seq[String], clientId: Option[String]) = Action { implicit req =>
    val csrfToken = CSRF.getToken(req)
    val clientIdOpt = ClientID(clientId)
    renderResubLink(configuration, clientIdOpt, error, csrfToken)
  }

  def sendResubLinkSent(clientId: Option[String], emailProvider: Option[String]) = Action { implicit request =>
    val clientIdOpt = ClientID(clientId)
    val emailProviderOpt = EmailProvider(emailProvider)
    renderSendSignInLinkSent(configuration, clientIdOpt, emailProviderOpt)
  }

  def reset(error: Seq[String], clientId: Option[String], returnUrl: Option[String]) = Action { implicit req =>
    val clientIdOpt = ClientID(clientId)
    val csrfToken = CSRF.getToken(req)
    val email: Option[String] = req.cookies.get("GU_SIGNIN_EMAIL").map(_.value)
    val _returnUrl = returnUrl.map(url => ReturnUrl(Some(url), req.headers.get("Referer"), configuration, clientIdOpt))

    renderResetPassword(configuration, error, csrfToken, email, resend = false, clientIdOpt, _returnUrl)
  }

  def resetResend(error: Seq[String], clientId: Option[String]) = Action { implicit req =>
    val clientIdOpt = ClientID(clientId)
    val csrfToken = CSRF.getToken(req)
    val email: Option[String] = req.cookies.get("GU_SIGNIN_EMAIL").map(_.value)

    renderResetPassword(configuration, error, csrfToken, email, resend = true, clientIdOpt)
  }

  def resetPasswordEmailSent(clientId: Option[String], emailProvider: Option[String]) = Action { implicit request =>
    val clientIdOpt = ClientID(clientId)
    val emailProviderOpt = EmailProvider(emailProvider)
    renderResetPasswordEmailSent(configuration, clientIdOpt, emailProviderOpt)
  }

  def changeEmail(clientId: Option[String]) = Action { implicit request =>
    val clientIdOpt = ClientID(clientId)
    renderEmailChange(configuration, clientIdOpt)
  }

  def invalidConsentToken(errorIds: Seq[String], token: String) = Action { implicit req =>
    val csrfToken = CSRF.getToken(req)
    renderInvalidConsentToken(configuration, token, csrfToken, errorIds)
  }

  def resendConsentTokenSent(error: Seq[String]) = Action { implicit req =>
    val csrfToken = CSRF.getToken(req)
    renderResendTokenSent(configuration, csrfToken, error)
  }

  def resendRepermissionTokenSent(error: Seq[String]) = Action { implicit req =>
    val csrfToken = CSRF.getToken(req)
    renderResendTokenSent(configuration, csrfToken, error)
  }

  def collectConsents(returnUrl: Option[String], clientId: Option[String]) = Action { implicit req =>
    val clientIdOpt = ClientID(clientId)
    val _returnUrl = returnUrl.getOrElse(configuration.dotcomBaseUrl)
    renderCollectConsents(configuration, clientIdOpt, _returnUrl)
  }

  //TODO: This is a placeholder until a generic invalid-token page is made for general token use
  def invalidRepermissioningToken(token: String) = Action { implicit req =>
    val csrfToken = CSRF.getToken(req)
    renderInvalidRepermissionToken(configuration, token, csrfToken)
  }
}
