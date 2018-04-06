/** ******************************************************************************
  * Copyright 2018, Oath Inc.
  * Licensed under the terms of the Apache Version 2.0 license.
  * See LICENSE file in project root directory for terms.
  * *******************************************************************************/

package com.aol.one.reporting.forecastapi.client

import com.fasterxml.jackson.annotation.JsonProperty

import scala.beans.BeanProperty

case class ForecastRequest(@BeanProperty @JsonProperty("timeSeries") timeSeries: Array[Double],
                           @BeanProperty @JsonProperty("spikeFilterWindow") spikeFilterWindow: Int,
                           @BeanProperty @JsonProperty("cannedSets") cannedSets: Array[String],
                           @BeanProperty @JsonProperty("numberHoldBack") numberHoldBack: Int,
                           @BeanProperty @JsonProperty("numberForecasts") numberForecasts: Int,
                           @BeanProperty @JsonProperty("massageForecast") massageForecast: Boolean = true) {

  def this(timeSeries: Array[Double], numberForecasts: Int, cannedSets: Array[String]) = {
    this(timeSeries, ForecastParams.SpikeFilteringWindow, cannedSets, timeSeries.length, numberForecasts)
  }
}
