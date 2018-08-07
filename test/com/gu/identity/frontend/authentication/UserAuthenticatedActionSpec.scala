package com.gu.identity.frontend.authentication

import java.net.URI

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import com.gu.identity.frontend.models.GroupCode
import com.gu.identity.frontend.test.SimpleFakeApplication
import com.gu.identity.model.{User => CookieUser}
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.mvc.{AnyContent, Cookie}
import play.api.mvc.Results.Ok
import play.api.test.{FakeRequest, Helpers}
import play.api.test.Helpers._

class UserAuthenticatedActionSpec extends PlaySpec with MockitoSugar {

  implicit lazy val materializer: Materializer = ActorMaterializer()(ActorSystem())
  val app = SimpleFakeApplication()
  val cc = Helpers.stubControllerComponents()

  def validCookieDecoding(cookieValue: String) = Some(CookieUser(id = "10000811"))

  def invalidCookieDecoding(cookieValue: String) = None


  "AuthenticatedUserAction" should {
    "stop a request if the cookie is absent or invalid" in {
      running(app) {
        val userAuthenticatedAction =
          new UserAuthenticatedAction(cc, invalidCookieDecoding)

        val action = userAuthenticatedAction {
          request: UserAuthenticatedRequest[AnyContent] => Ok
        }

        val request = FakeRequest("GET", "/agree/GRS")

        val result = call(action, request)

        status(result) mustEqual 303
        redirectLocation(result) mustEqual Some("/signin?group=GRS")
      }
    }

    "add a SC_GU_U cookie to the request if the a SC_GU_U cookie is valid" in {
      running(app) {
        val userAuthenticatedAction =
          new UserAuthenticatedAction(cc, validCookieDecoding)

        val action = userAuthenticatedAction {
          request: UserAuthenticatedRequest[AnyContent] => {
            val scGuUCookie = request.scGuUCookie
            val cookieName = scGuUCookie.name
            val cookieValue = scGuUCookie.value
            Ok(s"$cookieName: $cookieValue")
          }
        }

        val scGuUCookie = Cookie("SC_GU_U", "abc")
        val request = FakeRequest("GET", "/agree/GRS").withCookies(scGuUCookie)

        val result = call(action, request)

        status(result) mustEqual 200
        contentAsString(result) mustEqual "SC_GU_U: abc"
      }
    }

    "redirect to signin if the cookie is present but invalid" in {
      running(app) {
        val userAuthenticatedAction =
          new UserAuthenticatedAction(cc, invalidCookieDecoding)

        val action = userAuthenticatedAction{
          request: UserAuthenticatedRequest[AnyContent] => {
            Ok
          }
        }

        val scGuUCookie = Cookie("SC_GU_U", "abc")
        val request = FakeRequest("GET", "/agree/GRS").withCookies(scGuUCookie)

        val result = call(action, request)

        status(result) mustEqual 303
      }
    }
  }

  "extractGroupCodeFromURI" should {
    val userAuthenticatedAction =
      new UserAuthenticatedAction(cc, invalidCookieDecoding)

    "return a group code object when the uri contains a valid group code" in {

      val uri = new URI("/agree/GRS")
      val result = userAuthenticatedAction.extractGroupCodeFromURI(uri)
      result mustEqual GroupCode("GRS")
    }

    "return None when the uri does not contain a valid group code" in {
      val uri = new URI("/agree/ABC")
      val result = userAuthenticatedAction.extractGroupCodeFromURI(uri)
      result mustEqual None
    }

    "return None when the uri is empty" in {
      val uri = new URI("/")
      val result = userAuthenticatedAction.extractGroupCodeFromURI(uri)
      result mustEqual None
    }
  }
}
