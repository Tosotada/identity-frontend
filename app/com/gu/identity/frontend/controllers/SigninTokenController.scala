package com.gu.identity.frontend.controllers

import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.errors.SigninTokenRejected
import com.gu.identity.frontend.services.IdentityService
import com.gu.identity.frontend.views.ViewRenderer
import com.typesafe.scalalogging.LazyLogging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Controller, Results}

import scala.concurrent.ExecutionContext

class SigninTokenController(
  configuration: Configuration,
  identityService: IdentityService,
  val messagesApi: MessagesApi,
  implicit val executionContext: ExecutionContext
) extends Controller with LazyLogging with I18nSupport {

  def signinWithResubToken(token: String, returnUrl: Option[String]): Action[AnyContent] = Action.async {
    identityService.authenticateResubToken(token).map {
      case Right(cookies) =>
        SeeOther(returnUrl.getOrElse(configuration.dotcomBaseUrl)).withCookies(cookies: _*)
      case Left(_) =>
        ViewRenderer.renderErrorPage(configuration, SigninTokenRejected("The link was expired or invalid, please request a new one."), Results.Unauthorized.apply)
    }
  }

}
