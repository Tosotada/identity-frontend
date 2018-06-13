package com.gu.identity.frontend.controllers

import com.gu.identity.frontend.analytics.AnalyticsEventActor
import com.gu.identity.frontend.analytics.client.{ChangeEmailSuccess, ResubAuthenticationSuccess}
import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.errors.{EmailChangeTokenRejected, RedirectOnError, SigninTokenRejected}
import com.gu.identity.frontend.logging.{LogOnErrorAction, Logging}
import com.gu.identity.frontend.services.{IdentityService, ServiceAction}
import com.gu.identity.frontend.views.ViewRenderer
import play.api.i18n.I18nSupport
import play.api.mvc._

import scala.concurrent.ExecutionContext

class ChangeEmailController(
                             configuration: Configuration,
                             identityService: IdentityService,
                             cc: ControllerComponents,
                             eventActor: AnalyticsEventActor,
                             implicit val executionContext: ExecutionContext
                           ) extends AbstractController(cc) with Logging with I18nSupport {

  def changeEmail(token: String, returnUrl: Option[String]) = Action.async { implicit request =>
    identityService.changeEmailWithToken(token).map {
      case Right(okResponse) =>
        eventActor.forward(ChangeEmailSuccess(request, configuration.gaUID))
        NoCache(SeeOther(routes.Application.changeEmail().url))
      case Left(_) =>
        ViewRenderer.renderErrorPage(configuration, EmailChangeTokenRejected("The link was expired or invalid, please request a new one."), Results.Unauthorized.apply)
    }
  }

}
