package com.gu.identity.service.client

import com.gu.identity.service.client.request.UnsubscribeApiRequest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}
import org.mockito.Mockito._
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import scala.concurrent.ExecutionContext.Implicits.global
import play.core.server.Server
import play.api.routing.sird._
import play.api.mvc._
import play.api.test._
import scala.concurrent.duration._
import scala.util.Left

class IdentityClientTest extends FlatSpec with Matchers with ScalaFutures with MockitoSugar with RequestMethodExtractors {

  private val unsubscribeRequest = UnsubscribeApiRequest("emailType", "emailId", "userId", 1244627467l, "token")

  "unsubscribe" should "unsubscribe a user from an email in identity api" in {
    Server.withRouterFromComponents() { components =>
      import Results._
      import components.{defaultActionBuilder => Action}
    {
      case POST(p"/unsubscribe") => Action {
        NoContent
      }
    }
    } { implicit port =>
      WsTestClient.withClient { client =>
        val configMock = mock[IdentityClientConfiguration]
        when(configMock.hostWithProtocol) thenReturn ""
        val identityClient = new IdentityClient(client)

        whenReady(identityClient.unsubscribe(unsubscribeRequest)(configMock, global), Timeout(10 seconds)) { result =>
          result shouldBe Right(UnitResponse)
        }
      }
    }
  }

  "unsubscribe" should "return a failure on unexpected status code" in {
    Server.withRouterFromComponents() { components =>
      import Results._
      import components.{defaultActionBuilder => Action}
    {
      case POST(p"/unsubscribe") => Action {
        BadRequest
      }
    }
    } { implicit port =>
      WsTestClient.withClient { client =>
        val configMock = mock[IdentityClientConfiguration]
        when(configMock.hostWithProtocol) thenReturn ""
        val identityClient = new IdentityClient(client)

        whenReady(identityClient.unsubscribe(unsubscribeRequest)(configMock, global), Timeout(10 seconds)) { result =>
          result shouldBe Left(OtherClientBadRequestError("Invalid request"))
        }
      }
    }
  }


}
