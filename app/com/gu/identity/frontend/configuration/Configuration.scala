package com.gu.identity.frontend.configuration

import com.amazonaws.ClientConfiguration
import com.amazonaws.auth._
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions._
import play.api.{Configuration => PlayConfiguration}


case class Configuration(
  identityCookieDomain: String,

  identityApiKey: String,
  identityApiHost: String,

  identityProfileBaseUrl: String,

  dotcomBaseUrl: String,
  preferredMembershipUrl: String,

  preferredSupportUrl: String,

  jobsBaseUrl: String,

  identityFederationApiHost: String,

  recaptchaEnabled: Boolean,
  googleRecaptchaSiteKey: String,
  googleRecaptchaSecretKey: String,

  identityCookiePublicKey: String,

  // endpoint for reporting errors to Sentry
  sentryDsnJs: String,
  sentryDsnScala: String,

  gaUID: String,

  collectSignupConsents: Boolean,
  collectV2Consents: Boolean,

  underlying: PlayConfiguration)


object Configuration {
  def apply(appConfiguration: PlayConfiguration): Configuration = {

    def getString(path: String) =
      appConfiguration.get[String](path)

    def getBoolean(path: String) =
      appConfiguration.get[Boolean](path)


    Configuration(
      identityCookieDomain = getString("identity.frontend.cookieDomain"),

      identityApiKey = getString("identity.api.key"),
      identityApiHost = getString("identity.api.host"),

      identityFederationApiHost = getString("identity.federation.api.host"),

      identityProfileBaseUrl = getString("identity.frontend.baseUrl"),

      dotcomBaseUrl = getString("identity.frontend.dotcomBaseUrl"),

      preferredMembershipUrl = getString("identity.frontend.preferredMembershipUrl"),

      preferredSupportUrl = getString("identity.frontend.preferredSupportUrl"),

      jobsBaseUrl = getString("identity.frontend.jobsBaseUrl"),

      googleRecaptchaSiteKey = getString("google.recaptcha.site"),

      googleRecaptchaSecretKey = getString("google.recaptcha.secret"),

      recaptchaEnabled = getBoolean("google.recaptchaEnabled"),

      identityCookiePublicKey = getString("identityCookie.publicKey"),

      sentryDsnJs = getString("sentry.dsn.js"),

      sentryDsnScala = getString("sentry.dsn.scala"),

      gaUID = getString("ga-uid"),

      collectSignupConsents = false,
      collectV2Consents = false,

      underlying = appConfiguration
    )
  }


  lazy val testConfiguration = Configuration(
    identityCookieDomain = "dev-theguardian.com",

    identityApiKey = "--test-key--",
    identityApiHost = "idapi.code.dev-theguardian.com",

    identityFederationApiHost = "https://oauth.code.dev-theguardian.com",

    identityProfileBaseUrl = "https://profile.code.dev-theguardian.com",

    dotcomBaseUrl = "https://www.theguardian.com",

    preferredMembershipUrl = "https://membership.theguardian.com/supporter",

    preferredSupportUrl = "https://support.theguardian.com/contribute",

    jobsBaseUrl = "https://jobs.theguardian.com",

    googleRecaptchaSiteKey = "--recaptcha-key--",

    googleRecaptchaSecretKey = "--recaptcha-secret-key--",

    recaptchaEnabled = true,

    identityCookiePublicKey = "",

    sentryDsnJs = "--stubbed-sentry-dsn-js--",

    sentryDsnScala = "--stubbed-sentry-dsn-scala--",

    gaUID = "UA-78705427-123123",

    collectSignupConsents = false,
    collectV2Consents = false,

    underlying = PlayConfiguration.empty
  )

  object AWSConfig {
    val credentials: AWSCredentialsProvider = {
      val provider = new AWSCredentialsProviderChain(
        new EnvironmentVariableCredentialsProvider(),
        new SystemPropertiesCredentialsProvider(),
        new ProfileCredentialsProvider(),
        InstanceProfileCredentialsProvider.getInstance()
      )
      provider
    }

    val clientConfiguration: ClientConfiguration = new ClientConfiguration()

    val region: Region = {
      val defaultRegion: Region = Region.getRegion(EU_WEST_1)
      Option(getCurrentRegion()).getOrElse(defaultRegion)
    }

  }

  object Environment {
    lazy val stage = System.getProperty("stage", "DEV")
  }
}
