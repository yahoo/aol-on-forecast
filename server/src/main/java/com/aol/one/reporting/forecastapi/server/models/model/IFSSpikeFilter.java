/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.models.model;

import com.aol.one.reporting.forecastapi.server.jpe.spikesmooth.SpikeSmooth;

/**
 * Class for processing the spike_filter common model parameter.
 */
public final class IFSSpikeFilter {
	public final static int		Disable = 0;
	public final static int		MinClippingWindow = 3;
	public final static int		MaxClippingWindow = 30;
	
	/**
	 * Fetch spike filtered series with a string clipping_window parameter.
	 * Same as the integer clipping_window version except string is used
	 * allowing a model parameter value to be specified directly.
	 * 
	 * @see getSpikeFilteredSeries(double[] series, int clipping_window).
	 */
	public static double[] getSpikeFilteredSeries(
		double[]	series,
		String		clipping_window
	) throws IFSException {
		int			i_clipping_window = 0;
		
		try {
		i_clipping_window = Integer.parseInt(clipping_window);
		}
		catch (NumberFormatException ex) {
			throw new IFSException(40);
		}
		
		return(getSpikeFilteredSeries(series, i_clipping_window));
	}

	/**
	 * Fetch spike filtered series with an integer clipping_window parameter.
	 * 
	 * @param series Time series to filter.
	 * @param clipping_window Size of clipping window. A value of 0 disables
	 *    the filter (i.e. original series is returned). Otherwise clipping
	 *    window is expected to be in the range 3 to 30 inclusive.
	 *    
	 * @return Original series or filtered series depending on clipping_window
	 *    value.
	 *    
	 * @throws IFSException Thrown for invalid clipping window specifications
	 *    or an unexpected error occurred.
	 */
	public static double[] getSpikeFilteredSeries(
		double[]	series,
		int			clipping_window
	) throws IFSException {
		if (series == null
		|| series.length < MinClippingWindow
		|| clipping_window == Disable)
			return(series);
		
		if (clipping_window != Disable
		&& !(MinClippingWindow <= clipping_window 
				&& clipping_window <= MaxClippingWindow))
			throw new IFSException(41);
		
		double[]		filtered_series
			= SpikeSmooth.smoothSpike(series, clipping_window);
		if (filtered_series == null)
			throw new IFSException(42);
		
		return(filtered_series);
	}
	
	/**
	 * Canned usage information for spike_filter parameter.
	 * 
	 * @return Usage string.
	 */
	public static String usage() {
		return(
  "\n"
+ "spike_filter=<0 | [3,30]>\n"
+ "   0      -- Disable spike filtering. Time series returned is not changed.\n"
+ "   [3,30] -- Enable spike filtering with the specified clipping\n"
+ "             window size. The minimum size is 3 and the maximum is 30.\n"
		);
	}
}
