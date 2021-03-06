package com.gu.identity.frontend.views.models

import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.controllers.routes
import com.gu.identity.frontend.models._
import com.gu.identity.frontend.models.Text._
import com.gu.identity.frontend.mvt.ActiveMultiVariantTests
import play.api.i18n.Messages
import play.filters.csrf.CSRF.Token

case class TwoStepSignInStartViewModel private(
  layout: LayoutViewModel,

  oauth: OAuthViewModel,

  twoStepSignInPageText: Map[String, String],
  terms: TermsViewModel,

  override val errors: Seq[ErrorViewModel] = Seq.empty,

  csrfToken: Option[Token],
  returnUrl: String = "",
  skipConfirmation: Boolean = false,
  clientId: Option[ClientID],
  group: Option[GroupCode],

  email:Option[String],
  skipValidationReturn: Boolean = false,
  showSupportingParagraph: Boolean = false,

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
    csrfToken: Option[Token],
    errors: Seq[ErrorViewModel],
    returnUrl: ReturnUrl,
    skipConfirmation: Option[Boolean],
    clientId: Option[ClientID],
    group: Option[GroupCode],
    email: Option[String],
    skipValidationReturn: Option[Boolean])(implicit messages: Messages): TwoStepSignInStartViewModel = {

    val layout = LayoutViewModel(configuration, activeTests, clientId, Some(returnUrl))
    val recaptchaModel : Option[GoogleRecaptchaViewModel] = None

    val resources = getResources(layout) ++ Seq(IndirectlyLoadedExternalResources(UrlBuilder(configuration.identityProfileBaseUrl,routes.SigninAction.signInWithSmartLock())))

    TwoStepSignInStartViewModel(
      layout = layout,

      oauth = OAuthTwoStepSignInViewModel(configuration, returnUrl, skipConfirmation, clientId, group),

      twoStepSignInPageText = TwoStepSignInStartPageText.toMap(clientId),
      terms = Terms.getTermsModel(group),

      errors = errors,

      csrfToken = csrfToken,
      returnUrl = returnUrl.url,
      skipConfirmation = skipConfirmation.getOrElse(false),
      clientId = clientId,
      group = group,
      email = email,
      skipValidationReturn = skipValidationReturn.getOrElse(false),
      showSupportingParagraph = clientId.contains(GuardianRecurringContributionsClientID),
      registerUrl = UrlBuilder(routes.Application.twoStepSignInStart(), returnUrl, skipConfirmation, clientId, group.map(_.id), skipValidationReturn),
      signinUrl = UrlBuilder(routes.Application.twoStepSignInStart(), returnUrl, skipConfirmation, clientId, group.map(_.id)),
      forgotPasswordUrl = UrlBuilder("/reset", returnUrl, skipConfirmation, clientId, group.map(_.id)),

      recaptchaModel = recaptchaModel,

      resources = resources,
      indirectResources = layout.indirectResources
    )
  }

  private def getResources(layout: LayoutViewModel): Seq[PageResource with Product] ={
    layout.resources
  }
}
