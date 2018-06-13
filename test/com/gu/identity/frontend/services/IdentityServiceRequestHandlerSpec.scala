package com.gu.identity.frontend.services

import com.gu.identity.model.Consent._
import com.gu.identity.model.Consent
import com.gu.identity.service.client.request._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, OptionValues, WordSpec}
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSClient
import scala.concurrent.ExecutionContext.Implicits.global

class IdentityServiceRequestHandlerSpec extends WordSpec with Matchers with MockitoSugar with OptionValues {

  val mockWSClient = mock[WSClient]

  val handler = new IdentityServiceRequestHandler(mockWSClient)

  "When handling the request body" should {

    "Encode params correctly when using an Authenticate Cookies request." in {
      val email = "test@guardian.co.uk"
      val password = "some%thing"

      val requestBody = AuthenticateCookiesApiRequestBody(email, password)
      val result = handler.handleRequestBody(requestBody)

      result should equal ("email=test%40guardian.co.uk&password=some%25thing")
    }

    "Encode correct json when using a Register Request." in {
      val email = "test@guardian.co.uk"
      val password = "some%thing"
      val displayName = "myDisplayname"
      val firstName = "First"
      val secondName = "Last"
      val receiveGnmMarketing = false
      val receive3rdPartyMarketing = false
      val registrationIp = "123.456.789.012"
      val consents = List(Consent(Supporter.id, "user", true))

      val requestBodyModel = RegisterRequestBody(
        email,
        password,
        RegisterRequestBodyPublicFields(displayName),
        RegisterRequestBodyPrivateFields(firstName, secondName, registrationIp),
        RegisterRequestBodyStatusFields(false, false),
        consents
      )
      val bodyString: String = handler.handleRequestBody(requestBodyModel)
      val bodyJson: JsValue = Json.parse(bodyString)

      (bodyJson \ "primaryEmailAddress").as[String] should equal(email)
      (bodyJson \ "password").as[String] should equal(password)
      (bodyJson \ "publicFields" \ "displayName").as[String] should equal(displayName)
      (bodyJson \ "privateFields" \ "firstName").as[String] should equal(firstName)
      (bodyJson \ "privateFields" \ "secondName").as[String] should equal(secondName)
      (bodyJson \ "privateFields" \ "registrationIp").as[String] should equal(registrationIp)
      ((bodyJson \ "consents")(0) \ "consented").as[Boolean] should equal(consents.head.consented)
    }


    "Encode correct json when using a SendResetPasswordEmailRequest" in {
      val email = "test@guardian.co.uk"

      val requestBody = SendResetPasswordEmailRequestBody(email)

      val result: String = handler.handleRequestBody(requestBody)
      val jsonResult = Json.parse(result)

      (jsonResult \ "email-address").validate[String].asOpt.value should equal(email)
    }
  }
}
