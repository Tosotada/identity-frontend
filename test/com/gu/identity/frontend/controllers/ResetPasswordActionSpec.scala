package com.gu.identity.frontend.controllers

import akka.actor.{ActorRef, ActorSystem}
import akka.stream.{ActorMaterializer, Materializer}
import com.gu.identity.frontend.analytics.AnalyticsEventActor
import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.errors.ResetPasswordServiceGatewayAppException
import com.gu.identity.frontend.models.ClientIp
import com.gu.identity.frontend.request.{FormRequestBodyParser, ResetPasswordActionRequestBody, ResetPasswordActionRequestBodyParser}
import com.gu.identity.frontend.services.{IdentityService, ServiceAction}
import com.gu.identity.service.client.{ClientGatewayError, SendResetPasswordEmailResponse}
import org.scalatest.mockito.MockitoSugar
import org.mockito.Matchers.{any => argAny, eq => argEq}
import org.mockito.Mockito._
import org.scalatestplus.play.PlaySpec
import play.api.test.{FakeRequest, Helpers}
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class ResetPasswordActionSpec extends PlaySpec with MockitoSugar {

  implicit lazy val materializer: Materializer = ActorMaterializer()(ActorSystem())

  trait WithControllerMockedDependencies {
    val mockIdentityService = mock[IdentityService]
    val config = Configuration.testConfiguration
    val cc = Helpers.stubControllerComponents()
    val serviceAction = new ServiceAction(cc)
    val formRequestBodyParser = new FormRequestBodyParser(Helpers.stubPlayBodyParsers)
    val parser = new ResetPasswordActionRequestBodyParser(formRequestBodyParser)
    val eventActor = mock[AnalyticsEventActor]
    val controller = new ResetPasswordAction(mockIdentityService, cc, serviceAction, parser, eventActor, config)
  }

  def fakeRequest(email: String) =
    FakeRequest("POST", "/actions/reset")
      .withFormUrlEncodedBody("email" -> email, "csrfToken" -> "~stubbedtoken~")

  def fakeGatewayError(message: String = "Unexpected 500 error") =
    Seq(ResetPasswordServiceGatewayAppException(ClientGatewayError(message)))

  "POST /actions/reset" should {
    "redirect to the email validation sent confirmation page when email was properly sent" in {
      val mockIdentityService = mock[IdentityService]
      val config = Configuration.testConfiguration
      val cc = Helpers.stubControllerComponents()
      val serviceAction = new ServiceAction(cc)
      val formRequestBodyParser = new FormRequestBodyParser(Helpers.stubPlayBodyParsers)
      val parser = new ResetPasswordActionRequestBodyParser(formRequestBodyParser)
      val eventActor = mock[AnalyticsEventActor]
      lazy val controller =
        new ResetPasswordAction(mockIdentityService, cc, serviceAction, parser, eventActor, config)

      val email = "example@gu.com"

      when(mockIdentityService.sendResetPasswordEmail(argAny[ResetPasswordActionRequestBody], argAny[ClientIp])(argAny[ExecutionContext]))
        .thenReturn(Future.successful{Right(SendResetPasswordEmailResponse())})

      val result = call(controller.reset, fakeRequest(email))

      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustEqual Some("/reset/email-sent")
      header("Cache-Control", result) mustEqual Some("no-cache, private")
    }
  }

  "POST /actions/reset" should {
    "return an error if the email is empty" in new WithControllerMockedDependencies {
      val email = ""
      val result = call(controller.reset, fakeRequest(email))

      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustEqual Some("/reset?error=reset-password-error-email")
      header("Cache-Control", result) mustEqual Some("no-cache, private")
    }
  }

  "POST /actions/reset" should {
    "redirect to reset password form if an unexpected error happens when calling the service" in new WithControllerMockedDependencies {
      val email = "example@gu.com"

      when(mockIdentityService.sendResetPasswordEmail(argAny[ResetPasswordActionRequestBody], argAny[ClientIp])(argAny[ExecutionContext]))
        .thenReturn {
          Future.failed {
            new RuntimeException("Unexpected 500 error")
          }
        }

      val result = call(controller.reset, fakeRequest(email))

      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustEqual Some("/reset?error=error-unexpected")
      header("Cache-Control", result) mustEqual Some("no-cache, private")
    }
  }

  "POST /actions/reset" should {
    "return an error if the service failed to send the email" in new WithControllerMockedDependencies {
      val email = "example@gu.com"

      when(mockIdentityService.sendResetPasswordEmail(argAny[ResetPasswordActionRequestBody], argAny[ClientIp])(argAny[ExecutionContext]))
        .thenReturn {
          Future.successful {
            Left(fakeGatewayError())
          }
        }

      val result = call(controller.reset, fakeRequest(email))

      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustEqual Some("/reset?error=reset-password-error-gateway")
      header("Cache-Control", result) mustEqual Some("no-cache, private")
    }
  }
}
