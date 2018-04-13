package com.gu.identity.frontend.views.models

import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.controllers.routes
import com.gu.identity.frontend.csrf.CSRFToken
import com.gu.identity.frontend.models._
import com.gu.identity.frontend.models.Text._
import com.gu.identity.frontend.mvt.ActiveMultiVariantTests
import com.gu.identity.model.{CurrentUser, GuestUser, NewUser, UserType}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat

case class TwoStepSignInStartViewModel private(
  layout: LayoutViewModel,

  oauth: OAuthViewModel,

  twoStepSignInPageText: Map[String, String],
  terms: TermsViewModel,

  errors: Seq[ErrorViewModel] = Seq.empty,

  csrfToken: Option[CSRFToken],
  returnUrl: String = "",
  skipConfirmation: Boolean = false,
  clientId: Option[ClientID],
  group: Option[GroupCode],

  email:Option[String],

  registerUrl: String = "",
  signinUrl: String = "",
  forgotPasswordUrl: String = "",

  recaptchaModel: Option[Any],

  actions: Map[String, String] = Map(
    "signInWithEmail" -> routes.SigninAction.emailSignInFirstStep().url
  ),
  resources: Seq[PageResource with Product],
  indirectResources: Seq[PageResource with Product])
  extends ViewModel
    with ViewModelResources


object TwoStepSignInStartViewModel {
  def apply(
    configuration: Configuration,
    activeTests: ActiveMultiVariantTests,
    csrfToken: Option[CSRFToken],
    errors: Seq[ErrorViewModel],
    returnUrl: ReturnUrl,
    skipConfirmation: Option[Boolean],
    clientId: Option[ClientID],
    group: Option[GroupCode],
    email: Option[String])(implicit messages: Messages): TwoStepSignInStartViewModel = {

    val layout = LayoutViewModel(configuration, activeTests, clientId, Some(returnUrl))
    val recaptchaModel : Option[GoogleRecaptchaViewModel] = None

    val resources = getResources(layout, recaptchaModel) ++ Seq(IndirectlyLoadedExternalResources(UrlBuilder(configuration.identityProfileBaseUrl,routes.SigninAction.signInWithSmartLock())))

    val isMembership = clientId.exists(_ == GuardianMembersClientID)

    TwoStepSignInStartViewModel(
      layout = layout,

      oauth = OAuthSignInViewModel(configuration, returnUrl, skipConfirmation, clientId, group, activeTests),

      twoStepSignInPageText = TwoStepSignInStartPageText.toMap(isMembership),
      terms = Terms.getTermsModel(group),

      errors = errors,

      csrfToken = csrfToken,
      returnUrl = returnUrl.url,
      skipConfirmation = skipConfirmation.getOrElse(false),
      clientId = clientId,
      group = group,
      email = email,

      registerUrl = UrlBuilder(routes.Application.register(), returnUrl, skipConfirmation, clientId, group.map(_.id), Some(TwoStepSignInType)),
      signinUrl = UrlBuilder(routes.Application.twoStepSignInStart(), returnUrl, skipConfirmation, clientId, group.map(_.id)),
      forgotPasswordUrl = UrlBuilder("/reset", returnUrl, skipConfirmation, clientId, group.map(_.id)),

      recaptchaModel = recaptchaModel,

      resources = resources,
      indirectResources = layout.indirectResources
    )
  }

  private def getResources(layout: LayoutViewModel, recaptchaViewModel: Option[GoogleRecaptchaViewModel]): Seq[PageResource with Product] ={
    layout.resources
  }
}
