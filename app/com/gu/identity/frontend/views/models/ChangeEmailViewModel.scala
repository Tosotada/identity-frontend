package com.gu.identity.frontend.views.models

import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.models.{ClientID, EmailProvider}
import com.gu.identity.frontend.models.text.{ChangeEmailText, ResetPasswordEmailSentText, SendSignInLinkSentText}
import play.api.i18n.Messages

case class ChangeEmailViewModel private(
                                                    layout: LayoutViewModel,
                                                    ChangeEmailSuccessfulText: ChangeEmailText,
                                                    resources: Seq[PageResource with Product],
                                                    indirectResources: Seq[PageResource with Product]
                                                  )
  extends ViewModel
    with ViewModelResources

object ChangeEmailViewModel {

  def apply(configuration: Configuration, clientId: Option[ClientID])(implicit messages: Messages): ChangeEmailViewModel = {
    val layout = LayoutViewModel(configuration, clientId, returnUrl = None)

    ChangeEmailViewModel(
      layout = layout,
      ChangeEmailSuccessfulText = ChangeEmailText(),
      resources = layout.resources,
      indirectResources = layout.indirectResources
    )
  }
}

