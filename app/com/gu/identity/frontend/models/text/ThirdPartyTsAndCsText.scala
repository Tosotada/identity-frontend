package com.gu.identity.frontend.models.text

import java.net.URI

import com.gu.identity.frontend.models.{GroupCode, GuardianJobs, GuardianTeachersNetwork}
import play.api.i18n.Messages

case class BaseTsAndCsText private(
    title: String,
    explanationText: String,
    continueButtonText: String,
    termsText: String,
    termsOfServiceLinkText: String,
    privacyPolicyLinkText: String)

object BaseTsAndCsText {
  def apply(title: String, serviceName: String)(implicit messages: Messages): BaseTsAndCsText = {
    BaseTsAndCsText(
      title = messages("thirdPartyTerms.title", title),
      explanationText = messages("thirdPartyTerms.explanation", title),
      continueButtonText = messages("thirdPartyTerms.continueButton"),
      termsText = messages("thirdPartyTerms.terms", serviceName),
      termsOfServiceLinkText = messages("thirdPartyTerms.termsOfService"),
      privacyPolicyLinkText = messages("thirdPartyTerms.privacyPolicy")
    )
  }
}

case class ThirdPartyTsAndCsText (
    pageTitle: String,
    title: String,
    featureIntro: Option[String] = None,
    featureOutro: Option[String] = None,
    features: Seq[String],
    serviceName: String,
    terms: GroupOnlyTermsText,
    baseText: BaseTsAndCsText)

object TeachersTsAndCsText {
  def apply(signOutLink: URI)(implicit messages: Messages): ThirdPartyTsAndCsText = {
    val title = messages("thirdPartyTerms.teachersTitle").replace(' ', '\u00A0')
    val serviceName = messages("thirdPartyTerms.teachersServiceName")
    ThirdPartyTsAndCsText(
      pageTitle = messages("thirdPartyTerms.teachersPageTitle"),
      title = title,
      featureIntro = Some(messages("thirdPartyTerms.teachersFeatureIntro")),
      featureOutro = Some(messages("thirdPartyTerms.teachersFeatureOutro", signOutLink)),
      features = Seq(
        messages("thirdPartyTerms.teachersFeatures1"),
        messages("thirdPartyTerms.teachersFeatures2"),
        messages("thirdPartyTerms.teachersFeatures3")
      ),
      serviceName = serviceName,
      terms = GroupOnlyTermsText(
        termsUrl = "https://teachers.theguardian.com/guardian-teacher-network-terms-and-conditions",
        privacyUrl = "https://teachers.theguardian.com/guardian-teacher-network-privacy-policy",
        group = GuardianTeachersNetwork
      ),
      baseText = BaseTsAndCsText(title, serviceName)
    )
  }
}

object JobsTsAndCsText {
  def apply(signOutLink: URI)(implicit messages: Messages): ThirdPartyTsAndCsText = {
    val title = messages("thirdPartyTerms.jobsTitle")
    val serviceName = messages("thirdPartyTerms.jobsServiceName")
    ThirdPartyTsAndCsText(
      pageTitle = messages("thirdPartyTerms.jobsPageTitle"),
      title = title,
      featureIntro = Some(messages("thirdPartyTerms.jobsFeatureIntro")),
      featureOutro = Some(messages("thirdPartyTerms.jobsFeatureOutro", signOutLink)),
      features = Seq(
        messages("thirdPartyTerms.jobsFeature1"),
        messages("thirdPartyTerms.jobsFeature2"),
        messages("thirdPartyTerms.jobsFeature3")),
      serviceName = serviceName,
      terms = GroupOnlyTermsText(
        termsUrl = "https://jobs.theguardian.com/terms-and-conditions/",
        privacyUrl = "https://jobs.theguardian.com/privacy-policy/",
        group = GuardianJobs
      ),
      baseText = BaseTsAndCsText(title, serviceName)
    )
  }
}

object TsAndCsPageText {
  def getPageText(group: GroupCode, signOutLink: URI)(implicit messages: Messages): ThirdPartyTsAndCsText = {
    group match {
      case GuardianTeachersNetwork => TeachersTsAndCsText(signOutLink)
      case GuardianJobs => JobsTsAndCsText(signOutLink)
    }
  }
}
