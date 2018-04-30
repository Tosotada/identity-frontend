package com.gu.identity.frontend.test

import play.api.ApplicationLoader.Context
import play.api.routing.Router
import play.api.{Application, ApplicationLoader, BuiltInComponentsFromContext, Environment}
import play.filters.HttpFiltersComponents


/**
 * Loads a Simple FakeApplication for use with tests.
 *
 * Required as play.api.test.FakeApplication uses the Guice DI Injector
 * which fails with our app as we use Compile time (manual) injection.
 */
object SimpleFakeApplication {
  def apply(): Application = {
    val env = Environment.simple()
    val context = ApplicationLoader.createContext(env)

    SimpleFakeApplicationLoader.load(context)
  }
}


object SimpleFakeApplicationLoader extends ApplicationLoader {
  override def load(context: Context): Application =
    new ApplicationComponents(context).application

  private class ApplicationComponents(context: Context)
      extends BuiltInComponentsFromContext(context)
        with HttpFiltersComponents {

    override lazy val router = Router.empty
  }
}
