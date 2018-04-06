/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.models.alg;

import com.aol.one.reporting.forecastapi.server.models.model.IFSException;
import com.aol.one.reporting.forecastapi.server.models.model.IFSModel;
import com.aol.one.reporting.forecastapi.server.models.model.IFSParameterValue;
import com.aol.one.reporting.forecastapi.server.models.model.IFSUsageDescription;

import java.util.List;

/**
 * Forecasting model that implements a random walk with an optional cycle.
 * Basically the most recent specified number if values are copied forward
 * as the forecast. If the series is less than the specified number of values
 * in length, the most recent value is copied forward. The model supports the
 * following specific parameters:
 * 
 * cycle -- Number of most recent points to copy forward. If there are fewer
 *    fewer points, the most recent value will be copied forward. If set
 *    to -1, the cycle, if any, is set automatically.
 * 
 */
public final class IFSModelImplRW extends IFSModel {
	public static final String					ModelName = "model_rw";
	private static final IFSUsageDescription	UsageDescription
		= new IFSUsageDescription(
  "Forecast model that implements a random walk with an optional cycle.\n",
  "The model is implemented by copying forward the most recent specified\n"
+ "number of values. If the series is less than the specified number of\n"
+ "values in length, the most recent value is copied forward.\n",
"\n");
	
	/* Implements execModel method.
	 * 
	 * @see com.aol.ifs.soa.common.IFSModel#execModel(double[])
	 */
	@Override
	protected String execModel(
		double[]	series,
		double[]	forecasts,
		int			cycle
	) throws IFSException {
		if (series.length < cycle)
			for (int i = 0; i < forecasts.length; i++)
				forecasts[i] = series[series.length-1];
		else {
				if(cycle==0)
					cycle=1;
				for (int i = 0; i < forecasts.length; i++)
						forecasts[i] = series[series.length - cycle + i % cycle];
		}
		return String.format("rw::cycle:%d", cycle);
	}

	/* Implements getModelName method.
	 * 
	 * @see com.aol.ifs.soa.common.IFSModelInterface#getModelName()
	 */
	@Override
	public String getModelName() {
		return ModelName;
	}

	/* Implements getUsage method.
	 * 
	 * @see com.aol.ifs.soa.common.IFSModelInterface#getUsage()
	 */
	@Override
	public IFSUsageDescription getUsage() {
		return UsageDescription;
	}
	
	/* Implements injectParameters method.
	 * 
	 * @see com.aol.ifs.soa.common.IFSModel#injectParameters(List<IFSParameterValue>)
	 */
	@Override
	protected void injectParameters(
		List<IFSParameterValue>	parameters
	) throws IFSException {
		if (parameters != null && parameters.size() > 0)
			for (IFSParameterValue parameter : parameters)
				throw new IFSException(23, getModelName(),
					parameter.getParameter());
	}
}
