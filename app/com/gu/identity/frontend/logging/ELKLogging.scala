package com.gu.identity.frontend.logging

import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.logging.KinesisAppenderConfig
import com.typesafe.scalalogging.LazyLogging

class ELKLogging (config: Configuration) extends LazyLogging {

  logger.info("Adding Kinesis logger")
  try {
    config.kinesisStream match {
      case Some(stream) =>
        com.gu.identity.logging.LogStash.init(KinesisAppenderConfig(stream, "identity"))
        logger.info("Kinesis logger added")
      case None => logger.error("Kinesis logging stream name not present in configuration")
    }
  } catch {
    case e: Exception =>
      logger.error("Kinesis logging stream not correctly configured", e)
  }
}
