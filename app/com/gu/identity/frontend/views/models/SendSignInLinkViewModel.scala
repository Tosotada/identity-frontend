package com.gu.identity.frontend.views.models

import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.controllers.routes
import com.gu.identity.frontend.models.ClientID
import com.gu.identity.frontend.models.text.SendSignInLinkText
import play.api.i18n.Messages
import play.filters.csrf.CSRF.Token


case class SendSignInLinkViewModel private(
    layout: LayoutViewModel,
    sendSignInLinkText: SendSignInLinkText,
    actions: Map[String, String] = Map(
      "submit" -> routes.SigninAction.sendResubLinkAction().url
    ),
    errors: Seq[ErrorViewModel] = Seq.empty,
    csrfToken: Option[Token],
    returnUrl: String = "",
    clientId: Option[ClientID],
    resources: Seq[PageResource with Product],
    indirectResources: Seq[PageResource with Product]
  )
  extends ViewModel
  with ViewModelResources

object SendSignInLinkViewModel {

  def apply(
    configuration: Configuration,
    errors: Seq[ErrorViewModel],
    csrfToken: Option[Token],
    clientId: Option[ClientID])
    (implicit messages: Messages): SendSignInLinkViewModel = {
    val layout = LayoutViewModel(configuration, clientId, returnUrl = None)

    SendSignInLinkViewModel(
      layout = layout,
      sendSignInLinkText = SendSignInLinkText(),
      errors = errors,

      csrfToken = csrfToken,
      clientId = clientId,
      returnUrl = s"${configuration.identityProfileBaseUrl}/email-prefs?CMP=resub-email&utm_campaign=resub-email",

      resources = layout.resources,
      indirectResources = layout.indirectResources
    )
  }
}
