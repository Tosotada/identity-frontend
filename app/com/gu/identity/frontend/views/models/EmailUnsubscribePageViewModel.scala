package com.gu.identity.frontend.views.models

import com.gu.identity.frontend.configuration.Configuration
import play.api.i18n.Messages

case class UnsubscribePageText(pageTitle: String,
                               title: String,
                               description: String,
                               details: String)

object UnsubscribePageText {
  def apply(messages: Messages): UnsubscribePageText = {
    UnsubscribePageText(
      messages("email.unsubscribe.pageTitle"),
      messages("email.unsubscribe.title"),
      messages("email.unsubscribe.description"),
      messages("email.unsubscribe.details")
    )
  }
}

case class EmailUnsubscribePageViewModel(layout: LayoutViewModel,
                                         resources: Seq[PageResource with Product],
                                         text: UnsubscribePageText,
                                         emailPreferencesLink: String,
                                         indirectResources: Seq[PageResource with Product]) extends ViewModel with ViewModelResources

object EmailUnsubscribePageViewModel {
  def apply(configuration: Configuration)(implicit messages: Messages): EmailUnsubscribePageViewModel = {
    val layout = LayoutViewModel(configuration)

    EmailUnsubscribePageViewModel(
      layout = layout,
      resources = layout.resources,
      text = UnsubscribePageText(messages),
      emailPreferencesLink = s"${configuration.identityProfileBaseUrl}/email-prefs",
      indirectResources = layout.indirectResources
    )
  }
}
