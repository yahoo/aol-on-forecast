/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/
package com.aol.one.reporting.forecastapi.server.app;

import ch.qos.logback.classic.LoggerContext;
import com.aol.one.reporting.forecastapi.server.models.cs.IFSCannedSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * The class Log back ContextListener. A servlet context listener whose only job is to shut down the
 * log back context when the applications shuts down. This is necessary to make sure the worker
 * thread used by the AsyncAppender in log back gets properly shut down.
 */
@WebListener("Servlet Context Listener that shuts down log back on application shutdown")
public class CannedSetFileListener implements ServletContextListener {

    private static final Logger LOG = LoggerFactory.getLogger(CannedSetFileListener.class);

    private AtomicBoolean terminate = new AtomicBoolean(false);
    private Object sleepLock = new Object();
    private ExecutorService executorService;

    /* (non-Javadoc)
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextDestroyed(ServletContextEvent event) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.stop();
        terminate.set(true);
        synchronized (sleepLock) {
            sleepLock.notifyAll();
        }

        executorService.shutdownNow();
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextInitialized(ServletContextEvent event) {
        executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new CannedSetFileReader());
    }

    private class CannedSetFileReader implements Runnable {

        private static final String INTERVAL_PROPERTY_NAME = "ifs.file.change.check.interval";
        private static final long DEFAULT_INTERVAL = 60000;
        private static final String DEFAULT_PATH = "/data/input";


        @Override
        public void run() {
            LOG.debug("CannedSet file reader thread started");

            long lastModified = 0;

            while (!terminate.get()) {
                Long checkInterval;
                String path = null;
                Properties prop;
                try {
                    checkInterval = Long.parseLong((String) IfsConfig.config().getProperty(INTERVAL_PROPERTY_NAME));

                } catch (IOException ie) {
                    LOG.error("Failed read property '" + INTERVAL_PROPERTY_NAME + "'", ie);
                    checkInterval = DEFAULT_INTERVAL;

                }

                path = IfsConfig.getWebInfDir();

                if (checkInterval == null) {
                    LOG.error("Failed read property '"
                            + INTERVAL_PROPERTY_NAME
                            + "' using default " + DEFAULT_INTERVAL
                            + " milliseconds");
                    checkInterval = DEFAULT_INTERVAL;
                }

                if (path == null) {
                    LOG.error("Path variable is null");
                    threadSleep(checkInterval);
                }


                try {
                    long modifiedTime = CannedSetFiles.isModified(path, lastModified);

                    if (modifiedTime != -1) {
                        Map<String, List<IFSCannedSet>> collectionNameMap = new HashMap<>();
                        List<String> collectionNames = CannedSetFiles.getCollectionNames(path);
                        Map<String, IFSCannedSet> cannedSetMap = CannedSetFiles.readDefinitionFile(path);
                        for (String collectionName : collectionNames) {
                            List<IFSCannedSet> ifsCannedSets = CannedSetFiles.readCannedSetsFile(path, collectionName, cannedSetMap);
                            collectionNameMap.put(collectionName, ifsCannedSets);
                        }
                        IfsConfig.getCache().switchCache(cannedSetMap, collectionNameMap, collectionNames);
                        lastModified = modifiedTime;
                        Date dt = new Date(lastModified);
                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                        LOG.debug("Last Modified time set to : " + sdf.format(dt));

                    }
                } catch (Exception ex) {
                    LOG.error("Backend thread throwing exception : " + ex.getMessage(), ex);
                }
                threadSleep(checkInterval);
            }
        }

        private void threadSleep(long sleepTime) {
            try {
                synchronized (sleepLock) {
                    sleepLock.wait(sleepTime);
                }
            } catch (InterruptedException ie) {
            }
        }
    }
}
