package com.gu.identity.frontend.controllers

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.services._
import org.mockito.Matchers.{any => argAny, eq => eql}
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.mvc.Cookie
import play.api.test.{FakeRequest, Helpers}
import play.api.test.Helpers._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class ConsentControllerSpec extends PlaySpec with MockitoSugar {

  implicit lazy val materializer: Materializer = ActorMaterializer()(ActorSystem())

  trait WithControllerMockedDependencies {
    val mockIdentityService = mock[IdentityService]
    val config = Configuration.testConfiguration
    val cc = Helpers.stubControllerComponents()
    lazy val controller = new ConsentController(config, mockIdentityService, cc)
  }

  "GET /accept-consent/{token}" should {
    "post the provided token to the ID Api, redirect to the consent journey page with cookies set" in new WithControllerMockedDependencies {
      val returnUrl = Some("/consents/thank-you")
      val testCookie = Cookie("test-cookie", "cookie-value", secure = true)
      when(mockIdentityService.authenticateConsentToken(eql("consent-token"))(argAny[ExecutionContext])).thenReturn(Future.successful(Right(Seq(testCookie))))
      val result = call(controller.confirmConsents("consent-token"), FakeRequest("GET", "/accept-consent/consent-token"))
      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustEqual returnUrl
      Helpers.cookies(result) must contain(testCookie)
    }
  }

}
