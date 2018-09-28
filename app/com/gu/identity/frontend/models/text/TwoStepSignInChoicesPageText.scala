package com.gu.identity.frontend.models.text

import play.api.i18n.Messages

object TwoStepSignInChoicesPageText {
  def toMap()(implicit messages: Messages): Map[String, String] = {
    Map (
      "title" -> messages("signin.title"),
      "pageTitle" -> messages("signin.pagetitle"),
      "prelude" -> messages("signin.prelude"),
      "preludeMoreInfo" -> messages("signin.prelude.moreinfo"),
      "preludeFaq" -> messages("signin.prelude.faq"),
      "email" -> messages("signin.email"),
      "signInWithEmail" -> messages("signinTwoStep.signInWithEmailAction"),
      "password" -> messages("signin.password"),
      "forgottenPassword" -> messages("signin.forgottenpassword"),
      "rememberMe" -> messages("signinTwoStep.rememberme"),
      "signIn" -> messages("signin.signin"),
      "noAccount" -> messages("signin.noaccount"),
      "signUp" -> messages("signin.signup"),
      "conditions" -> messages("signin.conditions"),
      "continue" -> messages("signin.continue"),
      "termsOfService" -> messages("signin.termsofservice"),
      "privacyPolicy" -> messages("signin.privacypolicy"),

      "emailFieldTitle" -> messages("signinTwoStep.emailFieldTitle"),
      "emailTitle" -> messages("signinTwoStep.emailTitle"),
      "setPasswordTitle" -> messages("signinTwoStep.setPasswordTitle"),
      "setPasswordAction" -> messages("signinTwoStep.setPasswordAction"),
      "recoverPasswordAction" -> messages("signinTwoStep.recoverPasswordAction"),
      "divideText" -> messages("signinTwoStep.dividetext"),
      "welcome" -> messages("signinTwoStep.welcome"),
      "changeEmailLinkShort" -> messages("signinTwoStep.changeEmailLinkShort"),
      "changeEmailLink" -> messages("signinTwoStep.changeEmailLink"),
      "signInAction" -> messages("signinTwoStep.signInAction"),
      "continueAction" -> messages("signinTwoStep.continueAction"),
      "registerAction" -> messages("signinTwoStep.registerAction"),
      "passwordFieldTitle" -> messages("signinTwoStep.passwordFieldTitle"),
      "oauthStepTwoFieldTitle" -> messages("signinTwoStep.oauthStepTwoFieldTitle"),
      "signInCtaEmailAction" -> messages("signinTwoStep.signInCtaEmailAction"),

      "newUserCreateAccountAction" -> messages("signinTwoStep.newUserCreateAccountAction"),
      "newUserCreateSocialAccountAction" -> messages("signinTwoStep.newUserCreateSocialAccountAction")
    )
  }
}
