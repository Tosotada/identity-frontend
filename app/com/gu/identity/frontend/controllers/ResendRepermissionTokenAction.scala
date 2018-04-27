package com.gu.identity.frontend.controllers

import com.gu.identity.frontend.errors.RedirectOnError
import com.gu.identity.frontend.logging.{LogOnErrorAction, Logging}
import com.gu.identity.frontend.request.ResendTokenActionRequestBody
import com.gu.identity.frontend.services.{IdentityService, ServiceAction, ServiceActionBuilder}
import play.api.mvc.{Controller, Request}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

case class ResendRepermissionTokenAction(
  identityService: IdentityService) extends Controller with Logging {

  val redirectRoute: String = routes.Application.resendRepermissionTokenSent().url

  val ResendRepermissionTokenServiceAction: ServiceActionBuilder[Request] =
    ServiceAction andThen
      RedirectOnError(redirectRoute) andThen
      LogOnErrorAction(logger)

  val bodyParser = ResendTokenActionRequestBody.bodyParser

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
