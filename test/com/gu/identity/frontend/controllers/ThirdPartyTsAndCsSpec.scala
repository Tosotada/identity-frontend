package com.gu.identity.frontend.controllers

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.Timeout
import com.gu.identity.frontend.authentication.{CookieName, UserAuthenticatedAction, UserAuthenticatedRequest}
import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.errors.{AssignGroupAppException, ErrorHandler, GetUserAppException}
import com.gu.identity.frontend.models.{GroupCode, ReturnUrl}
import com.gu.identity.frontend.services.IdentityService
import com.gu.identity.service.client.{AssignGroupResponse, ClientGatewayError}
import com.gu.identity.service.client.models.{User, UserGroup}
import com.gu.identity.model.{User => CookieUser}
import org.mockito.Matchers.{any => argAny}
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.mvc.Results._
import play.api.mvc.{AnyContent, ControllerComponents, Cookie, RequestHeader}
import play.api.test.{FakeRequest, Helpers}
import play.api.test.Helpers._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class ThirdPartyTsAndCsSpec extends PlaySpec with MockitoSugar {

  implicit lazy val materializer: Materializer = ActorMaterializer()(ActorSystem())

  trait WithControllerMockedDependencies {
    val mockIdentityService = mock[IdentityService]

    val testConfig = Configuration.testConfiguration
    val mockErrorHandler = mock[ErrorHandler]
    val cc = Helpers.stubControllerComponents(playBodyParsers = Helpers.stubPlayBodyParsers)

    def validCookieDecoding(cookieValue: String) = Some(CookieUser(id = "10000811"))

    val userAuthAction = new UserAuthenticatedAction(cc, validCookieDecoding)

    val thirdPartyTsAndCsController =
      new ThirdPartyTsAndCs(mockIdentityService, testConfig,  mockErrorHandler, validCookieDecoding, userAuthAction, cc)

  }

  def successfulFakeRequest(groupCode: String, returnUrl: String, cookie: Cookie) = {
    FakeRequest("POST", "/actions/GTNF")
      .withFormUrlEncodedBody("groupCode" -> groupCode, "returnUrl" -> returnUrl)
      .withCookies(cookie)
  }

  def successfulUserAuthFakeRequest(groupCode: String, returnUrl: String, cookie: Cookie) = {
    val request = FakeRequest("POST", "/actions/GTNF")
      .withFormUrlEncodedBody("groupCode" -> groupCode, "returnUrl" -> returnUrl)
      .withCookies(cookie)

    new UserAuthenticatedRequest[AnyContent](cookie, request)
  }

  "Is user in group" should {
    "return true if user is in group specified" in new WithControllerMockedDependencies {
      val groupCode = "ABC"
      val userGroup = UserGroup(groupCode, "Group/ABC")
      val userGroups = List(userGroup)
      val user = User(userGroups = userGroups)

      thirdPartyTsAndCsController.isUserInGroup(user, groupCode) mustEqual true
    }

    "return false if user is not in the group specified" in  new WithControllerMockedDependencies {
      val groupCode = "123"
      val userGroup = UserGroup("ABC", "Group/ABC")
      val userGroups = List(userGroup)
      val user = User(userGroups = userGroups)

      thirdPartyTsAndCsController.isUserInGroup(user, groupCode) mustEqual false
    }
  }

  "checkUserForGroupMembership" should {
    "return a future of true when the user is in already in the group specified" in new WithControllerMockedDependencies {
      val groupCode = "GRS"
      val group = GroupCode(groupCode).get
      val userGroup = UserGroup(groupCode, "Group/GRS")
      val userGroups = List(userGroup)
      val user = User(userGroups = userGroups)

      val timeout = Timeout(5 seconds)

      val cookie = Cookie("Name", "Value")

      when(mockIdentityService.getUser(argAny[Cookie])(argAny[ExecutionContext]))
        .thenReturn {
          Future.successful{
            Right(user)
          }
        }

      val future = thirdPartyTsAndCsController.checkUserForGroupMembership(group, cookie)
      val result = Await.result(future, timeout.duration)

      result mustEqual Right(true)
    }

    "return a future of false when the user is not in the group specified" in new WithControllerMockedDependencies {
      val groupCode = "GTNF"
      val group = GroupCode(groupCode).get
      val userGroup = UserGroup("123", "Group/GTNF")
      val userGroups = List(userGroup)
      val user = User(userGroups = userGroups)

      val timeout = Timeout(5 seconds)

      val cookie = Cookie("Name", "Value")

      when(mockIdentityService.getUser(argAny[Cookie])(argAny[ExecutionContext]))
        .thenReturn {
          Future.successful{
            Right(user)
          }
        }

      val future = thirdPartyTsAndCsController.checkUserForGroupMembership(group, cookie)
      val result = Await.result(future, timeout.duration)

      result mustEqual Right(false)
    }

    "return a future of sequence of service errors if it was not possible to check if the user is in the group" in new WithControllerMockedDependencies {
      val groupCode = "GTNF"
      val group = GroupCode(groupCode).get
      val userGroup = UserGroup(groupCode, "Group/GTNF")
      val userGroups = List(userGroup)
      val user = User(userGroups = userGroups)

      val timeout = Timeout(5 seconds)

      val cookie = Cookie("Name", "Value")

      val stubbedError = GetUserAppException(ClientGatewayError("unexpected error"))

      when(mockIdentityService.getUser(argAny[Cookie])(argAny[ExecutionContext]))
        .thenReturn {
          Future.successful{
            Left(Seq(stubbedError))
          }
        }

      val future = thirdPartyTsAndCsController.checkUserForGroupMembership(group, cookie)
      val result = Await.result(future, timeout.duration)

      result mustEqual Left(Seq(stubbedError))
    }
  }

  "GET /agree/:group" should {



  }

  "confirm" should {
    "Redirect to the return url when the user is already a group member" in new WithControllerMockedDependencies {
      val groupCode = "GTNF"
      val group = GroupCode(groupCode).get
      val userGroup = UserGroup(groupCode, "Group/GTNF")
      val userGroups = List(userGroup)
      val user = User(userGroups = userGroups)
      val url = Some("https://www.theguardian.com/sport")
      val returnUrl = ReturnUrl(url, Configuration.testConfiguration)
      val cookie = Cookie("Name", "Value")
      val timeout = Timeout(5 seconds)
      implicit val fakeRequest = successfulUserAuthFakeRequest(groupCode, returnUrl.url, cookie)

      when(mockIdentityService.getUser(argAny[Cookie])(argAny[ExecutionContext]))
        .thenReturn {
          Future.successful{
            Right(user)
          }
        }

      val future = thirdPartyTsAndCsController.confirm(group, returnUrl, clientId = None, skipConfirmation = false, cookie)
      val result = Await.result(future, timeout.duration)
      val r = Future.successful(result.right.get)
      redirectLocation(r) mustEqual url
      status(r) mustEqual SEE_OTHER
    }

    "Redirect to the return url when the user successfully added to the group and skip confirmation is true" in new WithControllerMockedDependencies {
      val groupCode = "GTNF"
      val group = GroupCode(groupCode).get
      val userGroup = UserGroup("ABC", "Group/ABC")
      val userGroups = List(userGroup)
      val user = User(userGroups = userGroups)
      val url = Some("https://www.theguardian.com/sport")
      val returnUrl = ReturnUrl(url, Configuration.testConfiguration)
      val cookie = Cookie("Name", "Value")
      val timeout = Timeout(5 seconds)
      implicit val fakeRequest = successfulUserAuthFakeRequest(groupCode, returnUrl.url, cookie)

      when(mockIdentityService.getUser(argAny[Cookie])(argAny[ExecutionContext]))
        .thenReturn {
          Future.successful{
            Right(user)
          }
        }

      when(mockIdentityService.assignGroupCode(groupCode, cookie))
        .thenReturn{
          Future.successful{
            Right(AssignGroupResponse(groupCode))
          }
        }

      val future = thirdPartyTsAndCsController.confirm(group, returnUrl, clientId = None, skipConfirmation = true, cookie)
      val result = Await.result(future, timeout.duration)
      val r = Future.successful(result.right.get)
      redirectLocation(r) mustEqual url
      status(r) mustEqual SEE_OTHER
    }

    "Return errors if it is not possible to check the users group membership" in new WithControllerMockedDependencies {
      val groupCode = "GTNF"
      val group = GroupCode(groupCode).get
      val userGroup = UserGroup("ABC", "Group/ABC")
      val userGroups = List(userGroup)
      val user = User(userGroups = userGroups)
      val url = Some("https://www.theguardian.com/sport")
      val returnUrl = ReturnUrl(url, Configuration.testConfiguration)
      val cookie = Cookie("Name", "Value")
      val timeout = Timeout(5 seconds)
      implicit val fakeRequest = successfulUserAuthFakeRequest(groupCode, returnUrl.url, cookie)

      val stubbedError = GetUserAppException(ClientGatewayError("error"))

      when(mockIdentityService.getUser(argAny[Cookie])(argAny[ExecutionContext]))
        .thenReturn {
          Future.successful{
            Left(Seq(stubbedError))
          }
        }

      val future = thirdPartyTsAndCsController.confirm(group, returnUrl, clientId = None, skipConfirmation = false, cookie)
      val result = Await.result(future, timeout.duration)
      val r = result.left.get
      r mustEqual Seq(stubbedError)
    }
  }

  "POST action/agree" should {

    val validCookie: Cookie = {
      val validRequestCookieData = "WyIxMDAwMDgxMSIsMTQ2Mjg5MjgyNDYxMV0.MCwCFG_PdoPk2PpSO5KoXbRLWJ0BvuqhAhRFIt1mlDcO2SN1Y6X7ktSs_oRJJw"
      Cookie(name = CookieName.SC_GU_U.toString, value = validRequestCookieData)
    }



    "return a result of the return url when user has successfully been added to the group" in new WithControllerMockedDependencies {
      val groupCode = "GTNF"
      val returnUrl = "https://www.theguardian.com/sport"
      val cookie = validCookie
      val fakeRequest = successfulFakeRequest(groupCode, returnUrl, cookie)

      when(mockIdentityService.assignGroupCode(groupCode, cookie))
        .thenReturn{
          Future.successful{
            Right(AssignGroupResponse(groupCode))
          }
        }

      val result = call(thirdPartyTsAndCsController.addToGroupAction(), fakeRequest)
      redirectLocation(result) mustEqual Some(returnUrl)
      status(result) mustEqual SEE_OTHER
    }

    "return bad request when it is not possible to add the user to group" in new WithControllerMockedDependencies {
      val groupCode = "GTNF"
      val returnUrl = "https://www.theguardian.com/sport"
      val cookie = validCookie
      val fakeRequest = successfulFakeRequest(groupCode, returnUrl, cookie)

      when(mockIdentityService.assignGroupCode(groupCode, cookie))
        .thenReturn{
          Future.successful{
            Left(Seq(AssignGroupAppException(ClientGatewayError("error"))))
          }
        }

      when(mockErrorHandler.onClientError(argAny[RequestHeader], argAny[Int], argAny[String]))
        .thenReturn(Future.successful(BadRequest("Fail")))

      val result = call(thirdPartyTsAndCsController.addToGroupAction(), fakeRequest)
      status(result) mustEqual BAD_REQUEST
    }

    "return a not found error if group code is invalid" in new WithControllerMockedDependencies {
      val groupCode = "ABC"
      val returnUrl = "https://www.theguardian.com/sport"
      val cookie = validCookie
      val fakeRequest = successfulFakeRequest(groupCode, returnUrl, cookie)

      when(mockIdentityService.assignGroupCode(groupCode, cookie))
        .thenReturn{
          Future.successful{
            Left(Seq(AssignGroupAppException(ClientGatewayError("error"))))
          }
        }

      when(mockErrorHandler.onClientError(argAny[RequestHeader], argAny[Int], argAny[String]))
        .thenReturn(Future.successful(NotFound("Fail")))

      val result = call(thirdPartyTsAndCsController.addToGroupAction(), fakeRequest)
      status(result) mustEqual NOT_FOUND
    }

    "return bad request when the form submission to add to group action is invalid" in new WithControllerMockedDependencies {
      val groupCode = "GTNF"
      val returnUrl = "https://www.theguardian.com/sport"
      val cookie = validCookie
      val fakeRequest = FakeRequest("POST", "/actions/GTNF")
        .withFormUrlEncodedBody("group" -> groupCode, "returnUrl" -> returnUrl)
        .withCookies(cookie)

      when(mockErrorHandler.onClientError(argAny[RequestHeader], argAny[Int], argAny[String]))
        .thenReturn(Future.successful(BadRequest("Fail")))

      val result = call(thirdPartyTsAndCsController.addToGroupAction(), fakeRequest)
      status(result) mustEqual BAD_REQUEST
    }
  }
}
