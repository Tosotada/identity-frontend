package test.pages

import test.util.{LoadablePage, Browser, Config}

class RegisterConfirm extends LoadablePage with Browser {
  val url = s"${Config.baseUrl}/register/confirm?returnUrl=${Config.baseUrl}/test-return-test"

  def isCompleteRegistration(): Boolean = pageHasUrl("complete-registration")

  def hasPassedReturnUrl(): Boolean = pageHasUrl("test-return-test")

  override def hasLoaded(): Boolean = true
}
