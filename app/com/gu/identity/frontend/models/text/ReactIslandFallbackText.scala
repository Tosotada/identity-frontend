package com.gu.identity.frontend.models.text

import com.gu.identity.frontend.models.{ClientID, GuardianMembersClientID}
import play.api.i18n.Messages
import com.gu.identity.model.Consent._

object ReactIslandFallbackText {
  def toMap()(implicit messages: Messages): Map[String, String] = {
    Map(
      "loading" -> messages("reactIslandFallback.loading"),
      "error" -> messages("reactIslandFallback.error"),
      "cta" -> messages("reactIslandFallback.cta")
    )
  }
}
