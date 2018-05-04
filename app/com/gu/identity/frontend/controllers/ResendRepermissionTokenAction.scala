package com.gu.identity.frontend.controllers

import com.gu.identity.frontend.errors.RedirectOnError
import com.gu.identity.frontend.logging.{LogOnErrorAction, Logging}
import com.gu.identity.frontend.request.ResendTokenActionRequestBodyParser
import com.gu.identity.frontend.services.{IdentityService, ServiceAction, ServiceActionBuilder}
import play.api.mvc.{AbstractController, ControllerComponents, Request}

import scala.concurrent.ExecutionContext

case class ResendRepermissionTokenAction(
    identityService: IdentityService,
    cc: ControllerComponents,
    serviceAction: ServiceAction,
    resendTokenActionRequestBodyParser: ResendTokenActionRequestBodyParser)
    (implicit executionContext: ExecutionContext)
    extends AbstractController(cc) with Logging {

  val redirectRoute: String = routes.Application.resendRepermissionTokenSent().url

  val ResendRepermissionTokenServiceAction: ServiceActionBuilder[Request] =
    serviceAction andThen
      RedirectOnError(redirectRoute, cc) andThen
      (new LogOnErrorAction(logger, cc))

  val bodyParser = resendTokenActionRequestBodyParser.bodyParser

  def resend = ResendRepermissionTokenServiceAction(bodyParser) { request =>

    identityService.resendRepermissionToken(request.body).map {
      case Left(errors) =>
        Left(errors)

      case Right(okResponse) => Right {
        NoCache(SeeOther(routes.Application.resendRepermissionTokenSent().url))
      }
    }
  }

}
