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

@ApiModel(value = "Canned Set Selection Forecast Request")
public class SelectionForecastRequest {
 /*
    A. Inputs:
        - TimeSeries:
            * Real numbers from least recent to most recent. At least 1 value required.
        - SpikeFilterWindow: (optional)
            * -1: Defer to system
            * 0: Disable spike filter
            * >= 3: Spike filter window to use
        - IsDecliningSeries: (optional)
            * Boolean indicating whether the series is declining or not
        - YearlyCannedSetList: (optional)
            * Array of yearly canned set names (i.e. names end in 'YEAR') to consider
        - SeasonalCannedSetList: (optional)
            * Array of seasonal canned set names (i.e. names end in 'AUTO') to consider
        - NonSeasonalCannedSetList: (optional)
            * Array of non-seasonal canned set names (i.e. names do not end in 'YEAR' or 'AUTO') to consider
        - NumberForecasts:
            * Number of forecasts to produce
        - MassageForecasts: (optional)
            * Boolean indicating whether forecasts are to be rounded to integer values and negative values are to be set to 0. The default is true.
  */

    private static final Logger LOG = LoggerFactory.getLogger(SelectionForecastRequest.class);

    @ApiModelProperty(
            value = "Real numbers from least to mode recent. At least one value required",
            required = true)
    @NotNull
    private double[] timeSeries;

    @ApiModelProperty(
            value = "-1: Defer to System, 0: disable, >=3 and <= 30: Spike filter window to use",
            required = false)
    private Integer spikeFilterWindow;

    @ApiModelProperty(value = "Boolean indicating whether the series is declining or not", required = false)
    private Boolean decliningSeries;

    @ApiModelProperty(
            value = "Array of yearly canned set names (i.e. names end in 'YEAR') to consider",
            required = false)
    private String[] yearlyCannedSetList;

    @ApiModelProperty(
            value = "Array of seasonal canned set names (i.e. names end in 'AUTO') to consider",
            required = false)
    private String[] seasonalCannedSetList;

    @ApiModelProperty(
            value = "Array of non-seasonal canned set names (i.e. names do not end in 'YEAR' or 'AUTO') to consider",
            required = false)
    private String[] nonSeasonalCannedSetList;

    @ApiModelProperty(
            value = "Number of forecasts to produce",
            required = true)
    @NotNull
    private int numberForecasts;

    @ApiModelProperty(
            value = "Indicating whether forecasts are to be rounded to integer and negative values to be set 0",
            required = false)
    private Boolean massageForecast;

    public SelectionForecastRequest(
            @JsonProperty("timeSeries") double[] timeSeries,
            @JsonProperty("spikeFilterWindow") Integer spikeFilterWindow,
            @JsonProperty("decliningSeries") Boolean decliningSeries,
            @JsonProperty("YearlyCannedSetList") String[] yearlyCannedSetList,
            @JsonProperty("seasonalCannedSetList") String[] seasonalCannedSetList,
            @JsonProperty("nonSeasonalCannedSetList") String[] nonSeasonalCannedSetList,
            @JsonProperty("numberForecasts") int numberForecasts,
            @JsonProperty("massageForecast") Boolean massageForecast
    ) {
        this.timeSeries = timeSeries;
        this.spikeFilterWindow = spikeFilterWindow;
        this.yearlyCannedSetList = yearlyCannedSetList;
        this.seasonalCannedSetList = seasonalCannedSetList;
        this.nonSeasonalCannedSetList = nonSeasonalCannedSetList;
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

    public Boolean getDecliningSeries() {
        return decliningSeries == null ? Boolean.FALSE : decliningSeries;
    }

    public void setDecliningSeries(Boolean decliningSeries) {
        this.decliningSeries = decliningSeries;
    }

    public String[] getYearlyCannedSetList() {
        return yearlyCannedSetList;
    }

    public void setYearlyCannedSetList(String[] yearlyCannedSetList) {
        this.yearlyCannedSetList = yearlyCannedSetList;
    }

    public String[] getSeasonalCannedSetList() {
        return seasonalCannedSetList;
    }

    public void setSeasonalCannedSetList(String[] seasonalCannedSetList) {
        this.seasonalCannedSetList = seasonalCannedSetList;
    }

    public String[] getNonSeasonalCannedSetList() {
        return nonSeasonalCannedSetList;
    }

    public void setNonSeasonalCannedSetList(String[] nonSeasonalCannedSetList) {
        this.nonSeasonalCannedSetList = nonSeasonalCannedSetList;
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
        for(double val : timeSeries) {
            if (first) {
                first = false;
                sb.append(String.format("%12.2f",val));
            } else {
                sb.append(", ").append(String.format("%12.2f",val));
            }
        }
        int sfw = (this.spikeFilterWindow == null ? -1 : this.spikeFilterWindow);
        sb.append("] SpikeFilterWindow : ").append(String.format("%4d ",sfw));
        sb.append("YearlyCannedSetList : ");
        cannedSets(yearlyCannedSetList,sb);
        cannedSets(seasonalCannedSetList,sb);
        cannedSets(nonSeasonalCannedSetList,sb);
        sb.append(String.format("Number of Forecasts : %4d ",numberForecasts));
        sb.append(" MassageForecast : ").append(getMassageForecast());
        return sb.toString();
    }

    private void cannedSets(String[] sets, StringBuilder sb) {
        if (sets != null) {
            sb.append(" [");
            boolean first = true;
            for(String set : sets) {
                if (first) {
                    first = false;
                    sb.append(set);
                } else {
                    sb.append(", ").append(set);
                }
            }
            sb.append(" ]");
        } else {
            sb.append("null");
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SelectionForecastRequest that = (SelectionForecastRequest) o;

        if (numberForecasts != that.numberForecasts) return false;
        if (decliningSeries != null ? !decliningSeries.equals(that.decliningSeries) : that.decliningSeries != null)
            return false;
        if (massageForecast != null ? !massageForecast.equals(that.massageForecast) : that.massageForecast != null)
            return false;
        if (!Arrays.equals(nonSeasonalCannedSetList, that.nonSeasonalCannedSetList)) return false;
        if (!Arrays.equals(seasonalCannedSetList, that.seasonalCannedSetList)) return false;
        if (spikeFilterWindow != null ? !spikeFilterWindow.equals(that.spikeFilterWindow) : that.spikeFilterWindow != null)
            return false;
        if (!Arrays.equals(timeSeries, that.timeSeries)) return false;
        if (!Arrays.equals(yearlyCannedSetList, that.yearlyCannedSetList)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(timeSeries);
        result = 31 * result + (spikeFilterWindow != null ? spikeFilterWindow.hashCode() : 0);
        result = 31 * result + (decliningSeries != null ? decliningSeries.hashCode() : 0);
        result = 31 * result + (yearlyCannedSetList != null ? Arrays.hashCode(yearlyCannedSetList) : 0);
        result = 31 * result + (seasonalCannedSetList != null ? Arrays.hashCode(seasonalCannedSetList) : 0);
        result = 31 * result + (nonSeasonalCannedSetList != null ? Arrays.hashCode(nonSeasonalCannedSetList) : 0);
        result = 31 * result + numberForecasts;
        result = 31 * result + (massageForecast != null ? massageForecast.hashCode() : 0);
        return result;
    }
}
