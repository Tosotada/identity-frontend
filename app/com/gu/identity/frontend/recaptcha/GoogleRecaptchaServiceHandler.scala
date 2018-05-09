package com.gu.identity.frontend.services

import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.logging.Logging
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import scala.util.control.NonFatal


class GoogleRecaptchaServiceHandler(
    ws: WSClient,
    configuration: Configuration)
    (implicit executionContext: ExecutionContext)
  extends Logging{

  implicit val googleResponseFormat = Json.format[GoogleResponse]

  def isValidRecaptchaResponse(captchaResponseCode: String): Future[Boolean] = {
    val googleResponse = getRecaptchaResponseFromGoogle(captchaResponseCode)
    handleRecaptchaResponse(googleResponse)

  }

  def handleRecaptchaResponse(response: Future[Try[GoogleResponse]]): Future[Boolean] = {
    response.map {
      case Success(GoogleResponse(true,_)) => true
      case Success(GoogleResponse(false, errors)) => {
        logger.error(s"Google Recaptcha failed to authenticate ${errors}")
        false
      }
      case Failure(ex) => {
        logger.error(s"Json from google recaptcha could not be parsed ${ex.getMessage}", ex)
        false
      }
    }.recover{
      case NonFatal(ex) => {
        logger.error(s"Unexpected error from google recaptcha: ${ex.getMessage}", ex)
        false
      }
    }
  }

  def getRecaptchaResponseFromGoogle(captchaResponseCode: String): Future[Try[GoogleResponse]] = {
    ws.url("https://www.google.com/recaptcha/api/siteverify").post(
      Map("secret" -> Seq(configuration.googleRecaptchaSecretKey), "response" -> Seq(captchaResponseCode))
    ).map {
      googleResponse => {
        Try(googleResponse.json.as[GoogleResponse])
      }
    }
  }
}

case class GoogleResponse(success: Boolean, errorCodes: Option[List[String]])
