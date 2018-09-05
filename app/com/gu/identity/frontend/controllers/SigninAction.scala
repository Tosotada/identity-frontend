package com.gu.identity.frontend.controllers

import com.gu.identity.frontend.analytics.AnalyticsEventActor
import com.gu.identity.frontend.analytics.client._
import com.gu.identity.frontend.authentication.CookieService
import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.configuration.Configuration.Environment._
import com.gu.identity.frontend.errors.ErrorIDs.SignInGatewayErrorID
import com.gu.identity.frontend.errors.{SignInInvalidCredentialsAppException, _}
import com.gu.identity.frontend.logging.{LogOnErrorAction, Logging, MetricsLoggingActor}
import com.gu.identity.frontend.models._
import com.gu.identity.frontend.request._
import com.gu.identity.frontend.services._
import com.gu.identity.model.CurrentUser
import com.gu.identity.service.client.ClientGatewayError
import com.gu.tip.Tip
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
 * Form actions controller
 */
class SigninAction(
    identityService: IdentityService,
    cc: ControllerComponents,
    metricsActor: MetricsLoggingActor,
    eventActor: AnalyticsEventActor,
    val config: Configuration,
    serviceAction: ServiceAction,
    emailResubFormParser: EmailResubRequestsParser,
    signInActionRequestBodyParser: SignInActionRequestBodyParser)(implicit executionContext: ExecutionContext)
  extends AbstractController(cc) with Logging with I18nSupport {

  val redirectRoute: String = routes.Application.twoStepSignInStart().url

  val signInSecondStepCurrentRedirectRoute: String = routes.Application.twoStepSignInChoices(CurrentUser.name).url

  val SignInServiceAction =
    serviceAction andThen
      RedirectOnError(redirectRoute, cc) andThen
      (new LogOnErrorAction(logger, cc))

  val signInSecondStepCurrentServiceAction =
    serviceAction andThen
      RedirectOnError(signInSecondStepCurrentRedirectRoute, cc) andThen
      (new LogOnErrorAction(logger, cc))

  val SignInSmartLockServiceAction =
    serviceAction andThen
      ResultOnError(redirectRoute, cc) andThen
      (new LogOnErrorAction(logger, cc))

  val bodyParser = signInActionRequestBodyParser.bodyParser

  def signInMetricsLogger(request: Request[SignInActionRequestBody]) = {
    metricsActor.logSuccessfulSignin()

    if(request.body.gaClientId.isDefined) {
      eventActor.sendSuccessfulSignin(SigninEventRequest(request, config.gaUID))
    } else {
      logger.warn("No GA Client ID passed for sign in request")
    }
  }

  def signInSmartLockMetricsLogger(request: Request[SignInActionRequestBody]) = {
    metricsActor.logSuccessfulSmartLockSignin()

    if(request.body.gaClientId.isDefined) {
      eventActor.forward(SigninSmartLockEventRequest(request, config.gaUID))
    } else {
      logger.warn("No GA Client ID passed for sign in request")
    }
  }

  def signInFirstStepMetricsLogger(request: Request[SignInActionRequestBody]) = {
    metricsActor.logSuccessfulSigninFirstStep()

    if(request.body.gaClientId.isDefined) {
      eventActor.sendSuccessfulSigninSecondStep(SigninSecondStepEventRequest(request, config.gaUID))
    } else {
      logger.warn("No GA Client ID passed for sign in request")
    }
  }

  def signInSecondStepCurrent = signInSecondStepCurrentServiceAction(bodyParser) {
    signInAction(successfulSignInResponse, successfulAjaxSignInResponse, signInMetricsLogger)
  }

  def signIn = SignInServiceAction(bodyParser) {
    signInAction(successfulSignInResponse, successfulAjaxSignInResponse, signInMetricsLogger)
  }

  def signInWithSmartLock = SignInSmartLockServiceAction(bodyParser) {
    signInAction((_, cookies) => successfulSmartLockSignInResponse(cookies), successfulAjaxSignInResponse, signInSmartLockMetricsLogger)
  }

  def emailSignInFirstStep = SignInServiceAction(bodyParser) { req =>
    emailSignInFirstStepAction(successfulFirstStepResponse, signInFirstStepMetricsLogger)(req)
  }

  def emailSignInFirstStepAction(successResponse: (String, ReturnUrl, Seq[Cookie], Option[Boolean], Option[ClientID], Option[GroupCode], Option[Boolean]) => Result, metricsLogger: (Request[SignInActionRequestBody]) => Unit) = { implicit request: Request[SignInActionRequestBody] =>
    val body = request.body

    lazy val returnUrl = body.returnUrl.getOrElse(ReturnUrl.defaultForClient(config, body.clientId))

    val successfulReturnUrl = body.groupCode match {
      case Some(groupCode) =>
        UrlBuilder.buildThirdPartyReturnUrl(returnUrl, body.skipConfirmation, skipThirdPartyLandingPage = true, body.clientId, groupCode, config)
      case _ => returnUrl
    }

    identityService.getUserType(body).map {
      case Left(errors) =>
        Left(errors)

      case Right(response) =>
        metricsLogger(request)
        val emailLoginCookie = CookieService.signInEmailCookies(body.email)(config)
        Right(successResponse(response.userType, successfulReturnUrl, emailLoginCookie, body.skipConfirmation, body.clientId, body.groupCode, body.skipValidationReturn))
    }
  }

  def signInAction(
    successResponse: (ReturnUrl, Seq[Cookie]) => Result,
    successAjaxResponse: (ReturnUrl, Seq[Cookie]) => Result,
    metricsLogger: (Request[SignInActionRequestBody]) => Unit
  ): Request[SignInActionRequestBody] => Future[Either[ServiceExceptions, Result]] = { implicit request: Request[SignInActionRequestBody] =>
    val body = request.body

    val trackingData = TrackingData(request, body.returnUrl.flatMap(_.toStringOpt))
    lazy val returnUrl = body.returnUrl.getOrElse(ReturnUrl.defaultForClient(config, body.clientId))

    val successfulReturnUrl = body.groupCode match {
      case Some(validGroupCode) =>
        UrlBuilder.buildThirdPartyReturnUrl(returnUrl, body.skipConfirmation, skipThirdPartyLandingPage = true, body.clientId, validGroupCode, config)
      case _ => returnUrl
    }

    identityService.authenticate(body, trackingData).map {
      case Left(errors) =>
        Left(errors)
      case Right(cookies) => Right {
        if (stage == "PROD") Tip.verify("Account Signin")
        metricsLogger(request)
        if(request.headers.toSimpleMap.contains("x-gu-browser-rq")){
          successAjaxResponse(successfulReturnUrl, cookies)
        } else {
          successResponse(successfulReturnUrl, cookies)
        }
      }
    }
  }

  val TokenFromServiceAction: ServiceActionBuilder[Request] =
    serviceAction andThen
      RedirectOnError(redirectRoute, cc) andThen
      (new LogOnErrorAction(logger, cc))

  def permissionAuth(token:String, journey: Option[String]) = {
    TokenFromServiceAction {
      permissionAuthAction(successfulSignInResponse, token, journey)
    }
  }

  def permissionAuthAction(successResponse: (ReturnUrl, Seq[Cookie]) => Result, token:String, journeyOpt: Option[String]) = { implicit req: RequestHeader =>

    val journey = journeyOpt.getOrElse("repermission")
    val permissionRedirectString =  s"${config.identityProfileBaseUrl}/consent?journey=${journey}"
    val returnUrl = ReturnUrl(Some(permissionRedirectString), config)

    val trackingData = TrackingData(req, returnUrl.toStringOpt)

    identityService.authenticate(token, trackingData).map {
      case Left(errors) => Left(errors)
      case Right(cookies) => Right(successResponse(returnUrl, cookies))
    }
  }

  def sendResubLinkAction(): Action[EmailResubscribeRequest] = Action.async(emailResubFormParser.bodyParser) { _req =>
    val req = _req.body
    identityService.sendResubEmail(req, ClientIp(_req)).map {
      case Right(_) =>
        eventActor.forward(ResubRequestEvent(_req, config.gaUID))
        SeeOther(routes.Application.sendResubLinkSent(
          clientId = req.clientId.map(_.id),
          emailProvider = EmailProvider.getProviderForEmail(_req.body.email).map(_.id)
        ).url)
      case Left(errors) =>
        SeeOther(routes.Application.sendResubLink(error = errors.map(_.id.toString), req.clientId.map(_.id)).url)
    }.recover {
      case e: ClientGatewayError =>
        SeeOther(routes.Application.sendResubLink(error = List(SignInGatewayErrorID.toString), req.clientId.map(_.id)).url)
    }
  }

  def successfulSignInResponse(successfulReturnUrl: ReturnUrl, cookies: Seq[Cookie]): Result =
    SeeOther(successfulReturnUrl.url)
      .withCookies(cookies: _*)


  def successfulAjaxSignInResponse(successfulReturnUrl: ReturnUrl, cookies: Seq[Cookie]): Result =
    Ok(Json.obj(
      "status" -> true,
      "returnUrl" -> successfulReturnUrl.url.toString
    ))
      .withCookies(cookies: _*)


  def successfulFirstStepResponse(userType: String, successfulReturnUrl: ReturnUrl, cookies: Seq[Cookie], skipConfirmation: Option[Boolean], clientId: Option[ClientID], group: Option[GroupCode], skipValidationReturn: Option[Boolean]): Result ={
    val secondStepUrl = UrlBuilder(s"${config.identityProfileBaseUrl}/signin/$userType", Some(successfulReturnUrl), skipConfirmation, clientId, group, skipValidationReturn)
    SeeOther(secondStepUrl)
      .withCookies(cookies: _*)
  }

  def successfulSmartLockSignInResponse(cookies: Seq[Cookie]): Result =
    Ok("")
      .withCookies(cookies: _*)
}

