package com.gu.identity.frontend.controllers

import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.csrf.{CSRFAddToken, CSRFConfig, CSRFToken}
import com.gu.identity.frontend.logging.Logging
import com.gu.identity.frontend.models.{ClientID, GroupCode, ReturnUrl, SignInType}
import com.gu.identity.frontend.mvt.MultiVariantTestAction
import com.gu.identity.frontend.views.ViewRenderer._
import com.gu.identity.model.{CurrentUser, GuestUser, NewUser}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._


class Application(
  configuration: Configuration,
  val messagesApi: MessagesApi,
  csrfConfig: CSRFConfig
) extends Controller with Logging with I18nSupport {

  def signIn(error: Seq[String], returnUrl: Option[String], skipConfirmation: Option[Boolean], clientId: Option[String], group: Option[String]) = (CSRFAddToken(csrfConfig) andThen MultiVariantTestAction) { req =>
    val clientIdActual = ClientID(clientId)
    val returnUrlActual = ReturnUrl(returnUrl, req.headers.get("Referer"), configuration, clientIdActual)
    val csrfToken = CSRFToken.fromRequest(csrfConfig, req)
    val groupCode = GroupCode(group)
    val email : Option[String] = req.getQueryString("email")

    renderSignIn(configuration, req.activeTests, csrfToken, error, returnUrlActual, skipConfirmation, clientIdActual, groupCode, email)
  }

  def twoStepSignInStart(error: Seq[String], returnUrl: Option[String], skipConfirmation: Option[Boolean], clientId: Option[String], group: Option[String]) = (CSRFAddToken(csrfConfig) andThen MultiVariantTestAction) { req =>
    val clientIdActual = ClientID(clientId)
    val returnUrlActual = ReturnUrl(returnUrl, req.headers.get("Referer"), configuration, clientIdActual)
    val csrfToken = CSRFToken.fromRequest(csrfConfig, req)
    val groupCode = GroupCode(group)
    val email : Option[String] = req.getQueryString("email")

    renderTwoStepSignInStart(configuration, req.activeTests, csrfToken, error, returnUrlActual, skipConfirmation, clientIdActual, groupCode, email)
  }

  def twoStepSignInChoices(signInType: String, error: Seq[String], returnUrl: Option[String], skipConfirmation: Option[Boolean], clientId: Option[String], group: Option[String]) = (CSRFAddToken(csrfConfig) andThen MultiVariantTestAction) { req =>
    val clientIdActual = ClientID(clientId)
    val returnUrlActual = ReturnUrl(returnUrl, req.headers.get("Referer"), configuration, clientIdActual)
    val csrfToken = CSRFToken.fromRequest(csrfConfig, req)
    val groupCode = GroupCode(group)
    val email : Option[String] = req.cookies.get("GU_SIGNIN_EMAIL").map(_.value)
    val userType = Seq(CurrentUser, GuestUser, NewUser).find(_.name == signInType)

    renderTwoStepSignInChoices(configuration, req.activeTests, csrfToken, error, userType, returnUrlActual, skipConfirmation, clientIdActual, groupCode, email)
  }

  def register(error: Seq[String], returnUrl: Option[String], skipConfirmation: Option[Boolean], clientId: Option[String], group: Option[String], signInType: Option[String]) = (CSRFAddToken(csrfConfig) andThen MultiVariantTestAction) { implicit req =>
    val clientIdActual = ClientID(clientId)
    val returnUrlActual = ReturnUrl(returnUrl, req.headers.get("Referer"), configuration, clientIdActual)
    val signInTypeActual = SignInType(signInType)
    val csrfToken = CSRFToken.fromRequest(csrfConfig, req)
    val groupCode = GroupCode(group)
    val email : Option[String] = req.cookies.get("GU_SIGNIN_EMAIL").map(_.value)
    val shouldCollectConsents = configuration.collectSignupConsents
    val shouldCollectV2Consents = configuration.collectV2Consents

    renderRegister(configuration, req.activeTests, error, csrfToken, returnUrlActual, skipConfirmation, clientIdActual, groupCode, email, signInTypeActual, shouldCollectConsents, shouldCollectV2Consents)
  }

  def sendResubLink(error: Seq[String], clientId: Option[String]) = CSRFAddToken(csrfConfig) { req =>
    val csrfToken = CSRFToken.fromRequest(csrfConfig, req)
    val clientIdOpt = ClientID(clientId)
    renderResubLink(configuration, clientIdOpt, error, csrfToken)
  }

  def sendResubLinkSent(clientId: Option[String]) = Action {
    val clientIdOpt = ClientID(clientId)
    renderSendSignInLinkSent(configuration, clientIdOpt)
  }

  def reset(error: Seq[String], clientId: Option[String]) = CSRFAddToken(csrfConfig) { req =>
    val clientIdOpt = ClientID(clientId)
    val csrfToken = CSRFToken.fromRequest(csrfConfig, req)
    val email: Option[String] = req.cookies.get("GU_SIGNIN_EMAIL").map(_.value)

    renderResetPassword(configuration, error, csrfToken, email, resend = false, clientIdOpt)
  }

  def resetResend(error: Seq[String], clientId: Option[String]) = CSRFAddToken(csrfConfig) { req =>
    val clientIdOpt = ClientID(clientId)
    val csrfToken = CSRFToken.fromRequest(csrfConfig, req)
    val email: Option[String] = req.cookies.get("GU_SIGNIN_EMAIL").map(_.value)

    renderResetPassword(configuration, error, csrfToken, email, resend = true, clientIdOpt)
  }

  def resetPasswordEmailSent(clientId: Option[String]) = Action {
    val clientIdOpt = ClientID(clientId)
    renderResetPasswordEmailSent(configuration, clientIdOpt)
  }

  def invalidConsentToken(errorIds: Seq[String], token: String) = CSRFAddToken(csrfConfig)  { req =>
    val csrfToken = CSRFToken.fromRequest(csrfConfig, req)
    renderInvalidConsentToken(configuration, token, csrfToken, errorIds)
  }

  def resendConsentTokenSent(error: Seq[String]) = CSRFAddToken(csrfConfig)  { req =>
    val csrfToken = CSRFToken.fromRequest(csrfConfig, req)
    renderResendTokenSent(configuration, csrfToken, error)
  }

  def resendRepermissionTokenSent(error: Seq[String]) = CSRFAddToken(csrfConfig)  { req =>
    val csrfToken = CSRFToken.fromRequest(csrfConfig, req)
    renderResendTokenSent(configuration, csrfToken, error)
  }

  //TODO: This is a placeholder until a generic invalid-token page is made for general token use
  def invalidRepermissioningToken(token: String) = CSRFAddToken(csrfConfig)  { req =>
    val csrfToken = CSRFToken.fromRequest(csrfConfig, req)
    renderInvalidRepermissionToken(configuration, token, csrfToken)
  }
}
