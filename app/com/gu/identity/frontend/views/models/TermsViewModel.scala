package com.gu.identity.frontend.views.models

import com.gu.identity.frontend.models.{GroupCode, GuardianJobs, GuardianTeachersNetwork}
import com.gu.identity.frontend.models.text.{GroupTermsText, TermsText}
import play.api.i18n.Messages

sealed trait TermsViewModel{
  def conditionsText: String
  def privacyText: String
}

case class BasicTermsViewModel private(
  conditionsText: String,
  privacyText: String) extends ViewModel with TermsViewModel

case class TeachersTermsViewModel private(
  conditionsText: String,
  privacyText: String) extends ViewModel with TermsViewModel

case class JobsTermsViewModel private(
  conditionsText: String,
  privacyText: String) extends ViewModel with TermsViewModel

object BasicTermsViewModel {
  def apply()(implicit messages: Messages): BasicTermsViewModel = {
    val text = TermsText()
    BasicTermsViewModel(
      conditionsText = text.conditionsText,
      privacyText = text.privacyText
    )
  }
}

object TeachersTermsViewModel {
  def apply()(implicit messages: Messages): TeachersTermsViewModel = {
    val text = GroupTermsText(
      termsUrl = "https://teachers.theguardian.com/guardian-teacher-network-terms-and-conditions",
      privacyUrl = "https://teachers.theguardian.com/guardian-teacher-network-privacy-policy",
      group = GuardianTeachersNetwork
    )
    TeachersTermsViewModel(
      conditionsText = text.conditionsText,
      privacyText = text.privacyText
    )
  }
}

object JobsTermsViewModel {
  def apply()(implicit messages: Messages): JobsTermsViewModel = {
    val text = GroupTermsText(
      termsUrl = "https://jobs.theguardian.com/terms-and-conditions/",
      privacyUrl = "https://jobs.theguardian.com/privacy-policy/",
      group = GuardianJobs
    )
    JobsTermsViewModel(
      conditionsText = text.conditionsText,
      privacyText = text.privacyText
    )
  }
}

object Terms {
  def getTermsModel(group: Option[GroupCode])(implicit messages: Messages): TermsViewModel = {
    group match {
      case Some(GuardianTeachersNetwork) => TeachersTermsViewModel()
      case Some(GuardianJobs) => JobsTermsViewModel()
      case _ => BasicTermsViewModel()
    }
  }
}
