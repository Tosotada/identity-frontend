package com.gu.identity.frontend.models.text

import play.api.i18n.Messages

case class ChangeEmailText private(
                                               pageTitle: String,
                                               title: String,
                                               backToAccountAction: String,
                                               returnToGuardianAction: String,
                                               error:String)

object ChangeEmailText {
  def apply()(implicit messages: Messages): ChangeEmailText =
    ChangeEmailText(
      pageTitle = messages("changeEmailSuccessful.pageTitle"),
      title = messages("changeEmailSuccessful.title"),
      backToAccountAction = messages("changeEmailSuccessful.backToAccountAction"),
      returnToGuardianAction = messages("changeEmailSuccessful.returnToGuardianAction"),
      error = messages("changeEmailSuccessful.unexpectedTitle")
    )
}
