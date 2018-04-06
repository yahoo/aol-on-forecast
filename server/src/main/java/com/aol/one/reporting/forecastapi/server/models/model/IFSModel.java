/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.models.model;

import java.util.LinkedList;
import java.util.List;

import com.aol.one.reporting.forecastapi.server.models.model.IFSTransformType.Types;


/**
 * Class which implements the IFS forecast model base class. This class
 * implements common administration methods such as getting time series
 * and parameters into the model and getting forecasts out of the model.
 * This allows the actual model implementation to focus on generating forecasts
 * and not the interface administration. The only methods an implementation
 * needs to provide are the following:
 * 
 * - execute model method (protected method).
 * - fetch the model name (public method).
 * - inject model specific parameter values (protected method).
 * - provide a usage description (public method).
 * 
 * This keeps the nuances of getting data into and out of the model internal
 * to the public interfaces provided by this class leaving less room for 
 * messing up model implementations unless it is in the forecast algorithm
 * itself. Also common parameters are handled in a uniform manner. These are
 * the following parameters:
 * 
 * - cycle
 * - ndays_back
 * - spike_filter
 * - transform_type
 */
public abstract class IFSModel {
	private double[]			Series = null;
	private double[]			Forecasts = null;
	private String				CalibInfo = "";
	IFSParameterValue			Cycle = null;
	IFSParameterValue			NDaysBack = null;
	IFSParameterValue			SpikeFilter = null;
	IFSParameterValue			TransformType = null;
	List<IFSParameterValue>		Parameters = null;
	
	/**
	 * This method implements the algorithm to generate forecasts.
	 * 
	 * @param series Time series to use as forecast basis.
	 * @param forecasts Preallocated forecast vector in which to place
	 *    forecasts.
	 * @param cycle Seasonal cycle.
	 * 
	 * @returns String containing calibration summary information. Should
	 *    return at least an empty string. Returning null is a bad idea.
	 *    
	 * @throws IFSException if an unexpected error occurs in generating
	 *    forecasts.
	 */
	protected abstract String execModel(
		double[]	series,
		double[]	forecasts,
		int			cycle
	) throws IFSException;

	/**
	 * This method is called to generate the designated number of forecasts.
	 * This allows forecasts to be generated in the background rather than
	 * having a synchronous method that generates the forecasts. The generated
	 * forecasts can be fetched later using the forecast fetch method. Also
	 * the method must be called upon to generate at least 1 forecast point.
	 * Returns a string representing forecast model calibration information.
	 * 
	 * @param num_forecasts Number of forecasts to generate. Must be at least 1.
	 * 
	 * @return Forecast model calibration summary string.
	 * 
	 * @throws IFSException Thrown if number of forecasts is less than 1 or
	 *    series has not been set or some unforeseen event occurs in generating
	 *    the forecasts.
	 */
	public final String generateForecasts(
		int		num_forecasts
	) throws IFSException {
		if (num_forecasts < 1)
			throw new IFSException(9, getModelName());
		else if (Series == null || Series.length < 1)
			throw new IFSException(10, getModelName());
		
		// Check to see if forecasts have been previously generated.
		
		if (Forecasts != null)
			return CalibInfo;
		
		double[]	series = Series;
		Types		transform = Types.None;
		int			cycle = 0;
		
		// Clip any spikes first.
		
		if (SpikeFilter != null)
			series = IFSSpikeFilter.getSpikeFilteredSeries(series,
				SpikeFilter.getValue());
		
		// Determine seasonal cycle.
		
		if (Cycle != null)
			cycle = IFSCycle.getCycle(series, Cycle.getValue());
		
		// Re-shape the time series.
		
		if (NDaysBack != null)
			series = IFSNDaysBack.getNDaysBackSeries(series,
				NDaysBack.getValue());
		
		// Perform any transformation.
		
		if (TransformType != null) {
			transform = IFSTransformType.getTransformType(
				TransformType.getValue());
			series = IFSTransformType.getTransformedValues(series, transform);
		}
			
		double[]	forecasts = new double[num_forecasts];
		
		CalibInfo = execModel(series, forecasts, cycle);
		Forecasts = IFSTransformType.getUntransformedValues(forecasts, transform);
		
		return CalibInfo;
	}

	/**
	 * Fetch the forecasts that were generated. If there aren't any, an
	 * exception is thrown.
	 * 
	 * @return Generated forecasts.
	 * 
	 * @throws IFSException Thrown when no forecasts were previously generated.
	 */
	public final double[] getForecasts() throws IFSException {
		if (Forecasts == null)
			throw new IFSException(11, getModelName());
		
		return(Forecasts);
	}
	
	/**
	 * Fetch the name of the forecast model.
	 * 
	 * @return Forecast model name.
	 */
	public abstract String getModelName();

	/**
	 * Fetch the parameters used to guide the forecast generation (if any
	 * were specified).
	 * 
	 * @return Parameter values or null if none were specified.
	 */
	public final List<IFSParameterValue> getParameters() {
		return(Parameters);
	}

	/**
	 * Fetch the time series used as the basis for forecasts.
	 * 
	 * @return Time series data. Can be null if series data has not been set.
	 */
	public final double[] getSeries() {
		return(Series);
	}

	/**
	 * Every model is expected to provide a text description of itself.
	 * 
	 * @return Text description of model and its parameters.
	 */
	public abstract IFSUsageDescription getUsage();
	
	/**
	 * Inject model specific parameters into the model to guide its behavior.
	 * 
	 * @param parameters Model parameters. Some models don't have any. In any
	 *    case a null should cause settings to revert to default values.
	 * 
	 * @throws IFSException if there is a parameter problem.
	 */
	protected abstract void injectParameters(
		List<IFSParameterValue>	parameters
	) throws IFSException;
	
	/**
	 * Set model parameters. Generally a null value is allowed since it is
	 * expected models will have a robust set of default values allowing
	 * forecasts to be generated with just some historical data. However,
	 * unknown parameters should raise an exception. Note also that setting
	 * null should cause the default values to be set and not allow previously
	 * set parameter values to persist.
	 * 
	 * @param parameters Model parameter values. Null is legal as it should
	 *    cause defaults to be used. The list of parameters includes common
	 *    as well as specific parameters.
	 * 
	 * @throws IFSException Thrown when unknown or badly specified parameters
	 *    are passed.
	 */
	public final void setParameters(
		List<IFSParameterValue>	parameters
	) throws IFSException {
		IFSParameterValue		cycle = null;
		IFSParameterValue		ndays_back = null;
		IFSParameterValue		spike_filter = null;
		IFSParameterValue		transform_type = null;
		List<IFSParameterValue>	specific_parameters = null;
		
		if (parameters != null) {
			specific_parameters = new LinkedList<IFSParameterValue>();

			for (IFSParameterValue parameter : parameters)
				if (parameter.getParameter().equals("cycle"))
					cycle = parameter;
				else if (parameter.getParameter().equals("ndays_back"))
					ndays_back = parameter;
				else if (parameter.getParameter().equals("spike_filter"))
					spike_filter = parameter;
				else if (parameter.getParameter().equals("transform_type"))
					transform_type = parameter;
				else
					specific_parameters.add(parameter);
			
			// Check each common parameter to make sure it was specified
			// correctly. Use a fake series to do it.
			
			if (cycle != null)
				IFSCycle.getCycle(null, cycle.getValue());
			if (ndays_back != null)
				IFSNDaysBack.getNDaysBackSeries(new double[] {1, 2},
					ndays_back.getValue());
			if (spike_filter != null)
				IFSSpikeFilter.getSpikeFilteredSeries(new double[] {1, 2},
					spike_filter.getValue());
			if (transform_type != null)
				IFSTransformType.getTransformType(transform_type.getValue());
		}
		
		injectParameters(specific_parameters);
		
		Cycle = cycle;
		NDaysBack = ndays_back;
		TransformType = transform_type;
		SpikeFilter = spike_filter;
		Parameters = specific_parameters;
		Forecasts = null;
	}
	
	/**
	 * Set the time series history which is used as the forecast basis. The
	 * default transformation is none.
	 * 
	 * @param series Time series.
	 * 
	 * @throws IFSException Thrown if the series is null or empty.
	 */
	public final void setSeries(
		double[]	series
	) throws IFSException {
		if (series == null || series.length < 1)
			throw new IFSException(12, getModelName());
			
		Series = series;
		Forecasts = null;
	}
}
