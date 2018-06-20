package com.gu.identity.frontend.models.text

import com.gu.identity.frontend.models.{ClientID, GuardianMembersClientID}
import play.api.i18n.Messages
import com.gu.identity.model.Consent._

object RegisterText {
  def toMap(clientId: Option[ClientID])(implicit messages: Messages): Map[String, _] = {
    Map(
      "createAccount" -> messages("register.createAccount"),
      "continue" -> messages("register.continue"),
      "divideText" -> messages("register.divideText"),
      "email" -> messages("register.email"),
      "emailHelp" -> messages("register.emailHelp"),
      "firstName" -> messages("register.firstName"),
      "lastName" -> messages("register.lastName"),
      "firstOrLastNameHelp" -> messages("register.firstOrLastNameHelp"),
      "name" -> messages("register.name"),
      "pageTitle" -> messages("register.pageTitle"),
      "password" -> messages("register.password"),
      "passwordHelp" -> messages("register.passwordHelp"),
      "signIn" -> messages("register.signIn"),
      "signInCta" -> messages("register.signInCta"),
      "standfirst" -> (clientId match {
        case Some(GuardianMembersClientID) => messages("register.title")
        case _ => messages("register.standfirst")
      }),
      "title" -> (clientId match {
        case Some(GuardianMembersClientID) => messages("register.title.supporter")
        case _ => messages("register.title")
      }),
      "displayNameHelp" -> messages("register.displayNameHelp"),
      "displayNameHelpShortened" -> messages("register.displayNameHelpShortened"),
      "displayNameHelpExpanded" -> messages("register.displayNameHelpExpanded"),
      "phone " -> messages("register.phone"),
      "countryCode" -> messages("register.countryCode"),
      "whyPhone" -> messages("register.whyPhone"),
      "becausePhone" -> messages("register.becausePhone"),
      "consent" -> ConsentRegisterText()
    )
  }
}

// FIXME: Warning, these are placeholder consents and should be changed or verified before changing the config to display them!
case class ConsentRegisterText(
  SupporterConsentIdentifier: String = Supporter.id,
  SupporterConsentText: String = Supporter.latestWording.wording
)


