package com.gu.identity.frontend.analytics.client

import com.gu.identity.frontend.logging.Logging
import play.api.libs.ws.WSClient
import scala.concurrent.duration._


class MeasurementProtocolClient(ws: WSClient) extends Logging {
  def sendSuccessfulSigninEvent(signinEventRequest: SigninEventRequest) = makeRequest(signinEventRequest)
  def sendSuccessfulSigninFirstStepEvent(signinFirstStepEventRequest: SigninSecondStepEventRequest) = makeRequest(signinFirstStepEventRequest)
  def sendSuccessfulRegisterEvent(registerEventRequest: RegisterEventRequest) = makeRequest(registerEventRequest)

  def send(request: MeasurementProtocolRequest) = makeRequest(request)

  private def makeRequest(request: MeasurementProtocolRequest) =
    ws.url(request.url)
      .withRequestTimeout(2 seconds)
      .post(request.body)

}
