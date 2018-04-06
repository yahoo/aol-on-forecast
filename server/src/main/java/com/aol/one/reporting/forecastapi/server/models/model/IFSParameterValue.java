/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.models.model;

/**
 * IFS model parameter value.
 */
public final class IFSParameterValue {
	private String	Parameter;
	private String	Value;
	
	/**
	 * Default constructor.
	 */
	public IFSParameterValue() {
		setParameter("");
		setValue("");
	}
	
	/**
	 * Fully specified constructor.
	 * 
	 * @param parameter Parameter name.
	 * @param value Parameter value.
	 */
	public IFSParameterValue(
		String	parameter,
		String	value
	) {
		setParameter(parameter);
		setValue(value);
	}
	
	/**
	 * Clone this parameter value.
	 * 
	 * @return Parameter value clone.
	 */
	public IFSParameterValue clone() {
		return new IFSParameterValue(Parameter, Value);
	}
	
	/**
	 * @return the parameter name
	 */
	public String getParameter() {
		return Parameter;
	}
	
	/**
	 * @param parameter the parameter to set
	 */
	public void setParameter(String parameter) {
		Parameter = parameter;
	}
	
	/**
	 * @return the value
	 */
	public String getValue() {
		return Value;
	}
	
	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		Value = value;
	}
	
}
