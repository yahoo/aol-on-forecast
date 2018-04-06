/** ******************************************************************************
  * Copyright 2018, Oath Inc.
  * Licensed under the terms of the Apache Version 2.0 license.
  * See LICENSE file in project root directory for terms.
  * *******************************************************************************/

package com.aol.one.reporting.forecastapi.client

import com.fasterxml.jackson.databind.ObjectMapper
import org.mockito.Mockito
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}

class ForecastClientTest extends WordSpec with MockitoSugar with Matchers with BeforeAndAfterEach {

  private val objectMapper = new ObjectMapper()
  private val dummyValues = Array[Double](1, 2, 3)
  private val dummyResponseJson = objectMapper.writeValueAsString(ForecastResponse(dummyValues))
  private var httpClient: ForecastHttpClient = _
  private var forecastClient: ForecastClientImpl = _

  override def beforeEach: Unit = {
    httpClient = Mockito.mock(classOf[ForecastHttpClient])
    forecastClient = new ForecastClientImpl(httpClient)
  }

  "ForecastClient" should {

    "use appropriate canned set for short term daily forecast" in {
      val (request, requestJson) = buildRequest(Array[Double](1, 2, 3, 4, 5, 6, 7), 1)
      prepareResponse(requestJson, dummyResponseJson)

      val forecast = forecastClient.forecast(request.timeSeries, request.numberForecasts)
      assert(dummyValues === forecast.values)
    }

    "use appropriate canned set for long term daily forecast" in {
      val (request, requestJson) = buildRequest(Array[Double](1, 2, 3, 4, 5, 6, 7, 8), 20)
      prepareResponse(requestJson, dummyResponseJson)

      val forecast = forecastClient.forecast(request.timeSeries, request.numberForecasts)
      assert(dummyValues === forecast.values)
    }

    "sets lowest confidence and zero forecast on empty historical data" in {
      val (request, requestJson) = buildRequest(Array[Double](), 3)
      prepareResponse(requestJson, dummyResponseJson)

      val forecast = forecastClient.forecast(request.timeSeries, request.numberForecasts)

      assert(Double.MaxValue === forecast.confidence)
      assert(List[Double](0, 0, 0) === forecast.values)
    }

    "sets lowest confidence on insufficient historical data" in {
      val (request, requestJson) = buildRequest(Array[Double](1, 2), 3)
      prepareResponse(requestJson, dummyResponseJson)

      val forecast = forecastClient.forecast(request.timeSeries, request.numberForecasts)

      assert(Double.MaxValue === forecast.confidence)
      assert(dummyValues === forecast.values)
    }

    "calculates correct confidence on sufficient historical data" in {
      val hist = Array[Double](1, 1, 2, 2, 0, 0, 0, 1, 1, 2, 2, 0, 0, 0)
      val forecastValues = Array[Double](1, 1, 1, 1, 1, 1, 1)

      // mock confidence call
      val (_, confidenceRequestJson) = buildRequest(hist.take(7), 7)
      val (_, confidenceResponseJson) = buildResponse(forecastValues)
      prepareResponse(confidenceRequestJson, confidenceResponseJson)

      // mock forecast call
      val (forecastRequest, forecastRequestJson) = buildRequest(hist, 7)
      val (_, forecastResponseJson) = buildResponse(forecastValues)
      prepareResponse(forecastRequestJson, forecastResponseJson)

      val forecast = forecastClient.forecast(forecastRequest.timeSeries, forecastRequest.numberForecasts)

      val error = List(0, 0, 0.5, 0.5, 0, 0, 0)
      val expectedError = error.sum * 100.0 / error.length // -> error(hist - forecastValues) for the first 7 entries
      assert(math.abs(forecast.confidence - expectedError) < 0.0001)
    }
  }

  private def buildRequest(historical: Array[Double], horizon: Int) = {
    val request = new ForecastRequest(historical, horizon, ForecastParams.CannedSet)
    (request, objectMapper.writeValueAsString(request))
  }

  private def buildResponse(forecast: Array[Double]) = {
    val response = ForecastResponse(forecast)
    (response, objectMapper.writeValueAsString(response))
  }

  private def prepareResponse(request: String, response: String) = Mockito.when(httpClient.get(request)).thenReturn(response)
}
