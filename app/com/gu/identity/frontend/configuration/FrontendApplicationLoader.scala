package com.gu.identity.frontend.configuration

import akka.actor.{ActorRef, Props}
import com.gu.identity.cookie.{IdentityCookieDecoder, IdentityKeys}
import com.gu.identity.frontend.analytics.client.MeasurementProtocolClient
import com.gu.identity.frontend.analytics.{AnalyticsEventActor, EventActor}
import com.gu.identity.frontend.authentication.UserAuthenticatedAction
import com.gu.identity.frontend.controllers._
import com.gu.identity.frontend.errors.ErrorHandler
import com.gu.identity.frontend.filters._
import com.gu.identity.frontend.logging._
import com.gu.identity.frontend.mvt.MultiVariantTestAction
import com.gu.identity.frontend.request._
import com.gu.identity.frontend.services._
import com.gu.identity.service.client.IdentityClient
import jp.co.bizreach.play2handlebars.HandlebarsComponents
import play.api.ApplicationLoader.Context
import play.api.i18n.I18nComponents
import play.api.routing.Router
import play.api.{Application => _, _}
import play.filters.csrf.CSRFComponents
import play.filters.gzip.GzipFilter
import router.Routes

import scala.concurrent.ExecutionContext

class FrontendApplicationLoader extends ApplicationLoader {
  def load(context: Context) = {
    new ApplicationComponents(context).application
  }
}

class ApplicationComponents(context: Context)
    extends BuiltInComponentsFromContext(context)
    with I18nComponents
    with HandlebarsComponents
    with CSRFComponents
    with _root_.controllers.AssetsComponents
    with AhcWSComponents {

  lazy val frontendConfiguration = Configuration(configuration)

  lazy val identityServiceRequestHandler = new IdentityServiceRequestHandler(wsClient)
  lazy val identityClient: IdentityClient = new IdentityClient
  lazy val identityService: IdentityService = new IdentityServiceImpl(frontendConfiguration, identityServiceRequestHandler, identityClient)

  lazy val measurementProtocolClient: MeasurementProtocolClient = new MeasurementProtocolClient(wsClient)
  lazy val eventActor: ActorRef = actorSystem.actorOf(EventActor.getProps(measurementProtocolClient))
  lazy val analyticsEventActor: AnalyticsEventActor = new AnalyticsEventActor(eventActor)

  lazy val metricsActor: ActorRef = actorSystem.actorOf(Props[MetricsActor])
  lazy val metricsLoggingActor: MetricsLoggingActor = new MetricsLoggingActor(metricsActor)

  lazy val identityCookieDecoder: IdentityCookieDecoder = new IdentityCookieDecoder(IdentityKeys(frontendConfiguration.identityCookiePublicKey))

  // Parsers
  lazy val formRequestBodyParser = new FormRequestBodyParser(playBodyParsers)
  lazy val emailResubRequestsParser = new EmailResubRequestsParser(formRequestBodyParser)
  lazy val signInActionRequestBodyParser = new SignInActionRequestBodyParser(formRequestBodyParser)
  lazy val resetPasswordActionRequestBodyParser = new ResetPasswordActionRequestBodyParser(formRequestBodyParser)
  lazy val registerActionRequestBodyParser = new RegisterActionRequestBodyParser(formRequestBodyParser)
  lazy val resendTokenActionRequestBodyParser = new ResendTokenActionRequestBodyParser(formRequestBodyParser)

  // Actions
  lazy val userAuthenticatedAction = new UserAuthenticatedAction(controllerComponents, identityCookieDecoder.getUserDataForScGuU)
  lazy val serviceAction = new ServiceAction(controllerComponents)
  lazy val multiVariantTestAction = new MultiVariantTestAction(playBodyParsers.default)

  // Controllers
  lazy val applicationController = new Application(frontendConfiguration, controllerComponents, multiVariantTestAction)
  lazy val consentController = new ConsentController(frontendConfiguration, identityService, controllerComponents)
  lazy val healthcheckController = new HealthCheck(controllerComponents)
  lazy val digitalAssetLinksController = new DigitalAssetLinks(frontendConfiguration, controllerComponents)
  lazy val manifestController = new Manifest(controllerComponents)
  lazy val cspReporterController = new CSPViolationReporter(controllerComponents)
  lazy val googleRecaptchaServiceHandler = new GoogleRecaptchaServiceHandler(wsClient, frontendConfiguration)
  lazy val googleRecaptchaCheck = new GoogleRecaptchaCheck(googleRecaptchaServiceHandler)
  lazy val signinController = new SigninAction(identityService, controllerComponents, metricsLoggingActor, analyticsEventActor, frontendConfiguration, serviceAction, emailResubRequestsParser, signInActionRequestBodyParser)
  lazy val signOutController = new SignOutAction(identityService, controllerComponents, frontendConfiguration)
  lazy val registerController = new RegisterAction(identityService, controllerComponents, metricsLoggingActor, analyticsEventActor, frontendConfiguration, serviceAction, registerActionRequestBodyParser)
  lazy val thirdPartyTsAndCsController = new ThirdPartyTsAndCs(identityService, frontendConfiguration, httpErrorHandler, identityCookieDecoder.getUserDataForScGuU, userAuthenticatedAction, controllerComponents)
  lazy val resetPasswordController = new ResetPasswordAction(identityService, controllerComponents, serviceAction, resetPasswordActionRequestBodyParser, analyticsEventActor, frontendConfiguration)
  lazy val resendConsentTokenController = new ResendConsentTokenAction(identityService, controllerComponents, serviceAction, resendTokenActionRequestBodyParser)
  lazy val resendRepermissionTokenController = new ResendRepermissionTokenAction(identityService, controllerComponents, serviceAction, resendTokenActionRequestBodyParser)
  lazy val repermissionController = new RepermissionController(frontendConfiguration, identityService, controllerComponents, ExecutionContext.Implicits.global)
  lazy val signinTokenController = new SigninTokenController(frontendConfiguration, identityService, controllerComponents, analyticsEventActor, ExecutionContext.Implicits.global)
  lazy val optInController = new OptInController(controllerComponents)
  lazy val redirects = new Redirects(controllerComponents)

  override lazy val httpFilters = new Filters(
    new SecurityHeadersFilter(frontendConfiguration),
    new GzipFilter(),
    HtmlCompressorFilter(configuration, environment, materializer),
    new LogRequestsFilter(materializer),
    new StrictTransportSecurityHeaderFilter(materializer),
    csrfFilter
  ).filters

  override lazy val httpErrorHandler = new ErrorHandler(frontendConfiguration, messagesApi, environment, sourceMapper, Some(router))

  // Makes sure the logback.xml file is being found in DEV environments
  if (environment.mode == Mode.Dev) {
    LoggerConfigurator(environment.classLoader).foreach { _.configure(context.environment) }
  }

  if (environment.mode == Mode.Prod) {
    new SmallDataPointCloudwatchLogging(actorSystem).start
  }

  applicationLifecycle.addStopHook(() => {
    metricsLoggingActor.terminateActor()
    analyticsEventActor.terminateActor()
    actorSystem.terminate().map(_ => ())
  })


  override lazy val router: Router = new Routes(
    httpErrorHandler,
    applicationController,
    signinController,
    signOutController,
    thirdPartyTsAndCsController,
    registerController,
    consentController,
    resendConsentTokenController,
    repermissionController,
    resendRepermissionTokenController,
    resetPasswordController,
    cspReporterController,
    healthcheckController,
    digitalAssetLinksController,
    manifestController,
    optInController,
    assets,
    signinTokenController,
    redirects
  )

  val sentryLogging = new SentryLogging(frontendConfiguration) // don't make it lazy
}
