/** ******************************************************************************
  * Copyright 2018, Oath Inc.
  * Licensed under the terms of the Apache Version 2.0 license.
  * See LICENSE file in project root directory for terms.
  * *******************************************************************************/

package com.aol.one.reporting.forecastapi.client

import com.typesafe.config.{Config, ConfigFactory}

object ConfigUtil {

  def getConfigFile(env: String): String =  Option(env).filterNot(_.isEmpty).getOrElse("dev").toLowerCase()

  def getConfig(): Config = ConfigFactory.load(getConfigFile(sys.env("AOL_ENVIRONMENT")))
}
