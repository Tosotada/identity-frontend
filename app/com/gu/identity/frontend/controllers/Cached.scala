package com.gu.identity.frontend.controllers

import org.joda.time.{DateTime, DateTimeZone}
import org.joda.time.format.DateTimeFormat
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.math.max

object Cached {

  private val cacheableStatusCodes = Seq(200, 301, 404)

  private val tenDaysInSeconds = 864000

  def apply(result: Result): Result = apply(60)(result)
  def apply(result: Future[Result]): Future[Result] = result.map(apply)

  def apply(seconds: Int)(result: Result): Result = {
    if (suitableForCaching(result)) cacheHeaders(seconds, result) else result
  }

  def suitableForCaching(result: Result): Boolean = cacheableStatusCodes.contains(result.header.status)

  private def cacheHeaders(maxAge: Int, result: Result) = {
    val now = DateTime.now
    val staleWhileRevalidateSeconds = max(maxAge / 10, 1)
    result.withHeaders(
      "Cache-Control" -> s"public, max-age=$maxAge, stale-while-revalidate=$staleWhileRevalidateSeconds, stale-if-error=$tenDaysInSeconds",
      "Expires" -> toHttpDateTimeString(now.plusSeconds(maxAge)),
      "Date" -> toHttpDateTimeString(now)
    )
  }

  //http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.3.1
  private val HTTPDateFormat = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'").withZone(DateTimeZone.UTC)
  def toHttpDateTimeString(dateTime: DateTime): String = dateTime.toString(HTTPDateFormat)
}

class CachedAction(val parser: BodyParser[AnyContent])(implicit val executionContext: ExecutionContext)
    extends ActionBuilder[Request, AnyContent] {

  override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] =
    Cached(block(request))
}

object NoCache {
  def apply(result: Result): Result = result.withHeaders("Cache-Control" -> "no-cache, private", "Pragma" -> "no-cache")
  def apply(result: Future[Result]): Future[Result] = result.map(apply)
}
