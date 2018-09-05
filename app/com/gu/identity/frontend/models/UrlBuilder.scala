package com.gu.identity.frontend.models

import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.errors.{SeqAppExceptions, AppException}
import play.api.mvc.Call

object UrlBuilder {

  type UrlParameter = (String, String)
  type UrlParameters = Seq[UrlParameter]

  private def encode = java.net.URLEncoder.encode(_: String, "UTF8")

  def apply(baseUrl: String, params: UrlParameters): String =
    params.headOption match {
      case None => baseUrl
      case _ => {
        val paramString = params.map {
          case (key, value) => s"$key=${encode(value)}"
        }.mkString("&")

        val initialVariableDivider = if(baseUrl contains "?") "&" else "?"

        s"$baseUrl$initialVariableDivider$paramString"
      }
    }

  def apply(baseUrl: String, returnUrl: ReturnUrl): String =
    apply(baseUrl, returnUrl, skipConfirmation = None, clientId = None, group = None, skipThirdPartyLandingPage = None)

  def apply(baseUrl: String, returnUrl: ReturnUrl, clientId: Option[ClientID]): String =
    apply(baseUrl, returnUrl, skipConfirmation = None, clientId, group = None, skipThirdPartyLandingPage = None)

  def apply(baseUrl: String, returnUrl: ReturnUrl, skipConfirmation: Option[Boolean], clientId: Option[ClientID], group: Option[String]): String =
    apply(baseUrl, buildParams(Some(returnUrl), skipConfirmation, clientId, group))

  def apply(baseUrl: String, returnUrl: ReturnUrl, skipConfirmation: Option[Boolean], clientId: Option[ClientID], group: Option[String], skipThirdPartyLandingPage: Option[Boolean]): String =
    apply(baseUrl, buildParams(Some(returnUrl), skipConfirmation, clientId, group, skipThirdPartyLandingPage))

  def apply(baseUrl: String, returnUrl: ReturnUrl, skipConfirmation: Option[Boolean], clientId: Option[ClientID], group: Option[String], skipThirdPartyLandingPage: Option[Boolean], skipValidationReturn: Option[Boolean]): String =
    apply(baseUrl, buildParams(Some(returnUrl), skipConfirmation, clientId, group, skipThirdPartyLandingPage, skipValidationReturn))

  def apply(baseUrl: String, returnUrl: Option[ReturnUrl], skipConfirmation: Option[Boolean], clientId: Option[ClientID], group: Option[GroupCode], error: AppException): String =
    apply(baseUrl, buildParams(returnUrl, skipConfirmation, clientId, group.map(_.id), error = Some(error)))

  def apply(baseUrl: String, returnUrl: Option[ReturnUrl], skipConfirmation: Option[Boolean], clientId: Option[ClientID], group: Option[GroupCode], skipValidationReturn: Option[Boolean]): String =
    apply(baseUrl, buildParams(returnUrl = returnUrl, skipConfirmation = skipConfirmation, clientId = clientId, group = group.map(_.id), skipValidationReturn = skipValidationReturn))

  def apply(baseUrl: String, error: AppException): String =
    apply(baseUrl, buildParams(error = Some(error)))

  def apply(call: Call, params: UrlParameters): String =
    apply(call.url, params)

  def apply(call: Call, returnUrl: ReturnUrl, skipConfirmation: Option[Boolean], clientId: Option[ClientID], group: Option[String]): String =
    apply(call.url, returnUrl, skipConfirmation, clientId, group, skipThirdPartyLandingPage = None)

  def apply(call: Call, returnUrl: ReturnUrl, skipConfirmation: Option[Boolean], clientId: Option[ClientID], group: Option[String], skipValidationReturn: Option[Boolean] = None): String =
    apply(call.url, returnUrl, skipConfirmation, clientId, group, skipThirdPartyLandingPage = None, skipValidationReturn = skipValidationReturn)

  def apply(baseUrl: String, call: Call): String = s"$baseUrl${call.url}"

  def buildThirdPartyReturnUrl(
      returnUrl: ReturnUrl,
      skipConfirmation: Option[Boolean],
      skipThirdPartyLandingPage: Boolean,
      clientId: Option[ClientID],
      group: GroupCode,
      configuration: Configuration): ReturnUrl = {

    val baseThirdPartyReturnUrl = configuration.identityProfileBaseUrl + "/agree/" + group.id
    val thirdPartyReturnUrl = apply(baseThirdPartyReturnUrl, returnUrl, skipConfirmation, clientId, group = None, Some(skipThirdPartyLandingPage))
    ReturnUrl(Some(thirdPartyReturnUrl), configuration)
  }

  def buildOauthReturnUrl(
      baseUrl: String,
      returnUrl: ReturnUrl,
      skipConfirmation: Option[Boolean],
      clientId: Option[ClientID],
      group: GroupCode,
      configuration: Configuration): String = {

    val thirdPartyReturnUrl = buildThirdPartyReturnUrl(returnUrl, skipConfirmation, skipThirdPartyLandingPage = true, clientId, group, configuration)
    apply(baseUrl, thirdPartyReturnUrl, skipConfirmation, clientId, Some(group.id), skipThirdPartyLandingPage = None)
  }

  private def buildParams(
      returnUrl: Option[ReturnUrl] = None,
      skipConfirmation: Option[Boolean] = None,
      clientId: Option[ClientID] = None,
      group: Option[String] = None,
      skipThirdPartyLandingPage: Option[Boolean] = None,
      skipValidationReturn: Option[Boolean] = None,
      error: Option[AppException] = None): UrlParameters =
    Seq(
      returnUrl.flatMap(_.toStringOpt).map("returnUrl" -> _),
      skipConfirmation.map("skipConfirmation" -> _.toString),
      clientId.map("clientId" -> _.id),
      group.map("group" -> _),
      skipThirdPartyLandingPage.map("skipThirdPartyLandingPage" -> _.toString),
      skipValidationReturn.map("skipValidationReturn" -> _.toString)
    ).flatten ++ error.map(errorToUrlParameters).getOrElse(Seq.empty)


  private def errorToUrlParameters(error: AppException): UrlParameters =
    error match {
      case SeqAppExceptions(errors) => errors.flatMap(errorToUrlParameters)
      case e => Seq("error" -> e.id.key)
    }
}
