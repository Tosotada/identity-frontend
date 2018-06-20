package com.gu.identity.frontend.views.models

import buildinfo.BuildInfo
import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.models.{ClientID, GuardianJobsClientID, GuardianMembersClientID, ReturnUrl}
import com.gu.identity.frontend.models.Text.{ClientSideText, HeaderText, LayoutText}
import com.gu.identity.frontend.models.text.FooterText
import com.gu.identity.frontend.mvt
import com.gu.identity.frontend.mvt.{ActiveMultiVariantTests, MultiVariantTest, MultiVariantTests}
import play.api.i18n.Messages
import play.api.libs.json.Json
import com.gu.identity.frontend.configuration.Configuration.Environment
import com.gu.identity.frontend.controllers.routes

case object BaseLayoutViewModel extends ViewModel with ViewModelResources {

  val resources: Seq[PageResource with Product] = Seq(
    JavascriptResource("https://assets.guim.co.uk/polyfill.io/v2/polyfill.min.js?rum=0&features=default,es6,es7,es2017,fetch,promise&flags=gated&unknown=polyfill","https://assets.guim.co.uk/", isInHead = false),
    LocalJavascriptResource.fromAsset("main.bundle.js", isInHead = false)
  )

  val indirectResources: Seq[PageResource with Product] = Seq(
    IndirectlyLoadedInlinedFontResources,
    IndirectlyLoadedFontResources,
    IndirectlyLoadedImageResources,
    IndirectlyLoadedInlinedImageResources,
    UnsafeInlineCSSResource,
    IndirectlyLoadedExternalScriptResources("https://j.ophan.co.uk"),
    IndirectlyLoadedExternalScriptResources("https://www.google-analytics.com"),
    IndirectlyLoadedExternalResources(Environment.stage match {
      case "DEV" =>  "*.thegulocal.com"
      case "CODE" => "*.dev-theguardian.com"
      case _ => "*.theguardian.com"
    }),
    IndirectlyLoadedExternalResources("https://sentry.io"),
    IndirectlyLoadedExternalResources("https://ipv4.guim.co.uk"),
    IndirectlyLoadedExternalImageResources("https://www.google-analytics.com"),
    IndirectlyLoadedExternalImageResources("https://hits-secure.theguardian.com"),
    IndirectlyLoadedExternalImageResources("https://sb.scorecardresearch.com"),
    IndirectlyLoadedExternalImageResources("https://ophan.theguardian.com")
  )
}


case class SkinInViewModel(
  name: String,
  banner: Boolean = false
)


case class LayoutViewModel private(
    text: Map[String,String],
    headerText: Map[String, String],
    footerText: FooterText,
    links: LayoutLinks,
    resources: Seq[PageResource with Product],
    indirectResources: Seq[PageResource with Product],
    favicons: Seq[Favicon] = Favicons(),
    skin: SkinInViewModel)
  extends ViewModel
  with ViewModelResources


/**
 * Config that will be exposed as Javascript inlined into the html response.
 */
case class JavascriptConfig(
    sentryDsn: String,
    mvtTests: Seq[MultiVariantTest],
    routes: Map[String, String],
    text: Map[String, String],
    localisedErrors: Map[String, String],
    appVersion: String = BuildInfo.gitCommitId,
    gaUID: String) {
  self =>

  import mvt.Implicits._
  implicit val jsonWrites = Json.writes[JavascriptConfig]

  def toJSON =
    Json.toJson(self)

  def toJSONString: String =
    Json.stringify(toJSON)

  def toJavascript: String =
    s"this._idConfig=$toJSONString;"
}

case class JavascriptRuntimeParams(activeTests: Map[String, String]) {
  self =>

  implicit val jsonWrites = Json.writes[JavascriptRuntimeParams]

  def toJSON =
    Json.toJson(self)

  def toJSONString: String =
    Json.stringify(toJSON)

  def toJavascript: String =
    s"this._idRuntimeParams=$toJSONString;"
}

object LayoutViewModel {

  def apply(configuration: Configuration)(implicit messages: Messages): LayoutViewModel =
    apply(configuration, Map.empty, clientId = None, returnUrl = None)

  def apply(configuration: Configuration, token: Option[String])(implicit messages: Messages): LayoutViewModel =
    apply(configuration, Map.empty, clientId = None, returnUrl = None)

  def apply(configuration: Configuration, clientId: Option[ClientID], returnUrl: Option[ReturnUrl])(implicit messages: Messages): LayoutViewModel =
    apply(configuration, activeTests = Map.empty, clientId, returnUrl)

  def apply(configuration: Configuration, activeTests: ActiveMultiVariantTests, clientId: Option[ClientID], returnUrl: Option[ReturnUrl])(implicit messages: Messages): LayoutViewModel = {

    val skin = SkinInViewModel(
      name = clientId
        .filter(_.hasSkin)
        .map(c => s"skin-${c.id}")
        .getOrElse("skin-default"),
      banner = clientId.filter(_.hasSkinBanner) match {
        case Some(_) => true
        case _ => false
      }
    )

    val config = JavascriptConfig(
      sentryDsn = configuration.sentryDsnJs,
      mvtTests = MultiVariantTests.all.toSeq,
      routes = Map(
        "signIn" -> routes.Application.signIn().url,
        "reset" -> routes.Application.reset().url,
        "registerAction" -> routes.RegisterAction.register().url,
        "twoStepSignIn" -> routes.Application.twoStepSignInStart().url,
        "twoStepSignInAction" -> routes.SigninAction.emailSignInFirstStep().url,
        "signInSecondStepCurrentAction" -> routes.SigninAction.signInSecondStepCurrent().url,
        "sendResubLinkAction" -> routes.SigninAction.sendResubLinkAction().url,
        "smartlockSignIn" -> routes.SigninAction.signInWithSmartLock().url
      ),
      text = ClientSideText.toMap,
      localisedErrors = ErrorViewModel.errorMessages.map{case (errId, errMsg) => (errId.key, errMsg)},
      gaUID = configuration.gaUID
    )

    val runtime = activeTests.headOption.map { _ =>
      JavascriptRuntimeParams(activeTests.map {
        case (key, value) => key.name -> value.id
      })
    }

    val inlinedJSConfig = InlinedJSONResource("id_config", config.toJSONString)
    val inlinedJSRuntimeParams = runtime.map { r =>
      InlinedJSONResource("id_runtime_params", r.toJSONString)
    }

    val cssResources = LocalCSSResource.fromAsset("bundle.css")

    val resources: Seq[PageResource with Product] = BaseLayoutViewModel.resources ++ Seq(Some(inlinedJSConfig), inlinedJSRuntimeParams).flatten ++ Seq(cssResources)

    LayoutViewModel(
      text = LayoutText.toMap,
      headerText = HeaderText.toMap,
      footerText = FooterText(),
      links = LayoutLinks(configuration, clientId, returnUrl),
      resources = resources,
      indirectResources = BaseLayoutViewModel.indirectResources,
      skin = skin)
  }
}

case class LayoutLinks private(
    headerBack: String,
    headerLogo: String,
    footerHelp: String,
    footerTerms: String,
    footerContact: String,
    footerPrivacy: String,
    footerFeedback: String,
    footerCookies: String)

object LayoutLinks {
  def apply(configuration: Configuration, clientId: Option[ClientID], returnUrl: Option[ReturnUrl]): LayoutLinks = {
    val baseUrl = configuration.dotcomBaseUrl

    LayoutLinks(
      headerBack = returnUrl.map(_.url).getOrElse(baseUrl),
      headerLogo = logoUrl(configuration, clientId),
      footerHelp = s"$baseUrl/help/identity-faq",
      footerTerms = s"$baseUrl/help/terms-of-service",
      footerContact = s"$baseUrl/help/contact-us",
      footerPrivacy = s"$baseUrl/info/privacy",
      footerFeedback = s"$baseUrl/info/tech-feedback",
      footerCookies = s"$baseUrl/info/cookies"
    )
  }

  private def logoUrl(configuration: Configuration, clientId: Option[ClientID]) =
    clientId match {
      case Some(GuardianMembersClientID) => configuration.preferredMembershipUrl
      case Some(GuardianJobsClientID) => configuration.jobsBaseUrl
      case _ => configuration.dotcomBaseUrl
    }

}

case class Favicon(filename: String, rel: String, url: String, sizes: Option[String] = None)

object Favicons {

  import LocalResource.resolveAssetUrl

  private val iconFiles = Seq(
    (if (Environment.stage == "DEV") "32x32-dev.ico" else "32x32.ico"),
    "57x57.png",
    "72x72.png",
    "114x114.png",
    "120x120.png",
    "144x144.png",
    "152x152.png"
  )

  private val iconBaseDir = "components/favicons"

  private val iconUrls = iconFiles.map(f => f -> resolveAssetUrl(s"$iconBaseDir/$f"))

  def apply(): Seq[Favicon] = iconUrls.map {
    case (file, url) if file.endsWith(".png") => Favicon(file, "apple-touch-icon", url)
    case (file, url) => Favicon(file, "shortcut icon", url)
  }
}
