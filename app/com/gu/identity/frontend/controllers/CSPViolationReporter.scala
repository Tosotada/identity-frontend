package com.gu.identity.frontend.controllers

import com.gu.identity.frontend.logging.Logging
import play.api.mvc.{AbstractController, ControllerComponents}


class CSPViolationReporter(cc: ControllerComponents) extends AbstractController(cc) with Logging{

  def cspReport() = Action(parse.tolerantText) { implicit request =>

    val userAgent = request.headers.get("User-Agent")
    val report = request.body

    logger.error(s"Content Security Violation Error: User Agent: ${userAgent}.  Report: ${report}.")

    Ok("Content Security Policy Violation Logged.")
  }

}
