package com.gu.identity.frontend.logging

import akka.actor.ActorSystem
import com.amazonaws.handlers.AsyncHandler
import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsyncClientBuilder
import com.amazonaws.services.cloudwatch.model.{Dimension, MetricDatum, PutMetricDataRequest, PutMetricDataResult}
import com.gu.identity.frontend.configuration.Configuration._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object LoggingAsyncHandler extends AsyncHandler[PutMetricDataRequest, PutMetricDataResult] with Logging {
  def onError(exception: Exception) {
    logger.error(s"CloudWatch PutMetricDataRequest error: ${exception.getMessage}}")
  }

  def onSuccess(request: PutMetricDataRequest, result: PutMetricDataResult) {
    logger.debug("CloudWatch PutMetricDataRequest - success")
  }

}

object SuccessfulActionCloudwatchLogging {

  private lazy val cloudwatch = {
    AmazonCloudWatchAsyncClientBuilder.standard()
      .withCredentials(AWSConfig.credentials)
      .withClientConfiguration(AWSConfig.clientConfiguration)
      .build()
  }

  private lazy val stageDimension = new Dimension().withName("Stage").withValue(Environment.stage)

  private def createRequest(namespace: String, metricName: String) = {
    new PutMetricDataRequest()
      .withNamespace(namespace)
      .withMetricData(
        new MetricDatum()
          .withMetricName(metricName)
          .withUnit("Count")
          .withValue(1d)
          .withDimensions(stageDimension)
      )
  }

  private def createSmallDataPointRequest(namespace: String, metricName: String) = {
    new PutMetricDataRequest()
      .withNamespace(namespace)
      .withMetricData(
        new MetricDatum()
          .withMetricName(metricName)
          .withUnit("Count")
          .withValue(0.000000001d)
          .withDimensions(stageDimension)
      )
  }

  def putSignIn(): Unit = {
    val request = createRequest("SuccessfulSignIns", "SuccessfulSignIn")
    cloudwatch.putMetricDataAsync(request, LoggingAsyncHandler)
  }

  def putSignInFirstStep(): Unit = {
    val request = createRequest("SuccessfulSignIns", "SuccessfulSignInFirstStep")
    cloudwatch.putMetricDataAsync(request, LoggingAsyncHandler)
  }

  def putSmartLockSignIn(): Unit = {
    val request = createRequest("SuccessfulSmartLockSignIns", "SuccessfulSmartLockSignIn")
    cloudwatch.putMetricDataAsync(request, LoggingAsyncHandler)
  }

  def putSmallDataPointSignIn(): Unit = {
    val request = createSmallDataPointRequest("SuccessfulSignIns", "SuccessfulSignIn")
    cloudwatch.putMetricDataAsync(request, LoggingAsyncHandler)
  }

  def putRegister(): Unit = {
    val request = createRequest("SuccessfulRegistrations", "SuccessfulRegistration")
    cloudwatch.putMetricDataAsync(request, LoggingAsyncHandler)
  }
}

class SmallDataPointCloudwatchLogging(actorSystem: ActorSystem)(implicit executionContext: ExecutionContext)
    extends Logging {

  def start = {
    logger.info("Starting to send small data points to Cloudwatch every 10 seconds")

    actorSystem.scheduler.schedule(10.seconds, 10.seconds) {
      SuccessfulActionCloudwatchLogging.putSmallDataPointSignIn()
    }
  }
}
