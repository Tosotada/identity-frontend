package com.gu.identity.frontend.controllers

import com.gu.identity.frontend.errors.RedirectOnError
import com.gu.identity.frontend.logging.{LogOnErrorAction, Logging}
import com.gu.identity.frontend.request.ResendTokenActionRequestBody
import com.gu.identity.frontend.services.{IdentityService, ServiceAction, ServiceActionBuilder}
import play.api.mvc.{Controller, Request}
import play.api.libs.concurrent.Execution.Implicits.defaultContext


case class ResendConsentTokenAction(identityService: IdentityService) extends Controller with Logging {

  val redirectRoute: String = routes.Application.resendConsentTokenSent().url

  val ResendConsentTokenServiceAction: ServiceActionBuilder[Request] =
    ServiceAction andThen
      RedirectOnError(redirectRoute) andThen
      LogOnErrorAction(logger)

  val bodyParser = ResendTokenActionRequestBody.bodyParser

  def resend = ResendConsentTokenServiceAction(bodyParser) { request =>
    identityService.resendConsentToken(request.body).map { eitherResponse =>
      eitherResponse.right.map { _ =>
        NoCache(SeeOther(routes.Application.resendConsentTokenSent().url))
      }
    }
  }

}
