package com.gu.identity.frontend.mvt

import com.gu.identity.frontend.controllers.NoCache
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

case class MultiVariantTestRequest[A](
    mvtCookie: Option[MultiVariantTestID],
    activeTests: ActiveMultiVariantTests,
    request: Request[A])
  extends WrappedRequest[A](request)

object MultiVariantTestRequest {
  private val OVERRIDE_PARAM_PREFIX = "mvt_"

  def apply[A](request: Request[A]): MultiVariantTestRequest[A] = {
    val mvtCookie = MultiVariantTestID.fromRequest(request)
    val activeTests = TestResults.activeTests(mvtCookie)

    MultiVariantTestRequest[A](mvtCookie, activeTests ++ getTestOverrides(request.queryString), request)
  }

  /**
   * Retrieve test overrides from the request parameters. Use ?mvt_<testName>=<variantId>
   */
  private def getTestOverrides(queryString: Map[String, Seq[String]]) = for {
    (key, values) <- queryString
    if key.startsWith(OVERRIDE_PARAM_PREFIX)
    testName = key.substring(OVERRIDE_PARAM_PREFIX.length)
    test <- MultiVariantTests.allServerSide.find(_.name.equalsIgnoreCase(testName))
    overrideValue <- values.headOption
    variant <- test.variants.find(_.id.equalsIgnoreCase(overrideValue))
  } yield test -> variant

}

class MultiVariantTestAction(val parser: BodyParser[AnyContent])(implicit val executionContext: ExecutionContext)
    extends ActionBuilder[MultiVariantTestRequest, AnyContent] {

  override def invokeBlock[A](request: Request[A], block: (MultiVariantTestRequest[A]) => Future[Result]): Future[Result] = {
    val req = MultiVariantTestRequest[A](request)
    MultiVariantTests.allServerSide.headOption.map { _ =>
      NoCache(block(req))
    }.getOrElse {
      block(req)
    }
  }
}
