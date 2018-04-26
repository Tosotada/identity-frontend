package com.gu.identity.frontend.controllers

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import com.google.common.util.concurrent.MoreExecutors
import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.services._
import org.mockito.Matchers.{any => argAny, eq => eql}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.i18n.MessagesApi
import play.api.mvc.Cookie
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.{ExecutionContext, Future}

class ConsentControllerSpec extends PlaySpec with MockitoSugar {

  implicit lazy val materializer: Materializer = ActorMaterializer()(ActorSystem())

  trait WithControllerMockedDependencies {
    val mockIdentityService = mock[IdentityService]
    val messages = mock[MessagesApi]
    val config = Configuration.testConfiguration
    lazy val controller = new ConsentController(config, mockIdentityService, messages, ExecutionContext.fromExecutor(MoreExecutors.directExecutor()))
  }

  "GET /accept-consent/{token}" should {
    "post the provided token to the ID Api, redirect to the consent journey page with cookies set" in new WithControllerMockedDependencies {
      val returnUrl = Some("/consents/thank-you")
      val cookies = Seq(Cookie("test-cookie", "cookie-value", secure = true))
      when(mockIdentityService.authenticateConsentToken(eql("consent-token"))(argAny[ExecutionContext])).thenReturn(Future.successful(Right(cookies)))
      val result = call(controller.confirmConsents("consent-token"), FakeRequest("GET", "/accept-consent/consent-token"))
      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustEqual returnUrl
      headers(result) must contain("Set-Cookie" -> "test-cookie=cookie-value; Path=/; Secure; HTTPOnly")
    }
  }

}
