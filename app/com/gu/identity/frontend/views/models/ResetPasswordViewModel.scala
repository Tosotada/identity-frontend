package com.gu.identity.frontend.views.models

import com.gu.identity.frontend.controllers.routes
import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.models.ClientID
import com.gu.identity.frontend.models.text.{ResetPasswordResendText, ResetPasswordText}
import play.api.i18n.Messages
import play.filters.csrf.CSRF.Token

case class ResetPasswordViewModel private(
    layout: LayoutViewModel,
    resetPasswordText: ResetPasswordText,
    userHelpEmailAddress: String = ResetPasswordViewModel.userHelpEmailAddress,
    actions: Map[String, String] = Map("reset" -> routes.ResetPasswordAction.reset().url),
    errors: Seq[ErrorViewModel] = Seq.empty,
    csrfToken: Option[Token],
    email: Option[String],
    resources: Seq[PageResource with Product],
    indirectResources: Seq[PageResource with Product]
  )
  extends ViewModel
  with ViewModelResources

object ResetPasswordViewModel {

  val userHelpEmailAddress = "userhelp@theguardian.com?subject=Account%20help"

  def apply(
    configuration: Configuration,
    errors: Seq[ErrorViewModel],
    csrfToken: Option[Token],
    email: Option[String],
    resend: Boolean,
    clientId: Option[ClientID])
    (implicit messages: Messages): ResetPasswordViewModel = {
    val layout = LayoutViewModel(configuration, clientId = clientId, returnUrl = None)

    ResetPasswordViewModel(
      layout = layout,
      resetPasswordText = if(resend) ResetPasswordResendText() else ResetPasswordText(),
      errors = errors,
      csrfToken = csrfToken,
      email = email,
      resources = layout.resources,
      indirectResources = layout.indirectResources
    )
  }
}
