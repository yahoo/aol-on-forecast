/** ******************************************************************************
  * Copyright 2018, Oath Inc.
  * Licensed under the terms of the Apache Version 2.0 license.
  * See LICENSE file in project root directory for terms.
  * *******************************************************************************/

package com.aol.one.reporting.forecastapi.client

import com.typesafe.config.Config
import org.slf4j.LoggerFactory

import scalaj.http._

trait ForecastHttpClient {
  def get(request: String): String
}

class ForecastHttpClientImpl(serviceUrl: String) extends ForecastHttpClient {

  private lazy val logger = LoggerFactory.getLogger(this.getClass.getSimpleName)
  println(s"Forecast client using service url $serviceUrl")
  private val conf = ConfigUtil.getConfig().getConfig("http")
  private val requestPart = getRequest(conf)
  private val maxRetry = conf.getInt("max-retry")

  def get(request: String): String = _retry(maxRetry)(getResponse(request))

  private def getResponse(request: String): String = {
    val response = requestPart.postData(request).asString
    if (response.code != 200) {
      throw new RuntimeException(s"Server response was not 200 (SC_OK) response=" + response.code + "body=" + response.body)
    }
    response.body
  }

  private def _retry[String](n: Int)(fn: => String): String = {
    try {
      fn
    } catch {
      case e: RuntimeException =>
        if (n > 1) {
          logger.warn(s"Call to forecast service failed, retrying ($n) $e")
          _retry(n - 1)(fn)
        }
        else throw e
    }
  }

  private def getRequest(conf: Config): HttpRequest = {
    Http(serviceUrl)
      .header("Content-Type", "application/json")
      .timeout(conf.getInt("conn-timeout") * 1000, conf.getInt("read-timeout") * 1000)
  }
}
