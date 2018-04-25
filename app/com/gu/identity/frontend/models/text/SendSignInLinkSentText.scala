package com.gu.identity.frontend.models.text

import play.api.i18n.Messages

case class SendSignInLinkSentText private(
  pageTitle: String,
  title: String,
  text: String,
  cta: String
)

object SendSignInLinkSentText  {
  def apply()(implicit messages: Messages): SendSignInLinkSentText =
    SendSignInLinkSentText(
      pageTitle = messages("sendSignInLinkSent.pageTitle"),
      title = messages("sendSignInLinkSent.title"),
      text = messages("sendSignInLinkSent.text"),
      cta = messages("sendSignInLinkSent.cta")
    )

  def getEmailCtaText(service: String)(implicit messages: Messages) =
    messages("sendSignInLinkSent.emailCta",service)
}
