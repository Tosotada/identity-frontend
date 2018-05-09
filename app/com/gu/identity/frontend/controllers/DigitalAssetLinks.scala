package com.gu.identity.frontend.controllers

import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.models._
import play.api.mvc._
import play.api.libs.json._
import com.gu.identity.frontend.models.DigitalAsset._

class DigitalAssetLinks(
    val configuration: Configuration,
    cc: ControllerComponents)
  extends AbstractController(cc) {

  val loginPermission = "delegate_permission/common.get_login_creds"

  def links = Action {

    val webTarget = WebTarget("web", configuration.identityProfileBaseUrl)
    val webAsset = WebAsset(List(loginPermission), webTarget)

    val appTarget = AppTarget("android_app", "com.guardian", List("49:65:42:9E:AD:99:25:1C:84:C2:D5:57:D4:E6:F3:6C:7B:C3:B3:5C:1E:A1:8E:19:96:BC:CA:13:E5:5A:9E:7D"))
    val appAsset = AppAsset(List("delegate_permission/common.handle_all_urls", loginPermission), appTarget)

    val json = JsArray(Seq(Json.toJson((webAsset)))) ++ JsArray(Seq(Json.toJson((appAsset))))
    Ok(json)
  }
}

