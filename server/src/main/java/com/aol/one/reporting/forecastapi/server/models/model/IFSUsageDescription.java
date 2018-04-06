/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.models.model;

/**
 * Usage information description in component form. Support for information
 * reuse, particularly models.
 *
 */
public final class IFSUsageDescription {
	private String	Body;
	private String	Parameters;
	private String	Summary;
	
	/**
	 * Default constructor.
	 */
	public IFSUsageDescription() {
		setBody("");
		setParameters("");
		setSummary("");
	}
	
	/**
	 * Fully specified constructor.
	 * 
	 * @param summary Summary line.
	 * @param body Overall description.
	 * @param Parameters Parameter description. 
	 */
	public IFSUsageDescription(
		String	summary,
		String	body,
		String	parameters
	) {
		setSummary(summary);
		setBody(body);
		setParameters(parameters);
	}
	
	/**
	 * @return the body
	 */
	public String getBody() {
		return Body;
	}
	
	/**
	 * @param body the body to set
	 */
	public void setBody(String body) {
		Body = body;
	}
	
	/**
	 * @return the parameters
	 */
	public String getParameters() {
		return Parameters;
	}
	
	/**
	 * @param parameters the parameters to set
	 */
	public void setParameters(String parameters) {
		Parameters = parameters;
	}
	
	/**
	 * @return the summary
	 */
	public String getSummary() {
		return Summary;
	}
	
	/**
	 * @param summary the summary to set
	 */
	public void setSummary(String summary) {
		Summary = summary;
	}
	
}
