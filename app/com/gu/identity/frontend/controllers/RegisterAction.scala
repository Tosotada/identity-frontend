package com.gu.identity.frontend.controllers


import com.gu.identity.frontend.analytics.AnalyticsEventActor
import com.gu.identity.frontend.analytics.client.RegisterEventRequest
import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.csrf.{CSRFCheck, CSRFConfig}
import com.gu.identity.frontend.errors.RedirectOnError
import com.gu.identity.frontend.logging.{LogOnErrorAction, Logging, MetricsLoggingActor}
import com.gu.identity.frontend.models._
import com.gu.identity.frontend.request.RegisterActionRequestBody
import com.gu.identity.frontend.services.{IdentityService, ServiceAction, ServiceActionBuilder}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.{BodyParser, Controller, Request, Cookie => PlayCookie}
import Configuration.Environment._
import com.gu.tip.Tip


class RegisterAction(
    identityService: IdentityService,
    val messagesApi: MessagesApi,
    metricsLoggingActor: MetricsLoggingActor,
    analyticsEventActor: AnalyticsEventActor,
    val config: Configuration,
    csrfConfig: CSRFConfig)
  extends Controller
    with Logging
    with I18nSupport {

  val redirectRoute: String = routes.Application.register().url

  val RegisterServiceAction: ServiceActionBuilder[Request] =
    ServiceAction andThen
    RedirectOnError(redirectRoute) andThen
    LogOnErrorAction(logger) andThen
    CSRFCheck(csrfConfig)

  val bodyParser: BodyParser[RegisterActionRequestBody] = RegisterActionRequestBody.bodyParser

  def register = RegisterServiceAction(bodyParser) { implicit request =>
    val clientIp = ClientIp(request)
    val body = request.body

    val trackingData = TrackingData(request, body.returnUrl.flatMap(_.toStringOpt), body.skipValidationReturn)
    identityService.registerThenSignIn(body, clientIp, trackingData).map {
      case Left(errors) =>
        logger.error(s"Could not register: $errors $trackingData")
        Left(errors)
      case Right(cookies) => Right {
        registerSuccessRedirectUrl(cookies, body.returnUrl, body.skipConfirmation, body.groupCode, body.clientId)
      }
    }
  }


  private def registerSuccessRedirectUrl(
      cookies: Seq[PlayCookie],
      returnUrlOpt: Option[ReturnUrl],
      skipConfirmation: Option[Boolean],
      group: Option[GroupCode],
      clientId: Option[ClientID])(implicit request: Request[RegisterActionRequestBody]) = {

    val returnUrl = returnUrlOpt.getOrElse(ReturnUrl.defaultForClient(config, clientId))
    val registrationConfirmUrl = config.identityProfileBaseUrl+"/complete-registration"

    (group, skipConfirmation.getOrElse(false)) match {
      case(Some(group), false) => {
        val skipConfirmationReturnUrl = ReturnUrl(Some(UrlBuilder(registrationConfirmUrl, returnUrl, clientId)), config)
        val url = UrlBuilder.buildThirdPartyReturnUrl(skipConfirmationReturnUrl, skipConfirmation, skipThirdPartyLandingPage = true, clientId, group, config)
        registerSuccessResult(url, cookies)
      }
      case(Some(group), true) => {
        val url = UrlBuilder.buildThirdPartyReturnUrl(returnUrl, skipConfirmation, skipThirdPartyLandingPage = true, clientId, group, config)
        registerSuccessResult(url, cookies)
      }
      case (None, false) => {
        val url = ReturnUrl(Some(UrlBuilder(registrationConfirmUrl, returnUrl, clientId)), config)
        registerSuccessResult(url, cookies)
      }
      case (None, true) => {
        registerSuccessResult(returnUrl, cookies)
      }
    }
  }

  private def registerSuccessResult(returnUrl: ReturnUrl, cookies: Seq[PlayCookie])(implicit request: Request[RegisterActionRequestBody]) = {
    if (stage == "PROD") Tip.verify("Account Registration")
    metricsLoggingActor.logSuccessfulRegister()

    if(request.body.gaClientId.isDefined) {
      analyticsEventActor.sendSuccessfulRegister(RegisterEventRequest(request, config.gaUID))
    } else {
      logger.warn("No GA Client ID passed for register request")
    }

    SeeOther(returnUrl.url).withCookies(cookies: _*)
  }
}
