package com.gu.identity.frontend.models

/**
 * Defines a SignInType for determining where to point back links to.
 */

sealed trait EmailProvider extends Product2[EmailProvider, String] {
  self =>

  val id: String
  val name: String
  val inboxLink: String
  val matches: Seq[String]

  def _1 = self
  def _2 = id
}

case object GmailEmailProvider extends EmailProvider {
  val id = "gmail"
  val name = "Gmail"
  val matches = Seq("@gmail.","@googlemail.")
  val inboxLink = "http://mail.google.com/mail/"
}

case object YahooEmailProvider extends EmailProvider {
  val id = "yahoo"
  val name = "Yahoo!"
  val matches = Seq("@yahoo.","@yahoomail.")
  val inboxLink = "https://mail.yahoo.com/"
}

case object OutlookEmailProvider extends EmailProvider {
  val id = "outlook"
  val name = "Outlook.com"
  val matches = Seq("@outlook.","@live.","@msn.","@hotmail.")
  val inboxLink = "https://outlook.live.com/"
}

object EmailProvider {
  def all: Seq[EmailProvider] = Seq(GmailEmailProvider, YahooEmailProvider, OutlookEmailProvider)

  def apply(emailProvider: Option[String]): Option[EmailProvider] =
    all.find(_.id == emailProvider.getOrElse(""))

  def getProviderForEmail(email: String): Option[EmailProvider] =
    all.find(_.matches.exists(email contains _))
}
