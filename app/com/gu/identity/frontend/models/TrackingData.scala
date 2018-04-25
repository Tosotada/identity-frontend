package com.gu.identity.frontend.models

import com.gu.identity.frontend.utils.RemoteAddress
import com.gu.identity.service.client.HttpParameters
import play.api.mvc.RequestHeader

case class TrackingData(returnUrl:Option[String],
                        trackingReturnUrl:Option[String],
                        registrationType: Option[String],
                        ipAddress: Option[String],
                        referrer: Option[String],
                        userAgent: Option[String],
                        skipValidationReturn: Option[Boolean]) {
  def parameters: HttpParameters = List(
    returnUrl.map("returnUrl" -> _),
    trackingReturnUrl.map("trackingReturnUrl" -> _),
    registrationType.map("trackingRegistrationType" -> _),
    ipAddress.map("trackingIpAddress" -> _),
    referrer.map("trackingReferer" -> _),
    userAgent.map("trackingUserAgent" -> _),
    skipValidationReturn.map("skipValidationReturn" -> _.toString)
  ).flatten
}

object TrackingData extends RemoteAddress {

  def apply(request: RequestHeader, returnUrl: Option[String], skipValidationReturn: Option[Boolean] = None): TrackingData = {
    TrackingData(
      returnUrl = returnUrl,
      trackingReturnUrl = returnUrl,
      registrationType = request.getQueryString("type"),
      ipAddress = clientIp(request),
      referrer =  request.headers.get("Referer"),
      userAgent = request.headers.get("User-Agent"),
      skipValidationReturn = skipValidationReturn
    )
  }
}
