package com.gu.identity.frontend.controllers

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import com.gu.identity.frontend.authentication.CookieName
import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.errors.DeauthenticateAppException
import com.gu.identity.frontend.models.TrackingData
import com.gu.identity.frontend.services._
import com.gu.identity.service.client.ClientBadRequestError
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.{any => argAny}
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.mvc._
import play.api.test.{FakeRequest, Helpers}
import play.api.test.Helpers._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class SignOutActionSpec extends PlaySpec with MockitoSugar {

  implicit lazy val materializer: Materializer = ActorMaterializer()(ActorSystem())

  trait WithControllerMockedDependencies {
    val mockIdentityService = mock[IdentityService]
    val config = Configuration.testConfiguration
    lazy val controller = new SignOutAction(mockIdentityService, Helpers.stubControllerComponents(), config)
  }

  val secureCookie = Cookie(name = CookieName.SC_GU_U.toString, value = "SC_GU_U_data", maxAge = None, path = "/", domain = Some("dev-theguardian.com"), secure = true, httpOnly = true)

  val signedInCookies = Seq(
    secureCookie,
    Cookie(name = CookieName.SC_GU_LA.toString, value = "SC_GU_LA", maxAge = None, path = "/", domain = Some("dev-theguardian.com"), secure = true, httpOnly = true),
    Cookie(name = CookieName.GU_U.toString, value = "GU_U", maxAge = None, path = "/", domain = Some("dev-theguardian.com"), secure = true, httpOnly = false)
  )

  val referer = "https://www.theguardian.com/refs"
  val signOutCookie = Cookie(name = CookieName.GU_SO.toString, value = "data_for_GU_SO")

  def fakeSignOutRequest(cookies: Seq[Cookie] = Seq.empty) = FakeRequest("GET", "/signout")
    .withCookies(cookies: _*)
    .withHeaders("Referer" -> referer)

  "GET /signout" should {

    "redirect to returnUrl when user has SC_GU_U cookie" in new WithControllerMockedDependencies {
      val returnUrl = Some("https://www.theguardian.com/yeah")

      val captor = ArgumentCaptor.forClass(classOf[Cookie])

      when(mockIdentityService.deauthenticate(captor.capture(), argAny[TrackingData])(argAny[ExecutionContext]))
        .thenReturn {
          Future.successful {
            Right(Seq(signOutCookie))
          }
        }

      val result = call(controller.signOut(returnUrl), fakeSignOutRequest(signedInCookies))
      val resultCookies = cookies(result)

      captor.getValue.name mustEqual secureCookie.name
      captor.getValue.value mustEqual secureCookie.value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustEqual returnUrl

      resultCookies.size mustEqual 10
    }

    "redirect to returnUrl when Identity API call fails" in new WithControllerMockedDependencies {
      val returnUrl = Some("https://www.theguardian.com/yeah")

      when(mockIdentityService.deauthenticate(argAny[Cookie], argAny[TrackingData])(argAny[ExecutionContext]))
        .thenReturn {
          Future.successful {
            Left(Seq(DeauthenticateAppException(ClientBadRequestError("Something went wrong"))))
          }
        }

      val result = call(controller.signOut(returnUrl), fakeSignOutRequest(signedInCookies))
      val resultCookies = cookies(result)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustEqual returnUrl

      resultCookies.size mustEqual 9
    }

    "redirect to returnUrl despite lack of SC_GU_U cookie" in new WithControllerMockedDependencies {
      val returnUrl = Some("https://www.theguardian.com/yeah")

      val result = call(controller.signOut(returnUrl), fakeSignOutRequest(Seq.empty))
      val resultCookies = cookies(result)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustEqual returnUrl

      resultCookies.size mustEqual 9
    }

    "set GU_SO cookie when user has SC_GU_U cookie" in new WithControllerMockedDependencies {
      val returnUrl = Some("https://www.theguardian.com/yeah")

      val captor = ArgumentCaptor.forClass(classOf[Cookie])

      when(mockIdentityService.deauthenticate(captor.capture(), argAny[TrackingData])(argAny[ExecutionContext]))
        .thenReturn {
          Future.successful {
            Right(Seq(signOutCookie))
          }
        }

      val result = call(controller.signOut(returnUrl), fakeSignOutRequest(signedInCookies))
      val resultCookies = cookies(result)

      captor.getValue.name mustEqual secureCookie.name
      captor.getValue.value mustEqual secureCookie.value

      resultCookies.size mustEqual 10
      resultCookies.get("GU_SO").get.name == "GU_SO"
      resultCookies.get("GU_SO").get.value == "data_for_GU_SO"
    }

  }

  "redirect to referrer when no return is supplied" in new WithControllerMockedDependencies {

    val captor = ArgumentCaptor.forClass(classOf[Cookie])

    when(mockIdentityService.deauthenticate(captor.capture(), argAny[TrackingData])(argAny[ExecutionContext]))
      .thenReturn {
        Future.successful {
          Right(Seq(signOutCookie))
        }
      }

    val result = call(controller.signOut(None), fakeSignOutRequest(signedInCookies))
    val resultCookies = cookies(result)

    captor.getValue.name mustEqual secureCookie.name
    captor.getValue.value mustEqual secureCookie.value

    status(result) mustEqual SEE_OTHER
    redirectLocation(result) mustEqual Some(referer)

    resultCookies.size mustEqual 10
  }
}
