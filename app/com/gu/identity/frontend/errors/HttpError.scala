package com.gu.identity.frontend.errors

import play.api.UsefulException

sealed trait HttpError

case class NotFoundError(message: String) extends HttpError
case class BadRequestError(message: String, statusCode: Int = 400, rawMessage: Option[String] = None) extends HttpError
case class ForbiddenError(message: String) extends HttpError
case class SigninTokenRejected(message: String) extends HttpError
case class EmailChangeTokenRejected(message: String) extends HttpError

case class UnexpectedError private(id: String, title: String, description: String) extends HttpError

object UnexpectedError {
  def apply(ex: UsefulException): UnexpectedError =
    UnexpectedError(ex.id, ex.title, ex.description)
}
