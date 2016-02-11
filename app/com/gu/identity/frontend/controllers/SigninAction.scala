package com.gu.identity.frontend.controllers

import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.csrf.{CSRFConfig, CSRFCheck}
import com.gu.identity.frontend.logging.{MetricsLoggingActor, Logging}
import com.gu.identity.frontend.models._
import com.gu.identity.frontend.errors.RedirectOnError
import com.gu.identity.frontend.request.SignInActionRequestBody
import com.gu.identity.frontend.services._
import play.api.i18n.{MessagesApi, I18nSupport}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.{Request, RequestHeader, Controller}

import scala.concurrent.Future
import play.api.i18n.Messages.Implicits._


/**
 * Form actions controller
 */
class SigninAction(identityService: IdentityService, val messagesApi: MessagesApi, csrfConfig: CSRFConfig, googleRecaptchaCheck: GoogleRecaptchaCheck, config: Configuration) extends Controller with Logging with MetricsLoggingActor with I18nSupport {

  val redirectRoute: String = routes.Application.signIn().url

  final val SignInServiceAction =
    ServiceAction andThen
      RedirectOnError(redirectRoute) andThen
      CSRFCheck(csrfConfig) /*andThen GoogleRecaptchaCheck*/


  def signIn = SignInServiceAction(SignInActionRequestBody.parser) { request: Request[SignInActionRequestBody] =>
    val formParams = request.body

    val trackingData = TrackingData(request, formParams.returnUrl)
    val returnUrl = ReturnUrl(formParams.returnUrl, request.headers.get("Referer"), config, formParams.clientID)
    val successfulReturnUrl = formParams.groupCode match {
      case Some(validGroupCode) => {
        UrlBuilder.buildThirdPartyReturnUrl(returnUrl, formParams.skipConfirmation, skipThirdPartyLandingPage = true, formParams.clientID, validGroupCode, config)
      }
      case _ => returnUrl
    }

//    def googleRecaptchaError = Future.successful(
//      redirectToSigninPageWithErrorsAndEmail(Seq(ServiceBadRequest("error-captcha")), returnUrl, formParams.skipConfirmation, formParams.clientID)
//    )

//    googleRecaptchaCheck(formParams.googleRecaptchaResponse, googleRecaptchaError) {
      identityService.authenticate(formParams.email, formParams.password, formParams.rememberMe, trackingData).map {
        case Left(errors) => Left(errors)
        case Right(cookies) => Right {
          logSuccessfulSignin
          SeeOther(successfulReturnUrl.url)
            .withCookies(cookies: _*)
        }
      }
//    }
  }

//  private def redirectToSigninPageWithErrorsAndEmail(errors: Seq[ServiceError], returnUrl: ReturnUrl, skipConfirmation: Option[Boolean], clientID: Option[ClientID]) = {
//    val query = errors.map("signin-" + _.id)
//    SeeOther(routes.Application.signIn(query, Some(returnUrl.url), skipConfirmation).url)
//  }


  // Note: Limitation
  //       Error Handler only accepts RequestHeader instead of Request, so we cannot
  //       pass ReturnUrl and skipConfirmation as they're on the Request body.
  private def handleCSRFError(request: RequestHeader, msg: String) = Future.successful {
    logger.error(s"CSRF error during Sign-in: $msg")
    val errors = Seq("signin-error-csrf")

    SeeOther(routes.Application.signIn(errors).url)
  }

}

