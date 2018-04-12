/** ******************************************************************************
  * Copyright 2018, Oath Inc.
  * Licensed under the terms of the Apache Version 2.0 license.
  * See LICENSE file in project root directory for terms.
  * *******************************************************************************/

package com.aol.one.reporting.forecastapi.client

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}

trait ForecastClient {

  /**
    * Forecast data
    *
    * @param historical - historical data to base forecasts on
    * @param horizon    - number of data points to forecast into the future
    * @return forecasts and confidence level
    */
  def forecast(historical: Array[Double], horizon: Int): Forecast
}

case class Forecast(values: Array[Double], confidence: Double)


/** Forecast client
  *
  * @see http://service-location:port/forecast-api/doc/OverviewIFS.2015.pdf
  */
class ForecastClientImpl(client: ForecastHttpClient) extends ForecastClient {

  def this(serviceUrl: String) = this(new ForecastHttpClientImpl(serviceUrl))

  private val objectMapper = new ObjectMapper()
  objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

  override def forecast(historical: Array[Double], horizon: Int): Forecast = {
    if (historical.isEmpty) {
      Forecast(Array.fill(horizon)(0.0), Double.MaxValue)
    } else {
      val c = confidence(historical)
      val forecast = forecastInternal(historical, horizon)
      Forecast(forecast, c)
    }
  }

  private def forecastInternal(historical: Array[Double], horizon: Int): Array[Double] = {
    val request = new ForecastRequest(historical, horizon, ForecastParams.CannedSet)
    val requestJson = objectMapper.writeValueAsString(request)
    val response = client.get(requestJson)
    val forecastResponse = objectMapper.readValue(response, classOf[ForecastResponse])
    forecastResponse.forecast
  }

  private def confidence(historical: Array[Double]): Double = {
    val cHistorical = historical.take(historical.length - ForecastParams.ConfidenceHorizon)
    if (cHistorical.length < ForecastParams.MinConfidenceHistorical) {
      Double.MaxValue
    } else {
      val actual = historical.takeRight(ForecastParams.ConfidenceHorizon)
      val forecast = forecastInternal(cHistorical, ForecastParams.ConfidenceHorizon)
      val errorValues = (actual.map(math.max(_, 1)), forecast).zipped.map((a, f) => math.abs(a - f) / a)
      (errorValues.sum / errorValues.length) * 100
    }
  }
}
