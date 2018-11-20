package com.gu.identity.frontend.authentication


object CookieName extends Enumeration {

  type Name = Value
  val GU_U, SC_GU_U, SC_GU_LA, GU_SO, GU_ID_CSRF, GU_PROFILE_CSRF, gu_user_features_expiry, gu_paying_member, gu_recurring_contributor, gu_digital_subscriber, GU_SIGNIN_EMAIL, SC_GU_RP = Value

  val SECURE_COOKIE_PREFIX = "SC"
  def isHttpOnly(cookieName: String) = cookieName.startsWith(SECURE_COOKIE_PREFIX)


}
