package com.gu.identity.frontend.services

import com.gu.identity.frontend.configuration.Configuration
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.ws.WSClient
import org.scalatest.concurrent.ScalaFutures._
import scala.concurrent.Future
import scala.util.Try
import scala.concurrent.ExecutionContext.Implicits.global

class GoogleRecaptchaServiceHandlerTest extends WordSpec with Matchers with MockitoSugar{

  val mockWSClient = mock[WSClient]
  val mockConfig = mock[Configuration]

  val googleRecaptchaServiceHandler = new GoogleRecaptchaServiceHandler(mockWSClient, mockConfig)

  "When handling the recaptcha response" should {

    "return a future true if google successfully verified the recaptcha and it is valid" in {

      val response = Future.successful(Try(GoogleResponse(true, None)))

      val futureResult = googleRecaptchaServiceHandler.handleRecaptchaResponse(response)

      whenReady(futureResult) {
        result => result should equal(true)
      }
    }

    "return a future false if google successfully verified the recaptcha and it is invalid" in {

      val response = Future.successful(Try(GoogleResponse(false, None)))

      val futureResult = googleRecaptchaServiceHandler.handleRecaptchaResponse(response)

      whenReady(futureResult) {
        result => result should equal(false)
      }
    }

    "return a future false if google fails to verify the recaptcha" in {

      val response = Future.failed(new Exception())

      val futureResult = googleRecaptchaServiceHandler.handleRecaptchaResponse(response)

      whenReady(futureResult) {
        result => result should equal(false)
      }
    }
  }

}
