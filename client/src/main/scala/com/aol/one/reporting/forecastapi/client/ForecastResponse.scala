/** ******************************************************************************
  * Copyright 2018, Oath Inc.
  * Licensed under the terms of the Apache Version 2.0 license.
  * See LICENSE file in project root directory for terms.
  * *******************************************************************************/

package com.aol.one.reporting.forecastapi.client

import com.fasterxml.jackson.annotation.JsonProperty

import scala.beans.BeanProperty

case class ForecastResponse(@BeanProperty @JsonProperty("forecast") forecast: Array[Double])
