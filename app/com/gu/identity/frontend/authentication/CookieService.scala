package com.gu.identity.frontend.authentication

import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.service.client.AuthenticationCookiesResponseCookie
import org.joda.time.{DateTime, Seconds}
import play.api.mvc.{Cookie => PlayCookie}

sealed trait GuardianCookie {
  def name: CookieName.Name
}

final case class DotComCookie(name: CookieName.Name) extends GuardianCookie

final case class IdentityCookie(name: CookieName.Name) extends GuardianCookie

final case class IdentityApiCookie(name: String, value: String, isSession: Boolean, expires: DateTime)

object IdentityApiCookie {
  def apply(c: AuthenticationCookiesResponseCookie, expires: DateTime): IdentityApiCookie =
    IdentityApiCookie(name = c.key, value = c.value, isSession = c.sessionCookie.getOrElse(false), expires = expires)
}

object CookieService {

  def getMaxAge(expiration: DateTime, now: Option[DateTime] = None) = Seconds.secondsBetween(now.getOrElse(DateTime.now), expiration).getSeconds

  def signInCookies(cookies: Seq[IdentityApiCookie], rememberMe: Boolean, now: Option[DateTime] = None)(config: Configuration): Seq[PlayCookie] = {
    cookies.map { c =>
      val maxAge = if (rememberMe) Some(getMaxAge(c.expires, now)) else None
      val httpOnlyCookie = CookieName.isHttpOnly(c.name)
      val cookieMaxAgeOpt = maxAge.filterNot(_ => c.isSession)

      PlayCookie(
        name = c.name,
        value = c.value,
        maxAge = cookieMaxAgeOpt,
        path = "/",
        domain = Some(config.identityCookieDomain),
        secure = true,
        httpOnly = httpOnlyCookie
      )
    }
  }

  def signOutCookies(cookies: Seq[IdentityApiCookie], now: Option[DateTime] = None)(config: Configuration): Seq[PlayCookie] = {
    cookies.map { c =>
      val maxAgeOpt = Some(getMaxAge(c.expires, now))

      PlayCookie(
        name = c.name,
        value = c.value,
        maxAge = maxAgeOpt,
        path = "/",
        domain = Some(config.identityCookieDomain),
        secure = true,
        httpOnly = false
      )

    }
  }

  def signInEmailCookies(email: String)(config: Configuration): Seq[PlayCookie] = {
    Seq(PlayCookie(
      name = "GU_SIGNIN_EMAIL",
      value = email,
      maxAge = None,
      path = "/",
      domain = Some(config.identityCookieDomain),
      secure = true,
      httpOnly = false
    )
    )
  }

}
