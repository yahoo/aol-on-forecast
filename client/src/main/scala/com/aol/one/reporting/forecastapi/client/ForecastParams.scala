/** ******************************************************************************
  * Copyright 2018, Oath Inc.
  * Licensed under the terms of the Apache Version 2.0 license.
  * See LICENSE file in project root directory for terms.
  * *******************************************************************************/

package com.aol.one.reporting.forecastapi.client

object ForecastParams {

  val CannedSet = Array(
    "RW-NONE-DAY",
    "RW-NONE-DAY-WEEK",
    "REG-NONE-ADD-DAY",
    "REG-LINEAR-ADD-DAY",
    "REG-LINEAR-ADD-DAY-WEEK",
    "REG-LINEAR-ADD-DAY-LOG",
    "REG-LINEAR-ADD-DAY-WEEK-LOG",
    "AR-NONE-DEMEAN-WEEK",
    "RW-NONE-WEEK",
    "REG-NONE-ADD-WEEK-SAG",
    "ARIMA-0,2,2-0,1,1s-WEEK",
    "EXP-LINEAR-MULT-YEAR",
    "REG-LINEAR-ADD-YEAR",
    "REG-NONE-PHASE2-WEEK-YEAR"
  )

  /**
    * Until confidence is implemented by IFS we use the most recent 7 days to work out confidence by ourselves.
    */
  val ConfidenceHorizon = 7

  /**
    * We need at least 2 points to fit a line and at least 3 points for basic exponential smoothing
    * If historical data is insufficient we can still make a forecast but we set confidence level to most unreliable.
    */
  val MinConfidenceHistorical = 3

  /**
    * Enable spike filtering with the specified clipping window size.
    * The minimum size is 3 and the maximum is 30.
    * The clipping window size is used to define a localized moving average that is used as the basis for identifying outliers as
    * well as defining the value to use instead.
    */
  val SpikeFilteringWindow = 4
}
