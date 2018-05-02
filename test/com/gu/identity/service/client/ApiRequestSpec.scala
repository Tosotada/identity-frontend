package com.gu.identity.service.client

import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.mvc.Cookie

class ApiRequestSpec extends PlaySpec with MockitoSugar{

  val handler = mock[IdentityClientRequestHandler]

  implicit val testConfig = IdentityClientConfiguration("test.gu.com", "##key##", handler)

  val cookie = Cookie("SC_GU_TEST", "123")

  "ApiRequest" should {

    "create a url for the api endpoint when given a path" in {
      ApiRequest.apiEndpoint("test/test") mustEqual "https://test.gu.com/test/test"
    }

    "create a url for the api endpoint when given a list of path components" in {
      val pathComponents = Seq("test", "test")
      ApiRequest.apiEndpoint(pathComponents: _*) mustEqual "https://test.gu.com/test/test"
    }

    "return the base url for the api if the list of path components is empty" in {
      val pathComponents = Seq.empty
      ApiRequest.apiEndpoint(pathComponents: _*) mustEqual "https://test.gu.com/"
    }

    "return a iterable containing the X-GU-ID-Client-Access-Token and value for this token" in {
      val headers = Seq(ApiRequest.apiKeyHeader)
      headers.head equals ("X-GU-ID-Client-Access-Token","Bearer ##key##")
    }

    "return a secure cookie user header" in {
      val userHeader = ApiRequest.apiSecureCookieUserHeader(cookie)
      userHeader.head equals ("X-GU-ID-FOWARDED-SC-GU-U", "123")
    }
  }

}
