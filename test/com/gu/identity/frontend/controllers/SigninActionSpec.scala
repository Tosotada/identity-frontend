package com.gu.identity.frontend.controllers

import com.gu.identity.frontend.models.TrackingData
import com.gu.identity.frontend.services._
import org.mockito.Mockito._
import org.mockito.Matchers.{any => argAny, eq => argEq}
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.inject.bind
import play.api.inject.guice.GuiceInjectorBuilder
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.Cookie
import play.api.test.FakeRequest
import play.api.test.Helpers._
import org.scalatest.Matchers._

import scala.concurrent.{ExecutionContext, Future}


class SigninActionSpec extends PlaySpec with MockitoSugar {

  trait WithControllerMockedDependencies {
    val mockIdentityService = mock[IdentityService]

    val injector = new GuiceInjectorBuilder()
      .overrides(bind[IdentityService].to(mockIdentityService))
      .injector()

    val controller = injector.instanceOf[SigninAction]
  }

  def fakeSigninRequest(email: Option[String], password: Option[String], rememberMe: Option[String], returnUrl: Option[String]) = {
    val bodyParams = Seq("email" -> email, "password" -> password, "keepMeSignedIn" -> rememberMe, "returnUrl" -> returnUrl)
      .filter(_._2.isDefined)
      .map(p => p._1 -> p._2.get)

    FakeRequest("POST", "/actions/signin")
      .withFormUrlEncodedBody(bodyParams: _*)
  }


  "POST /signin" should {

    "redirect to returnUrl when passed authentication" in new WithControllerMockedDependencies {
      val email = Some("me@me.com")
      val password = Some("password")
      val rememberMe = None
      val returnUrl = Some("http://www.theguardian.com/yeah")

      val testCookie = Cookie("SC_GU_U", "##hash##")

      when(mockIdentityService.authenticate(argEq(email), argEq(password), argEq(rememberMe.isDefined), argAny[TrackingData])(argAny[ExecutionContext]))
        .thenReturn {
          Future.successful {
            Right(Seq(testCookie))
          }
        }

      val result = call(controller.signIn, fakeSigninRequest(email, password, None, returnUrl))
      val resultCookies = cookies(result)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustEqual returnUrl

      resultCookies.size mustEqual 1
      resultCookies.head mustEqual testCookie
    }

    "redirect to sign in page when failed authentication" in new WithControllerMockedDependencies {
      val email = Some("me@me.com")
      val password = Some("password")
      val rememberMe = None
      val returnUrl = Some("http://www.theguardian.com/yeah")

      when(mockIdentityService.authenticate(argEq(email), argEq(password), argEq(rememberMe.isDefined), argAny[TrackingData])(argAny[ExecutionContext]))
        .thenReturn {
          Future.successful {
            Left(Seq(ServiceBadRequest("Invalid email or password")))
          }
        }

      val result = call(controller.signIn, fakeSigninRequest(email, password, None, returnUrl))

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).get should startWith (routes.Application.signIn(None, Seq.empty).url)

      // TODO check error parameters
    }

    "redirect to sign in page when service error" in new WithControllerMockedDependencies {
      val email = Some("me@me.com")
      val password = Some("password")
      val rememberMe = None
      val returnUrl = Some("http://www.theguardian.com/yeah")

      when(mockIdentityService.authenticate(argEq(email), argEq(password), argEq(rememberMe.isDefined), argAny[TrackingData])(argAny[ExecutionContext]))
        .thenReturn {
          Future.successful {
            Left(Seq(ServiceGatewayError("Unexpected 500 error")))
          }
        }

      val result = call(controller.signIn, fakeSigninRequest(email, password, None, returnUrl))

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).get should startWith (routes.Application.signIn(None, Seq.empty).url)

      // TODO check error parameters
    }


    "redirect to sign in page when error from future" in new WithControllerMockedDependencies {
      val email = Some("me@me.com")
      val password = Some("password")
      val rememberMe = None
      val returnUrl = Some("http://www.theguardian.com/yeah")

      when(mockIdentityService.authenticate(argEq(email), argEq(password), argEq(rememberMe.isDefined), argAny[TrackingData])(argAny[ExecutionContext]))
        .thenReturn {
          Future.failed {
            new RuntimeException("Unexpected 500 error")
          }
        }

      val result = call(controller.signIn, fakeSigninRequest(email, password, None, returnUrl))

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).get should startWith (routes.Application.signIn(None, Seq.empty).url)

      // TODO check error parameters
    }

  }

}
