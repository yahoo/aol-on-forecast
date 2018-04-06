/** ******************************************************************************
  * Copyright 2018, Oath Inc.
  * Licensed under the terms of the Apache Version 2.0 license.
  * See LICENSE file in project root directory for terms.
  * *******************************************************************************/

package com.aol.one.reporting.forecastapi.client

import com.aol.one.reporting.forecastapi.client.ITTestUtil._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, FreeSpec, Matchers}

class ForecastClientIntegrationTest extends FreeSpec with MockitoSugar with Matchers with BeforeAndAfterEach {

  "ForecastClient.forecast weekly" - {

    "forecast weekly seasonal data with high confidence" in {
      testForecast("weekly-seasonal")
    }

    "forecast weekly trend data with high confidence" in {
      testForecast("weekly-trend")
    }

    "forecast weekly seasonal + trend data with high confidence" in {
      testForecast("weekly-seasonal-with-trend")
    }

    "forecast weekly random data with low confidence" in {
      testForecast("weekly-random")
    }
  }

  "ForecastClient.forecast 2 weeks" - {

    "forecast weekly seasonal data with high confidence" in {
      testForecast("two-weeks-seasonal")
    }
  }

  "ForecastClient.forecast daily" - {
    "forecast daily seasonal data with high confidence" in {
      testForecast("daily-seasonal")
    }

    "forecast daily seasonal + trend data with high confidence" in {
      testForecast("daily-seasonal-with-trend")
    }
  }

  "ForecastClient.forecast yearly" - {
    "forecast yearly seasonal data with high confidence" in {
      testForecast("yearly-seasonal")
    }

    "forecast yearly seasonal + trend data with high confidence" in {
      testForecast("yearly-seasonal-with-trend")
    }

    "forecast weekly and yearly seasonal + trend data with high confidence" in {
      testForecast("weekly-yearly-seasonal-with-trend")
    }

    "forecast supply forecast with trend" in {
      testForecast("real-data-video-view-supply-with-trend")
    }
  }

  "ForecastClient.forecast edge case" - {
    "forecast insufficient data with low confidence" in {
      testForecast("insufficient-data")
    }

    "forecast sudden drop with non negative forecast and low confidence" in {
      testForecast("sudden-drop")
    }
  }
}

