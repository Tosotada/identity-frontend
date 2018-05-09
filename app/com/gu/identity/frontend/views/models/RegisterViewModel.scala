package com.gu.identity.frontend.views.models

import buildinfo.BuildInfo
import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.controllers.routes
import com.gu.identity.frontend.models._
import com.gu.identity.frontend.models.text.RegisterText
import com.gu.identity.frontend.mvt._
import com.gu.identity.frontend.request.RegisterActionRequestBodyFormMapping
import play.api.i18n.Messages
import play.filters.csrf.CSRF.Token


case class RegisterViewModel(
                              layout: LayoutViewModel,

                              oauth: OAuthRegistrationViewModel,

                              registerPageText: RegisterText,
                              terms: TermsViewModel,

                              hasErrors: Boolean,
                              errors: RegisterErrorViewModel,
                              showStandfirst: Boolean,
                              askForPhoneNumber: Boolean,
                              hideDisplayName: Boolean,

                              csrfToken: Option[Token],
                              returnUrl: String,
                              skipConfirmation: Boolean,
                              skipValidationReturn: Boolean,
                              clientId: Option[ClientID],
                              group: Option[GroupCode],
                              email: Option[String],

                              shouldCollectConsents: Boolean,
                              shouldCollectV2Consents: Boolean,

                              actions: RegisterActions,
                              links: RegisterLinks,

                              resources: Seq[PageResource with Product],
                              indirectResources: Seq[PageResource with Product],
                              countryCodes: Option[CountryCodes],
                              gitCommitId: String,
                              emailValidationRegex: String
  )
  extends ViewModel with ViewModelResources


object RegisterViewModel {

  def apply(
      configuration: Configuration,
      activeTests: ActiveMultiVariantTests,
      errors: Seq[String],
      csrfToken: Option[Token],
      returnUrl: ReturnUrl,
      skipConfirmation: Option[Boolean],
      clientId: Option[ClientID],
      group: Option[GroupCode],
      email: Option[String],
      signInType: Option[SignInType],
      shouldCollectConsents: Boolean,
      shouldCollectV2Consents: Boolean,
      skipValidationReturn: Option[Boolean])
      (implicit messages: Messages): RegisterViewModel = {

    val layout = LayoutViewModel(configuration, activeTests, clientId, Some(returnUrl))

    val codes = countryCodes(clientId)

    RegisterViewModel(
      layout = layout,

      oauth = OAuthRegistrationViewModel(configuration, returnUrl, skipConfirmation, clientId, group, activeTests),

      registerPageText = RegisterText.loadText(clientId),
      terms = Terms.getTermsModel(group),

      hasErrors = errors.nonEmpty,
      errors = RegisterErrorViewModel(errors),

      showStandfirst = showStandfirst(clientId),
      askForPhoneNumber = askForPhoneNumber(clientId),
      hideDisplayName = true,

      csrfToken = csrfToken,
      returnUrl = returnUrl.url,
      skipConfirmation = skipConfirmation.getOrElse(false),
      skipValidationReturn = skipValidationReturn.getOrElse(false),
      clientId = clientId,
      group = group,
      email = email,

      shouldCollectConsents = shouldCollectConsents,
      shouldCollectV2Consents = shouldCollectV2Consents,

      actions = RegisterActions(),
      links = RegisterLinks(returnUrl, skipConfirmation, clientId, signInType),

      resources = layout.resources,
      indirectResources = layout.indirectResources,

      countryCodes = codes,
      gitCommitId = BuildInfo.gitCommitId,
      emailValidationRegex = RegisterActionRequestBodyFormMapping.dotlessDomainEmailRegex.pattern.toString
    )
  }

  private def showStandfirst(clientId: Option[ClientID]) =
    clientId.contains(GuardianJobsClientID) || clientId.contains(GuardianMembersClientID)

  private def askForPhoneNumber(clientId: Option[ClientID]) =
    clientId.contains(GuardianCommentersClientID)

  private def countryCodes(clientId: Option[ClientID]) : Option[CountryCodes] = {
    clientId match {
      case Some(c) => if (c.id == "comments") Option(CountryCodes.apply) else None
      case _ => None
    }
  }
}


case class RegisterActions private(
    register: String)

object RegisterActions {
  def apply(): RegisterActions =
    RegisterActions(
      register = routes.RegisterAction.register().url
    )
}


case class RegisterLinks private(
    signIn: String)

object RegisterLinks {
  def apply(returnUrl: ReturnUrl, skipConfirmation: Option[Boolean], clientId: Option[ClientID], signInType: Option[SignInType]): RegisterLinks =
    RegisterLinks(
      signIn = signInType match {
        case Some(TwoStepSignInType) => UrlBuilder(routes.Application.twoStepSignInStart().url, returnUrl, skipConfirmation, clientId, group = None)
        case _ => UrlBuilder(routes.Application.signIn().url, returnUrl, skipConfirmation, clientId, group = None)
      }
    )
}
