package com.gu.identity.frontend.controllers

import buildinfo.BuildInfo
import play.api.mvc.{AbstractController, ControllerComponents}

class Manifest(cc: ControllerComponents) extends AbstractController(cc) {
  def manifest = Action {
    val data = Map(
      "Build" -> BuildInfo.buildNumber,
      "Commit" -> BuildInfo.gitCommitId
    )

    Ok(data map { case (k, v) => s"$k: $v"} mkString "\n")
  }
}
