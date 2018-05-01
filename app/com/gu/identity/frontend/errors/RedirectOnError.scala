package com.gu.identity.frontend.errors

import com.gu.identity.frontend.controllers.NoCache
import com.gu.identity.frontend.models.UrlBuilder
import com.gu.identity.frontend.request.RegisterActionRequestBody
import com.gu.identity.frontend.request.RequestParameters.CoreSessionParameters
import com.gu.identity.frontend.utils.ErrorRecoveryActionBuilder
import play.api.libs.json._
import play.api.mvc._
import play.api.mvc.Results.{BadRequest, SeeOther}

import scala.concurrent.Future
import scala.util.control.NonFatal

/**
  * Recover from application errors. Other errors will be caught by the global
  * error handler, eg: 500s.
  */
abstract class ResultFromAppException(route: String, cc: ControllerComponents) extends ErrorRecoveryActionBuilder(cc) {

  def recoverErrors[A](request: Request[A]) = PartialFunction[Throwable, Future[Result]] {
    case ex: AppException => resultFromAppException(request, ex, response(_))
    case NonFatal(ex) =>
      resultFromAppException(request, UnexpectedAppException(s"Unexpected error: ${ex.getMessage}", Some(ex)), response(_))
  }

  private def resultFromAppException[A](request: Request[A], error: AppException, response: String => Future[Result]): Future[Result] = {
    val url = request.body match {
      case CoreSessionParameters(returnUrl, skipConfirmation, clientId, groupCode) =>
        UrlBuilder(route, returnUrl, skipConfirmation, clientId, groupCode, error)

      case _ =>
        UrlBuilder(route, error)
    }
    response(url)
  }

  def response(url: String): Future[Result]

}

case class RedirectOnError(route: String, cc: ControllerComponents) extends ResultFromAppException(route: String, cc) {
  def response(url: String) = Future.successful(NoCache(SeeOther(url)))
}

case class ResultOnError(route: String, cc: ControllerComponents) extends ResultFromAppException(route: String, cc) {
  def response(url: String) = Future.successful(NoCache(BadRequest(JsObject(Seq("url" -> JsString(url))))))
}
