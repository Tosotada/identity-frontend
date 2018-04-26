package com.gu.identity.frontend.filters

import com.mohiva.play.htmlcompressor.HTMLCompressorFilter
import play.api.http.HttpFilters
import play.filters.gzip.GzipFilter

class Filters(
    securityHeadersFilter: SecurityHeadersFilter,
    gzipFilter: GzipFilter,
    htmlCompressorFilter: HTMLCompressorFilter,
    logRequestsFilter: LogRequestsFilter,
    strictTransportSecurityHeaderFilter: StrictTransportSecurityHeaderFilter) extends HttpFilters {

  // Note: filters are processed in reverse (foldRight), so need to explicitly
  //       put htmlCompressorFilter at end so that its processed first
  def filters = Seq(logRequestsFilter, strictTransportSecurityHeaderFilter, securityHeadersFilter, gzipFilter, htmlCompressorFilter)
}
