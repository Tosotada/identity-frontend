package com.gu.identity.frontend.authentication

import com.gu.identity.frontend.authentication.CookieName.Name
import com.gu.identity.frontend.controllers.NoCache
import com.gu.identity.model.User
import play.api.mvc.{Result, Cookie, DiscardingCookie, RequestHeader}
import play.api.mvc.Results._

case class AuthenticatedUser(userId: String)

object AuthenticationService {

  val knownCookies: Seq[GuardianCookie] = Seq(
    DotComCookie(CookieName.gu_user_features_expiry),
    DotComCookie(CookieName.gu_paying_member),
    DotComCookie(CookieName.gu_recurring_contributor),
    DotComCookie(CookieName.gu_digital_subscriber),
    IdentityCookie(CookieName.GU_U),
    IdentityCookie(CookieName.GU_ID_CSRF),
    IdentityCookie(CookieName.GU_PROFILE_CSRF),
    IdentityCookie(CookieName.SC_GU_U),
    IdentityCookie(CookieName.SC_GU_RP),
    IdentityCookie(CookieName.GU_SIGNIN_EMAIL),
    IdentityCookie(CookieName.SC_GU_LA),
  )

  implicit def cookieNameToString(cookieName: Name): String = cookieName.toString

  def authenticatedUserFor[A](request: RequestHeader, cookieDecoder: String => Option[User]): Option[AuthenticatedUser] = for {
    scGuU <- request.cookies.get(CookieName.SC_GU_U)
    minimalSecureUser <- cookieDecoder(scGuU.value)
    userId <- Option(minimalSecureUser.id)
  } yield AuthenticatedUser(userId)

  def terminateSession(
      verifiedReturnUrl: String,
      cookieDomain: String,
      newCookies: Seq[Cookie] = Seq.empty): Result = {

    val cookiesToDiscard: Seq[DiscardingCookie] = knownCookies.map { cookie =>
      DiscardingCookie(name = cookie.name, path = "/", domain = Some(cookieDomain), secure = true)
    }

    NoCache(
      SeeOther(verifiedReturnUrl)
        .withCookies(newCookies: _*)
        .discardingCookies(cookiesToDiscard:_*)
    )
  }
}


