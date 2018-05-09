package com.gu.identity.frontend.controllers

import play.api.mvc.{AbstractController, ControllerComponents}

class HealthCheck(cc: ControllerComponents) extends AbstractController(cc) {

  def healthCheck = Action {
    Ok("200 OK")
  }
}
