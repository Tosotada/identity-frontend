package com.gu.identity.frontend.analytics

import akka.actor.{Actor, ActorRef, PoisonPill, Props}
import com.gu.identity.frontend.analytics.client._
import com.gu.identity.frontend.logging.Logging
import com.gu.identity.frontend.request.RequestParameters.GaClientIdRequestParameter

private sealed trait Message
private sealed trait Event extends Message {
  val request: MeasurementProtocolRequest
}

private object Terminate extends Message
private case class SignIn(request: SigninEventRequest) extends Event
private case class Register(request: RegisterEventRequest) extends Event
private case class SignInSecondStep(request: SigninSecondStepEventRequest) extends Event

class AnalyticsEventActor(eventActor: ActorRef) {

  def sendSuccessfulRegister(registerEventRequest: RegisterEventRequest) = {
    eventActor ! Register(registerEventRequest)
  }

  def sendSuccessfulSignin(signinEventRequest: SigninEventRequest) = {
    eventActor ! SignIn(signinEventRequest)
  }

  def sendSuccessfulSigninFirstStep(signinSecondStepEventRequest: SigninSecondStepEventRequest) = {
    eventActor ! SignInSecondStep(signinSecondStepEventRequest)
  }

  def forward(req: MeasurementProtocolRequest) = {
    eventActor ! req
  }

  def terminateActor() = {
    eventActor ! PoisonPill
  }
}

object EventActor {
  def getProps(measurementProtocolClient: MeasurementProtocolClient): Props =
    Props(new EventActor(measurementProtocolClient))
}

private class EventActor(measurementProtocolClient: MeasurementProtocolClient) extends Actor with Logging {

  override def receive: Receive = {
    case SignIn(event) => measurementProtocolClient.sendSuccessfulSigninEvent(event)
    case SignInSecondStep(event) => measurementProtocolClient.sendSuccessfulSigninFirstStepEvent(event)
    case Register(event) => measurementProtocolClient.sendSuccessfulRegisterEvent(event)
    case e: MeasurementProtocolRequest => measurementProtocolClient.send(e)
    case _ =>  logger.warn("Unexpected event received by analytics event actor.")
  }
}
