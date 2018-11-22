package com.gu.identity.frontend.controllers

import cats.data.EitherT
import cats.implicits._
import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.errors.BadRequestError
import com.gu.identity.frontend.logging.Logging
import com.gu.identity.frontend.services.IdentityService
import com.gu.identity.frontend.views.ViewRenderer
import com.gu.identity.model.Consent
import com.gu.identity.service.client.request.UnsubscribeApiRequest
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

case class RawUnsubscribeRequest(emailId: String, userId: String, timestamp: String)

object UnsubscribeRequest {
  private def fromData(data: String): Either[String, RawUnsubscribeRequest] = data.split(":").toList match {
    case emailId :: userId :: timestamp :: Nil => Right(RawUnsubscribeRequest(emailId, userId, timestamp))
    case _ => Left(s"Invalid request data $data")
  }

  def findConsentWording(unsubscribeApiRequest: UnsubscribeApiRequest): Option[String] = {
    Consent
      .allConsentsById
      .get(unsubscribeApiRequest.emailId)
      .flatMap(_.wordings.headOption.map(_.wording))
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

  private def unsubscribeViaIdapi(data: String, token: String, emailType: String): EitherT[Future, String, UnsubscribeApiRequest] = {
    for {
      unsubscribeRequest <- EitherT.fromEither[Future](UnsubscribeRequest.createUnsubscribeApiRequest(emailType, data, token))
      _ <- EitherT(identityService.unsubscribe(unsubscribeRequest))
    } yield unsubscribeRequest
  }

  private def unsubscribeErrorPage()(implicit messages: Messages) = {
    ViewRenderer.renderErrorPage(
      configuration,
      BadRequestError(message = "", rawMessage = Some("""Unable to unsubscribe, please <a href="https://www.theguardian.com/info/tech-feedback">contact us</a> for help.""")),
      Results.BadRequest.apply
    )
  }

  def unsubscribeNewsletter(data: String, token: String): Action[AnyContent] = Action.async { implicit request =>

    val unsubscribeInfo: EitherT[Future, String, UnsubscribeApiRequest] = unsubscribeViaIdapi(data, token, "newsletter")

    unsubscribeInfo.value.map {
      case Right(_) =>
        ViewRenderer.renderUnsubscribePage(configuration)
      case Left(error) =>
        logger.warn(s"Error on UnsubscribeController.unsubscribe $error $data $token")
        unsubscribeErrorPage()
    }
  }

  def unsubscribeMarketing(data: String, token: String): Action[AnyContent] = Action.async { implicit request =>

    val unsubscribeInfo: EitherT[Future, String, UnsubscribeApiRequest] = unsubscribeViaIdapi(data, token, "marketing")

    unsubscribeInfo.value.map {
      case Right(unsubscribeData) =>
        UnsubscribeRequest.findConsentWording(unsubscribeData)
          .map(consentWording => ViewRenderer.renderConsentUnsubscribePage(configuration, consentWording))
          .getOrElse {
            logger.error(s"unsubscribeMarketing unrecognised unsubscribe page for $unsubscribeData")
            unsubscribeErrorPage()
          }
      case Left(error) =>
        logger.warn(s"Error on unsubscribeMarketing $error $data $token")
        unsubscribeErrorPage()
    }
  }

}
