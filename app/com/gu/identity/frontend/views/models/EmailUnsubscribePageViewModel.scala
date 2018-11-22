package com.gu.identity.frontend.views.models

import com.gu.identity.frontend.configuration.Configuration
import play.api.i18n.Messages

case class UnsubscribePageText(pageTitle: String,
                               title: String,
                               description: String,
                               link: String)

object UnsubscribePageText {
  def apply(messages: Messages): UnsubscribePageText = {
    UnsubscribePageText(
      messages("email.unsubscribe.pageTitle"),
      messages("email.unsubscribe.title"),
      messages("email.unsubscribe.description"),
      messages("email.unsubscribe.link")
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
      emailPreferencesLink = s"${configuration.identityProfileBaseUrl}/email-prefs?INTCMP=ID_UNSUBSCRIBE_PREFS",
      indirectResources = layout.indirectResources
    )
  }
}

case class ConsentUnsubscribePageText(pageTitle: String,
                                      title: String,
                                      description: String,
                                      link: String,
                                      bullet1: String,
                                      bullet2: String,
                                      bullet3: String)

object ConsentUnsubscribePageText {
  def apply(messages: Messages, consentName: String): ConsentUnsubscribePageText = {
    ConsentUnsubscribePageText(
      messages("email.consent.unsubscribe.pageTitle"),
      messages("email.consent.unsubscribe.title"),
      messages("email.consent.unsubscribe.description", consentName),
      messages("email.consent.unsubscribe.link"),
      messages("email.consent.unsubscribe.bullet.1"),
      messages("email.consent.unsubscribe.bullet.2"),
      messages("email.consent.unsubscribe.bullet.3", consentName)
    )
  }
}


case class ConsentEmailUnsubscribePageViewModel(layout: LayoutViewModel,
                                         resources: Seq[PageResource with Product],
                                         text: ConsentUnsubscribePageText,
                                         emailPreferencesLink: String,
                                         indirectResources: Seq[PageResource with Product]) extends ViewModel with ViewModelResources

object ConsentEmailUnsubscribePageViewModel {
  def apply(configuration: Configuration, consentName: String)(implicit messages: Messages): ConsentEmailUnsubscribePageViewModel = {
    val layout = LayoutViewModel(configuration)

    ConsentEmailUnsubscribePageViewModel(
      layout = layout,
      resources = layout.resources,
      text = ConsentUnsubscribePageText(messages, consentName),
      emailPreferencesLink = s"${configuration.identityProfileBaseUrl}/email-prefs?INTCMP=ID_UNSUBSCRIBE_PREFS",
      indirectResources = layout.indirectResources
    )
  }
}
