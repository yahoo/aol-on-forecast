/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.models.model;

/**
 * Class for processing the cycle common model parameter.
 */
public final class IFSCycle {
	
	/**
	 * Fetch seasonal cycle. The specification is as follows:
	 * 
	 * <eq -1>     -- If the seasonal adjustment is to be determined
	 *                automatically.
	 * <eq 0>      -- If there is no seasonal adjustment.
	 * <eq 1>      -- If there is no seasonal adjustment.
	 * <gt 1>      -- The seasonal cycle to use. The historical data
	 *                length must be at least 2 times the cycle for the
	 *                seasonal variation  to be applied.
	 *
	 * @param series Time series to reshape.
	 * @param spec Cycle specification. See above for possible values.
	 * 
	 * @return Seasonal cycle.
	 * 
	 * @throws IFSException for invalid specifications or unexpected series
	 *    values.
	 */
	public static int getCycle(
		double[]	series,
		String		spec
	) throws IFSException {
		int			cycle = 0;
		
		try {
		cycle = Integer.parseInt(spec);
		}
		catch (NumberFormatException ex) {
			throw new IFSException(72);
		}
		if (cycle != -1 && cycle < 0)
			throw new IFSException(73);
		else if (cycle == -1) {
			if (series != null)
				cycle = IFSDetectSeasonalCycle.getSeasonalCycle(series);
		}
		
		return cycle;
	}
	
	/**
	 * Canned usage information for ndays_back parameter.
	 * 
	 * @return Usage string.
	 */
	public static String usage() {
		return(
  "\n"
+ "cycle=<integer>\n"
+ "   <eq -1> -- The seasonal adjustment is to be determined automatically.\n"
+ "   <eq 0>  -- There is no seasonal adjustment.\n"
+ "   <gt 1>  -- The cycle to mirror in the seasonal adjustment. The historical\n"
+ "              data length must be at least 2 times the cycle for the seasonal\n"
+ "              variation to be applied.\n"
		);
	}
}
