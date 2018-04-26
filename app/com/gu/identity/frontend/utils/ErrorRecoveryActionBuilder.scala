package com.gu.identity.frontend.utils

import play.api.mvc._

import scala.concurrent.Future


/**
 * Allows a `ActionBuilder` which can recover from errors raised from when the
 * action block is invoked (`invokeBlock`), when the action is composed on
 * apply in `composeAction`, and when the request body is composed in `composeParser`.
 *
 * Clients should implement `recoverErrors` to recover from errors.
 */
trait ErrorRecoveryActionBuilder extends ComposableActionBuilder[Request] {

  def recoverErrors[A](request: Request[A]): PartialFunction[Throwable, Future[Result]]


  /**
   * Recover from Future.failed errors when ActionBuilder block is invoked
   */
  final def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] =
    block(request)
      .recoverWith(recoverErrors(request))(executionContext)


  /**
   * Catch known AppExceptions when the Action is composed and applied.
   * Play's CSRF checks when Action is applied.
   */
  override def composeAction[A](other: Action[A]): Action[A] =
    new Action[A] {
      def parser: BodyParser[A] = other.parser

      def apply(request: Request[A]): Future[Result] =
        other.apply(request).recoverWith(recoverErrors(request))(executionContext)
    }


  /**
   * Catch errors when attempting to parse request body.
   */
  override def composeParser[A](other: BodyParser[A]): BodyParser[A] =
    BodyParser { requestHeader =>
      val requestNoBody = Request(requestHeader, ())
      val recover = recoverErrors(requestNoBody)

      // Catch errors from body parser here with recoverM
      other.apply(requestHeader).recoverWith {
        case t if recover.isDefinedAt(t) => recover(t).map(Left.apply)(executionContext)
      }(executionContext)
    }

}
