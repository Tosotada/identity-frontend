package com.gu.identity.frontend.models

import play.api.i18n.Messages

object Text {
  object SignInPageText {
    def toMap(isMembership: Boolean)(implicit messages: Messages): Map[String, String] = {
      Map (
        "title" -> (if(isMembership) messages("signin.title.supporter") else messages("signin.title")),
        "pageTitle" -> messages("signin.pagetitle"),
        "prelude" -> messages("signin.prelude"),
        "preludeMoreInfo" -> messages("signin.prelude.moreinfo"),
        "preludeFaq" -> messages("signin.prelude.faq"),
        "email" -> messages("signin.email"),
        "divideText" -> messages("signin.dividetext"),
        "password" -> messages("signin.password"),
        "forgottenPassword" -> messages("signin.forgottenpassword"),
        "rememberMe" -> messages("signin.rememberme"),
        "signIn" -> messages("signin.signin"),
        "noAccount" -> messages("signin.noaccount"),
        "signUp" -> messages("signin.signup"),
        "conditions" -> messages("signin.conditions"),
        "continue" -> messages("signin.continue"),
        "termsOfService" -> messages("signin.termsofservice"),
        "privacyPolicy" -> messages("signin.privacypolicy")
      )
    }
  }

  object TwoStepSignInStartPageText {
    def toMap(isMembership: Boolean)(implicit messages: Messages): Map[String, String] = {
      Map (
        "title" -> (if(isMembership) messages("signin.title.supporter") else messages("signinTwoStep.welcomeStepOne")),
        "pageTitle" -> messages("signin.pagetitle"),
        "prelude" -> messages("signin.prelude"),
        "preludeMoreInfo" -> messages("signin.prelude.moreinfo"),
        "preludeFaq" -> messages("signin.prelude.faq"),
        "email" -> messages("signin.email"),
        "signInWithEmail" -> messages("signinTwoStep.signInWithEmailAction"),
        "password" -> messages("signin.password"),
        "forgottenPassword" -> messages("signin.forgottenpassword"),
        "rememberMe" -> messages("signin.rememberme"),
        "signIn" -> messages("signin.signin"),
        "noAccount" -> messages("signin.noaccount"),
        "signUp" -> messages("signin.signup"),
        "conditions" -> messages("signin.conditions"),
        "continue" -> messages("signin.continue"),
        "termsOfService" -> messages("signin.termsofservice"),
        "privacyPolicy" -> messages("signin.privacypolicy"),

        "emailFieldTitle" -> messages("signinTwoStep.emailFieldTitle"),
        "setPasswordTitle" -> messages("signinTwoStep.setPasswordTitle"),
        "setPasswordAction" -> messages("signinTwoStep.setPasswordAction"),
        "recoverPasswordAction" -> messages("signinTwoStep.recoverPasswordAction"),
        "divideText" -> messages("signinTwoStep.dividetext"),
        "welcome" -> messages("signinTwoStep.welcome"),
        "changeEmailLink" -> messages("signinTwoStep.changeEmailLink"),
        "changeEmailLinkShort" -> messages("signinTwoStep.changeEmailLinkShort"),
        "signInAction" -> messages("signinTwoStep.signInAction"),
        "continueAction" -> messages("signinTwoStep.continueAction"),
        "registerAction" -> messages("signinTwoStep.registerAction"),
        "passwordFieldTitle" -> messages("signinTwoStep.passwordFieldTitle"),
        "oauthStepTwoFieldTitle" -> messages("signinTwoStep.oauthStepTwoFieldTitle"),
        "signInCtaEmailAction" -> messages("signinTwoStep.signInCtaEmailAction")
      )
    }
  }
  
  object LayoutText {
    def toMap(implicit messages: Messages): Map[String, String] = {
      Map(
        "layoutPageTitle" -> messages("layout.pagetitle"),
        "skipToContent" -> messages("layout.skip")
      )
    }
  }

  object ClientSideText {
    def toMap(implicit messages: Messages): Map[String, String] = {
      Map(
        "actions.signIn" -> messages("actions.signIn"),
        "actions.reset" -> messages("actions.reset")
      )
    }
  }

  object HeaderText {
    def toMap(implicit messages: Messages): Map[String, String] = {
      Map(
        "back" -> messages("header.backtext"),
        "logo" -> messages("header.logo")
      )
    }
  }
}
