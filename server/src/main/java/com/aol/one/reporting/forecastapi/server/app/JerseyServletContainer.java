/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.app;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import javax.servlet.annotation.WebServlet;

@WebServlet(loadOnStartup = 1)
public class JerseyServletContainer extends ServletContainer {
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new jersey servlet container.
     */
    public JerseyServletContainer() {
        super();
    }

    /**
     * Instantiates a new jersey servlet container.
     *
     * @param resourceConfig the resource config
     */
    public JerseyServletContainer(ResourceConfig resourceConfig) {
        super(resourceConfig);
    }
}
