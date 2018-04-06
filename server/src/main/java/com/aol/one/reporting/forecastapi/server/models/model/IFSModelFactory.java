/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.models.model;

import java.util.List;
import java.util.TreeMap;

import com.aol.one.reporting.forecastapi.server.models.alg.IFSModelImplAR;
import com.aol.one.reporting.forecastapi.server.models.alg.IFSModelImplARIMA;
import com.aol.one.reporting.forecastapi.server.models.alg.IFSModelImplExpSm;
import com.aol.one.reporting.forecastapi.server.models.alg.IFSModelImplMovAvg;
import com.aol.one.reporting.forecastapi.server.models.alg.IFSModelImplRW;
import com.aol.one.reporting.forecastapi.server.models.alg.IFSModelImplRegress;

/**
 * Class that manufactures forecast models given the model name, model
 * parameters, and time series data.
 */
@SuppressWarnings("unchecked")
public final class IFSModelFactory {
	@SuppressWarnings("rawtypes")
	private static TreeMap<String, Class>	Models = new TreeMap<String, Class>();
	static {
		Models.put(IFSModelImplAR.ModelName,
			IFSModelImplAR.class);
		Models.put(IFSModelImplARIMA.ModelName,
			IFSModelImplARIMA.class);
		Models.put(IFSModelImplExpSm.ModelName,
			IFSModelImplExpSm.class);
		Models.put(IFSModelImplMovAvg.ModelName,
			IFSModelImplMovAvg.class);
		Models.put(IFSModelImplRegress.ModelName,
			IFSModelImplRegress.class);
		Models.put(IFSModelImplRW.ModelName,
			IFSModelImplRW.class);
	}
	
	/**
	 * Create a forecast model based on model name. The model created can be
	 * reused to do more than one set of forecasts. The setup method can be
	 * used to process common parameters that reshape the time series data
	 * and pass along specific parameters to the model.
	 * 
	 * @param model_name Name of the forecast model to create.
	 * 
	 * @return Forecast model.
	 * 
	 * @throws IFSException if the model name is unknown.
	 */
	public static IFSModel create(
		String			model_name
	) throws IFSException {
		if (model_name == null || model_name.equals("")) {
			throw new IFSException(13);
		}
		
		Class<IFSModel>	model_class = Models.get(model_name);
		
		if (model_class == null) {
			throw new IFSException(14, model_name);
		}
		
		IFSModel		model = null;
		
		try {
		model = model_class.newInstance();
		} catch (IllegalAccessException ex) {
			throw new IFSException(15, model_name, ex.getMessage());
		} catch (InstantiationException ex) {
			throw new IFSException(15, model_name, ex.getMessage());
		}
		
		return(model);
	}
	
	/**
	 * Setup a forecast model based on time series data and the model
	 * parameters. The parameters can include common ones that cause
	 * the time series to be re-shaped as well as parameters specific to
	 * the model. This method allows forecast models to be reused.
	 * 
	 * @param model Forecast model to setup.
	 * @param series Time series data.
	 * @param parameters Model parameters (common and specific).
	 * 
	 * @return Forecast model.
	 * 
	 * @throws IFSException if there is a problem with the time series,
	 *    the model parameters, or a null model was passed.
	 */
	public static void setup(
		IFSModel				model,
		double[]				series,
		List<IFSParameterValue>	parameters
	) throws IFSException {
		if (model == null) {
			throw new IFSException(16);
		}

		model.setSeries(series);
		model.setParameters(parameters);
	}
	
	/**
	 * Fetch usage information for all the supported models.
	 * 
	 * @return Model usage information.
	 */
	public static String usage() {
		StringBuffer		usage_info = new StringBuffer();
		Class<IFSModel>		model_class = null;
		IFSModel			model = null;
		IFSUsageDescription	usage_desc = null;
		int					i = 1;
		
		for (String model_name : Models.keySet()) {
			model_class = Models.get(model_name);
			try {
			model = model_class.newInstance();
			} catch (InstantiationException ex) {
				System.err.println("Cannot create forecast model '"
				+ model_name
				+ "'. "
				+ ex.getMessage());
				System.exit(1);
			} catch (IllegalAccessException ex) {
				System.err.println("Cannot create forecast model '"
				+ model_name
				+ "'. "
				+ ex.getMessage());
				System.exit(1);
			}
			usage_desc = model.getUsage();
			usage_info.append("\n");
			usage_info.append(String.format("%2d. %-14.14s -- %s\n",
				i, model_name, usage_desc.getSummary()));
			usage_info.append(usage_desc.getBody());
			usage_info.append(usage_desc.getParameters());
			i++;
		}
		
		return(usage_info.toString());
	}
}
