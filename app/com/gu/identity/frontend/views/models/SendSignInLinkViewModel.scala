package com.gu.identity.frontend.views.models

import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.controllers.routes
import com.gu.identity.frontend.csrf.CSRFToken
import com.gu.identity.frontend.models.ClientID
import com.gu.identity.frontend.models.text.SendSignInLinkText
import play.api.i18n.Messages


case class SendSignInLinkViewModel private(
    layout: LayoutViewModel,
    pageBanner: String,
    sendSignInLinkText: SendSignInLinkText,
    actions: Map[String, String] = Map(
      "submit" -> routes.SigninAction.sendResubLinkAction().url
    ),
    errors: Seq[ErrorViewModel] = Seq.empty,
    csrfToken: Option[CSRFToken],
    returnUrl: String = "",
    resources: Seq[PageResource with Product],
    indirectResources: Seq[PageResource with Product]
  )
  extends ViewModel
  with ViewModelResources

object SendSignInLinkViewModel {

  def apply(
    configuration: Configuration,
    errors: Seq[ErrorViewModel],
    csrfToken: Option[CSRFToken])
    (implicit messages: Messages): SendSignInLinkViewModel = {
    val layout = LayoutViewModel(configuration, clientId = None, returnUrl = None)

    SendSignInLinkViewModel(
      layout = layout,
      pageBanner = "opt-in",
      sendSignInLinkText = SendSignInLinkText(),
      errors = errors,

      csrfToken = csrfToken,
      returnUrl = "https://profile.theguardian.com/consents/staywithus?CMP=resub-email&utm_campaign=resub-email",

      resources = layout.resources,
      indirectResources = layout.indirectResources
    )
  }
}
