package com.gu.identity.frontend.controllers

import com.gu.identity.frontend.errors.RedirectOnError
import com.gu.identity.frontend.logging.{LogOnErrorAction, Logging}
import com.gu.identity.frontend.models.ClientIp
import com.gu.identity.frontend.request.ResetPasswordActionRequestBody
import com.gu.identity.frontend.services.{IdentityService, ServiceAction, ServiceActionBuilder}
import play.api.mvc.{Controller, Request}
import play.api.libs.concurrent.Execution.Implicits.defaultContext


case class ResetPasswordAction(identityService: IdentityService) extends Controller with Logging {

  val redirectRoute: String = routes.Application.reset().url

  val ResetPasswordServiceAction: ServiceActionBuilder[Request] =
    ServiceAction andThen
      RedirectOnError(redirectRoute) andThen
      LogOnErrorAction(logger)

  val bodyParser = ResetPasswordActionRequestBody.bodyParser

  def reset = ResetPasswordServiceAction(bodyParser) { request =>
    val ip = ClientIp(request)
    identityService.sendResetPasswordEmail(request.body, ip).map {
      case Left(errors) =>
        Left(errors)

      case Right(okResponse) => Right {
        NoCache(SeeOther(routes.Application.resetPasswordEmailSent().url))
      }
    }
  }

}
