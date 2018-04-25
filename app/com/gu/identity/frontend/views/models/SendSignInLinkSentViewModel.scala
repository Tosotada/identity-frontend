package com.gu.identity.frontend.views.models

import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.models.text.{SendSignInLinkSentText, SendSignInLinkText}
import play.api.i18n.Messages


case class SendSignInLinkSentViewModel private(
    layout: LayoutViewModel,
    pageBanner: String,
    sendSignInLinkSentText: SendSignInLinkSentText,
    actions: Map[String, String] = Map.empty,
    resources: Seq[PageResource with Product],
    indirectResources: Seq[PageResource with Product]
  )
  extends ViewModel
  with ViewModelResources

object SendSignInLinkSentViewModel {

  def apply(
    configuration: Configuration)
    (implicit messages: Messages): SendSignInLinkSentViewModel = {
    val layout = LayoutViewModel(configuration, clientId = None, returnUrl = None)

    SendSignInLinkSentViewModel(
      layout = layout,
      pageBanner = "opt-in",
      sendSignInLinkSentText = SendSignInLinkSentText(),
      actions = Map(
        "returnUrl" -> configuration.dotcomBaseUrl
      ),
      resources = layout.resources,
      indirectResources = layout.indirectResources
    )
  }
}
