package com.gu.identity.frontend.models.text

import play.api.i18n.Messages

case class ChangeEmailText private(
                                               pageTitle: String,
                                               title: String,
                                               button1: String,
                                               button2: String,
                                               error:String)

object ChangeEmailText {
  def apply()(implicit messages: Messages): ChangeEmailText =
    ChangeEmailText(
      pageTitle = messages("changeEmailSuccessful.pageTitle"),
      title = messages("changeEmailSuccessful.title"),
      button1 = messages("changeEmailSuccessful.button1"),
      button2 = messages("changeEmailSuccessful.button2"),
      error = messages("changeEmailSuccessful.unexpectedTitle")
    )
}
