package com.gu.identity.frontend.controllers

import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.errors.{ConsentTokenUnauthorizedException, NotFoundError}
import com.gu.identity.frontend.logging.Logging
import com.gu.identity.frontend.services.IdentityService
import com.gu.identity.frontend.views.ViewRenderer._
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

class ConsentController(
    configuration: Configuration,
    identityService: IdentityService,
    cc: ControllerComponents,
    implicit val executionContext: ExecutionContext)
    extends AbstractController(cc)
    with Logging
    with I18nSupport {

  def confirmConsents(consentToken: String): Action[AnyContent] = Action.async { implicit request =>
    identityService.authenticateConsentToken(consentToken).map {
      case Right(playCookies) =>
        Redirect("/consents/thank-you").withCookies(playCookies: _*)
      case Left(ConsentTokenUnauthorizedException :: _) => Redirect(routes.Application.invalidConsentToken(consentToken = consentToken))
      case Left(_) => renderErrorPage(configuration, NotFoundError("The requested page was not found."), NotFound.apply)
    }.recover {
      case NonFatal(e) =>
        logger.error("Failed to process consent token", e)
        renderErrorPage(configuration, NotFoundError("The requested page was not found."), NotFound.apply)}
  }
}
