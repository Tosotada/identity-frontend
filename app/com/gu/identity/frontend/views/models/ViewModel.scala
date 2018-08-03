package com.gu.identity.frontend.views.models

trait ViewModel {
  def errors: Seq[ErrorViewModel] = Seq.empty
}

trait ViewModelResources {
  /**
   * Resources embedded within the Page, such as Javascript and CSS.
   *
   * Used by Views, and for calculating Content-Security-Policy.
   */
  val resources: Seq[PageResource with Product]

  /**
   * Indirectly loaded resources in the Page, that will load from another
   * source, such as images embedded in CSS.
   *
   * Used for calculating Content-Security-Policy.
   */
  val indirectResources: Seq[PageResource with Product]
}
