package com.gu.identity.service.client.request

import com.gu.identity.frontend.models.TrackingData
import com.gu.identity.service.client.{ClientBadRequestError, IdentityClientConfiguration, IdentityClientRequestHandler}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}


class AuthenticateCookiesApiRequestSpec extends WordSpec with Matchers with MockitoSugar {

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

  "Authenticate Cookies API Request" should {
    "Parse correctly with a valid email address, password" in {
      val email = Some("test@guardian.co.uk")
      val password = Some("god")

      val result = AuthenticateCookiesApiRequest(email, password, rememberMe = false, None, trackingData)
      val resultBody = result.right.get.body.get.asInstanceOf[AuthenticateCookiesApiRequestBody]

      result.isRight shouldBe true
      resultBody.email should equal (email.get)
      resultBody.password should equal(password.get)
      result.right.get.parameters should contain("persistent" -> "false")
    }

    "Set persistent parameter on request" in {
      val email = Some("test@guardian.co.uk")
      val password = Some("god")

      val result = AuthenticateCookiesApiRequest(email, password, rememberMe = true, None, trackingData)

      result.isRight shouldBe true
      result.right.get.parameters should contain("persistent" -> "true")
    }

    "Set tracking data parameters on request" in {
      val email = Some("test@guardian.co.uk")
      val password = Some("god")

      val result = AuthenticateCookiesApiRequest(email, password, rememberMe = true, None, trackingData)

      result.isRight shouldBe true
      val params = result.right.get.parameters
      trackingData.parameters.foreach( td => params should contain(td))
    }

    "Fail when given an invalid email domain input" in {
      val email = Some("me@")
      val password = Some("god")

      val result = AuthenticateCookiesApiRequest(email, password, rememberMe = false, None, trackingData)

      result.isLeft shouldBe true
      result.left.get shouldBe a[ClientBadRequestError]
    }


    "Fail when given an invalid start of email input" in {
      val email = Some("@blah.com")
      val password = Some("god")

      val result = AuthenticateCookiesApiRequest(email, password, rememberMe = false, None, trackingData)

      result.isLeft shouldBe true
      result.left.get shouldBe a[ClientBadRequestError]
    }

    "Fail when given an empty email input" in {
      val email = Some("")
      val password = Some("god")

      val result = AuthenticateCookiesApiRequest(email, password, rememberMe = false, None,trackingData)

      result.isLeft shouldBe true
      result.left.get shouldBe a[ClientBadRequestError]
    }

    "Fail when given an invalid password input" in {
      val email = Some("me@guardian.co.uk")
      val password = Some("")

      val result = AuthenticateCookiesApiRequest(email, password, rememberMe = false, None, trackingData)

      result.isLeft shouldBe true
      result.left.get shouldBe a[ClientBadRequestError]
    }

    "include X-Forwarded-For header on requests" in {
      val email = Some("test@guardian.co.uk")
      val password = Some("god")

      val result = AuthenticateCookiesApiRequest(email, password, rememberMe = false, None, trackingData)

      result shouldBe 'right
      result.right.get.headers should contain("X-Forwarded-For" -> "127.0.0.1")
    }

    "Fail when given an incorrectly formatted encrypted token" in {
      val token = "3t6IXpUjzYazRb%2BKZAjoduO5KPQ8%.2BSgvNDu8p42m5DAon3Z6aswedWBGoLEbXu9PyZbG4Rzf0WAiWo4AF9aJvw%3D%3D.asdkasdk"
      val result = AuthenticateCookiesApiRequest(None, None, false, Some(token), trackingData)
      result.isLeft shouldBe true
      result.left.get shouldBe a[ClientBadRequestError]
    }

    "Succeed when given an incorrectly formatted encrypted token" in {
      val token = "3t6IXpUjzYazRb%2BKZAjoduO5KPQ8%.2BSgvNDu8p42m5DAon3Z6aswedWBGoLEbXu9PyZbG4Rzf0WAiWo4AF9aJvw%3D%3D.asdkasdk"
      val result = AuthenticateCookiesApiRequest(None, None, false, Some(token), trackingData)
      result.isLeft shouldBe true
      result.left.get shouldBe a[ClientBadRequestError]
    }

    "Parse correctly with a valid formatted token" in {
      val token = "3t6IXpUjzYazRb%2BKZAjoduO5KPQ8%2BSgvNDu8p42m5DAon3Z6aswedWBGoLEbXu9PyZbG4Rzf0WAiWo4AF9aJvw%3D%3D.asdkasdk"
      val result = AuthenticateCookiesApiRequest(None, None, false, Some(token), trackingData)
      result.isRight shouldBe true
    }

  }

}
