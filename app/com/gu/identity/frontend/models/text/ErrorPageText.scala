package com.gu.identity.frontend.models.text

import com.gu.identity.frontend.errors._
import play.api.i18n.Messages

sealed trait ErrorPageText {
  val pageTitle: String
  val title: String
  val description: String
}

object ErrorPageText {
  def apply(error: HttpError)(implicit messages: Messages): ErrorPageText = error match {
    case NotFoundError(_) => NotFoundErrorPageText()
    case ForbiddenError(_) => ForbiddenErrorPageText()
    case SigninTokenRejected(_) => SigninLinkFailedText()
    case EmailChangeTokenRejected(_) => EmailChangeFailedText()
    case err: BadRequestError => BadRequestErrorPageText(err, err.rawMessage)
    case err: UnexpectedError => UnexpectedErrorPageText(err)
  }
}


/** 400 Bad Request text */
case class BadRequestErrorPageText private(
    pageTitle: String,
    title: String,
    description: String,
    details: String,
    rawDetails: Option[String])
  extends ErrorPageText

object BadRequestErrorPageText {
  def apply(err: BadRequestError, rawDetails: Option[String] = None)(implicit messages: Messages): BadRequestErrorPageText =
    BadRequestErrorPageText(
      pageTitle = messages("errors.badRequest.pageTitle", err.statusCode),
      title = messages("errors.badRequest.title", err.statusCode),
      description = messages("errors.badRequest.description"),
      details = messages("errors.unexpected.details", err.message),
      rawDetails = rawDetails
    )
}

/** 403 Forbidden text */
case class ForbiddenErrorPageText private(
    pageTitle: String,
    title: String,
    description: String)
  extends ErrorPageText

object ForbiddenErrorPageText {
  def apply()(implicit messages: Messages): ForbiddenErrorPageText =
    ForbiddenErrorPageText(
      pageTitle = messages("errors.forbidden.pageTitle"),
      title = messages("errors.forbidden.title"),
      description = messages("errors.forbidden.description")
    )
}


/** 404 Not found text */
case class NotFoundErrorPageText private(
    pageTitle: String,
    title: String,
    description: String)
  extends ErrorPageText

object NotFoundErrorPageText {
  def apply()(implicit messages: Messages): NotFoundErrorPageText =
    NotFoundErrorPageText(
      pageTitle = messages("errors.notFound.pageTitle"),
      title = messages("errors.notFound.title"),
      description = messages("errors.notFound.description")
    )
}

case class SigninLinkFailedText private(
                                            pageTitle: String,
                                            title: String,
                                            description: String)
  extends ErrorPageText

object SigninLinkFailedText {
  def apply()(implicit messages: Messages): SigninLinkFailedText =
    SigninLinkFailedText(
      pageTitle = messages("errors.signinLink.pageTitle"),
      title = messages("errors.signinLink.title"),
      description = messages("errors.signinLink.description")
    )
}

case class EmailChangeFailedText private(
                                         pageTitle: String,
                                         title: String,
                                         description: String)
  extends ErrorPageText

object EmailChangeFailedText {
  def apply()(implicit messages: Messages): EmailChangeFailedText =
    EmailChangeFailedText(
      pageTitle = messages("errors.emailChange.pageTitle"),
      title = messages("errors.emailChange.title"),
      description = messages("errors.emailChange.description")
    )
}

/** 5xx Unexpected text */
case class UnexpectedErrorPageText private(
    pageTitle: String,
    title: String,
    description: String,
    details: String)
  extends ErrorPageText

object UnexpectedErrorPageText {
  def apply(err: UnexpectedError)(implicit messages: Messages): UnexpectedErrorPageText =
    UnexpectedErrorPageText(
      pageTitle = messages("errors.unexpected.pageTitle"),
      title = messages("errors.unexpected.title", err.id),
      description = messages("errors.unexpected.description"),
      details = messages("errors.unexpected.details", err.description)
    )
}
