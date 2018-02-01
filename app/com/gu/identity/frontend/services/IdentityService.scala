package com.gu.identity.frontend.services

import com.gu.identity.frontend.authentication.{CookieService, IdentityApiCookie}
import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.errors._
import com.gu.identity.frontend.logging.Logging
import com.gu.identity.frontend.models.{ClientIp, TrackingData}
import com.gu.identity.frontend.request.RequestParameters.SignInRequestParameters
import com.gu.identity.frontend.request.{RegisterActionRequestBody, ResendConsentTokenActionRequestBody, ResetPasswordActionRequestBody}
import com.gu.identity.service.client._
import com.gu.identity.service.client.models.User
import com.gu.identity.service.client.request._
import play.api.mvc.{Cookie => PlayCookie}

import scala.concurrent.{ExecutionContext, Future}


/**
 * Adapter for the identity service client.
 */
trait IdentityService {
  type PlayCookies = Seq[PlayCookie]

  def authenticate(signInRequest: SignInRequestParameters, trackingData: TrackingData)(implicit ec: ExecutionContext): Future[Either[ServiceExceptions, PlayCookies]]
  def authenticate(token: String, trackingData: TrackingData)(implicit ec: ExecutionContext): Future[Either[ServiceExceptions, PlayCookies]]
  def deauthenticate(cookie: PlayCookie, trackingData: TrackingData)(implicit ec: ExecutionContext): Future[Either[ServiceExceptions, PlayCookies]]
  def registerThenSignIn(request: RegisterActionRequestBody, clientIp: ClientIp, trackingData: TrackingData)(implicit ec: ExecutionContext): Future[Either[ServiceExceptions, PlayCookies]]
  def register(request: RegisterActionRequestBody, clientIp: ClientIp, trackingData: TrackingData)(implicit ec: ExecutionContext): Future[Either[ServiceExceptions, RegisterResponseUser]]
  def resendConsentToken(data: ResendConsentTokenActionRequestBody)(implicit ec: ExecutionContext): Future[Either[ServiceExceptions, ResendConsentTokenResponse ]]
  def sendResetPasswordEmail(data: ResetPasswordActionRequestBody, clientIp: ClientIp)(implicit ec: ExecutionContext): Future[Either[ServiceExceptions, SendResetPasswordEmailResponse ]]
  def getUser(cookie: PlayCookie)(implicit ec: ExecutionContext): Future[Either[ServiceExceptions, User]]
  def assignGroupCode(group: String, cookie: PlayCookie)(implicit ec: ExecutionContext): Future[Either[ServiceExceptions, AssignGroupResponse]]
  def processConsentToken(token: String)(implicit ec: ExecutionContext): Future[Either[ServiceException, PlayCookies]]
  def processRepermissionToken(token: String)(implicit ec: ExecutionContext): Future[Either[ServiceException, PlayCookies]]
}


class IdentityServiceImpl(config: Configuration, adapter: IdentityServiceRequestHandler, client: IdentityClient) extends IdentityService with Logging {

  implicit val clientConfiguration = IdentityClientConfiguration(config.identityApiHost, config.identityApiKey, adapter)

  override def authenticate(signInRequest: SignInRequestParameters, trackingData: TrackingData)(implicit ec: ExecutionContext) = {
    client.authenticateCookies(signInRequest.email, signInRequest.password, signInRequest.rememberMe, trackingData).map {
      case Left(errors) =>
        Left(errors.map(SignInServiceAppException.apply))

      case Right(cookies) => Right(CookieService.signInCookies(cookies, signInRequest.rememberMe)(config))
    }
  }
  override def authenticate(token: String, trackingData: TrackingData)(implicit ec: ExecutionContext) =
    client.authenticateTokenCookies(token, trackingData).map {
      case Left(errors) =>
        Left(errors.map(SignInServiceAppException.apply))

      case Right(cookies) => Right(CookieService.signInCookies(cookies, false)(config))
    }

  override def deauthenticate(cookie: PlayCookie, trackingData: TrackingData)(implicit ec: ExecutionContext) = {
    val apiRequest = DeauthenticateApiRequest(cookie, trackingData)
    client.deauthenticate(apiRequest).map {
      case Left(errors) =>
        Left(errors.map(DeauthenticateAppException.apply))

      case Right(cookies) => Right(CookieService.signOutCookies(cookies)(config))
    }
  }

  override def register(request: RegisterActionRequestBody, clientIp: ClientIp, trackingData: TrackingData)(implicit ec: ExecutionContext): Future[Either[ServiceExceptions, RegisterResponseUser]] = {
    val apiRequest = RegisterApiRequest(request, clientIp, trackingData)
    client.register(apiRequest).map {
      case Left(errors) =>
        Left(errors.map(RegisterServiceAppException.apply))

      case Right(user) => Right(user)
    }
  }

  override def registerThenSignIn(request: RegisterActionRequestBody,
                                  clientIp: ClientIp,
                                  trackingData: TrackingData
                                 )(implicit ec: ExecutionContext): Future[Either[ServiceExceptions, PlayCookies]] = {
    register(request, clientIp, trackingData).flatMap{
      case Left(errors) => Future.successful(Left(errors))
      case Right(user) => {
        authenticate(request, trackingData).map {
          case Left(signInErrors) => {
            logger.error(s"User could not be logged in after successfully registering: $signInErrors $trackingData")
            signInErrors.foreach { err =>
              logger.error(s"Sign in error after registering: ${err.getMessage}", err)
            }
            Right(Seq.empty)
          }
          case Right(cookies) => Right(cookies)
        }
      }
    }
  }

  override def resendConsentToken(resendConsentTokenData: ResendConsentTokenActionRequestBody)(implicit ec: ExecutionContext): Future[Either[ServiceExceptions, ResendConsentTokenResponse]] = {
    val apiRequest = ResendConsentTokenApiRequest(resendConsentTokenData)
    client.resendConsentToken(apiRequest).map {
      case Left(errors) =>
        Left(errors.map(ResendConsentTokenExeption.apply))
      case Right(okResponse) => Right(okResponse)
    }
  }

  override def sendResetPasswordEmail(resetPasswordData: ResetPasswordActionRequestBody, clientIp: ClientIp)(implicit ec: ExecutionContext): Future[Either[ServiceExceptions, SendResetPasswordEmailResponse]] = {
    val apiRequest = SendResetPasswordEmailApiRequest(resetPasswordData, clientIp)
    client.sendResetPasswordEmail(apiRequest).map {
      case Left(errors) =>
        Left(errors.map(ResetPasswordAppException.apply))

      case Right(okResponse) => Right(okResponse)
    }
  }

  override def getUser(cookie: PlayCookie)(implicit ec: ExecutionContext): Future[Either[ServiceExceptions, User]] = {
    val apiRequest = UserApiRequest(cookie)
    client.getUser(apiRequest).map {
      case Left(errors) =>
        Left(errors.map(GetUserAppException.apply))

      case Right(user) => Right(user)
    }
  }

  override def assignGroupCode(group: String, cookie: PlayCookie)(implicit ec: ExecutionContext): Future[Either[ServiceExceptions, AssignGroupResponse]] = {
    val apiRequest = AssignGroupApiRequest(group, cookie)
    client.assignGroupCode(apiRequest).map {
      case Left(errors) =>
        Left(errors.map(AssignGroupAppException.apply))

      case Right(response) => Right(response)
    }
  }

  override def processConsentToken(token: String)(implicit ec: ExecutionContext): Future[Either[ServiceException, PlayCookies]] = {
    client.postConsentToken(token) map {
      case Left(IdentityUnauthorizedError :: _) => Left(ConsentTokenUnauthorizedException(IdentityUnauthorizedError))
      case Left(error) => Left(ConsentTokenAppException(error.head))
      case Right(AuthenticationCookiesResponse(cookies)) =>
        val rpCookies = CookieService.signInCookies(
          cookies.values.map(IdentityApiCookie(_, cookies.expiresAt)),
          rememberMe = false
        )(config)
        Right(rpCookies)
      case Right(other) =>
        logger.warn(s"Unexpected API Response for consent-token, $other", new IllegalStateException(s"Illegal Response ${other.getClass}, expected AuthenticationCookiesResponse"))
        Left(ConsentTokenAppException(ClientGatewayError("An Unexpected Error Occurred")))
    }
  }

  override def processRepermissionToken(token: String)(implicit ec: ExecutionContext): Future[Either[ServiceException, PlayCookies]] = {
    client.postRepermissionToken(token) map {
      case Left(IdentityUnauthorizedError :: _) => Left(RepermissionTokenUnauthorizedException(IdentityUnauthorizedError))
      case Left(error) => Left(RepermissionTokenAppException(error.head))
      case Right(AuthenticationCookiesResponse(cookies)) =>
        val rpCookies = CookieService.signInCookies(
          cookies.values.map(IdentityApiCookie(_, cookies.expiresAt)),
          rememberMe = false
        )(config)
        Right(rpCookies)
      case Right(other) =>
        logger.warn(s"Unexpected API Response for repermission-token, $other", new IllegalStateException(s"Illegal Response ${other.getClass}, expected AuthenticationCookiesResponse"))
        Left(ConsentTokenAppException(ClientGatewayError("An Unexpected Error Occurred")))
    }
  }
}
