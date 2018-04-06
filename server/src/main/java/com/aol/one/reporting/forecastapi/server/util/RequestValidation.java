/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.util;

import com.aol.one.reporting.forecastapi.server.app.IfsCache;
import com.aol.one.reporting.forecastapi.server.models.cs.IFSCannedSet;
import com.aol.one.reporting.forecastapi.server.models.model.IFSException;
import com.aol.one.reporting.forecastapi.server.models.model.IFSParameterValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class RequestValidation {

    private static final Logger LOG = LoggerFactory.getLogger(RequestValidation.class);

    public static Integer spikeFilter(Integer sfw) throws Exception {
        if (sfw != null) {
            if (sfw < -1 || sfw == 1 || sfw == 2 || sfw > 30) {
                LOG.error("Invalid SpikeFilterWindow : " + sfw + " specified in request");
                throw new Exception("Invalid spikeFilterWindow value : " + sfw);
            }

        } else {
            sfw = -1;
        }
        return sfw;
    }

    public static void numberForecasts(int numberForecasts) throws Exception {
        if (numberForecasts < 1) {
            LOG.error("Invalid number of forecasts : " + numberForecasts + " in request");
            throw new Exception("Invalid number of forecasts : " + numberForecasts + " in request");
        }
    }


    public static void timeSeries(double[] timeSeries) throws Exception {
        if (timeSeries == null || timeSeries.length < 1) {
            LOG.error("Invalid Time Series data  in request");
            throw new Exception("Invalid Time Series data  in request");
        }
    }


    public static void easyRequestCacheValidation(IfsCache cache, String name) throws IFSException, Exception {

        if (!cache.getCollectionNames().contains(name)) {
            LOG.error("Invalid Canned Set Collection Name : " + name);
            throw new Exception("Invalid Canned Set Collection name : " + name);
        }

        IFSCannedSet arNoneNone = cache.getMap().get("AR-NONE-NONE");
        IFSCannedSet avgNone28New = cache.getMap().get("AVG-NONE-28-NEW");
        IFSCannedSet regNoneAddAutoNew = cache.getMap().get("REG-NONE-ADD-AUTO-NEW");

        if (arNoneNone == null) {
            LOG.error("AR-NONE-NONE default canned set is null");
            throw new IFSException("AR-NONE-NONE default canned set is null");
        }

        if (avgNone28New == null) {
            LOG.error("AVG-NONE-28-NEW is null");
            throw new IFSException("AVG-NONE-28-NEW default canned set is null");
        }

        if (regNoneAddAutoNew == null) {
            LOG.error("REG-NONE-ADD-AUTO-NEW is null");
            throw new IFSException("REG-NONE-ADD-AUTO-NEW default canned set is null");
        }
        if (cache.getList(name) == null) {
            LOG.error("Cached CannedSet List is null");
            throw new IFSException("Cached CannedSet List null");
        }
        LOG.debug("CannedSet List :");
        for (IFSCannedSet ifsCannedSet : cache.getList(name)) {
            LOG.debug("CannedSet  : " + ifsCannedSet.getName());
            for (IFSParameterValue param : ifsCannedSet.getParameterSpec().getParameterValues())
                LOG.debug("     Parameter Key : " + param.getParameter() + " Value :" + param.getValue());
        }
    }

    public static void selectionRequestCacheValidation(IfsCache cache, List<IFSCannedSet> ifsCannedSets) throws IFSException, Exception {
        IFSCannedSet arNoneNone = cache.getMap().get("AR-NONE-NONE");
        IFSCannedSet avgNone28New = cache.getMap().get("AVG-NONE-28-NEW");
        IFSCannedSet regNoneAddAutoNew = cache.getMap().get("REG-NONE-ADD-AUTO-NEW");

        if (arNoneNone == null) {
            LOG.error("AR-NONE-NONE is null");
            throw new IFSException("AR-NONE-NONE default canned set is null");
        }

        if (avgNone28New == null) {
            LOG.error("AVG-NONE-28-NEW is null");
            throw new IFSException("AVG-NONE-28-NEW default canned set is null");
        }

        if (regNoneAddAutoNew == null) {
            LOG.error("REG-NONE-ADD-AUTO-NEW is null");
            throw new IFSException("REG-NONE-ADD-AUTO-NEW default canned set is null");
        }

        LOG.debug("CannedSet List :");
        for (IFSCannedSet ifsCannedSet : ifsCannedSets) {
            LOG.debug("CannedSet  : " + ifsCannedSet.getName());
            for (IFSParameterValue param : ifsCannedSet.getParameterSpec().getParameterValues())
                LOG.debug("     Parameter Key : " + param.getParameter() + " Value :" + param.getValue());
        }
    }
}
