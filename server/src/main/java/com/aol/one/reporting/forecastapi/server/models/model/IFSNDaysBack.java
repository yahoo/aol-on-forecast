/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.models.model;

/**
 * Class for processing the ndays_back common model parameter.
 */
public final class IFSNDaysBack {
	
	/**
	 * Fetch ndays_back filtered series with a specification parameter.
	 * The series will be reshaped according to the specification. The
	 * specification is as follows:
	 * 
	 * <fract>    -- Percentage of most recent historical values to use.
	 *    Value must be > 0.0 and <= 1.0.
	 * p<-fract>   -- Percentage of least recent historical values to not use.
	 *    Value must be > -1.0 and < 0.0.
	 * z<fract>    -- Remove leading data in historical values up to and
	 *    including last zero as long as data removed is less than or equal
	 *    to the specified percentage of values. If there are no zeroes in
	 *    data or last zero exceeds percentage, no values are removed. Value
	 *    must be > 0.0 and < 1.0.
	 * <gt 0>      -- # most recent historical values to use.
	 * <eq 0>      -- Use all historical values.
	 * <lt 0>      -- # least recent historical values to not use.
	 *
	 * @param series Time series to reshape.
	 * @param spec Reshaping specification. See above for possible values.
	 * 
	 * @return Reshaped time series.
	 * 
	 * @throws IFSException for invalid specifications or unexpected series
	 *    values.
	 */
	public static double[] getNDaysBackSeries(
		double[]	series,
		String		spec
	) throws IFSException {
		
		if (series == null || series.length < 1)
			throw new IFSException(31);
		else if (spec == null || spec.equals(""))
			throw new IFSException(32);
		
		// Reshape the series.
		
		int			nv = 0;
		int			tnv = 0;
		double		fraction = 0.0;
		
		// Process fractional specification.
		
		if (spec.charAt(0) == 'p') {
			try {
			fraction = Double.parseDouble(spec.substring(1));
			}
			catch (NumberFormatException ex) {
				throw new IFSException(33);
			}
			if (fraction == 0.0)
				throw new IFSException(34);
			else if (fraction <= -1.0)
				throw new IFSException(35);
			else if (fraction > 1.0)
				throw new IFSException(36);
			
			if (fraction > 0.0)
				nv = (int)(fraction*series.length);
			else {
				fraction = -fraction;
				tnv = (int)(fraction*series.length);
				nv = series.length-tnv;
			}
		}
		
		// Process remove initial zeroes specification.
		
		else if (spec.charAt(0) == 'z') {
			int			zpos;
			double		zfrac;

			try {
			fraction = Double.parseDouble(spec.substring(1));
			}
			catch (NumberFormatException ex) {
				throw new IFSException(37);
			}
			if (fraction <= 0.0 || fraction >= 1.0)
				throw new IFSException(38);

			zpos = 0;
			for (int i = 0; i < series.length; i++)
				if (series[i] == 0.0)
					zpos = i+1;
			zfrac = (double)zpos / (double)series.length;
			if (zfrac <= fraction)
				nv = series.length-zpos;
			else
				nv = series.length;
		}
		
		// Process integer specification.
		
		else {
			try {
			tnv = Integer.parseInt(spec);
			}
			catch (NumberFormatException ex) {
				throw new IFSException(39);
			}
			
			if (tnv == 0)
				nv = series.length;
			else if (tnv > 0)
				nv = (tnv > series.length) ? series.length : tnv;
			else
				nv = series.length-tnv;
			if (nv <= 1)
				nv = 2;
		}
	
		// Allocate and copy into new series shape.
		
		double[]	new_series = new double[nv];
		
		for (int i = series.length-nv, j = 0; i < series.length; j++, i++)
			new_series[j] = (i < 0) ? 0 : series[i];
		return(new_series);
	}
	
	/**
	 * Canned usage information for ndays_back parameter.
	 * 
	 * @return Usage string.
	 */
	public static String usage() {
		return(
  "\n"
+ "ndays_back=<ndays back spec>\n"
+ "   p<fract>  -- Percentage of most recent historical values to use.\n"
+ "                Value must be > 0.0 and <= 1.0.\n"
+ "   p<-fract> -- Percentage of least recent historical values to not use.\n"
+ "                Value must be > -1.0 and < 0.0.\n"
+ "   z<fract>  -- Remove leading data in historical values up to and\n"
+ "                including last zero as long as data removed is less than or\n"
+ "                equal to the specified percentage of values. If there are\n"
+ "                no zeroes in data or last zero exceeds percentage, no values\n"
+ "                are removed. Value must be > 0.0 and < 1.0.\n"
+ "   <gt 0>    -- # most recent historical values to use.\n"
+ "   <eq 0>    -- Use all historical values.\n"
+ "   <lt 0>    -- # least recent historical values to not use.\n"
		);
	}
}
