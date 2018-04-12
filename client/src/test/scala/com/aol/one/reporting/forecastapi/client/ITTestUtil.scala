/** ******************************************************************************
  * Copyright 2018, Oath Inc.
  * Licensed under the terms of the Apache Version 2.0 license.
  * See LICENSE file in project root directory for terms.
  * *******************************************************************************/

package com.aol.one.reporting.forecastapi.client

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import org.scalatest.FreeSpec

object ITTestUtil extends FreeSpec {

  private val forecastClient = new ForecastClientImpl(serviceUrl)
  private val plotActual = ConfigUtil.getConfig().getBoolean("plot-actual")

  def testForecast(scenarioId: String, plotIfEnabled: Boolean = true): Unit = {
    val scenario = getTestCase(scenarioId + "/test.json")

    val result = forecastClient.forecast(scenario.timeSeries, scenario.horizon)

    // empty forecast in test files signals not to check forecasts for the test case
    if (!scenario.expectedForecast.isEmpty) {
      assertForecast(scenario.expectedForecast, result.values, scenario.allowedError)
      if (plotIfEnabled && plotActual) {
        plotForecast(scenarioId, scenario, result)
      }
    }
    assertConfidence(scenario.expectedConfidence, result.confidence)
  }

  private def assertConfidence(expected: Double, actual: Double) = {
    // confidence of -1 in the test files signals high confidence value
    if (expected == -1.0) {
      assert(actual > 50.0)
    } else {
      assert(actual <= expected)
    }
  }

  private def assertForecast(expected: Array[Double], actual: Array[Double], allowedError: Double) = {
    org.junit.Assert.assertEquals(expected.length, actual.length)
    (expected, actual).zipped.foreach((e, a) => {
      val forecastError = 100 * math.abs(e - a) / math.max(a, 0.00001)
      try {
        assertDouble(forecastError, 0.0, allowedError)
      } catch {
        case ex: AssertionError =>
          throw new AssertionError(s"Expected: $e actual: $a allowed error: $allowedError"
            + "\nExpected full: " + expected.map(_.toInt).mkString(",")
            + "\nActual full: " + actual.map(_.toInt).mkString(","), ex)
      }
    })
  }

  private def assertDouble(expected: Double, actual: Double, threshold: Double): Unit = {
    org.junit.Assert.assertEquals(expected, actual, threshold)
  }

  private def getResourceFile(file: String) = getClass.getClassLoader.getResourceAsStream(file)

  private def getTestCase(testCaseFile: String): TestCase = {
    new ObjectMapper().readValue(getResourceFile("forecast-client/" + testCaseFile), classOf[TestCase])
  }

  private def plotForecast(scenarioId: String, scenario: TestCase, forecast: Forecast) = {
    val historical = scenario.timeSeries.mkString(",")
    val actual = forecast.values.mkString(",")
    sys.process.Process(Seq("ls"), new java.io.File("./src/test/resources/scripts")).!!

    sys.process.Process(Seq("python", "plot_results.py", scenarioId, historical, actual), new java.io.File("./src/test/resources/scripts")).!!
  }

  private def serviceUrl = sys.env("FORECAST_API_SERVICE_URL")

  case class TestCase(@JsonProperty("timeSeries") timeSeries: Array[Double],
                      @JsonProperty("horizon") horizon: Int,
                      @JsonProperty("seasonality") seasonality: String,
                      @JsonProperty("allowForecastPercentError") allowedError: Double,
                      @JsonProperty("expectedForecast") expectedForecast: Array[Double],
                      @JsonProperty("expectedConfidence") expectedConfidence: Double)

}
