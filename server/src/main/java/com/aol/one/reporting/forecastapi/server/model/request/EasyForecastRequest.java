/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.Arrays;

@ApiModel(value = "Easy Forecast Request")
public class EasyForecastRequest {

    private static final Logger LOG = LoggerFactory.getLogger(EasyForecastRequest.class);
    public static final String CANNED_SET_DEFAULT_COLLECTION_NAME = "Default";

    @ApiModelProperty(value = "Real numbers from least to mode recent. At least one value required", required = true)
    @NotNull
    private double[] timeSeries;


    @ApiModelProperty(value = "-1: Defer to System, 0: disable, >=3 and <= 30: Spike filter window to use", required = false)
    private Integer spikeFilterWindow;

    @ApiModelProperty(value = "Canned Set Collection Name", required = false)
    private String cannedSetCollectionName;

    @ApiModelProperty(value = "Number of forecasts to produce", required = true)
    @NotNull
    private int numberForecasts;

    @ApiModelProperty(value = "Indicating whether forecasts are to be rounded to integer and negative values to be set 0", required = false)
    private Boolean massageForecast;

    public EasyForecastRequest(
            @JsonProperty("timeSeries") double[] timeSeries,
            @JsonProperty("spikeFilterWindow") Integer spikeFilterWindow,
            @JsonProperty("cannedSetCollectionName") String cannedSetCollectionName,
            @JsonProperty("numberForecasts") int numberForecasts,
            @JsonProperty("massageForecast") Boolean massageForecast
    ) {
        this.timeSeries = timeSeries;
        this.spikeFilterWindow = spikeFilterWindow;
        this.cannedSetCollectionName = cannedSetCollectionName;
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

    public String getCannedSetCollectionName() {
        return cannedSetCollectionName == null ? CANNED_SET_DEFAULT_COLLECTION_NAME : cannedSetCollectionName;
    }

    public void setCannedSetCollectionName(String cannedSetCollectionName) {
        this.cannedSetCollectionName = cannedSetCollectionName;
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
        sb.append("CannedSetCollectionName : ").append(getCannedSetCollectionName());
        sb.append(String.format("Number of Forecasts : %4d ", numberForecasts));
        sb.append(" MassageForecast : ").append(getMassageForecast());
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EasyForecastRequest that = (EasyForecastRequest) o;

        if (numberForecasts != that.numberForecasts) return false;
        if (cannedSetCollectionName != null ? !cannedSetCollectionName.equals(that.cannedSetCollectionName) : that.cannedSetCollectionName != null)
            return false;
        if (massageForecast != null ? !massageForecast.equals(that.massageForecast) : that.massageForecast != null)
            return false;
        if (spikeFilterWindow != null ? !spikeFilterWindow.equals(that.spikeFilterWindow) : that.spikeFilterWindow != null)
            return false;
        if (!Arrays.equals(timeSeries, that.timeSeries)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = timeSeries != null ? Arrays.hashCode(timeSeries) : 0;
        result = 31 * result + (spikeFilterWindow != null ? spikeFilterWindow.hashCode() : 0);
        result = 31 * result + (cannedSetCollectionName != null ? cannedSetCollectionName.hashCode() : 0);
        result = 31 * result + numberForecasts;
        result = 31 * result + (massageForecast != null ? massageForecast.hashCode() : 0);
        return result;
    }
}
