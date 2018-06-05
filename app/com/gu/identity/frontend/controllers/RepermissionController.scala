package com.gu.identity.frontend.controllers

import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.errors.{NotFoundError, RepermissionTokenUnauthorizedException}
import com.gu.identity.frontend.logging.Logging
import com.gu.identity.frontend.services.IdentityService
import com.gu.identity.frontend.views.ViewRenderer._
import play.api.i18n.I18nSupport
import play.api.mvc._

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

class RepermissionController(
    configuration: Configuration,
    identityService: IdentityService,
    cc: ControllerComponents,
    implicit val executionContext: ExecutionContext)
  extends AbstractController(cc)
    with Logging
    with I18nSupport {

  def acceptToken(repermissionToken: String): Action[AnyContent] = Action.async { implicit request =>
    identityService.authenticateRepermissionToken(repermissionToken).map {
      case Right(playCookies) =>
        Redirect("/consents", request.queryString).withCookies(playCookies: _*)
      case Left(RepermissionTokenUnauthorizedException :: _) => Redirect(routes.Application.invalidRepermissioningToken(repermissionToken))
      case Left(_) => renderErrorPage(configuration, NotFoundError("The requested page was not found."), NotFound.apply)
    }.recover {
      case NonFatal(e) =>
        logger.error("Failed to process consent token", e)
        renderErrorPage(configuration, NotFoundError("The requested page was not found."), NotFound.apply)}
  }
}
