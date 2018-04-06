/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.models.alg;

import java.util.List;

import com.aol.one.reporting.forecastapi.server.models.model.IFSException;
import com.aol.one.reporting.forecastapi.server.models.model.IFSModel;
import com.aol.one.reporting.forecastapi.server.models.model.IFSParameterValue;
import com.aol.one.reporting.forecastapi.server.models.model.IFSUsageDescription;

/**
 * Class implementing moving average forecasting model. The most recent point
 * window is averaged and the value is copied forward as the forecast. The
 * model supports the following specific parameters:
 * 
 * window -- Number of most recent points to use in computing the average. If
 *    there are fewer points, those will be used instead.
 * 
 * If there are less than 2 points in the series a random walk forecast is
 * produced. 
 */
public final class IFSModelImplMovAvg extends IFSModel {
	public static final String					ModelName = "model_movavg";
	private static final IFSUsageDescription	UsageDescription
	= new IFSUsageDescription(
  "Moving average forecast model implementation.\n",
  "Implements the moving average forecast model which averages the most\n"
+ "recent point window and uses that as the forecast.\n",
  "\n"
+ "window=<# points> -- # of recent points to average. If there are fewer\n"
+ "                     points, those will be used instead. The default is 14.\n"
	);
	
	private static final int				DefaultWindowSize = 14;
	private static final int				MinSeriesLength = 2;
	
	private int								Window = DefaultWindowSize;

	/* (non-Javadoc)
	 * @see com.aol.ifs.soa.common.IFSModel#execModel(double[], double[])
	 */
	@Override
	protected String execModel(
		double[]	series,
		double[]	forecasts,
		int			cycle
	) throws IFSException {
		int			n = series.length;
		int			nf = forecasts.length;
		
		if (n < MinSeriesLength) {
			for (int i = 0; i < nf; i++)
				forecasts[i] = series[n-1];
			return "rw";
		}
		
		double		avg = 0.0;
		double		sum = 0.0;
		int			nv = 0;
		
		if (n >= Window) {
			for (int i = n-Window; i < n; i++)
				sum += series[i];
			avg = sum/(double)Window;
			nv = Window;
		} else {
			for (double value : series)
				sum += value;
			avg = sum/(double)n;
			nv = n;
		}
		
		for (int i = 0; i < nf; i++)
			forecasts[i] = avg;
		
		return String.format("movavg::avg:%.3f,nv:%d", avg, nv);
	}

	/* (non-Javadoc)
	 * @see com.aol.ifs.soa.common.IFSModel#getModelName()
	 */
	@Override
	public String getModelName() {
		return ModelName;
	}

	/* (non-Javadoc)
	 * @see com.aol.ifs.soa.common.IFSModel#getUsage()
	 */
	@Override
	public IFSUsageDescription getUsage() {
		return UsageDescription;
	}

	/* (non-Javadoc)
	 * @see com.aol.ifs.soa.common.IFSModel#injectParameters(java.util.List)
	 */
	@Override
	protected void injectParameters(
		List<IFSParameterValue> parameters
	) throws IFSException {
		int	window = DefaultWindowSize;
		
		if (parameters != null && parameters.size() > 0)
			for (IFSParameterValue parameter : parameters)
				if (parameter.getParameter().equals("window")) {
					try {
					window = Integer.parseInt(parameter.getValue());
					}
					catch (NumberFormatException ex) {
						throw new IFSException(25, getModelName(), "window");
					}
					if (window < 1)
						throw new IFSException(25, getModelName(), "window");
				} else
					throw new IFSException(23, getModelName(),
						parameter.getParameter());
		
		Window = window;
	}
}
