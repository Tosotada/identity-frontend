package com.gu.identity.frontend.views.models

case class SignInLinksViewModel(socialFacebook: String = "https://oauth.theguardian.com/facebook/signin",
                                socialGoogle: String = "https://oauth.theguardian.com/google/signin") extends ViewModel {
  def toMap =
    Map("socialFacebook" -> socialFacebook, "socialGoogle" -> socialGoogle)
}

case class SignInViewModel(title: String = "Sign in to the Guardian",
                           pageTitle: String = "Sign in",
                           links: SignInLinksViewModel = SignInLinksViewModel()) extends ViewModel {
  def toMap =
    Map("title" -> title, "pageTitle" -> pageTitle, "links" -> links.toMap)
}
