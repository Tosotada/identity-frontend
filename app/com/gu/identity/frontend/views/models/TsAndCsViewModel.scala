package com.gu.identity.frontend.views.models


import java.net.URI

import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.models.text._
import com.gu.identity.frontend.controllers._
import com.gu.identity.frontend.models._
import play.api.i18n.Messages
import play.filters.csrf.CSRF.Token

case class TsAndCsViewModel private(
    layout: LayoutViewModel,
    resources: Seq[PageResource with Product],
    indirectResources: Seq[PageResource with Product],
    clientId: Option[ClientID],
    tsAndCsPageText: ThirdPartyTsAndCsText,
    returnUrl: String,
    groupCode: String,
    continueFormUrl: String = routes.ThirdPartyTsAndCs.addToGroupAction.url,
    csrfToken: Option[Token]
) extends ViewModel with ViewModelResources

object TsAndCsViewModel {
  def apply(
    configuration: Configuration,
    clientId: Option[ClientID],
    group: GroupCode,
    returnUrl: ReturnUrl,
    signOutLink: URI,
    csrfToken: Option[Token])(implicit messages: Messages): TsAndCsViewModel = {

    val layout = LayoutViewModel(configuration, clientId, Some(returnUrl))
    TsAndCsViewModel(
      layout = layout,
      resources = layout.resources,
      indirectResources = layout.indirectResources,
      clientId = clientId,
      tsAndCsPageText = TsAndCsPageText.getPageText(group, signOutLink),
      groupCode = group.id,
      returnUrl = returnUrl.url,
      csrfToken = csrfToken
    )
  }
}
