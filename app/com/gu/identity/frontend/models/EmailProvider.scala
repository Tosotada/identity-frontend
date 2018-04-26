package com.gu.identity.frontend.models

/**
 * Defines a SignInType for determining where to point back links to.
 */

sealed trait EmailProvider {
  val id: String
  val name: String
  val inboxLink: String
  val matches: Seq[String]
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

case object BtEmailProvider extends EmailProvider {
  val id = "bt"
  val name = "BT Mail"
  val matches = Seq("@btinternet.")
  val inboxLink = "https://btmail.bt.com/cp/ps/main/index#mail"
}

case object AolEmailProvider extends EmailProvider {
  val id = "bt"
  val name = "BT Mail"
  val matches = Seq("@aol.")
  val inboxLink = "https://mail.aol.com/"
}

case object OutlookEmailProvider extends EmailProvider {
  val id = "outlook"
  val name = "Outlook.com"
  val matches = Seq("@outlook.","@live.","@msn.","@hotmail.")
  val inboxLink = "https://outlook.live.com/"
}

object EmailProvider {
  def all: Seq[EmailProvider] = Seq(GmailEmailProvider, YahooEmailProvider, OutlookEmailProvider, BtEmailProvider, AolEmailProvider)

  def apply(emailProvider: Option[String]): Option[EmailProvider] =
    all.find(_.id == emailProvider.getOrElse(""))

  def getProviderForEmail(email: String): Option[EmailProvider] =
    all.find(_.matches.exists(email contains _))
}
