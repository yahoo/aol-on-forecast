/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.metrics;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.servlets.AdminServlet;
import com.codahale.metrics.servlets.HealthCheckServlet;
import com.codahale.metrics.servlets.MetricsServlet;
import com.codahale.metrics.servlets.MetricsServlet.ContextListener;
import com.codahale.metrics.servlets.PingServlet;
import com.codahale.metrics.servlets.ThreadDumpServlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;
import javax.servlet.annotation.WebServlet;


/**
 * Servlet context listener to make the metrics registry for this app available to the Metrics
 * Servlet provided by the Metrics library.
 */
@WebListener("Servlet Context Listener that creates global metrics registry")
public class MetricsContextListener extends ContextListener {

    private final static MetricRegistry METRICS = new MetricRegistry();
    JmxReporter reporter = null;

    /**
     * Gets the metrics.
     *
     * @return the metrics
     */
    public static MetricRegistry getMetrics() {
        return METRICS;
    }


    /*
     * (non-Javadoc)
     *
     * @see com.codahale.metrics.servlets.MetricsServlet.ContextListener#getMetricRegistry()
     */
    @Override
    protected MetricRegistry getMetricRegistry() {
        return METRICS;
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        super.contextDestroyed(sce);
        this.reporter.stop();
        this.reporter = null;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        super.contextInitialized(sce);

        // start up Metrics JMX Reporter
        this.reporter = JmxReporter.forRegistry(getMetrics()).build();
        this.reporter.start();
    }

    @WebServlet(urlPatterns = {"/admin/*"}, loadOnStartup = 1)
    public static class MetricsAdminServlet extends AdminServlet {
        private static final long serialVersionUID = 1L;

    }

    @WebServlet(urlPatterns = {"/admin/ping/*"}, loadOnStartup = 1)
    public static class MetricsPingServlet extends PingServlet {
        private static final long serialVersionUID = 1L;

    }

    @WebServlet(urlPatterns = {"/admin/healthcheck/*"}, loadOnStartup = 1)
    public static class MetricsHealthCheckServlet extends HealthCheckServlet {
        private static final long serialVersionUID = 1L;

    }

    @WebServlet(urlPatterns = {"/admin/metrics/*"}, loadOnStartup = 1)
    public static class MetricsMetricsServlet extends MetricsServlet {
        private static final long serialVersionUID = 1L;

    }

    @WebServlet(urlPatterns = {"/admin/threads/*"}, loadOnStartup = 1)
    public static class MetricsThreadsServlet extends ThreadDumpServlet {
        private static final long serialVersionUID = 1L;

    }
}
