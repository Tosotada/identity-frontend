package com.gu.identity.frontend.analytics.client

import java.util.UUID

import com.gu.identity.frontend.logging.Logging
import com.gu.identity.frontend.request.RequestParameters.GaClientIdRequestParameter
import com.gu.identity.frontend.request.{EmailResubscribeRequest, RegisterActionRequestBody, ResetPasswordActionRequestBody, SignInActionRequestBody}
import play.api.mvc.{AnyContent, Request}

// Utility typeclass for extracting a GA Client Id from a request
trait GaClientIdExtractor[T] {
  def gaClientIdFromRequest(req: Request[T]): Option[String]
}

object GaClientIdExtractor {
  implicit def instanceForParam[T <: GaClientIdRequestParameter]: GaClientIdExtractor[T] = (req: Request[GaClientIdRequestParameter]) => req.body.gaClientId
  implicit def instanceForAnyContent: GaClientIdExtractor[AnyContent] = (req: Request[AnyContent]) => None
  def apply[T](implicit gaClientId: GaClientIdExtractor[T]): GaClientIdExtractor[T] = (req: Request[T]) => gaClientId.gaClientIdFromRequest(req)
}

object GaClientId {
  def apply[T: GaClientIdExtractor](req: Request[T]): Option[String] = GaClientIdExtractor[T].gaClientIdFromRequest(req)
}

trait MeasurementProtocolRequestBody extends Logging {
  def apply[T](request: Request[T], gaUID: String)(implicit gaClientId: GaClientIdExtractor[T]): String = {
    val params = commonBodyParameters(
      GaClientId(request).getOrElse(UUID.randomUUID().toString),
      request.remoteAddress,
      request.headers.get("User-Agent").getOrElse(""),
      request.acceptLanguages.headOption.map(_.language).getOrElse(""),
      request.host + request.uri,
      gaUID
    ) ++ extraBodyParams

    encodeBody(params: _*)
  }

  protected val extraBodyParams: Seq[(String, String)] = Seq()

  private def commonBodyParameters(
      clientId: String,
      ipAddress: String,
      userAgent: String,
      userLanguage: String,
      url: String,
      gaUID: String): Seq[(String, String)] =
    Seq(
      "v" -> "1",
      "tid" -> gaUID,
      "cid" -> clientId,
      "t" -> "event",
      "uip" -> ipAddress,
      "ua" -> userAgent,
      "de" -> "UTF-8",
      "ul" -> userLanguage,
      "dl" -> url,
      "ec" -> "identity",
      "cd3" -> "profile.theguardian.com",
      "cd4" -> userAgent,
      "cd5" -> url
    )

  private def encodeBody(params: (String, String)*) = {
    def encode = java.net.URLEncoder.encode(_: String, "UTF8")

    params.map(p => s"${p._1}=${encode(p._2)}").mkString("&")
  }
}

trait MeasurementProtocolRequest {
  val url: String = s"https://www.google-analytics.com/collect"
  val body: String
}

private object SigninEventRequestBody extends MeasurementProtocolRequestBody {
  override val extraBodyParams = Seq(
    "ea" -> "SigninSuccessful",
    "el" -> "RegularSignin",
    "cm2" -> "1"
  )
}

case class SigninEventRequest(request: Request[SignInActionRequestBody], gaUID: String) extends MeasurementProtocolRequest {
  override val body = SigninEventRequestBody(request, gaUID)
}

private object SigninSecondStepEventRequestBody extends MeasurementProtocolRequestBody {
  override val extraBodyParams = Seq(
    "ea" -> "AuthenticationProgress",
    "el" -> "ReachedSecondStep"
  )
}

case class ResubAuthenticationSuccess(request: Request[AnyContent], gaUID: String) extends MeasurementProtocolRequest {
  override val body: String = ResubSigninSuccessBody(request, gaUID)
}

case object ResubSigninSuccessBody extends MeasurementProtocolRequestBody {
  override val extraBodyParams = Seq(
    "ea" -> "SigninSuccessful",
    "el" -> "ResubEmail"
  )
}

case class ResubRequestEvent(request: Request[EmailResubscribeRequest], gaUID: String) extends MeasurementProtocolRequest {
  override val body = ResubRequestEventBody(request, gaUID)
}

private object ResubRequestEventBody extends MeasurementProtocolRequestBody {
  override val extraBodyParams = Seq(
    "ea" -> "AuthenticationProgress",
    "el" -> "RequestedResub"
  )
}

case class PasswordResetRequestEvent(request: Request[ResetPasswordActionRequestBody], gaUID: String) extends MeasurementProtocolRequest {
  override val body = PasswordResetRequestEventBody(request, gaUID)
}

private object PasswordResetRequestEventBody extends MeasurementProtocolRequestBody {
  override val extraBodyParams = Seq(
    "ea" -> "AuthenticationProgress",
    "el" -> "RequestedPasswordReset"
  )
}

case class SigninSecondStepEventRequest(request: Request[SignInActionRequestBody], gaUID: String) extends MeasurementProtocolRequest {
  override val body = SigninSecondStepEventRequestBody(request, gaUID)
}

private object RegisterEventRequestBody extends MeasurementProtocolRequestBody {
  override val extraBodyParams = Seq(
    "ea" -> "RegisterSuccessful",
    "el" -> "RegularRegistration",
    "cm1" -> "1"
  )
}

case class RegisterEventRequest(request: Request[RegisterActionRequestBody], gaUID: String) extends MeasurementProtocolRequest {
  override val body = RegisterEventRequestBody(request, gaUID)
}
