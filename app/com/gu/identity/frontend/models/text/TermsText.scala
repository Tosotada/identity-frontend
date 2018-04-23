package com.gu.identity.frontend.models.text

import com.gu.identity.frontend.models.GroupCode
import play.api.i18n.Messages

case class TermsText private(
  conditionsText: String,
  privacyText: String
)

case class GroupTermsText private(
  conditionsText: String,
  privacyText: String
)

object Helpers {
  def makeLink(name: String, url: String) = s"<a href=\042$url\042>$name</a>"
}

object TermsText {
  def apply()(implicit messages: Messages): TermsText = {
    lazy val conditionsLink: String = Helpers.makeLink(
      messages("terms.termsOfService"),
      messages("terms.termsOfServiceUrl")
    )
    lazy val privacyLink: String = Helpers.makeLink(
      messages("terms.privacyPolicy"),
      messages("terms.privacyPolicyUrl")
    )
    TermsText(
      conditionsText = messages("terms.conditions", conditionsLink),
      privacyText = messages("terms.privacy", privacyLink)
    )
  }
}

object GroupTermsText {
  def apply(
    termsUrl: String,
    privacyUrl: String,
    group: GroupCode
  )(implicit messages: Messages): GroupTermsText = {
    lazy val conditionsLink: String = Helpers.makeLink(
      messages("terms.termsOfService"),
      messages("terms.termsOfServiceUrl")
    )
    lazy val privacyLink: String = Helpers.makeLink(
      messages("terms.privacyPolicy"),
      messages("terms.privacyPolicyUrl")
    )
    lazy val conditionsGroupLink: String = Helpers.makeLink(
      messages("terms.termsOfService"),
      termsUrl
    )
    lazy val privacyGroupLink: String = Helpers.makeLink(
      messages("terms.privacyPolicy"),
      privacyUrl
    )
    GroupTermsText (
      conditionsText = messages("terms.conditionsWithGroup", conditionsLink, messages(s"terms.${group.id}.name"), conditionsGroupLink),
      privacyText = messages("terms.privacyWithGroup", privacyLink, messages(s"terms.${group.id}.name"), privacyGroupLink)
    )
  }
}
