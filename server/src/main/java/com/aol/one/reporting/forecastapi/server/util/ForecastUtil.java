/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.util;

import com.aol.one.reporting.forecastapi.server.app.IfsCache;
import com.aol.one.reporting.forecastapi.server.model.request.EasyForecastRequest;
import com.aol.one.reporting.forecastapi.server.model.request.SelectionForecastRequest;
import com.aol.one.reporting.forecastapi.server.models.cs.IFSCannedSet;
import com.aol.one.reporting.forecastapi.server.models.model.IFSParameterSpec;
import com.aol.one.reporting.forecastapi.server.models.model.IFSParameterValue;

import java.util.ArrayList;
import java.util.List;

public final class ForecastUtil {

    private ForecastUtil() {}


    public static List<IFSCannedSet> changeSpikeFilterWindow(List<IFSCannedSet> list, Integer sfw) {
        if (sfw == -1)
            return list;
        List<IFSCannedSet> newList = new ArrayList<>();
        for(IFSCannedSet ifsCannedSet : list) {
            IFSCannedSet newIfsCannedSet = ifsCannedSet.clone();
            IFSParameterSpec spec = newIfsCannedSet.getParameterSpec();
            List<IFSParameterValue> paramList = spec.getParameterValues();
            List<IFSParameterValue> newParamList = new ArrayList<>();
            for(IFSParameterValue parameterValue : paramList) {
                IFSParameterValue newParameterValue  = new IFSParameterValue();
                newParameterValue.setParameter(new String(parameterValue.getParameter()));
                if (parameterValue.getParameter().equals("spike_filter")) {
                    newParameterValue.setValue(new String(sfw.toString()));
                } else {
                    newParameterValue.setValue(new String(parameterValue.getValue()));
                }
                newParamList.add(newParameterValue);
            }
            spec.setParameterValues(newParamList);
            newList.add(newIfsCannedSet);
        }
        return newList;
    }

    public static void messageForecast(double[] forecast) {
        for(int index = 0; index < forecast.length; index++) {
            if (forecast[index] < 0.0)
                forecast[index] = 0.0;
            else
                forecast[index] = Math.round(forecast[index]);
        }
    }

    public static List<IFSCannedSet> setupCannedSets(IfsCache cache, SelectionForecastRequest request) throws Exception {
        List<IFSCannedSet> newCannedSets = new ArrayList<>();
        checkTypeAndAdd(request.getYearlyCannedSetList(),1,cache,newCannedSets);
        checkTypeAndAdd(request.getSeasonalCannedSetList(),2,cache,newCannedSets);
        checkTypeAndAdd(request.getNonSeasonalCannedSetList(),3,cache,newCannedSets);
        return newCannedSets;
    }


    private static void checkTypeAndAdd(
            String[] sets,
            int type,
            IfsCache cache,
            List<IFSCannedSet> newCannedSets
        ) throws Exception {
        List<IFSCannedSet> defaultList = cache.getList(EasyForecastRequest.CANNED_SET_DEFAULT_COLLECTION_NAME);
        String cannedSetType = null;
        String attribute = null;
        switch(type) {
            case 1: cannedSetType = "YEAR";
                    attribute = "YearlyCannedSetList";
                    break;
            case 2: cannedSetType = "AUTO";
                    attribute = "SeasonalCannedSetList";
                    break;
            case 3: cannedSetType = "OTHER";
                    attribute = "NonSeasonalCannedSetList";
                    break;
        }
        if (sets != null && sets.length > 0) {
            for(String cannedSet : sets) {
                IFSCannedSet ifsCannedSet = cache.getMap().get(cannedSet);
                if (ifsCannedSet == null) {
                    throw new Exception("Invalid canned set name : " + cannedSet + " in " + attribute);
                }
                newCannedSets.add(ifsCannedSet);
            }
        } else {
            for(IFSCannedSet ifsCannedSet : defaultList) {
                String cannedSet = ifsCannedSet.getName();
                if ((type == 1 || type == 2) && cannedSet.endsWith(cannedSetType))
                    newCannedSets.add(ifsCannedSet);
                else if (type == 3) {
                    if (!(cannedSet.endsWith("YEAR") || cannedSet.endsWith("AUTO")))
                        newCannedSets.add(ifsCannedSet);

                }
            }
        }
    }
}
