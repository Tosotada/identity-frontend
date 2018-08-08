package com.gu.identity.frontend.views

import java.net.URI

import com.gu.identity.model.Consent.{ConsentType, Jobs}
import com.gu.identity.frontend.configuration._
import com.gu.identity.frontend.controllers.{NoCache, routes}
import com.gu.identity.frontend.errors.{HttpError, NotFoundError}
import com.gu.identity.frontend.models._
import com.gu.identity.frontend.mvt.{MultiVariantTest, MultiVariantTestVariant}
import com.gu.identity.frontend.views.models._
import com.gu.identity.model.UserType
import jp.co.bizreach.play2handlebars.HBS
import play.api.i18n.Messages
import play.api.mvc.{Result, Results}
import play.api.mvc.Results.{NotFound, SeeOther}
import play.filters.csrf.CSRF.Token
import play.twirl.api.Html

/**
  * Adapter for Handlebars view renderer
  */
object ViewRenderer {
  def render(view: String, attributes: Map[String, Any] = Map.empty) =
    HBS(view, attributes)

  def renderTwoStepSignInStart(
    configuration: Configuration,
    activeTests: Map[MultiVariantTest, MultiVariantTestVariant],
    csrfToken: Option[Token],
    errorIds: Seq[String],
    returnUrl: ReturnUrl,
    skipConfirmation: Option[Boolean],
    clientId: Option[ClientID],
    group: Option[GroupCode],
    email: Option[String],
    skipValidationReturn: Option[Boolean])
    (implicit messages: Messages) = {

    val model = TwoStepSignInStartViewModel(
      configuration = configuration,
      activeTests = activeTests,
      csrfToken = csrfToken,
      errors = errorIds.map(ErrorViewModel.apply),
      returnUrl = returnUrl,
      skipConfirmation = skipConfirmation,
      clientId = clientId,
      group = group,
      email = email,
      skipValidationReturn = skipValidationReturn
    )

    renderViewModel("two-step-signin-start-page", model)
  }

  def renderTwoStepSignInChoices(
    configuration: Configuration,
    activeTests: Map[MultiVariantTest, MultiVariantTestVariant],
    csrfToken: Option[Token],
    errorIds: Seq[String],
    userType: Option[UserType],
    returnUrl: ReturnUrl,
    skipConfirmation: Option[Boolean],
    clientId: Option[ClientID],
    group: Option[GroupCode],
    email: Option[String],
    skipValidationReturn: Option[Boolean])(implicit messages: Messages) = {

    val model = TwoStepSignInChoicesViewModel(
      configuration = configuration,
      activeTests = activeTests,
      csrfToken = csrfToken,
      errors = errorIds.map(ErrorViewModel.apply),
      returnUrl = returnUrl,
      skipConfirmation = skipConfirmation,
      clientId = clientId,
      group = group,
      email = email,
      userType = userType,
      skipValidationReturn = skipValidationReturn,
      consents = Seq.empty[ConsentType]
    )

    val _model = clientId match {
      case Some(GuardianJobsClientID) => model.copy(consents = (model.consents ++ Seq(Jobs)).distinct)
      case _ => model
    }
    
    (userType.isDefined, email.isDefined) match {
      case (true, true) => renderViewModel("two-step-signin-choices-page", _model)
      case (true, false) => NoCache(SeeOther(routes.Application.twoStepSignInStart().url))
      case _ => renderErrorPage(configuration, NotFoundError("The requested page was not found."), NotFound.apply)
    }
  }

  def renderResetPassword(
    configuration: Configuration,
    errorIds: Seq[String],
    csrfToken: Option[Token],
    email: Option[String],
    resend: Boolean = false,
    clientId: Option[ClientID],
    returnUrl: Option[ReturnUrl] = None)
    (implicit messages: Messages) = {
    val model = ResetPasswordViewModel(
      configuration = configuration,
      errors = errorIds.map(ErrorViewModel.apply),
      csrfToken = csrfToken,
      email = email,
      resend = resend,
      clientId = clientId,
      returnUrl = returnUrl
    )
    renderViewModel("reset-password-page", model)
  }

  def renderResetPasswordEmailSent(configuration: Configuration, clientId: Option[ClientID],     emailProvider: Option[EmailProvider]
  )(implicit messages: Messages) = {
    val model = ResetPasswordEmailSentViewModel(
      configuration = configuration,
      clientId = clientId,
      emailProvider = emailProvider
    )
    renderViewModel("reset-password-email-sent-page", model)
  }

  def renderEmailChange(configuration: Configuration, clientId: Option[ClientID])(implicit message: Messages) = {
    val model = ChangeEmailViewModel(
      configuration = configuration,
      clientId = clientId
    )
    renderViewModel("change-email-page", model)
  }

  def renderResubLink(
    configuration: Configuration,
    clientId: Option[ClientID],
    errorIds: Seq[String],
    csrfToken: Option[Token])
    (implicit messages: Messages) = {
    val model = SendSignInLinkViewModel(
      configuration = configuration,
      errors = errorIds.map(ErrorViewModel.apply),
      csrfToken = csrfToken,
      clientId = clientId
    )
    renderViewModel("send-resub-link", model)
  }

  def renderSendSignInLinkSent(
    configuration: Configuration,
    clientId: Option[ClientID],
    emailProvider: Option[EmailProvider]
  )
    (implicit messages: Messages) = {
    val model = SendSignInLinkSentViewModel(
      configuration = configuration,
      clientId = clientId,
      emailProvider = emailProvider
    )
    renderViewModel("send-sign-in-link-sent", model)
  }

  def renderInvalidConsentToken(configuration: Configuration, token: String, csrfToken: Option[Token], errorIds: Seq[String])(implicit messages: Messages) = {
    val model = InvalidConsentTokenViewModel(
      configuration = configuration,
      token = token,
      csrfToken = csrfToken,
      errors = errorIds.map(ErrorViewModel.apply)
    )
    renderViewModel("invalid-consent-token-page", model)
  }

  def renderResendTokenSent(configuration: Configuration, csrfToken: Option[Token], errorIds: Seq[String])(implicit messages: Messages) = {
    val errors = errorIds.map(ErrorViewModel.apply)
    val maybeErrors = if(errors.isEmpty) None else Some(errors)

    val model = ResendTokenSentViewModel(
      configuration = configuration,
      csrfToken = csrfToken,
      errorIds = maybeErrors
    )
    renderViewModel("resend-link-sent-page", model)
  }

  def renderInvalidRepermissionToken(configuration: Configuration, token: String, csrfToken: Option[Token])(implicit messages: Messages) = {
    val model = InvalidRepermissionTokenViewModel(
      configuration = configuration,
      token = token,
      csrfToken = csrfToken
    )

    renderViewModel("invalid-repermission-token-page", model)
  }

  def renderCollectConsents(configuration: Configuration, clientId: Option[ClientID], returnUrl: String)(implicit messages: Messages) = {
    val model = CollectConsentsViewModel(
      configuration = configuration,
      clientId = clientId,
      returnUrl = returnUrl,
    )
    renderViewModel("collect-consents", model)
  }

  def renderErrorPage(configuration: Configuration, error: HttpError, resultGenerator: Html => Result)(implicit messages: Messages) =
    renderViewModel("error-page", ErrorPageViewModel(configuration, error), resultGenerator)

  def renderTsAndCs(configuration: Configuration, clientId: Option[ClientID], group: GroupCode, returnUrl: ReturnUrl, signOutLink: URI, csrfToken: Option[Token])(implicit messages: Messages) = {
    val model = TsAndCsViewModel(configuration, clientId, group, returnUrl, signOutLink, csrfToken)
    renderViewModel("third-party-ts-and-cs-page", model)
  }

  def renderViewModel(
    view: String,
    model: ViewModel with ViewModelResources with Product,
    resultGenerator: Html => Result = Results.Ok.apply): Result = {

    val html = HBS.withProduct(view, model)

    resultGenerator(html)
      .withHeaders(ContentSecurityPolicy.cspForViewModel(model))
  }

}
