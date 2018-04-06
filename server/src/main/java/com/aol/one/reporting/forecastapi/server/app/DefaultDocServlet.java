/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.app;

import org.apache.catalina.servlets.DefaultServlet;

import javax.servlet.annotation.WebServlet;

/**
 * The Class DefaultDocServlet. This is a placeholder just so we can add the @WebServlet annotation
 * for servlet deployment.
 */
@WebServlet(urlPatterns = {"/doc/*"})
public class DefaultDocServlet extends DefaultServlet {
    private static final long serialVersionUID = 1L;
}
