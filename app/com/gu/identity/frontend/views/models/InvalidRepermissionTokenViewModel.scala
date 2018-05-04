package com.gu.identity.frontend.views.models

import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.models.text.InvalidRepermissionTokenText
import play.api.i18n.Messages
import play.filters.csrf.CSRF.Token

case class InvalidRepermissionTokenViewModel private(
                                                 layout: LayoutViewModel,
                                                 token: String,
                                                 csrfToken: Option[Token],
                                                 text: InvalidRepermissionTokenText,
                                                 resources: Seq[PageResource with Product],
                                                 indirectResources: Seq[PageResource with Product]
                                               )
  extends ViewModel
    with ViewModelResources

object InvalidRepermissionTokenViewModel {

  def apply(configuration: Configuration, token: String, csrfToken: Option[Token])(implicit messages: Messages): InvalidRepermissionTokenViewModel = {
    val layout = LayoutViewModel(configuration, token = Some(token))

    InvalidRepermissionTokenViewModel(
      layout = layout,
      token = token,
      csrfToken = csrfToken,
      text = InvalidRepermissionTokenText(),
      resources = layout.resources,
      indirectResources = layout.indirectResources
    )
  }
}
