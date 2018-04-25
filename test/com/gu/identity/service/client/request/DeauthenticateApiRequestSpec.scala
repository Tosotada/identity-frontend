package com.gu.identity.service.client.request

import com.gu.identity.frontend.authentication.CookieName
import com.gu.identity.frontend.models.TrackingData
import com.gu.identity.service.client.{POST, IdentityClientConfiguration, IdentityClientRequestHandler}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import play.api.mvc.Cookie


class DeauthenticateApiRequestSpec extends WordSpec with Matchers with MockitoSugar {

  val handler = mock[IdentityClientRequestHandler]

  implicit val testConfig = IdentityClientConfiguration("test.gu.com", "##key##", handler)

  val trackingData = TrackingData(
    returnUrl = Some("https://profile.theguardian.com"),
    trackingReturnUrl = Some("https://profile.theguardian.com"),
    registrationType = Some("facebook"),
    ipAddress = Some("127.0.0.1"),
    referrer = Some("https://www.theguardian.com"),
    userAgent = Some("chrome"),
    skipValidationReturn = Some(false)
  )

  val secureCookie = Cookie(name = CookieName.SC_GU_U.toString, value = "SC_GU_U_value")

  "Deauthenticate API Request" should {

    "POST to /unauth on Identity API" in {
      val result = DeauthenticateApiRequest(secureCookie, trackingData)
      result.url should be("https://test.gu.com/unauth")
      result.method should be(POST)
    }

    "have correct headers" in {
      val result = DeauthenticateApiRequest(secureCookie, trackingData)
      result.headers.filter(_._1 == "X-GU-ID-Client-Access-Token").head._2 should be("Bearer ##key##")
      result.headers.filter(_._1 == "X-GU-ID-FOWARDED-SC-GU-U").head._2 should be("SC_GU_U_value")
    }

  }

}
