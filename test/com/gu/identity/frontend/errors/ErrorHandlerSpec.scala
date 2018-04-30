package com.gu.identity.frontend.errors

import com.gu.identity.frontend.configuration.Configuration
import org.scalatest.BeforeAndAfter
import play.api.mvc.Result
import play.api._
import play.api.test._
import play.api.test.Helpers._
import org.scalatestplus.play.PlaySpec
import play.api.i18n.Messages
import play.twirl.api.Html

import scala.concurrent.Future


class ErrorHandlerSpec extends PlaySpec with BeforeAndAfter {

  trait WithMockedErrorHandler {
    val mockedErrorHandler = new MockedErrorHandler

    lazy val lastError = mockedErrorHandler.lastError
  }


  "Error handler" must {

    "display 403 forbidden" in new WithMockedErrorHandler {
      val resp = mockedErrorHandler.onClientError(FakeRequest(), 403, "go away")

      status(resp) must equal(403)
      contentAsString(resp) must include("mocked error page")

      lastError.value mustBe a [ForbiddenError]
      lastError.value.asInstanceOf[ForbiddenError].message must equal("go away")
    }

    "display 404 not found error" in new WithMockedErrorHandler {
      val resp = mockedErrorHandler.onClientError(FakeRequest(), 404, "thing not found")

      status(resp) must equal(404)
      contentAsString(resp) must include("mocked error page")

      lastError.value mustBe a [NotFoundError]
      lastError.value.asInstanceOf[NotFoundError].message must equal("thing not found")
    }

    "display 400 bad request error" in new WithMockedErrorHandler {
      val resp = mockedErrorHandler.onClientError(FakeRequest(), 400, "bad thing")

      status(resp) must equal(400)
      contentAsString(resp) must include("mocked error page")

      lastError.value mustBe a [BadRequestError]
      lastError.value.asInstanceOf[BadRequestError].message must equal("bad thing")
    }

    "display 410 gone error" in new WithMockedErrorHandler {
      val resp = mockedErrorHandler.onClientError(FakeRequest(), 410, "goooooone")

      status(resp) must equal(410)
      contentAsString(resp) must include("mocked error page")

      lastError.value mustBe a [BadRequestError]
      lastError.value.asInstanceOf[BadRequestError].message must equal("goooooone")
    }

    "display 500 error" in new WithMockedErrorHandler {
      val err = new RuntimeException("failed!")
      val resp = mockedErrorHandler.onServerError(FakeRequest(), err)

      status(resp) must equal(500)
      contentAsString(resp) must include("mocked error page")

      lastError.value mustBe a [UnexpectedError]
      lastError.value.asInstanceOf[UnexpectedError].description must equal("RuntimeException: failed!")
    }

  }


  class MockedErrorHandler
    extends ErrorHandler(
      configuration = Configuration.testConfiguration,
      messagesApi = Helpers.stubMessagesApi(),
      environment = Environment.simple(mode = Mode.Prod),
      sourceMapper = None,
      router = None) {

    // crude state holder for error passed to error handler
    var lastError: Option[HttpError] = None

    override def renderErrorPage(error: HttpError, resultGenerator: Html => Result)(implicit messages: Messages) = {
      lastError = Some(error)

      Future.successful(resultGenerator(Html("mocked error page")))
    }
  }

}
