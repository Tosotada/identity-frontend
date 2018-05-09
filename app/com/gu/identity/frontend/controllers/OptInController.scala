package com.gu.identity.frontend.controllers

import play.api.mvc._

import scala.concurrent.duration._

/*
 * Opting in a flag: /opt/in/name-of-the-flag
 * Opting out a flag: /opt/out/name-of-the-flag
 * Deleting the cookie for a given flag: /opt/delete/name-of-the-flag
 * Delete all cookies: /opt/reset
 */

class OptInController(cc: ControllerComponents) extends AbstractController(cc) {

  private val lifetime: Int = 90.days.toSeconds.toInt

  private def opt(feature: String, choice: String): Result = choice match {
    case "in" => optIn(feature)
    case "out" => optOut(feature)
    case "delete" => optDelete(feature)
  }
  def optIn(cookieName: String): Result = SeeOther("/").withCookies(Cookie(cookieName, "true", maxAge = Some(lifetime)))
  def optOut(cookieName: String): Result = SeeOther("/").discardingCookies(DiscardingCookie(cookieName))
  def optDelete(cookieName: String): Result = SeeOther("/").discardingCookies(DiscardingCookie(cookieName))

  def reset(): Action[AnyContent] = Action { implicit request =>
    val discardingCookies = com.gu.identity.frontend.experiments.ActiveExperiments.allExperiments.map(experiment => DiscardingCookie(experiment.headerName())).toSeq
    SeeOther("/").discardingCookies(discardingCookies:_*)
  }

  def handle(feature: String, choice: String): Action[AnyContent] = Action { implicit request =>
    com.gu.identity.frontend.experiments.ActiveExperiments.allExperiments
      .find(_.name == feature)
      .map(experiment => opt(experiment.headerName(), choice))
      .getOrElse(NotFound)
  }
}
