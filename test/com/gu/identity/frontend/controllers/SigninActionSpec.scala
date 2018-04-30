package com.gu.identity.frontend.controllers

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import com.gu.identity.frontend.analytics.AnalyticsEventActor
import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.errors.{SignInServiceBadRequestException, SignInServiceGatewayAppException}
import com.gu.identity.frontend.logging.MetricsLoggingActor
import com.gu.identity.frontend.models.TrackingData
import com.gu.identity.frontend.request.RequestParameters.SignInRequestParameters
import com.gu.identity.frontend.services._
import com.gu.identity.service.client.{ClientBadRequestError, ClientGatewayError}
import org.mockito.ArgumentMatcher
import org.mockito.Mockito._
import org.mockito.Matchers.{argThat, any => argAny}
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.i18n.MessagesApi
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import org.scalatest.Matchers._
import org.scalatestplus.play.guice.GuiceOneServerPerSuite

import scala.concurrent.{ExecutionContext, Future}

class SigninActionSpec extends PlaySpec with MockitoSugar with GuiceOneServerPerSuite {

  implicit lazy val materializer: Materializer = ActorMaterializer()(ActorSystem())
  implicit lazy val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  val signInPageUrl = routes.Application.signIn().url


  trait WithControllerMockedDependencies {
    val mockIdentityService = mock[IdentityService]
    val messages = mock[MessagesApi]
    val config = Configuration.testConfiguration
    val metricsActor = mock[MetricsLoggingActor]
    val eventActor = mock[AnalyticsEventActor]

    lazy val controller =
      new SigninAction(mockIdentityService, app.injector.instanceOf[ControllerComponents], metricsActor, eventActor, config, mock[ServiceAction])

    def mockAuthenticate(
        email: String,
        password: String,
        rememberMe: Boolean = false) = {

      val mockRequest = MockSignInRequest(email, password, rememberMe)

      mockIdentityService.authenticate(
        argThat(signInRequestParamsMatcher(mockRequest)),
        argAny[TrackingData]
      )(argAny[ExecutionContext])
    }
  }

  private case class MockSignInRequest(email: String, password: String, rememberMe: Boolean)
    extends SignInRequestParameters

  def signInRequestParamsMatcher(expect: SignInRequestParameters) =
    new ArgumentMatcher[SignInRequestParameters] {
      def matches(arg: scala.Any): Boolean = arg match {
        case r: SignInRequestParameters
          if r.email == expect.email && r.password == expect.password && r.rememberMe == expect.rememberMe => true
        case _ => false
      }
    }



  def fakeSigninRequest(
      email: Option[String] = None,
      password: Option[String] = None,
      rememberMe: Option[String] = None,
      returnUrl: Option[String] = None) = {

    val bodyParams = Seq(
      email.map("email" -> _),
      password.map("password" -> _),
      rememberMe.map("rememberMe" -> _),
      returnUrl.map("returnUrl" -> _),
      Some("csrfToken" -> "~~fake token~~"),
      Some("gaClientId" -> "~~fake client id~~")
    ).flatten

    FakeRequest("POST", "/actions/signin")
      .withFormUrlEncodedBody(bodyParams: _*)
  }

  def fakeBadRequestError(message: String) =
    Seq(SignInServiceBadRequestException(ClientBadRequestError(message)))

  def fakeGatewayError(message: String = "Unexpected 500 error") =
    Seq(SignInServiceGatewayAppException(ClientGatewayError(message)))


  "POST /signin" should {

    "redirect to returnUrl when passed authentication" in new WithControllerMockedDependencies {
      val email = "me@me.com"
      val password = "password"
      val returnUrl = Some("https://www.theguardian.com/yeah")

      val testCookie = Cookie("SC_GU_U", "##hash##")

      when(mockAuthenticate(email, password))
        .thenReturn {
          Future.successful {
            Right(Seq(testCookie))
          }
        }

      val result = call(controller.signIn, fakeSigninRequest(Some(email), Some(password), returnUrl = returnUrl))
      val resultCookies = cookies(result)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustEqual returnUrl

      resultCookies.size mustEqual 1
      resultCookies.head mustEqual testCookie
    }

    "redirect to sign in page when failed authentication" in new WithControllerMockedDependencies {
      val email = "me@me.com"
      val password = "password"
      val returnUrl = Some("https://www.theguardian.com/yeah")

      when(mockAuthenticate(email, password))
        .thenReturn {
          Future.successful {
            Left(fakeBadRequestError("Invalid email or password"))
          }
        }

      val result = call(controller.signIn, fakeSigninRequest(Some(email), Some(password), returnUrl = returnUrl))

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).get should startWith (signInPageUrl)
      redirectLocation(result).get should include ("error=signin-error-bad-request")
    }

    "redirect to sign in page when service error" in new WithControllerMockedDependencies {
      val email = "me@me.com"
      val password = "password"
      val returnUrl = Some("https://www.theguardian.com/yeah")

      when(mockAuthenticate(email, password))
        .thenReturn {
          Future.successful {
            Left(fakeGatewayError())
          }
        }

      val result = call(controller.signIn, fakeSigninRequest(Some(email), Some(password), None, returnUrl))

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value should startWith (signInPageUrl)
      redirectLocation(result).value should include ("error=signin-error-gateway")
    }


    "redirect to sign in page when error from future" in new WithControllerMockedDependencies {
      val email = "me@me.com"
      val password = "password"
      val returnUrl = Some("https://www.theguardian.com/yeah")

      when(mockAuthenticate(email, password))
        .thenReturn {
          Future.failed {
            new RuntimeException("Unexpected 500 error")
          }
        }

      val result = call(controller.signIn, fakeSigninRequest(Some(email), Some(password), None, returnUrl))

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value should startWith (signInPageUrl)
      redirectLocation(result).value should include ("error=error-unexpected")

    }

  }

}
