package com.gu.identity.frontend.authentication

import com.gu.identity.cookie.{IdentityKeys, IdentityCookieDecoder}
import com.gu.identity.frontend.authentication.CookieName._
import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.model.User
import org.scalatestplus.play.PlaySpec
import play.api.mvc.Cookie
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class AuthenticationServiceSpec extends PlaySpec {

  implicit def cookieNameToString(cookieName: Name): String = cookieName.toString

  val testKeys = new IdentityKeys(Configuration.testConfiguration.identityCookiePublicKey)

  def requestWithCookies(cookies: Seq[Cookie]) = {
    FakeRequest().withCookies(cookies: _*)
  }

  "AuthenticatedUserFor" should {

    "decode valid cookie" in {
      val cookie = Cookie(CookieName.SC_GU_U, "SC_GU_U_data")
      val validCookieRequest = requestWithCookies(Seq(cookie))
      val response = AuthenticationService.authenticatedUserFor(validCookieRequest, _ => Some(User(id = "10000811", primaryEmailAddress = "")))
      val expectedUser = AuthenticatedUser("10000811")

      response mustEqual Some(expectedUser)
    }

    "fail to decode invalid cookie" in {
      val invalidCookieRequest  = requestWithCookies(Seq(Cookie("abc", CookieName.SC_GU_U)))
      val response = AuthenticationService.authenticatedUserFor(invalidCookieRequest, _ => None)

      response mustEqual None
    }
  }

  "terminateSession" should {

    "return a response where all cookies have no value" in {
      val result = AuthenticationService.terminateSession("www.theguardian.com", "www.theguardian.com")
      val resultCookies = cookies(Future.successful(result))
      resultCookies.get(CookieName.gu_user_features_expiry).value.value.isEmpty mustBe true
      resultCookies.get(CookieName.gu_paying_member).value.value.isEmpty mustBe true
      resultCookies.get(CookieName.gu_recurring_contributor).value.value.isEmpty mustBe true
      resultCookies.get(CookieName.gu_digital_subscriber).value.value.isEmpty mustBe true
      resultCookies.get(CookieName.GU_U).value.value.isEmpty mustBe true
      resultCookies.get(CookieName.SC_GU_U).value.value.isEmpty mustBe true
      resultCookies.get(CookieName.GU_ID_CSRF).value.value.isEmpty mustBe true
      resultCookies.get(CookieName.SC_GU_RP).value.value.isEmpty mustBe true
    }

    "return a response with a GU_SO cookie" in {
      val testCookie = Seq(Cookie(name = "GU_SO", value = "test_value"))
      val result = AuthenticationService.terminateSession("www.theguardian.com", "www.theguardian.com", testCookie)
      val resultCookies = cookies(Future.successful(result))
      resultCookies.get(CookieName.gu_user_features_expiry).value.value.isEmpty mustBe true
      resultCookies.get(CookieName.gu_paying_member).value.value.isEmpty mustBe true
      resultCookies.get(CookieName.gu_recurring_contributor).value.value.isEmpty mustBe true
      resultCookies.get(CookieName.gu_digital_subscriber).value.value.isEmpty mustBe true
      resultCookies.get(CookieName.GU_U).value.value.isEmpty mustBe true
      resultCookies.get(CookieName.SC_GU_U).value.value.isEmpty mustBe true
      resultCookies.get(CookieName.GU_ID_CSRF).value.value.isEmpty mustBe true
      resultCookies.get(CookieName.SC_GU_RP).value.value.isEmpty mustBe true
      resultCookies.get("GU_SO").value.value mustEqual "test_value"
    }
  }
}
