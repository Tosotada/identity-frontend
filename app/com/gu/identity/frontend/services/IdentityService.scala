package com.gu.identity.frontend.services

import com.gu.identity.frontend.authentication.CookieService
import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.errors._
import com.gu.identity.frontend.logging.Logging
import com.gu.identity.frontend.models.{ClientIp, TrackingData}
import com.gu.identity.frontend.request.RequestParameters.SignInRequestParameters
import com.gu.identity.frontend.request.{EmailResubscribeRequest, RegisterActionRequestBody, ResendTokenActionRequestBody, ResetPasswordActionRequestBody}
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
  def authenticateConsentToken(token: String)(implicit ec: ExecutionContext): Future[Either[ServiceExceptions, PlayCookies]]
  def authenticateRepermissionToken(token: String)(implicit ec: ExecutionContext): Future[Either[ServiceExceptions, PlayCookies]]
  def authenticateResubToken(loginToken: String)(implicit ec: ExecutionContext): Future[Either[ServiceExceptions, PlayCookies]]
  def changeEmailWithToken(changeEmailToken: String)(implicit ec: ExecutionContext): Future[Either[ServiceExceptions, SendChangeEmailResponse]]
  def deauthenticate(cookie: PlayCookie, trackingData: TrackingData)(implicit ec: ExecutionContext): Future[Either[ServiceExceptions, PlayCookies]]
  def registerThenSignIn(request: RegisterActionRequestBody, clientIp: ClientIp, trackingData: TrackingData)(implicit ec: ExecutionContext): Future[Either[ServiceExceptions, PlayCookies]]
  def register(request: RegisterActionRequestBody, clientIp: ClientIp, trackingData: TrackingData)(implicit ec: ExecutionContext): Future[Either[ServiceExceptions, RegisterResponseUser]]
  def resendConsentToken(data: ResendTokenActionRequestBody)(implicit ec: ExecutionContext): Future[Either[ServiceExceptions, ResendTokenResponse ]]
  def resendRepermissionToken(data: ResendTokenActionRequestBody)(implicit ec: ExecutionContext): Future[Either[ServiceExceptions, ResendTokenResponse ]]
  def sendResetPasswordEmail(data: ResetPasswordActionRequestBody, clientIp: ClientIp)(implicit ec: ExecutionContext): Future[Either[ServiceExceptions, SendResetPasswordEmailResponse ]]
  def sendResubEmail(data: EmailResubscribeRequest, clientIp: ClientIp)(implicit ec: ExecutionContext): Future[Either[ServiceExceptions, SendSignInTokenEmailResponse ]]
  def getUser(cookie: PlayCookie)(implicit ec: ExecutionContext): Future[Either[ServiceExceptions, User]]
  def assignGroupCode(group: String, cookie: PlayCookie)(implicit ec: ExecutionContext): Future[Either[ServiceExceptions, AssignGroupResponse]]
  def getUserType(signInRequest: SignInRequestParameters)(implicit ec: ExecutionContext): Future[Either[ServiceExceptions, UserTypeResponse]]
  def unsubscribe(unsubscribeRequest: UnsubscribeApiRequest)(implicit ec: ExecutionContext): Future[Either[String, UnitResponse.type]]
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

      case Right(cookies) => Right(CookieService.signInCookies(cookies, rememberMe = false)(config))
    }

  override def authenticateConsentToken(token: String)(implicit ec: ExecutionContext): Future[Either[Seq[ConsentTokenAppException], PlayCookies]] = {
    client.postConsentToken(token) map {
      case Left(errors) =>
        Left(errors.map(ConsentTokenAppException.apply))
      case Right(cookies) =>
        Right(CookieService.signInCookies(cookies, rememberMe = false)(config))
    }
  }

  override def authenticateRepermissionToken(token: String)(implicit ec: ExecutionContext): Future[Either[Seq[RepermissionTokenAppException], PlayCookies]] = {
    client.postRepermissionToken(token) map {
      case Left(errors) =>
        Left(errors.map(RepermissionTokenAppException.apply))
      case Right(cookies) =>
        Right(CookieService.signInCookies(cookies, rememberMe = false)(config))
    }
  }

  def authenticateResubToken(loginToken: String)(implicit ec: ExecutionContext): Future[Either[ServiceExceptions, PlayCookies]] = {
    client.authenticateResubToken(loginToken) map {
      case Left(errors) =>
        Left(errors.map(SignInServiceAppException.apply))
      case Right(cookies) => Right(CookieService.signInCookies(cookies, rememberMe = false)(config))
    }
  }

  def changeEmailWithToken(changeEmailToken: String)(implicit ec: ExecutionContext): Future[Either[ServiceExceptions, SendChangeEmailResponse]] = {
    client.changeEmailWithToken(changeEmailToken) map {
      case Left(errors) =>
        Left(errors.map(ChangeEmailTokenAppException.apply))
      case Right(okResponse) => Right(okResponse)
    }
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
    oldConsentsInvariant(apiRequest)
    client.register(apiRequest).map {
      case Left(errors) =>
        Left(errors.map(RegisterServiceAppException.apply))

      case Right(user) => Right(user)
    }
  }

  private def oldConsentsInvariant(request: RegisterApiRequest) = {
    request.body.foreach { body =>
      val registerBody = body.asInstanceOf[RegisterRequestBody]
      require(registerBody.statusFields.receive3rdPartyMarketing == false, "do not set old consents")
      require(registerBody.statusFields.receiveGnmMarketing == false, "do not set old consents")
    }
  }

  override def registerThenSignIn(request: RegisterActionRequestBody,
                                  clientIp: ClientIp,
                                  trackingData: TrackingData
                                 )(implicit ec: ExecutionContext): Future[Either[ServiceExceptions, PlayCookies]] = {
    register(request, clientIp, trackingData).flatMap{
      case Left(errors) => Future.successful(Left(errors))
      case Right(_) => {
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

  override def resendConsentToken(resendConsentTokenData: ResendTokenActionRequestBody)(implicit ec: ExecutionContext): Future[Either[ServiceExceptions, ResendTokenResponse]] = {
    val apiRequest = ResendConsentTokenApiRequest(resendConsentTokenData)
    client.resendConsentToken(apiRequest).map(_.left.map{ errors =>
      errors.map(ResendTokenException.apply)
    })
  }

  override def resendRepermissionToken(resendRepermissionTokenData: ResendTokenActionRequestBody)(implicit ec: ExecutionContext): Future[Either[ServiceExceptions, ResendTokenResponse]] = {
    val apiRequest = ResendRepermissionTokenApiRequest(resendRepermissionTokenData)
    client.resendRepermissionToken(apiRequest).map {
      _.left.map { errors =>
        errors.map(RepermissionTokenAppException.apply)
      }
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

  override def sendResubEmail(data: EmailResubscribeRequest, clientIp: ClientIp)(implicit ec: ExecutionContext): Future[Either[ServiceExceptions, SendSignInTokenEmailResponse]] = {
    val apiRequest = SendResubEmailApiRequest(data)
    client.sendResubEmail(apiRequest).map {
      case Left(errors) =>
        Left(errors.map(SignInServiceAppException.apply))
      case Right(ok) =>
        Right(ok)
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

  override def getUserType(signInRequest: SignInRequestParameters)(implicit ec: ExecutionContext): Future[Either[Seq[ServiceException], UserTypeResponse]] ={
    val apiRequest = UserTypeRequest(signInRequest.email)
    client.getUserType(apiRequest).map {
      case Left(errors) =>
        Left(errors.map(SignInServiceAppException.apply))
      case Right(response) => Right(response)
    }
  }

  override def unsubscribe(unsubscribeRequest: UnsubscribeApiRequest)(implicit ec: ExecutionContext): Future[Either[String, UnitResponse.type]] = {
    client.unsubscribe(unsubscribeRequest).map(_.left.map(_.message))
  }
}


