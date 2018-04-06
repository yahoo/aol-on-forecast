/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.model.request;

import com.aol.one.reporting.forecastapi.server.service.ForecastService;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.Arrays;


@ApiModel(value = "Impression Forecast Request")
public class ImpressionForecastRequest {
/*
        - TimeSeries:
            * Real numbers from least recent to most recent. At least 1 value required.
        - SpikeFilterWindow: (optional)
            * -1: Defer to system
            * 0: Disable spike filter
            * >= 3: Spike filter window to use
        - CannedSetlist:
            * One or more canned set names to use. If more than one canned set name is specified, a competition is performed.
        - NumberHoldBack: (optional)
            * Maximum number of historical values to hold back used to determine a canned set winner. The default is 30.
        - NumberForecasts:
            * Number of forecasts to produce
        - MassageForecasts: (optional)
            * Boolean indicating whether forecasts are to be rounded to integer values and negative values are to be set to 0. The default is true.
*/


    private static final Logger LOG = LoggerFactory.getLogger(ImpressionForecastRequest.class);

    @ApiModelProperty(value = "Real numbers from least to mode recent. At least one value required", required = true)
    @NotNull
    private double[] timeSeries;

    @ApiModelProperty(value = "-1: Defer to System, 0: disable, >=3 and <= 30: Spike filter window to use", required = false)
    private Integer spikeFilterWindow;

    @ApiModelProperty(value = "Array of Canned Sets", required = true)
    @NotNull
    private String[] cannedSets;

    @ApiModelProperty(value = "Maximum number of historical values to hold back used to determine a canned set winner.", required = false)
    private Integer numberHoldBack;

    @ApiModelProperty(value = "Number of forecasts to produce", required = true)
    @NotNull
    private int numberForecasts;

    @ApiModelProperty(value = "Indicating whether forecasts are to be rounded to integer and negative values to be set 0", required = false)
    private Boolean massageForecast;

    public ImpressionForecastRequest(
            @JsonProperty("timeSeries") double[] timeSeries,
            @JsonProperty("spikeFilterWindow") Integer spikeFilterWindow,
            @JsonProperty("cannedSets") String[] cannedSets,
            @JsonProperty("numberForecasts") int numberForecasts,
            @JsonProperty("massageForecast") Boolean massageForecast
    ) {
        this.timeSeries = timeSeries;
        this.spikeFilterWindow = spikeFilterWindow;
        this.cannedSets = cannedSets;
        this.numberForecasts = numberForecasts;
        this.massageForecast = massageForecast;
    }

    public double[] getTimeSeries() {
        return timeSeries;
    }

    public void setTimeSeries(double[] timeSeries) {
        this.timeSeries = timeSeries;
    }

    public Integer getSpikeFilterWindow() {
        return spikeFilterWindow;
    }

    public void setSpikeFilterWindow(Integer spikeFilterWindow) {
        this.spikeFilterWindow = spikeFilterWindow;
    }

    public String[] getCannedSets() {
        return cannedSets;
    }

    public void setCannedSets(String[] cannedSets) {
        this.cannedSets = cannedSets;
    }

    public Integer getNumberHoldBack() {
        return numberHoldBack == null ? ForecastService.HOLD_BACK_DAYS : numberHoldBack;
    }

    public void setNumberHoldBack(Integer numberHoldBack) {
        this.numberHoldBack = numberHoldBack;
    }

    public int getNumberForecasts() {
        return numberForecasts;
    }

    public void setNumberForecasts(int numberForecasts) {
        this.numberForecasts = numberForecasts;
    }

    public Boolean getMassageForecast() {
        return massageForecast == null ? Boolean.FALSE : massageForecast;
    }

    public void setMassageForecast(Boolean massageForecast) {
        this.massageForecast = massageForecast;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        sb.append("timeSeries : [");
        for (double val : timeSeries) {
            if (first) {
                first = false;
                sb.append(String.format("%12.2f", val));
            } else {
                sb.append(", ").append(String.format("%12.2f", val));
            }
        }
        int sfw = (this.spikeFilterWindow == null ? -1 : this.spikeFilterWindow);
        sb.append("] SpikeFilterWindow : ").append(String.format("%4d ", sfw));
        sb.append("CannedSets [");
        first = true;
        for (String cannedSet : cannedSets) {
            if (first) {
                sb.append(cannedSet);
                first = false;
            } else {
                sb.append(", ").append(cannedSet);
            }
        }
        sb.append("]");
        sb.append("numberHoldBack : ").append(getNumberHoldBack());
        sb.append(String.format("Number of Forecasts : %4d ", numberForecasts));
        sb.append(" MassageForecast : ").append(getMassageForecast());
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImpressionForecastRequest that = (ImpressionForecastRequest) o;

        if (numberForecasts != that.numberForecasts) return false;
        if (!Arrays.equals(cannedSets, that.cannedSets)) return false;
        if (massageForecast != null ? !massageForecast.equals(that.massageForecast) : that.massageForecast != null)
            return false;
        if (numberHoldBack != null ? !numberHoldBack.equals(that.numberHoldBack) : that.numberHoldBack != null)
            return false;
        if (spikeFilterWindow != null ? !spikeFilterWindow.equals(that.spikeFilterWindow) : that.spikeFilterWindow != null)
            return false;
        if (!Arrays.equals(timeSeries, that.timeSeries)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(timeSeries);
        result = 31 * result + (spikeFilterWindow != null ? spikeFilterWindow.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(cannedSets);
        result = 31 * result + (numberHoldBack != null ? numberHoldBack.hashCode() : 0);
        result = 31 * result + numberForecasts;
        result = 31 * result + (massageForecast != null ? massageForecast.hashCode() : 0);
        return result;
    }
}
