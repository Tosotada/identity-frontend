package com.gu.identity.frontend.filters

import akka.stream.Materializer
import com.googlecode.htmlcompressor.compressor.HtmlCompressor
import com.mohiva.play.htmlcompressor.{HTMLCompressorFilter => MohivaHTMLCompressorFilter}
import play.api.{Configuration, Environment, Mode}

/**
 * Adapter for HtmlCompressorFilter which compresses output of HTML responses only.
 *
 * @see https://github.com/mohiva/play-html-compressor
 */
final case class HtmlCompressorFilter(
    val configuration: Configuration,
    val environment: Environment,
    val mat: Materializer) extends MohivaHTMLCompressorFilter {

  val compressor: HtmlCompressor = {
    val c = new HtmlCompressor()

    if (environment.mode == Mode.Dev) {
      c.setPreserveLineBreaks(true)
    }

    c.setRemoveComments(true)
    c.setRemoveIntertagSpaces(false)

    // Remove https from protocol defs only - assume running under https
    c.setRemoveHttpProtocol(false)
    c.setRemoveHttpsProtocol(true)

    c
  }
}
