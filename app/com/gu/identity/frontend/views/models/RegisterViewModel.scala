package com.gu.identity.frontend.views.models

import buildinfo.BuildInfo
import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.controllers.routes
import com.gu.identity.frontend.models._
import com.gu.identity.frontend.models.text.{RegisterFormText, RegisterText}
import com.gu.identity.frontend.mvt._
import com.gu.identity.frontend.request.RegisterActionRequestBodyFormMapping
import play.api.i18n.Messages
import play.filters.csrf.CSRF.Token

import scala.util.Try


case class RegisterViewModel(
                              layout: LayoutViewModel,

                              oauth: OAuthRegistrationViewModel,

                              registerPageText: Map[String, _],
                              terms: TermsViewModel,

                              hasErrors: Boolean,
                              override val errors: Seq[ErrorViewModel],
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
      shouldCollectConsents: Boolean,
      shouldCollectV2Consents: Boolean,
      skipValidationReturn: Option[Boolean])
      (implicit messages: Messages): RegisterViewModel = {

    val layout = LayoutViewModel(configuration, activeTests, clientId, Some(returnUrl))

    val codes = countryCodes(clientId)

    RegisterViewModel(
      layout = layout,

      oauth = OAuthRegistrationViewModel(configuration, returnUrl, skipConfirmation, clientId, group),

      registerPageText = RegisterText.toMap(clientId) ++ Map(
        "registerForm" -> RegisterFormText.toMap()
      ),
      terms = Terms.getTermsModel(group),

      hasErrors = errors.nonEmpty,
      errors = errors.flatMap(id => Try(ErrorViewModel(id)).toOption),

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
      links = RegisterLinks(returnUrl, skipConfirmation, clientId),

      resources = layout.resources,
      indirectResources = layout.indirectResources,

      countryCodes = codes,
      gitCommitId = BuildInfo.gitCommitId,
      emailValidationRegex = RegisterActionRequestBodyFormMapping.dotlessDomainEmailRegex.pattern.toString
    )
  }

  def showStandfirst(clientId: Option[ClientID]) =
    clientId.contains(GuardianJobsClientID) || clientId.contains(GuardianMembersClientID) || clientId.contains(GuardianRecurringContributionsClientID)

  def askForPhoneNumber(clientId: Option[ClientID]) =
    clientId.contains(GuardianCommentersClientID)

  def countryCodes(clientId: Option[ClientID]) : Option[CountryCodes] = {
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
  def apply(returnUrl: ReturnUrl, skipConfirmation: Option[Boolean], clientId: Option[ClientID]): RegisterLinks =
    RegisterLinks(UrlBuilder(routes.Application.twoStepSignInStart().url, returnUrl, skipConfirmation, clientId, group = None))
}
