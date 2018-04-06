/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/
package com.aol.one.reporting.forecastapi.server.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

import java.util.Arrays;

@ApiModel(value = "Forecast response object including selected CannedSet and time to respond")
public class ForecastResponse {

    @ApiModelProperty(value = "Real numbers from nearest to farthest", required = true)
    private double[] forecast;

    @ApiModelProperty(value = "Name of canned set used to produce forecast")
    private String selectedCannedSet;

    @ApiModelProperty(value = "Number of milliseconds to to produce forecast", required = true)
    private long time;

    public ForecastResponse() {

    }


    public ForecastResponse(
            @JsonProperty("forecast") double[] forecast,
            @JsonProperty("selectedCannedSet") String selectedCannedSet,
            @JsonProperty("time") long time
    ) {
        this.forecast = forecast;
        this.selectedCannedSet = selectedCannedSet;
        this.time = time;
    }

    public double[] getForecast() {
        return forecast;
    }

    public void setForecast(double[] forecast) {
        this.forecast = forecast;
    }


    public String getSelectedCannedSet() {
        return selectedCannedSet;
    }

    public void setSelectedCannedSet(String selectedCannedSet) {
        this.selectedCannedSet = selectedCannedSet;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        sb.append("Forecast : [");
        for (double val : forecast) {
            if (first) {
                first = false;
                sb.append(String.format("%12f", val));
            } else {
                sb.append(", ").append(String.format("%12f", val));
            }
        }
        sb.append("], Selected Canned Set :")
                .append(selectedCannedSet)
                .append(String.format(", Elapsed Millis : %10d", time));
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ForecastResponse that = (ForecastResponse) o;

        if (time != that.time) return false;
        if (!Arrays.equals(forecast, that.forecast)) return false;
        if (selectedCannedSet != null ? !selectedCannedSet.equals(that.selectedCannedSet) : that.selectedCannedSet != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = forecast != null ? Arrays.hashCode(forecast) : 0;
        result = 31 * result + (selectedCannedSet != null ? selectedCannedSet.hashCode() : 0);
        result = 31 * result + (int) (time ^ (time >>> 32));
        return result;
    }
}
