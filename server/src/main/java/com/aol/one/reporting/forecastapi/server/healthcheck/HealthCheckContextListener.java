/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.healthcheck;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlets.HealthCheckServlet.ContextListener;

import javax.servlet.annotation.WebListener;

/**
 * The class HealthCheckContextListener. The servlet context listener that creates the global health
 * check registry on application startup.
 */
@WebListener("Servlet Context Listener that creates global healthcheck registry")
public class HealthCheckContextListener extends ContextListener {
    private static HealthCheckRegistry healthChecks = new HealthCheckRegistry();

    /*
     * (non-Javadoc)
     *
     * @see com.codahale.metrics.servlets.HealthCheckServlet.ContextListener#getHealthCheckRegistry()
     */
    @Override
    protected HealthCheckRegistry getHealthCheckRegistry() {
        return healthChecks;
    }


    /**
     * Gets the healthChecks.
     *
     * @return the healthChecks
     */
    public static HealthCheckRegistry getHealthchecks() {
        return healthChecks;
    }

}
