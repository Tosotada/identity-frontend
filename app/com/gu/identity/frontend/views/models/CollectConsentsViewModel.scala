package com.gu.identity.frontend.views.models

import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.models.{ClientID, ReturnUrl}
import com.gu.identity.frontend.models.text.ReactIslandFallbackText
import play.api.i18n.Messages
import play.api.libs.json.Json

case class CollectConsentsViewModel private(
  layout: LayoutViewModel,
  reactIslandFallbackText: Map[String, String],
  resources: Seq[PageResource with Product],
  indirectResources: Seq[PageResource with Product],
  bootstrap: String,

  returnUrl: String
) extends ViewModel with ViewModelResources

object CollectConsentsViewModel {

  def apply(configuration: Configuration, clientId: Option[ClientID], returnUrl: String)(implicit messages: Messages): CollectConsentsViewModel = {
    val layout = LayoutViewModel(configuration, clientId, returnUrl = None)

    CollectConsentsViewModel(
      layout = layout,
      reactIslandFallbackText = ReactIslandFallbackText.toMap(),
      resources = layout.resources,
      indirectResources = layout.indirectResources,

      bootstrap = Json.toJson(Map(
        "returnUrl" -> returnUrl
      )).toString(),
      returnUrl = returnUrl
    )
  }
}

