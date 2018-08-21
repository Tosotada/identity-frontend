package com.gu.identity.frontend.controllers

import com.gu.identity.service.client.request.UnsubscribeApiRequest
import org.scalatest.{FlatSpec, Matchers}

class UnsubscribeControllerTest extends FlatSpec with Matchers {

  "createUnsubscribeApiRequest" should "create an unsubscribe api request from the url data" in {
    UnsubscribeRequest.createUnsubscribeApiRequest("newsletter", "today-uk:100000003:1533904851", "2C76C5CAEBF1BFB5BF9A2C99C5C3ED3034698B9808BA18168788E6B1468FAF10") shouldBe
      Right(UnsubscribeApiRequest("newsletter", "today-uk", "100000003", 1533904851l, "2C76C5CAEBF1BFB5BF9A2C99C5C3ED3034698B9808BA18168788E6B1468FAF10"))
  }

  "createUnsubscribeApiRequest" should "fail to parse an invalid timestamp" in {
    UnsubscribeRequest.createUnsubscribeApiRequest("newsletter", "today-uk:100000003:1f533904851", "2C76C5CAEBF1BFB5BF9A2C99C5C3ED3034698B9808BA18168788E6B1468FAF10").isLeft shouldBe true
  }

  "createUnsubscribeApiRequest" should "fail to parse invalid data" in {
    UnsubscribeRequest.createUnsubscribeApiRequest("newsletter", "today-uk:100000003:1533904851:asdf", "2C76C5CAEBF1BFB5BF9A2C99C5C3ED3034698B9808BA18168788E6B1468FAF10").isLeft shouldBe true
  }
}
