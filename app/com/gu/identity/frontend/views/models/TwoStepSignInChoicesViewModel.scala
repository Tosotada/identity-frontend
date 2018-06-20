package com.gu.identity.frontend.views.models

import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.controllers.routes
import com.gu.identity.frontend.models.Text._
import com.gu.identity.frontend.models._
import com.gu.identity.frontend.models.text.{RegisterFormText, TwoStepSignInChoicesPageText}
import com.gu.identity.frontend.mvt.ActiveMultiVariantTests
import com.gu.identity.frontend.request.RegisterActionRequestBodyFormMapping
import com.gu.identity.frontend.views.models.RegisterViewModel.{countryCodes,askForPhoneNumber}
import com.gu.identity.model.{CurrentUser, GuestUser, NewUser, UserType}
import play.api.i18n.Messages
import play.filters.csrf.CSRF.Token
import play.twirl.api.HtmlFormat

case class TwoStepSignInChoicesViewModel private(
  layout: LayoutViewModel,

  oauth: OAuthViewModel,

  twoStepSignInPageText: Map[String, _],
  terms: TermsViewModel,

  errors: Seq[ErrorViewModel] = Seq.empty,

  csrfToken: Option[Token],
  returnUrl: String = "",
  skipConfirmation: Boolean = false,
  skipValidationReturn: Boolean = false,
  clientId: Option[ClientID],
  group: Option[GroupCode],

  email:Option[String],
  emailForDisplay:Option[String],

  registerUrl: String = "",
  signinUrl: String = "",
  forgotPasswordUrl: String = "",

  userTypes: Map[String, Boolean],

  askForPhoneNumber: Boolean,
  countryCodes: Option[CountryCodes],
  emailValidationRegex: String,
  recaptchaModel: Option[Any],

  actions: Map[String, String] = Map(
    "signInWithEmailAndPassword" -> routes.SigninAction.signIn().url,
    "resetPassword" -> routes.ResetPasswordAction.reset().url,
    "signInSecondStepCurrent" -> routes.SigninAction.signInSecondStepCurrent().url,
    "register" -> routes.RegisterAction.register().url
  ),
  resources: Seq[PageResource with Product],
  indirectResources: Seq[PageResource with Product])
  extends ViewModel
    with ViewModelResources


object TwoStepSignInChoicesViewModel {
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
    userType: Option[UserType],
    skipValidationReturn: Option[Boolean])(implicit messages: Messages): TwoStepSignInChoicesViewModel = {

    val layout = LayoutViewModel(configuration, activeTests, clientId, Some(returnUrl))
    val recaptchaModel : Option[GoogleRecaptchaViewModel] = None

    val resources = getResources(layout, recaptchaModel) ++ Seq(IndirectlyLoadedExternalResources(UrlBuilder(configuration.identityProfileBaseUrl,routes.SigninAction.signInWithSmartLock())))

    val isMembership = clientId.exists(_ == GuardianMembersClientID)

    val oauth = userType match {
      case Some(NewUser) => OAuthRegistrationViewModel(configuration, returnUrl, skipConfirmation, clientId, group, activeTests)
      case _ => OAuthSignInViewModel(configuration, returnUrl, skipConfirmation, clientId, group, activeTests)
    }

    TwoStepSignInChoicesViewModel(
      layout = layout,

      oauth = oauth,

      twoStepSignInPageText = TwoStepSignInChoicesPageText.toMap() ++ Map(
        "registerForm" -> RegisterFormText.toMap()
      ),
      terms = Terms.getTermsModel(group),

      errors = errors,

      csrfToken = csrfToken,
      returnUrl = returnUrl.url,
      skipConfirmation = skipConfirmation.getOrElse(false),
      skipValidationReturn = skipValidationReturn.getOrElse(false),
      clientId = clientId,
      group = group,
      email = email,
      emailForDisplay = email.map(breakEmailWords),

      registerUrl = UrlBuilder(routes.Application.register(), returnUrl, skipConfirmation, clientId, group.map(_.id), Some(TwoStepSignInType), skipValidationReturn),
      signinUrl = UrlBuilder(routes.Application.twoStepSignInStart(), returnUrl, skipConfirmation, clientId, group.map(_.id)),
      forgotPasswordUrl = UrlBuilder("/reset", returnUrl, skipConfirmation, clientId, group.map(_.id)),

      userTypes = Map(
        ("isNew", userType.contains(NewUser)),
        ("isExisting", userType.contains(CurrentUser)),
        ("isGuest", userType.contains(GuestUser))
      ),

      countryCodes = countryCodes(clientId),
      askForPhoneNumber = askForPhoneNumber(clientId),
      emailValidationRegex = RegisterActionRequestBodyFormMapping.dotlessDomainEmailRegex.pattern.toString,
      recaptchaModel = recaptchaModel,

      resources = resources,
      indirectResources = layout.indirectResources
    )
  }

  private def breakEmailWords(email: String) = {
    HtmlFormat.escape(email).toString.flatMap {
      case '@' => s"<wbr>@"
      case '+' => s"<wbr>+"
      case c   => s"$c"
    }
  }

  private def getResources(layout: LayoutViewModel, recaptchaViewModel: Option[GoogleRecaptchaViewModel]): Seq[PageResource with Product] ={
    layout.resources
  }
}
