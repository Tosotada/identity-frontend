package com.gu.identity.frontend.controllers

import com.gu.identity.frontend.analytics.AnalyticsEventActor
import com.gu.identity.frontend.analytics.client.PasswordResetRequestEvent
import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.errors.RedirectOnError
import com.gu.identity.frontend.logging.{LogOnErrorAction, Logging}
import com.gu.identity.frontend.models.{ClientIp, EmailProvider}
import com.gu.identity.frontend.request.{ResetPasswordActionRequestBody, ResetPasswordActionRequestBodyParser}
import com.gu.identity.frontend.services.{IdentityService, ServiceAction, ServiceActionBuilder}
import play.api.mvc.{AbstractController, Action, ControllerComponents, Request}

import scala.concurrent.ExecutionContext


case class ResetPasswordAction(
    identityService: IdentityService,
    cc: ControllerComponents,
    serviceAction: ServiceAction,
    resetPasswordActionRequestBodyParser: ResetPasswordActionRequestBodyParser,
    eventActor: AnalyticsEventActor,
    config: Configuration)
    (implicit executionContext: ExecutionContext)
    extends AbstractController(cc) with Logging {

  val redirectRoute: String = routes.Application.reset().url

  val ResetPasswordServiceAction: ServiceActionBuilder[Request] =
    serviceAction andThen
      RedirectOnError(redirectRoute, cc) andThen
      (new LogOnErrorAction(logger, cc))

  val bodyParser = resetPasswordActionRequestBodyParser.bodyParser

  def reset: Action[ResetPasswordActionRequestBody] = ResetPasswordServiceAction(bodyParser) { request =>
    val ip = ClientIp(request)
    identityService.sendResetPasswordEmail(request.body, ip).map {
      case Left(errors) =>
        Left(errors)

      case Right(okResponse) =>
        request.body.gaClientId.foreach(_ => eventActor.forward(PasswordResetRequestEvent(request, config.gaUID)))
        Right {
          NoCache(SeeOther(routes.Application.resetPasswordEmailSent(emailProvider = EmailProvider.getProviderForEmail(request.body.email).map(_.id)
          ).url))
        }
    }
  }

}
