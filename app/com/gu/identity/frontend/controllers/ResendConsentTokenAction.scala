package com.gu.identity.frontend.controllers

import com.gu.identity.frontend.errors.RedirectOnError
import com.gu.identity.frontend.logging.{LogOnErrorAction, Logging}
import com.gu.identity.frontend.request.ResendTokenActionRequestBody
import com.gu.identity.frontend.services.{IdentityService, ServiceAction, ServiceActionBuilder}
import play.api.mvc.{AbstractController, ControllerComponents, Request}

import scala.concurrent.ExecutionContext


case class ResendConsentTokenAction(
    identityService: IdentityService,
    cc: ControllerComponents,
    serviceAction: ServiceAction)
    (implicit executionContext: ExecutionContext) extends AbstractController(cc) with Logging {

  val redirectRoute: String = routes.Application.resendConsentTokenSent().url

  val ResendConsentTokenServiceAction: ServiceActionBuilder[Request] =
    serviceAction andThen
      RedirectOnError(redirectRoute, cc) andThen
      (new LogOnErrorAction(logger, cc))

  val bodyParser = ResendTokenActionRequestBody.bodyParser

  def resend = ResendConsentTokenServiceAction(bodyParser) { request =>
    identityService.resendConsentToken(request.body).map { eitherResponse =>
      eitherResponse.right.map { _ =>
        NoCache(SeeOther(routes.Application.resendConsentTokenSent().url))
      }
    }
  }

}
