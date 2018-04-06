/** ******************************************************************************
  * Copyright 2018, Oath Inc.
  * Licensed under the terms of the Apache Version 2.0 license.
  * See LICENSE file in project root directory for terms.
  * *******************************************************************************/

package com.aol.one.reporting.forecastapi.client

import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}

class ConfigUtilTest extends WordSpec with MockitoSugar with Matchers with BeforeAndAfterEach {

  "ConfigUtil" should {

    "getConfig selects correct environment variable" in {
      assert("dev" === ConfigUtil.getConfigFile("DEV"))
      assert("prod" === ConfigUtil.getConfigFile("prod"))
      assert("autotests" === ConfigUtil.getConfigFile("autotests"))
      assert("stage" === ConfigUtil.getConfigFile("STAGE"))

      assert("dev" === ConfigUtil.getConfigFile(null))
      assert("dev" === ConfigUtil.getConfigFile(""))
    }
  }
}
