/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.app;

import com.aol.one.reporting.forecastapi.server.models.cs.IFSCannedSet;

import java.util.List;
import java.util.Map;

public class IfsCache {


    private Object lock = new Object();
    private Map<String, IFSCannedSet> map;
    private Map<String, List<IFSCannedSet>> list;
    private List<String> collectionNames;


    public void switchCache(
            Map<String, IFSCannedSet> map,
            Map<String, List<IFSCannedSet>> list,
            List<String> collectionNames) {
        synchronized (lock) {
            this.map = map;
            this.list = list;
            this.collectionNames = collectionNames;
        }
    }

    public Map<String, IFSCannedSet> getMap() {
        synchronized (lock) {
            return map;
        }
    }

    public List<IFSCannedSet> getList(String collectionName) {
        synchronized (lock) {
            return list.get(collectionName);
        }
    }

    public List<String> getCollectionNames() {
        synchronized (lock) {
            return collectionNames;
        }
    }

}
