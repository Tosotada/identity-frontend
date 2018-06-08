package com.gu.identity.frontend.views.models

import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.models.{ClientID, EmailProvider}
import com.gu.identity.frontend.models.text.{ResetPasswordEmailSentText, SendSignInLinkSentText}
import play.api.i18n.Messages

case class ResetPasswordEmailSentViewModel private(
    layout: LayoutViewModel,
    resetPasswordEmailSentText: ResetPasswordEmailSentText,
    inboxCtaLink: Option[String],
    inboxCtaText: Option[String],
    resources: Seq[PageResource with Product],
    indirectResources: Seq[PageResource with Product]
  )
  extends ViewModel
  with ViewModelResources

object ResetPasswordEmailSentViewModel {

  def apply(configuration: Configuration, clientId: Option[ClientID], emailProvider: Option[EmailProvider]
  )(implicit messages: Messages): ResetPasswordEmailSentViewModel = {
    val layout = LayoutViewModel(configuration, clientId, returnUrl = None)

    ResetPasswordEmailSentViewModel(
      layout = layout,
      resetPasswordEmailSentText = ResetPasswordEmailSentText(),
      inboxCtaLink = emailProvider.map(_.inboxLink),
      inboxCtaText = emailProvider.map(s => SendSignInLinkSentText.getEmailCtaText(s.name)),
      resources = layout.resources,
      indirectResources = layout.indirectResources
    )
  }
}

