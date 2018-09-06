package com.gu.identity.frontend.models

import java.net.URI
import com.gu.identity.frontend.configuration.Configuration

import scala.util.Try

case class ReturnUrl private[models](
    uri: URI,
    isDefault: Boolean = false) {

  lazy val url: String = uri.toString

  lazy val toStringOpt: Option[String] =
    Some(url).filterNot(_ => isDefault)
}

object ReturnUrl {

  val domains = List(".theguardian.com", ".code.dev-theguardian.com", ".thegulocal.com")
  val validUris = List(new URI("sso.com.theguardian.jobs://ssologoutsuccess"))
  val defaultInvalidUrlPaths = List("/signin", "/register")

  def opt(returnUrlParam: Option[String], refererHeader: Option[String], invalidUrlPaths: List[String]): Option[ReturnUrl] =
    returnUrlParam
      .flatMap(uriOpt)
      .orElse(refererHeader.flatMap(uriOpt))
      .filter { uri =>
        validUris.contains(uri) || validDomain(uri) && validUrlPath(uri, invalidUrlPaths)
      }
      .map(uri => ReturnUrl(uri))

  def apply(returnUrlParam: Option[String], refererHeader: Option[String], configuration: Configuration, clientId: Option[ClientID], invalidUrlPaths: List[String]): ReturnUrl =
    opt(returnUrlParam, refererHeader, invalidUrlPaths)
      .getOrElse {
        defaultForClient(configuration, clientId)
      }

  def apply(returnUrlParam: Option[String], configuration: Configuration): ReturnUrl =
    apply(returnUrlParam, refererHeader = None, configuration, clientId = None,  ReturnUrl.defaultInvalidUrlPaths)

  def apply(returnUrlParam: Option[String], refererHeader: Option[String], configuration: Configuration, clientId: Option[ClientID]): ReturnUrl =
    apply(returnUrlParam, refererHeader, configuration, clientId, ReturnUrl.defaultInvalidUrlPaths)

  def default(configuration: Configuration) =
    defaultFromUrl(configuration.dotcomBaseUrl)

  def defaultForMembership(configuration: Configuration) =
    defaultFromUrl(configuration.preferredMembershipUrl)

  def defaultForSupport(configuration: Configuration) =
    defaultFromUrl(configuration.preferredSupportUrl)

  def defaultForJobs(configuration: Configuration) =
    defaultFromUrl(configuration.jobsBaseUrl)

  def defaultForClient(configuration: Configuration, clientId: Option[ClientID]) =
    clientId match {
      case Some(GuardianMembersClientID) => defaultForMembership(configuration)
      case Some(GuardianRecurringContributionsClientID) => defaultForSupport(configuration)
      case Some(GuardianJobsClientID) => defaultForJobs(configuration)
      case _ => default(configuration)
    }

  private def defaultFromUrl(url: String) = {
    val defaultUri = uri(url).getOrElse {
      sys.error("Invalid defaultReturnUrl specified in configuration")
    }

    ReturnUrl(defaultUri, isDefault = true)
  }

  /**
   * Parse a URI from a url string, ensure URISyntaxException is caught
   * by wrapping in a Try.
   */
  def uri(url: String): Try[URI] =
    Try(new URI(url))

  def uriOpt(url: String): Option[URI] =
    uri(url).toOption

  private[models] def validDomain(uri: URI): Boolean = {
    val hostname = uri.getHost
    domains.exists(s".$hostname".endsWith(_))
  }

  private[models] def validUrlPath(uri: URI, invalidUrlPaths: List[String]): Boolean = {
    val urlPath = uri.getPath
    !invalidUrlPaths.exists(urlPath.startsWith(_))
  }


  object FormMapping {
    import play.api.data.Mapping
    import play.api.data.Forms.{optional, text}

    def returnUrl(refererHeader: Option[String]): Mapping[Option[ReturnUrl]] =
      optional(text).transform(ReturnUrl.opt(_: Option[String], refererHeader, defaultInvalidUrlPaths), _.flatMap(_.toStringOpt))
  }

}
