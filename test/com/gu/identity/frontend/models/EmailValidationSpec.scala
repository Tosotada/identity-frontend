package com.gu.identity.frontend.models

import com.gu.identity.frontend.request.RegisterActionRequestBodyFormMapping.dotlessDomainEmail
import org.scalatest.{AppendedClues, FlatSpec, Matchers}
import play.api.data.Forms._
import play.api.data._

class EmailValidationSpec extends FlatSpec with Matchers with AppendedClues{
  it should "correctly detect valid and invalid email addresses" in {
    /*
      The regex used to validate email addresses is based on the one used by WebKit for html email validation
      and comes from here: https://html.spec.whatwg.org/#valid-e-mail-address
      However it has the additional constraint that the domain must not be dotless

      There are a few patterns which it doesn't match correctly, but these are the same as all the
      major browsers so do not represent a regression.

      Unsupported valid patterns:
        email@[123.123.123.123] //Square bracket around IP address is considered valid
        "email"@domain.com //Quotes around email is considered valid

      Unsupported Invalid patterns;
        email..email@domain.com //Multiple dots in the first part of the email
        email@domain.web //Invalid top level domains
        email@111.222.333.44444 //Invalid IP format
    */

    val valid = List(
      "email@domain.com", //Valid email
      "firstname.lastname@domain.com", //Email contains dot in the address field
      "email@subdomain.domain.com", //Email contains dot with subdomain
      "firstname+lastname@domain.com", //Plus sign is considered valid character
      "email@123.123.123.123", //Domain is valid IP address
      "1234567890@domain.com", //Digits in address are valid
      "email@domain-one.com", //Dash in domain name is valid
      "_______@domain.com", //Underscore in the address field is valid
      "email@domain.name", //.name is valid Top Level Domain name
      "email@domain.co.jp", //Dot in Top Level Domain name also considered valid (use co.jp as example here)
      "te44st@gmail.com",
      "f@ggg.fm",
      "email'withapostrophe@gmail.com",
      "firstname-lastname@domain.com") //Dash in address field is valid

    val invalid = List(
      "blah@gmail", //dotless domain
      "plainaddress", //Missing @ sign and domain
      "#@%^%#$@#$@#.com", //Garbage
      "@domain.com", //Missing username
      "Joe Smith <email@domain.com>", //Encoded html within email is invalid
      "emailwith’smartquote@gmail.com", //Smart quotes are invalid
      "email.domain.com", //Missing @
      "email@domain@domain.com", //Two @ sign
      ".email@domain.com", //Leading dot in address is not allowed
      "email.@domain.com", //Trailing dot in address is not allowed
      "あいうえお@domain.com", //Unicode char as address
      "email@domain.com (Joe Smith)", //Text followed email is not allowed
      "email@domain", //Missing top level domain (.com/.net/.org/etc)
      "email@-domain.com", //Leading dash in front of domain is invalid
      "email@domain..com") //Multiple dot in the domain portion is invalid

    val singleForm = Form(
      single(
        "email" -> dotlessDomainEmail
      )
    )

    valid.foreach {
      email =>
        singleForm.bind(Map("email" -> email)).errors shouldBe Nil withClue s"$email failed to validate but is a valid email"
    }

    invalid.foreach {
      email =>
        singleForm.bind(Map("email" -> email)).errors.nonEmpty shouldBe true withClue s"$email validated but is an invalid email"
    }

  }
}
