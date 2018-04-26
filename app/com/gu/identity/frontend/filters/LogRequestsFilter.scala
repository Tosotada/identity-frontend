package com.gu.identity.frontend.filters

import akka.stream.Materializer
import play.api.Logger
import play.api.mvc.{Filter, RequestHeader, Result}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

class LogRequestsFilter(val mat: Materializer) extends Filter {

  private val logger = Logger(this.getClass)

  def apply(f: (RequestHeader) => Future[Result])(rh: RequestHeader): Future[Result] = {
    val start = System.currentTimeMillis

    def logTime(result: Result) {
      if(!rh.path.contains("/management/healthcheck")) {
        val time = System.currentTimeMillis - start
        val activity = s"${rh.method} ${rh.uri}"
        logger.info(s"$activity completed in $time ms with status ${result.header.status}")
      }
    }

    val result = f(rh)

    result.foreach(logTime)

    result
  }
}
