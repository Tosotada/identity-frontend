package com.gu.identity.frontend.controllers

import com.gu.identity.frontend.authentication.{AuthenticationService, CookieName}
import com.gu.identity.frontend.authentication.CookieName._
import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.logging.Logging
import com.gu.identity.frontend.models.{ReturnUrl, TrackingData}
import com.gu.identity.frontend.services.IdentityService
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.http.HeaderNames

import scala.concurrent.{ExecutionContext, Future}

class SignOutAction(
    identityService: IdentityService,
    cc: ControllerComponents,
    config: Configuration)
    (implicit executionContext: ExecutionContext)
    extends AbstractController(cc) with Logging with I18nSupport {

  implicit def cookieNameToString(cookieName: Name): String = cookieName.toString

  def signOut(returnUrl: Option[String]) = Action.async { implicit request =>
    val referrer = request.headers.get(HeaderNames.REFERER)
    val validReturnUrl = ReturnUrl(returnUrl ,referrer, config, None, List("/signin"))
    val trackingData = TrackingData(request, None)
    request.cookies.get(CookieName.SC_GU_U).map { cookie =>
      identityService.deauthenticate(cookie, trackingData).map {
        case Left(errors) => {
          logger.info(s"Error returned from API signout: ${errors.map(_.getMessage).mkString(", ")}")
          performSignout(validReturnUrl, Seq.empty)
        }
        case Right(signOutCookies) => performSignout(validReturnUrl, signOutCookies)
      }
    }.getOrElse {
      logger.info("User attempting signout without SC_GU_U cookie")
      Future.successful(performSignout(validReturnUrl, Seq.empty))
    }
  }

  def performSignout(returnUrl: ReturnUrl, signoutCookies: Seq[Cookie]) =
    AuthenticationService.terminateSession(returnUrl.url, config.identityCookieDomain, signoutCookies)

}
