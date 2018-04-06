/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.service;

import com.aol.one.reporting.forecastapi.server.app.IfsCache;
import com.aol.one.reporting.forecastapi.server.app.IfsConfig;
import com.aol.one.reporting.forecastapi.server.model.request.EasyForecastRequest;
import com.aol.one.reporting.forecastapi.server.model.request.ImpressionForecastRequest;
import com.aol.one.reporting.forecastapi.server.model.request.SelectionForecastRequest;
import com.aol.one.reporting.forecastapi.server.model.response.CannedSetResponse;
import com.aol.one.reporting.forecastapi.server.model.response.CollectionResponse;
import com.aol.one.reporting.forecastapi.server.model.response.ForecastResponse;
import com.aol.one.reporting.forecastapi.server.util.ForecastUtil;
import com.aol.one.reporting.forecastapi.server.util.RequestValidation;
import com.aol.one.reporting.forecastapi.server.models.cs.*;
import com.aol.one.reporting.forecastapi.server.models.model.IFSModel;
import com.aol.one.reporting.forecastapi.server.models.model.IFSModelFactory;
import com.aol.one.reporting.forecastapi.server.models.model.IFSParameterValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public final class ForecastService {

    private static final Logger LOG = LoggerFactory.getLogger(ForecastService.class);

    public static final int HOLD_BACK_DAYS = 30;


    private ForecastService() {}

    public static ForecastResponse easyForecast(EasyForecastRequest easyForecastRequest, long start) throws Exception {
        Integer sfw = RequestValidation.spikeFilter(easyForecastRequest.getSpikeFilterWindow());
        easyForecastRequest.setSpikeFilterWindow(sfw);
        RequestValidation.numberForecasts(easyForecastRequest.getNumberForecasts());
        RequestValidation.timeSeries(easyForecastRequest.getTimeSeries());

        IfsCache cache = IfsConfig.getCache();
        RequestValidation.easyRequestCacheValidation(cache, easyForecastRequest.getCannedSetCollectionName());
        List<Integer> declineProfitCenterList = new ArrayList<>();

        IFSCannedSet arNoneNone = cache.getMap().get("AR-NONE-NONE");
        IFSCannedSet avgNone28New = cache.getMap().get("AVG-NONE-28-NEW");
        IFSCannedSet regNoneAddAutoNew = cache.getMap().get("REG-NONE-ADD-AUTO-NEW");

        LOG.debug(easyForecastRequest.toString());

        List<IFSCannedSet> ifsCannedSetList = cache.getList(easyForecastRequest.getCannedSetCollectionName());
        if (easyForecastRequest.getSpikeFilterWindow() != -1) {
            ifsCannedSetList = ForecastUtil.changeSpikeFilterWindow(ifsCannedSetList, easyForecastRequest.getSpikeFilterWindow());
        }

        IFSCannedSetSelectionConstraints constraints = new IFSCannedSetSelectionConstraints();
        constraints.setCannedSetDecline(arNoneNone);
        constraints.setCannedSetNoneNew(avgNone28New);
        constraints.setCannedSetWeekNew(regNoneAddAutoNew);
        constraints.setProfitCentersDecline(declineProfitCenterList);
        IFSCannedSetSelectionContext context = new IFSCannedSetSelectionContext();
        context.setSeries(easyForecastRequest.getTimeSeries(), HOLD_BACK_DAYS);
        context.setID(1);
        context.setProfitCenter(1);
        context.setCannedSetCandidates(ifsCannedSetList);

        IFSCannedSet selectedCannedSet = IFSCannedSetSelection.selectCannedSet(constraints, context);
        IFSModel model = IFSModelFactory.create(selectedCannedSet.getParameterSpec().getModel());
        IFSModelFactory.setup(model, easyForecastRequest.getTimeSeries(), selectedCannedSet.getParameterSpec().getParameterValues());
        model.generateForecasts(easyForecastRequest.getNumberForecasts());
        double[] forecast = model.getForecasts();

        if (easyForecastRequest.getMassageForecast() && forecast != null) {
            ForecastUtil.messageForecast(forecast);
        }

        ForecastResponse response = new ForecastResponse();
        response.setForecast(forecast);
        response.setSelectedCannedSet(selectedCannedSet.getName());
        long end = System.currentTimeMillis();
        long time = (end - start);
        response.setTime(time);
        return response;
    }

    public static ForecastResponse selectionForecast(SelectionForecastRequest request, long start) throws Exception {
        Integer sfw = RequestValidation.spikeFilter(request.getSpikeFilterWindow());
        request.setSpikeFilterWindow(sfw);
        RequestValidation.numberForecasts(request.getNumberForecasts());
        RequestValidation.timeSeries(request.getTimeSeries());


        IfsCache cache = IfsConfig.getCache();
        List<IFSCannedSet> cannedSets = ForecastUtil.setupCannedSets(cache,request);

        RequestValidation.selectionRequestCacheValidation(cache, cannedSets);
        List<Integer> declineProfitCenterList = new ArrayList<>();
        if (request.getDecliningSeries()) {
            declineProfitCenterList.add(1);
        }
        IFSCannedSet arNoneNone = cache.getMap().get("AR-NONE-NONE");
        IFSCannedSet avgNone28New = cache.getMap().get("AVG-NONE-28-NEW");
        IFSCannedSet regNoneAddAutoNew = cache.getMap().get("REG-NONE-ADD-AUTO-NEW");

        LOG.debug(request.toString());


        if (request.getSpikeFilterWindow() != -1) {
            cannedSets = ForecastUtil.changeSpikeFilterWindow(cannedSets, request.getSpikeFilterWindow());
        }

        IFSCannedSetSelectionConstraints constraints = new IFSCannedSetSelectionConstraints();
        constraints.setCannedSetDecline(arNoneNone);
        constraints.setCannedSetNoneNew(avgNone28New);
        constraints.setCannedSetWeekNew(regNoneAddAutoNew);
        constraints.setProfitCentersDecline(declineProfitCenterList);
        IFSCannedSetSelectionContext context = new IFSCannedSetSelectionContext();
        context.setSeries(request.getTimeSeries(), HOLD_BACK_DAYS);
        context.setID(1);
        context.setProfitCenter(1);
        context.setCannedSetCandidates(cannedSets);

        IFSCannedSet selectedCannedSet = IFSCannedSetSelection.selectCannedSet(constraints, context);
        IFSModel model = IFSModelFactory.create(selectedCannedSet.getParameterSpec().getModel());
        IFSModelFactory.setup(model, request.getTimeSeries(), selectedCannedSet.getParameterSpec().getParameterValues());
        model.generateForecasts(request.getNumberForecasts());
        double[] forecast = model.getForecasts();

        if (request.getMassageForecast() && forecast != null) {
            ForecastUtil.messageForecast(forecast);
        }

        ForecastResponse response = new ForecastResponse();
        response.setForecast(forecast);
        response.setSelectedCannedSet(selectedCannedSet.getName());
        long end = System.currentTimeMillis();
        long time = (end - start);
        response.setTime(time);
        return response;
    }

    public static List<CannedSetResponse> getCannedSets(String regex) {
        List<CannedSetResponse> responses = new ArrayList<>();
        IfsCache cache = IfsConfig.getCache();
        for(IFSCannedSet ifsCannedSet : cache.getMap().values()) {
            if (regex == null) {
                CannedSetResponse response = new CannedSetResponse();
                response.setCannedSetName(ifsCannedSet.getName());
                response.setDescription(ifsCannedSet.getDescription());
                responses.add(response);
            } else {
                if (ifsCannedSet.getName().matches(regex)) {
                    CannedSetResponse response = new CannedSetResponse();
                    response.setCannedSetName(ifsCannedSet.getName());
                    response.setDescription(ifsCannedSet.getDescription());
                    responses.add(response);
                }
            }
        }
        Collections.sort(responses);
        return responses;
    }

    public static CollectionResponse[] getCollectionCannedSets(String regex) {
        IfsCache cache = IfsConfig.getCache();
        Map<String, List<CannedSetResponse>> map = new HashMap<>();
        for(String collectionName : cache.getCollectionNames()) {
            if (regex == null || collectionName.matches(regex)) {
                List<CannedSetResponse> list = new ArrayList<>();
                List<IFSCannedSet> ifsCannedSets = cache.getList(collectionName);
                for(IFSCannedSet ifsCannedSet : ifsCannedSets) {
                    CannedSetResponse response = new CannedSetResponse();
                    response.setCannedSetName(ifsCannedSet.getName());
                    response.setDescription(ifsCannedSet.getDescription());
                    list.add(response);
                }
                map.put(collectionName,list);
            }
        }
        CollectionResponse[] array = new CollectionResponse[map.size()];
        int i = 0;
        for(String collectionName : map.keySet()) {
            CollectionResponse collectionResponse = new CollectionResponse();
            collectionResponse.setCollectionName(collectionName);
            List<CannedSetResponse> list = map.get(collectionName);
            CannedSetResponse[] cannedSetResponses = new CannedSetResponse[list.size()];
            int j = 0;
            for(CannedSetResponse cannedSetResponse : list) {
                cannedSetResponses[j] = cannedSetResponse;
                j++;
            }
            Arrays.sort(cannedSetResponses);
            collectionResponse.setCannedSets(cannedSetResponses);
            array[i] = collectionResponse;
            i++;
        }
        Arrays.sort(array);
        return array;
    }

    public static ForecastResponse impressionForecast(ImpressionForecastRequest request, long start) throws Exception {
        Integer sfw = RequestValidation.spikeFilter(request.getSpikeFilterWindow());
        request.setSpikeFilterWindow(sfw);
        RequestValidation.numberForecasts(request.getNumberForecasts());
        RequestValidation.timeSeries(request.getTimeSeries());

        if (request.getCannedSets() == null || request.getCannedSets().length == 0) {
            throw new Exception("Empty Canned Set Not allowed");
        }

        IfsCache cache = IfsConfig.getCache();
        List<IFSCannedSet> ifsCannedSetList = new ArrayList<>();
        for(String cannedSet : request.getCannedSets()) {
            IFSCannedSet ifsCannedSet = cache.getMap().get(cannedSet);
            ifsCannedSetList.add(ifsCannedSet);
        }

        LOG.debug("CannedSet List :");
        for(IFSCannedSet ifsCannedSet : ifsCannedSetList ) {
            LOG.debug("CannedSet  : " + ifsCannedSet.getName());
            for(IFSParameterValue param : ifsCannedSet.getParameterSpec().getParameterValues())
                LOG.debug("     Parameter Key : " + param.getParameter() + " Value :" + param.getValue());
        }

        LOG.debug(request.toString());

        if (request.getSpikeFilterWindow() != -1) {
            ifsCannedSetList = ForecastUtil.changeSpikeFilterWindow(ifsCannedSetList, request.getSpikeFilterWindow());
        }

        IFSCannedSetSelectionContext context = new IFSCannedSetSelectionContext();
        context.setSeries(request.getTimeSeries(), request.getNumberHoldBack());
        context.setID(1);
        context.setProfitCenter(1);
        context.setCannedSetCandidates(ifsCannedSetList);

        
        IFSCannedSet selectedCannedSet = IFSCannedSetCompetition.competeCannedSets(context,ifsCannedSetList);

        IFSModel model = IFSModelFactory.create(selectedCannedSet.getParameterSpec().getModel());
        IFSModelFactory.setup(model, request.getTimeSeries(), selectedCannedSet.getParameterSpec().getParameterValues());
        model.generateForecasts(request.getNumberForecasts());
        double[] forecast = model.getForecasts();

        if (request.getMassageForecast() && forecast != null) {
            ForecastUtil.messageForecast(forecast);
        }

        ForecastResponse response = new ForecastResponse();
        response.setForecast(forecast);
        response.setSelectedCannedSet(selectedCannedSet.getName());
        long end = System.currentTimeMillis();
        long time = (end - start);
        response.setTime(time);
        return response;
    }

}
