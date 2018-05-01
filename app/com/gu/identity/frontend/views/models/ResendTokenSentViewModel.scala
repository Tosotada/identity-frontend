package com.gu.identity.frontend.views.models

import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.models.text.ResendTokenSentText
import play.api.i18n.Messages
import play.filters.csrf.CSRF.Token

case class ResendTokenSentViewModel private(
  layout: LayoutViewModel,
  csrfToken: Option[Token],
  errors: Option[Seq[ErrorViewModel]] = Some(Seq.empty),
  resendLinkEmailSentText: ResendTokenSentText,
  resources: Seq[PageResource with Product],
  indirectResources: Seq[PageResource with Product]
  )
  extends ViewModel
  with ViewModelResources

object ResendTokenSentViewModel {

  def apply(configuration: Configuration, csrfToken: Option[Token], errorIds: Option[Seq[ErrorViewModel]])(implicit messages: Messages): ResendTokenSentViewModel = {
    val layout = LayoutViewModel(configuration)

    ResendTokenSentViewModel(
      layout = layout,
      csrfToken = csrfToken,
      errorIds,
      resendLinkEmailSentText = ResendTokenSentText(),
      resources = layout.resources,
      indirectResources = layout.indirectResources
    )
  }
}
