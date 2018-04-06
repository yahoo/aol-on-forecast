/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.app;

import com.aol.one.reporting.forecastapi.server.models.cs.GetCannedSetCandidates;
import com.aol.one.reporting.forecastapi.server.models.cs.GetCannedSetDefinitions;
import com.aol.one.reporting.forecastapi.server.models.cs.IFSCannedSet;
import com.aol.one.reporting.forecastapi.server.models.model.IFSParameterSpec;
import com.aol.one.reporting.forecastapi.server.models.model.IFSParameterValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public final class CannedSetFiles {
    private static final Logger LOG = LoggerFactory.getLogger(CannedSetFiles.class);


    public static final String COLLECTION_PREFIX = "candidates_";
    public static final String CANNED_SET_DEFINITION_FILE = "canned_set_definitions.txt";


    private CannedSetFiles() {}


    public static long isModified(String path, Long lastModified) throws Exception {
        Long maxModified = Long.MIN_VALUE;
        Date dt = new Date(lastModified);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyy HH:mm:ss");
//        LOG.debug("Last Modified : " + sdf.format(dt));
        File dir = new File(path);

        if (!dir.isDirectory()) {
            LOG.error("Path : " + path + " to read definition/candidates files not exist");
            throw new Exception("Path : " + path + " to read definition/candidates files not exist");
        }
        String regex = COLLECTION_PREFIX + "[A-Z][a-z]*.txt";
        String[] files = dir.list();
        for(String file : files) {
            String newFile = path + "/" + file;
            File cfh = new File(newFile);
            if (cfh.isDirectory()) {
                continue;
            }
            if (!file.matches(regex)) {
                continue;
            }
            dt = new Date(cfh.lastModified());
            maxModified = Math.max(maxModified, cfh.lastModified());
            Date dt1 = new Date(maxModified);
//            LOG.debug("file : "+ file + " Last Modified Date : " + sdf.format(dt) + ", Max Modified : " + sdf.format(dt1));
        }

        String file = path + "/" + CANNED_SET_DEFINITION_FILE;
        File cfh = new File(file);
        if (!cfh.exists()) {
            LOG.error("Canned Set Definition File : " + file + " not found");
            throw new Exception("Canned Set Definition File : " + file + " not found" );
        }
        maxModified = Math.max(maxModified, cfh.lastModified());

        if (maxModified > lastModified ) {
            dt = new Date(maxModified);
            LOG.debug("Max modified " + sdf.format(dt));
            return maxModified;
        }
//        LOG.debug("Max Modified -1");
        return -1;
    }

    public static Map<String, IFSCannedSet> readDefinitionFile(String path) throws Exception {
        String file = path + "/" + CANNED_SET_DEFINITION_FILE;
        return GetCannedSetDefinitions.getCannedSetDefinitions(file);
    }



    public static List<IFSCannedSet> readCannedSetsFile(String path, String cannedSetCollection,Map<String, IFSCannedSet> map) throws Exception {
        String file = path + "/" + COLLECTION_PREFIX + cannedSetCollection + ".txt";
        List<IFSCannedSet> list = GetCannedSetCandidates.getCannedSetCandidates(file,map);
        return list;
    }

    public static List<String> getCollectionNames(String path) throws Exception {
        File dir = new File(path);
        Set<String> collectionNameSet = new HashSet<>();
        String regex = COLLECTION_PREFIX + "[A-Z][a-z]*.txt";
        String[] files = dir.list();
        for(String file : files) {
            if (!file.matches(regex)) {
                continue;
            }
            String newFile = path + "/" + file;
            File cfh = new File(newFile);
            if (cfh.isDirectory())
                continue;
            String[] splits = file.split("[_]");
            if (splits.length != 2) {
                LOG.info("Collection Name files should of format candidates_[A-Z][a-z]*.txt, but file name is : " + file);
                continue;
            }
            splits[1] = splits[1].trim();
            int len = splits[1].length();
            if (len < 5) {
                LOG.error("Candidate canned set collection files expected file name format is 'candidates_[A-Z][a-z]*.txt");
                throw new Exception("Candidate canned set collection files expected file name format is 'candidates_[A-Z][a-z]*.txt");
            }
            len -= 4;

            String collectionName = splits[1].substring(0,len);
            LOG.debug("Collection Name : " + collectionName + ", file : " + file);
            collectionNameSet.add(collectionName);

        }
        List<String> collectionNameList = new ArrayList<>(collectionNameSet.size());
        collectionNameList.addAll(collectionNameSet);
        if (collectionNameList.isEmpty()) {
            LOG.error("Candidates collection name files not exist");
            throw new Exception("Candidates collection name files not exist");
        }
        Collections.sort(collectionNameList);
        return collectionNameList;
    }

    public static void main(String[] args) throws Exception {
        String path = "~/src/main/resources";

        Map<String, IFSCannedSet> map = readDefinitionFile(path);
        Map<String, List<IFSCannedSet>> collectionMap = new HashMap<>();
        List<String> collectionNames = getCollectionNames(path);
        for(String collectionName : collectionNames) {
            List<IFSCannedSet> list = readCannedSetsFile(path,collectionName,map);
            collectionMap.put(collectionName, list);
        }

        System.out.println("Map :");
        for(String cannedSet : map.keySet()) {
            IFSCannedSet ifsCannedSet = map.get(cannedSet);
            printIfsCannedSet(ifsCannedSet);
        }
        for(String collectionName : collectionNames) {
            System.out.println(collectionName + " List :");
            List<IFSCannedSet> list = collectionMap.get(collectionName);

            for (IFSCannedSet ifsCannedSet : list) {
                printIfsCannedSet(ifsCannedSet);
            }
        }
    }

    private static void printIfsCannedSet(IFSCannedSet ifsCannedSet) {
        String cannedSetName = ifsCannedSet.getName();
        IFSParameterSpec spec = ifsCannedSet.getParameterSpec();
        String model = spec.getModel();
        List<IFSParameterValue> paramList = spec.getParameterValues();
        for(IFSParameterValue parameterValue : paramList) {
            System.out.println(String.format("%-30s %-30s %-30s %-30s",cannedSetName,model,parameterValue.getParameter(), parameterValue.getValue()));
        }
    }
}
