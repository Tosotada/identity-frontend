package com.gu.identity.frontend.models.text

import play.api.i18n.Messages

case class ChangeEmailText private(
                                               pageTitle: String,
                                               title: String,
                                               description: String,
                                               error:String)

object ChangeEmailText {
  def apply()(implicit messages: Messages): ChangeEmailText =
    ChangeEmailText(
      pageTitle = messages("changeEmailSuccessful.pageTitle"),
      title = messages("changeEmailSuccessful.title"),
      description = messages("changeEmailSuccessful.description"),
      error = messages("changeEmailSuccessful.unexpectedTitle")
    )
}
