/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.resource;

import com.aol.one.reporting.forecastapi.server.app.IfsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@Path("/")
@Produces(MediaType.TEXT_HTML)
public class WelcomeResource {
	private static final Logger LOG = LoggerFactory.getLogger(WelcomeResource.class);
	
	@GET
	public String getIndex() {
		String swaggerBaseUrl = null;
		try {
			swaggerBaseUrl = IfsConfig.config().getProperty(
				"swagger.api.basepath", "swagger.api.basepath.not.set");
		} catch (IOException ex) {
			LOG.error("Cannot fetch swagger.api.basepath property.", ex);
			swaggerBaseUrl = "swagger.api.basepath.not.set";
		}
		String welcome = "<!DOCTYPE html>"
			+ "<html><body>"
			+ "<iframe"
			+ String.format(" src=\"%s/doc/index.html\" ", swaggerBaseUrl)
			+ "onload=\"this.width=screen.width;this.height=screen.height;\"></iframe>"
			+ "</body></html>";
		LOG.debug(String.format("Welcome this: %s", welcome));
		return welcome;
	}


}
