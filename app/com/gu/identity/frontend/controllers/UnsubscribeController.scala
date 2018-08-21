package com.gu.identity.frontend.controllers

import cats.data.EitherT
import cats.implicits._
import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.errors.BadRequestError
import com.gu.identity.frontend.logging.Logging
import com.gu.identity.frontend.services.IdentityService
import com.gu.identity.frontend.views.ViewRenderer
import com.gu.identity.service.client.request.UnsubscribeApiRequest
import play.api.i18n.I18nSupport
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

case class RawUnsubscribeRequest(emailId: String, userId: String, timestamp: String)

object UnsubscribeRequest {
  private def fromData(data: String): Either[String, RawUnsubscribeRequest] = data.split(":").toList match {
    case emailId :: userId :: timestamp :: Nil => Right(RawUnsubscribeRequest(emailId, userId, timestamp))
    case _ => Left(s"Invalid request data $data")
  }

  def createUnsubscribeApiRequest(emailType: String, data: String, token: String): Either[String, UnsubscribeApiRequest] = {
    for {
      rawRequestData <- fromData(data)
      timestamp <- Try(rawRequestData.timestamp.toLong).toOption.toRight(s"Invalid timestamp ${rawRequestData.timestamp}")
    } yield UnsubscribeApiRequest(emailType, rawRequestData.emailId, rawRequestData.userId, timestamp, token)
  }
}

class UnsubscribeController(identityService: IdentityService,
                            configuration: Configuration,
                            cc: ControllerComponents)
                           (implicit val executionContext: ExecutionContext) extends AbstractController(cc) with I18nSupport with Logging {


  def unsubscribe(emailType: String, data: String, token: String): Action[AnyContent] = Action.async { implicit request =>

    val unsubscribeResult = for {
      unsubscribeRequest <- EitherT.fromEither[Future](UnsubscribeRequest.createUnsubscribeApiRequest(emailType, data, token))
      result <- EitherT(identityService.unsubscribe(unsubscribeRequest))
    } yield result

    unsubscribeResult.value.map {
      case Right(_) =>
        ViewRenderer.renderUnsubscribePage(configuration)
      case Left(error) =>
        logger.warn(s"Error on UnsubscribeController.unsubscribe $error")
        ViewRenderer.renderErrorPage(
          configuration,
          BadRequestError(message = "", rawMessage = Some("""Unable to unsubscribe, please <a href="https://www.theguardian.com/info/tech-feedback">contact us</a> for help.""")),
          Results.BadRequest.apply
        )
    }
  }

}
