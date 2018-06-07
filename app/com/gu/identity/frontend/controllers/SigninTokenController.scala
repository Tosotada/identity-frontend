package com.gu.identity.frontend.controllers

import com.gu.identity.frontend.analytics.AnalyticsEventActor
import com.gu.identity.frontend.analytics.client.ResubAuthenticationSuccess
import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.errors.{RedirectOnError, SigninTokenRejected}
import com.gu.identity.frontend.logging.{LogOnErrorAction, Logging}
import com.gu.identity.frontend.services.{IdentityService, ServiceAction}
import com.gu.identity.frontend.views.ViewRenderer
import play.api.i18n.I18nSupport
import play.api.mvc._

import scala.concurrent.ExecutionContext

class SigninTokenController(
  configuration: Configuration,
  identityService: IdentityService,
  cc: ControllerComponents,
  eventActor: AnalyticsEventActor,
  implicit val executionContext: ExecutionContext
) extends AbstractController(cc) with Logging with I18nSupport {

  def signinWithResubToken(token: String, returnUrl: Option[String]) = Action.async { implicit request =>
    identityService.authenticateResubToken(token).map {
      case Right(cookies) =>
        eventActor.forward(ResubAuthenticationSuccess(request, configuration.gaUID))
        SeeOther(returnUrl.getOrElse(configuration.dotcomBaseUrl)).withCookies(cookies: _*)
      case Left(_) =>
        ViewRenderer.renderErrorPage(configuration, SigninTokenRejected("The link was expired or invalid, please request a new one."), Results.Unauthorized.apply)
    }
  }

}
