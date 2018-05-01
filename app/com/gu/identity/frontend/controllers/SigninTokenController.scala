package com.gu.identity.frontend.controllers

import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.errors.SigninTokenRejected
import com.gu.identity.frontend.services.IdentityService
import com.gu.identity.frontend.views.ViewRenderer
import com.typesafe.scalalogging.LazyLogging
import play.api.i18n.I18nSupport
import play.api.mvc._

import scala.concurrent.ExecutionContext

class SigninTokenController(
  configuration: Configuration,
  identityService: IdentityService,
  cc: ControllerComponents,
  implicit val executionContext: ExecutionContext
) extends AbstractController(cc) with LazyLogging with I18nSupport {

  def signinWithResubToken(token: String, returnUrl: Option[String]): Action[AnyContent] = Action.async { implicit request =>
    identityService.authenticateResubToken(token).map {
      case Right(cookies) =>
        SeeOther(returnUrl.getOrElse(configuration.dotcomBaseUrl)).withCookies(cookies: _*)
      case Left(_) =>
        ViewRenderer.renderErrorPage(configuration, SigninTokenRejected("The link was expired or invalid, please request a new one."), Results.Unauthorized.apply)
    }
  }

}
