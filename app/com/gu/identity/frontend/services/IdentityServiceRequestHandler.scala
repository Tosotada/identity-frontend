package com.gu.identity.frontend.services

import com.gu.identity.frontend.logging.{Logging => ApplicationLogging}
import com.gu.identity.model.Consent
import com.gu.identity.service.client._
import com.gu.identity.service.client.models._
import com.gu.identity.service.client.request._
import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, _}
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.control.NonFatal

class IdentityServiceRequestHandler(
    ws: WSClient)
    (implicit executionContext: ExecutionContext)
    extends IdentityClientRequestHandler with ApplicationLogging {

  private val dateTimePattern = "yyyy-MM-dd'T'HH:mm:ssZ"
  implicit val dateReads = JodaReads.jodaDateReads(dateTimePattern)
  implicit val dateWrite = JodaWrites.jodaDateWrites(dateTimePattern)
  implicit val dateTimeFormat = Format[DateTime](dateReads, dateWrite)

  // Cannot use just Json.format[Consent] because:
  // https://github.com/playframework/playframework/issues/2031
  implicit val consentFormat = (
    (__ \ 'actor).format[String] and
    (__ \ 'id).format[String] and
    (__ \ 'version).format[Int] and
    (__ \ 'consented).format[Boolean] and
    (__ \ 'timestamp).format[DateTime] and
    (__ \ 'privacyPolicyVersion).format[Int]
  )(Consent.apply, unlift(Consent.unapply _))

  implicit val apiErrorResponseErrorReads = Json.format[ApiErrorResponseError]
  implicit val apiErrorResponseReads = Json.format[ApiErrorResponse]

  implicit val authenticationCookieReads = Json.format[AuthenticationCookiesResponseCookie]
  implicit val authenticationCookiesListReads = Json.format[AuthenticationCookiesResponseCookieList]
  implicit val authenticationReads = Json.format[AuthenticationCookiesResponse]

  implicit val deauthenticationCookieReads = Json.format[DeauthenticationCookiesResponseCookie]
  implicit val deauthenticationCookiesListReads = Json.format[DeauthenticationCookiesResponseCookieList]
  implicit val deauthenticationReads = Json.format[DeauthenticationCookiesResponse]

  implicit val registerRequestTelephoneNumberFormat = Json.format[RegisterRequestTelephoneNumber]
  implicit val registerRequestBodyPrivateFieldsFormat = Json.format[RegisterRequestBodyPrivateFields]
  implicit val registerRequestBodyStatusFieldsFormat = Json.format[RegisterRequestBodyStatusFields]
  implicit val registerRequestBodyFormat = Json.format[RegisterRequestBody]

  implicit val sendResetPasswordEmailRequestBody = Json.format[SendResetPasswordEmailRequestBody]

  implicit val resendRepermissionEmailRequestBody = Json.format[ResendRepermissionFromTokenApiRequestBody]

  implicit val registerResponseUserGroupsFormat = Json.format[RegisterResponseUserGroups]
  implicit val registerResponseUserFormat = Json.format[RegisterResponseUser]
  implicit val registerResponseFormat = Json.format[RegisterResponse]

  implicit val userResponseGroupFormat = Json.format[UserGroup]
  implicit val userResponseStatusFieldsFormat = Json.format[StatusFields]
  implicit val userResponsePublicFieldsFormat = Json.format[PublicFields]
  implicit val userResponseLastActiveLocationFormat = Json.format[LastActiveLocation]
  implicit val userResponsePrivateFieldsFormat = Json.format[PrivateFields]
  implicit val userResponseCreationDateFormat = Json.format[CreationDate]
  implicit val userResponseUserFormat = Json.format[User]
  implicit val userResponseFormat = Json.format[UserResponse]
  implicit val userTypeFormat = Json.format[UserTypeResponse]

  implicit val resubEmailRequestBodyFormat = Json.format[SendResubEmailApiRequestBody]
  implicit val resubTokenRequestBodyFormat = Json.format[ResubTokenRequestBody]

  implicit val changeEmailRequestBodyFormat = Json.format[ChangeEmailTokenRequestBody]

  implicit val assignGroupResponseFormat = Json.format[AssignGroupResponse]

  def handleRequest(request: ApiRequest): Future[Either[IdentityClientErrors, ApiResponse]] = {
    ws.url(request.url)
      .withHttpHeaders(request.headers.toSeq: _*)
      .withQueryStringParameters(request.parameters.toSeq: _*)
      .withRequestTimeout(10 seconds)
      .withBody(request.body.map(handleRequestBody).getOrElse(""))
      .execute(request.method.toString)
      .map(handleResponse(request))
      .flatMap {
        case Left(Seq(ClientRateLimitError)) => Future.successful(Left(Seq(ClientRateLimitError)))
        case other => Future.successful(other)
      }
      .recoverWith {
        case NonFatal(err) => Future.failed {
          ClientGatewayError(
            "Request Error",
            Some(s"Error executing ${request.method} request to: ${request.url} - ${err.getMessage}"),
            cause = Some(err)
          )
        }
      }
  }

  def handleRequestBody(body: ApiRequestBody): String = body match {
    case b: RegisterRequestBody => Json.stringify(Json.toJson(b))
    case AuthenticateCookiesApiRequestBody(email, password) => encodeBody("email" -> email, "password" -> password)
    case AuthenticateCookiesFromTokenApiRequestBody(token) => encodeBody("token" -> token)
    case b: ResendRepermissionFromTokenApiRequestBody => Json.stringify(Json.toJson(b))
    case b: SendResetPasswordEmailRequestBody => Json.stringify(Json.toJson(b))
    case b: SendResubEmailApiRequestBody => Json.stringify(Json.toJson(b))
    case b: ResubTokenRequestBody => Json.stringify(Json.toJson(b))
    case b: ChangeEmailTokenRequestBody => Json.stringify(Json.toJson(b))
  }

  private def encodeBody(params: (String, String)*) = {
    def encode = java.net.URLEncoder.encode(_: String, "UTF8")

    params.map(p => s"${p._1}=${encode(p._2)}").mkString("&")
  }

  def handleResponse(request: ApiRequest)(response: WSResponse): Either[IdentityClientErrors, ApiResponse] = request match {
    case r if isErrorResponse(response) => Left {
      handleErrorResponse(response)
    }

    case _: AuthenticateCookiesApiRequest | _: ResubTokenRequest  =>
      response.json.asOpt[AuthenticationCookiesResponse]
        .map(Right.apply)
        .getOrElse(handleUnexpectedResponse(response))

    case r: RegisterApiRequest =>
      response.json.asOpt[RegisterResponse]
        .map(Right.apply)
        .getOrElse(handleUnexpectedResponse(response))

    case r: UserApiRequest =>
      response.json.asOpt[UserResponse]
        .map(Right.apply)
        .getOrElse(handleUnexpectedResponse(response))

    case r: AssignGroupApiRequest =>
      response.json.asOpt[AssignGroupResponse]
        .map(Right.apply)
        .getOrElse(handleUnexpectedResponse(response))

    case r: DeauthenticateApiRequest =>
      response.json.asOpt[DeauthenticationCookiesResponse]
        .map(Right.apply)
        .getOrElse(handleUnexpectedResponse(response))

    case r: SendResetPasswordEmailApiRequest =>
      if (response.status == 200) {
        Right(SendResetPasswordEmailResponse())
      } else {
        handleUnexpectedResponse(response)
      }

    case r: ResendConsentTokenApiRequest =>
      if (response.status == 200) {
        Right(ResendTokenResponse())
      }
      else {
        handleUnexpectedResponse(response)
      }

    case _: SendResubEmailApiRequest =>
      if (response.status == 200)
        Right(SendSignInTokenEmailResponse())
      else
        handleUnexpectedResponse(response)
    case r: ResendRepermissionTokenApiRequest =>
      if (response.status == 200) {
        Right(ResendTokenResponse())
      }
      else {
        handleUnexpectedResponse(response)
      }

    case r: UserConsentTokenRequest =>
      if (response.status == 200) {
        Right(response.json.as[AuthenticationCookiesResponse])
      } else {
        handleUnexpectedResponse(response)
      }

    case r: UserRepermissionTokenRequest =>
      if (response.status == 200) {
        Right(response.json.as[AuthenticationCookiesResponse])
      } else {
        handleUnexpectedResponse(response)
      }

    case r: UserTypeRequest =>
      if (response.status == 200) {
        Right(response.json.as[UserTypeResponse])
      } else {
        handleUnexpectedResponse(response)
      }

    case _ => Left(Seq(ClientGatewayError("Unsupported request")))
  }

  def isErrorResponse(response: WSResponse) =
    response.status >= 400 && response.status < 600

  def isBadRequestError(response: WSResponse) =
    response.status >= 400 && response.status < 500

  def handleErrorResponse(response: WSResponse): IdentityClientErrors =
    response.json.asOpt[ApiErrorResponse]
      .map(
        _.errors.map(error => IdentityClientError(response.status, error.message, error.description, error.context))
      )
      .getOrElse {
        logger.warn(s"Unexpected error response: ${response.status} ${response.statusText} ${response.body}")

        Seq(
          IdentityClientError(response.status, s"Unexpected response: ${response.status} ${response.statusText}")
        )
      }

  def handleUnexpectedResponse(response: WSResponse): Either[IdentityClientErrors, ApiResponse] = {
    logger.warn(s"Unexpected response from server: ${response.status} ${response.statusText} ${response.body}")
    Left(Seq(ClientGatewayError("Unexpected response from server")))
  }
}
