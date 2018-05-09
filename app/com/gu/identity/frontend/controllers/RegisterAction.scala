package com.gu.identity.frontend.controllers


import com.gu.identity.frontend.analytics.AnalyticsEventActor
import com.gu.identity.frontend.analytics.client.RegisterEventRequest
import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.errors.RedirectOnError
import com.gu.identity.frontend.logging.{LogOnErrorAction, Logging, MetricsLoggingActor}
import com.gu.identity.frontend.models._
import com.gu.identity.frontend.request.{RegisterActionRequestBody, RegisterActionRequestBodyParser}
import com.gu.identity.frontend.services.{IdentityService, ServiceAction, ServiceActionBuilder}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, BodyParser, ControllerComponents, Request, Cookie => PlayCookie}
import Configuration.Environment._
import com.gu.tip.Tip

import scala.concurrent.ExecutionContext


class RegisterAction(
    identityService: IdentityService,
    cc: ControllerComponents,
    metricsLoggingActor: MetricsLoggingActor,
    analyticsEventActor: AnalyticsEventActor,
    val config: Configuration,
    serviceAction: ServiceAction,
    registerActionRequestBodyParser: RegisterActionRequestBodyParser)
    (implicit executionContext: ExecutionContext)
  extends AbstractController(cc)
    with Logging
    with I18nSupport {

  val redirectRoute: String = routes.Application.register().url

  val RegisterServiceAction: ServiceActionBuilder[Request] =
    serviceAction andThen
      RedirectOnError(redirectRoute, cc) andThen
      (new LogOnErrorAction(logger, cc))

  val bodyParser: BodyParser[RegisterActionRequestBody] = registerActionRequestBodyParser.bodyParser

  def register = RegisterServiceAction(bodyParser) { implicit request =>
    val clientIp = ClientIp(request)
    val body = request.body

    /* validationReturnUrl is the returnUrl passed to identity API and used for the returnUrl in the validation email and should be ignored based on the skipValidationReturn flag
     body.returnUrl is the returnUrl used for the immediate returnUrl of the page and its use should not be dependant on skipValidationReturn flag*/

    val validationReturnUrl = if(body.skipValidationReturn.getOrElse(false)) None else body.returnUrl.flatMap(_.toStringOpt)

    val trackingData = TrackingData(request, validationReturnUrl)
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
