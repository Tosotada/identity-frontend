package com.gu.identity.frontend.controllers

import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.errors.ErrorIDs.{RepermissionConsentTokenErrorID, UnauthorizedRepermissionTokenErrorID}
import com.gu.identity.frontend.errors.{NotFoundError, RepermissionTokenUnauthorizedException}
import com.gu.identity.frontend.logging.Logging
import com.gu.identity.frontend.services.IdentityService
import com.gu.identity.frontend.views.ViewRenderer._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

class RepermissionController(
                         configuration: Configuration,
                         identityService: IdentityService,
                         val messagesApi: MessagesApi,
                         implicit val executionContext: ExecutionContext
                       ) extends Controller
  with Logging
  with I18nSupport {

  def acceptToken(repermissionToken: String) = Action.async {
    identityService.processRepermissionToken(repermissionToken).map {
      case Right(playCookies) =>
        Redirect("/consents").withCookies(playCookies: _*)
      case Left(err) if err.id == UnauthorizedRepermissionTokenErrorID => Redirect(routes.Application.invalidRepermissioningToken(List("error-unexpected")))
      case Left(_) => renderErrorPage(configuration, NotFoundError("The requested page was not found."), NotFound.apply)
    }.recover {
      case NonFatal(e) =>
        logger.error("Failed to process consent token", e)
        renderErrorPage(configuration, NotFoundError("The requested page was not found."), NotFound.apply)}
  }
}
