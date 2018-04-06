/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.models.model;

import java.util.ArrayList;
import java.util.List;

/**
 * IFS model parameter specification.
 */
public final class IFSParameterSpec {
	private String					Model;
	private List<IFSParameterValue>	ParameterValues;
	
	/**
	 * Default constructor.
	 */
	public IFSParameterSpec() {
		setModel("");
		setParameterValues(null);
	}

	/**
	 * Fully specified constructor.
	 * 
	 * @param model IFS model name.
	 * @param parameter_values Model parameters.
	 */
	public IFSParameterSpec(
		String					model,
		List<IFSParameterValue>	parameter_values
	) {
		setModel(model);
		setParameterValues(parameter_values);
	}
	
	/**
	 * Clone this parameter spec.
	 * 
	 * @return Parameter spec clone.
	 */
	public IFSParameterSpec clone() {
		List<IFSParameterValue>	values = new ArrayList<>();
		
		for (IFSParameterValue value : ParameterValues) {
			values.add(value.clone());
		}
		return new IFSParameterSpec(Model, values);
	}
	
	/**
	 * @return the model
	 */
	public String getModel() {
		return Model;
	}
	
	/**
	 * @param model the model to set
	 */
	public void setModel(String model) {
		Model = model;
	}
	
	/**
	 * @return the parameterValues
	 */
	public List<IFSParameterValue> getParameterValues() {
		return ParameterValues;
	}
	
	/**
	 * @param parameterValues the parameterValues to set
	 */
	public void setParameterValues(List<IFSParameterValue> parameterValues) {
		ParameterValues = parameterValues;
	}
	
}
