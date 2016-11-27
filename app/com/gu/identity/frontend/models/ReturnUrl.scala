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
  val invalidUrlPaths = List("/signin", "/register")
  val validJobsAppLogoutDomain = "sso.com.theguardian.jobs"
  val validJobsAppLogoutPath = "://ssologoutsuccess"

  def apply(returnUrlParam: Option[String], configuration: Configuration): ReturnUrl =
    apply(returnUrlParam, refererHeader = None, configuration, clientId = None)

  def apply(returnUrlParam: Option[String], refererHeader: Option[String]): Option[ReturnUrl] =
    returnUrlParam
      .flatMap(uriOpt)
      .orElse(refererHeader.flatMap(uriOpt))
      .filter(validDomain)
      .filter(validUrlPath)
        .filter()
      .map(uri => ReturnUrl(uri))

  def apply(returnUrlParam: Option[String], refererHeader: Option[String], configuration: Configuration, clientId: Option[ClientID]): ReturnUrl =
    apply(returnUrlParam, refererHeader)
      .getOrElse {
        defaultForClient(configuration, clientId)
      }

  def default(configuration: Configuration) =
    defaultFromUrl(configuration.dotcomBaseUrl)

  def defaultForMembership(configuration: Configuration) =
    defaultFromUrl(configuration.preferredMembershipUrl)

  def defaultForClient(configuration: Configuration, clientId: Option[ClientID]) =
    clientId match {
      case Some(GuardianMembersAClientID) => defaultForMembership(configuration)
      case Some(GuardianMembersBClientID) => defaultForMembership(configuration)
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

  private[models] def validJobsAppLogout(uri: URI): Boolean = {
    if (uri.getHost == validJobsAppLogoutDomain) {
      uri.getPath.equals(validJobsAppLogoutPath)
    }
    else true
  }

  private[models] def validUrlPath(uri: URI): Boolean = {
    val urlPath = uri.getPath
    !invalidUrlPaths.contains(urlPath)
  }


  object FormMapping {
    import play.api.data.Mapping
    import play.api.data.Forms.{optional, text}

    def returnUrl(refererHeader: Option[String]): Mapping[Option[ReturnUrl]] =
      optional(text).transform(ReturnUrl.apply(_: Option[String], refererHeader), _.flatMap(_.toStringOpt))
  }

}
