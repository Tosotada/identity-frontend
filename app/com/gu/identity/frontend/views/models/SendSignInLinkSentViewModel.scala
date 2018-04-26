package com.gu.identity.frontend.views.models

import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.models.{ClientID, EmailProvider}
import com.gu.identity.frontend.models.text.{SendSignInLinkSentText, SendSignInLinkText}
import play.api.i18n.Messages

case class SendSignInLinkSentViewModel private(
    layout: LayoutViewModel,
    pageBanner: String,
    sendSignInLinkSentText: SendSignInLinkSentText,
    actions: Map[String, String] = Map.empty,
    inboxCtaLink: Option[String],
    inboxCtaText: Option[String],
    resources: Seq[PageResource with Product],
    indirectResources: Seq[PageResource with Product]
  )
  extends ViewModel
  with ViewModelResources

object SendSignInLinkSentViewModel {

  def apply(
    configuration: Configuration,
    clientId: Option[ClientID],
    emailProvider: Option[EmailProvider])
    (implicit messages: Messages): SendSignInLinkSentViewModel = {
    val layout = LayoutViewModel(configuration, clientId, returnUrl = None)

    SendSignInLinkSentViewModel(
      layout = layout,
      pageBanner = "opt-in",
      sendSignInLinkSentText = SendSignInLinkSentText(),
      actions = Map(
        "returnUrl" -> configuration.dotcomBaseUrl
      ),
      inboxCtaLink = emailProvider.map(_.inboxLink),
      inboxCtaText = emailProvider.map(s => SendSignInLinkSentText.getEmailCtaText(s.name)),
      resources = layout.resources,
      indirectResources = layout.indirectResources
    )
  }
}
