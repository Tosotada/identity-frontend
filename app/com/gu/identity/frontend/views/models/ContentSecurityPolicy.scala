package com.gu.identity.frontend.views.models

import com.gu.identity.frontend.controllers._

object ContentSecurityPolicy {

  val CSP_DEFAULT_SRC = "default-src"
  val CSP_CONNECT_SRC = "connect-src"
  val CSP_SCRIPT_SRC = "script-src"
  val CSP_STYLE_SRC = "style-src"
  val CSP_FONT_SRC = "font-src"
  val CSP_IMG_SRC = "img-src"
  val CSP_FRAME_SRC = "frame-src"

  val CSP_SELF_DOMAIN = "'self'"
  val CSP_DATA_PROTOCOL = "data:"
  val CSP_UNSAFE_INLINE = "'unsafe-inline'"

  val CSP_REPORT_URI = "report-uri"
  val CSP_REPORT_URI_PATH = routes.CSPViolationReporter.cspReport.url

  val defaultCsp = Map(
    CSP_DEFAULT_SRC -> Seq(CSP_SELF_DOMAIN)
  )

  val violationReportingCsp = Map(
    CSP_REPORT_URI -> Seq(CSP_REPORT_URI_PATH)
  )


  def cspForViewModel(viewModel: ViewModel with ViewModelResources) = {
    val allResources = viewModel.resources ++ viewModel.indirectResources
    val grouped = allResources.filter(noPolicyRequirement).groupBy {
      case _: ConnectResource => CSP_CONNECT_SRC
      case _: ScriptResource => CSP_SCRIPT_SRC
      case _: StylesResource => CSP_STYLE_SRC
      case _: ImageResource => CSP_IMG_SRC
      case _: FontResource => CSP_FONT_SRC
      case _: FrameResource => CSP_FRAME_SRC
    }

    val transformed = grouped.mapValues(_.map(cspStatementForResource).distinct)

    "Content-Security-Policy" -> toCSPHeader(defaultCsp ++ transformed ++ violationReportingCsp)
  }

  private def noPolicyRequirement(resource: PageResource) = resource match {
    case _: NoPolicyRequirement => false
    case _ => true
  }

  private def cspStatementForResource(resource: PageResource): String = resource match {
    case _: UnsafeResource => CSP_UNSAFE_INLINE
    case _: LocalResource => CSP_SELF_DOMAIN
    case r: ScriptResource with InlinedSource => toCSPShaDefinition(r)
    case _: InlinedResource | _: InlinedJSONResource => CSP_DATA_PROTOCOL
    case r: ExternalResource => r.domain
  }


  private def toCSPHeader(csp: Map[String, Seq[String]]) =
    csp.map {
      case (key, value) => s"$key ${value.mkString(" ")}"
    }.mkString("; ")


  private def toCSPShaDefinition(source: InlinedSource) =
    s"'sha256-${source.sha256}'"

}
