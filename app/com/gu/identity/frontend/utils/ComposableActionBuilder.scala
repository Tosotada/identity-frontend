package com.gu.identity.frontend.utils

import play.api.mvc._

import scala.concurrent.Future

/**
 * Adapts / patches Play's ActionBuilder fixing issues where composeAction
 * and composeParser does not properly compose when using `andThen` to chain.
 */
abstract class ComposableActionBuilder[+R[_]](cc: ControllerComponents) extends ActionBuilder[R, AnyContent] {
  self =>

  override val executionContext = cc.executionContext
  override def parser: BodyParser[AnyContent] = cc.parsers.default

  // Patched accessors for ActionBuilder compose methods to get around
  // protected access in ActionBuilder
  protected def patchedComposeAction[A](action: Action[A]): Action[A] =
    composeAction(action)

  protected def patchedComposeParser[A](bodyParser: BodyParser[A]): BodyParser[A] =
    composeParser(bodyParser)


  override def andThen[Q[_]](other: ActionFunction[R, Q]): ComposableActionBuilder[Q] =
    new ComposableActionBuilder[Q](cc) {


      def invokeBlock[A](request: Request[A], block: Q[A] => Future[Result]): Future[Result] =
        self.invokeBlock[A](request, other.invokeBlock[A](_, block))

      override protected def composeParser[A](bodyParser: BodyParser[A]): BodyParser[A] =
        self.composeParser {
          other match {
            case o: ComposableActionBuilder[Q] => o.patchedComposeParser(bodyParser)
            case _ => bodyParser
          }
        }

      override protected def composeAction[A](action: Action[A]): Action[A] = self.composeAction {
        other match {
          case o: ComposableActionBuilder[Q] => o.patchedComposeAction(action)
          case _ => action
        }
      }
    }
}
