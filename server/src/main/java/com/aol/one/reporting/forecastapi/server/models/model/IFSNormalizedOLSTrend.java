/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.models.model;

import com.aol.one.reporting.forecastapi.server.models.util.GetTimeSeries;
import com.aol.one.reporting.forecastapi.server.models.util.Pair;


/**
 * This class places a normalized ordinary least squares (OLS) trend line
 * through a designated time series. The slope and y-intercept are then
 * available. To develop the trend line, the data is normalized into the
 * interval [0,1] and then a regression line over the point index is fitted
 * to the data. BUT simple OLS trend line is also supported.
 */
public final class IFSNormalizedOLSTrend {
	
	/**
	 * Compute normalized trend slope.
	 * 
	 * @param time_series Time series to which to fit a normalized trend line.
	 * 
	 * @return Trend line pair containing first an intercept and then a slope.

	 * @throws IFSException if series is null or not at least 2 points.
	 */
	public static Pair<Double, Double> getNormalizedOLSTrend(
		double[]	time_series
	) throws IFSException {
		if (time_series == null) {
			throw new IFSException("Normalized OLS trend processing does not "
			+ "support null time series.");
		}
		if (time_series.length < 2) {
			throw new IFSException("Normalized OLS trend processing does not "
			+ "support time series with less than two points.");
		}
		
		double[]	time_series_norm = new double[time_series.length];
		double		min_value = time_series[0];
		double		max_value = time_series[0];
		double		diff_value = 0;
		double		mean_value = 0;
		double		diff_t = 0;
		double		mean_t = 0;
		double		cov_value_t = 0;
		double		var_t = 0;
		
		for (int i = 1; i < time_series.length; i++) {
			min_value = Math.min(min_value, time_series[i]);
			max_value = Math.max(max_value, time_series[i]);
		}
		diff_value = max_value-min_value;
		if (diff_value == 0) {
			diff_value = 1;
		}
		
		for (int i = 0; i < time_series.length; i++) {
			time_series_norm[i] = (time_series[i]-min_value) / diff_value;
			mean_value += time_series_norm[i];
			mean_t += (double)(i+1);
		}
		mean_value /= time_series.length;
		mean_t /= time_series.length;
		
		for (int i = 0; i < time_series.length; i++) {
			diff_t = (double)(i+1)-mean_t;
			cov_value_t += diff_t * (time_series_norm[i]-mean_value);
			var_t += diff_t * diff_t;
		}
		
		double	slope = cov_value_t / var_t;
		double	intercept = mean_value - slope*mean_t;
		
		return new Pair<Double, Double>(intercept, slope);
	}
	
	/**
	 * Compute normalized trend slope with mean smoothed time series.
	 * 
	 * @param time_series Time series to which to fit a normalized mean
	 *    smoothed trend line.
	 * @param num_segms Number of mean segments to use. Must be at least 2
	 *    segments.
	 * 
	 * @return Trend line pair containing first an intercept and then a slope.

	 * @throws IFSException if series is null or not at least 3 points.
	 */
	public static Pair<Double, Double> getNormalizedOLSTrendMS(
		double[]	time_series,
		int			num_segms
	) throws IFSException {
		if (time_series == null) {
			throw new IFSException("Normalized OLS MS trend processing does not "
			+ "support null time series.");
		}
		if (time_series.length < 3) {
			throw new IFSException("Normalized OLS MS trend processing does not "
			+ "support time series with less than three points.");
		}
		
		return getNormalizedOLSTrend(getMeanSmooth(time_series, num_segms));
	}
	
	/**
	 * Compute trend slope.
	 * 
	 * @param time_series Time series to which to fit a trend line.
	 * 
	 * @return Trend line pair containing first an intercept and then a slope.

	 * @throws IFSException if series is null or not at least 2 points.
	 */
	public static Pair<Double, Double> getOLSTrend(
		double[]	time_series
	) throws IFSException {
		if (time_series == null) {
			throw new IFSException("OLS trend processing does not support "
			+ "null time series.");
		}
		if (time_series.length < 2) {
			throw new IFSException("OLS trend processing does not support "
			+ "time series with less than two points.");
		}
		
		double		mean_value = 0;
		double		diff_t = 0;
		double		mean_t = 0;
		double		cov_value_t = 0;
		double		var_t = 0;
		
		for (int i = 0; i < time_series.length; i++) {
			mean_value += time_series[i];
			mean_t += (double)(i+1);
		}
		mean_value /= time_series.length;
		mean_t /= time_series.length;
		
		for (int i = 0; i < time_series.length; i++) {
			diff_t = (double)(i+1)-mean_t;
			cov_value_t += diff_t * (time_series[i]-mean_value);
			var_t += diff_t * diff_t;
		}
		
		double	slope = cov_value_t / var_t;
		double	intercept = mean_value - slope*mean_t;
		
		return new Pair<Double, Double>(intercept, slope);
	}

	/**
	 * This program implements reading in a time series, referencing the
	 * designated amount of most recent data, fitting a normalized OLS trend
	 * line to the data, and then printing the slope and y-intercept.
	 * 
	 * @param args Time series file to read and how much data to use.
	 */
	public static void main(
		String[]	args
	) {
		int		offset = 0;
		OLSType	ols_type = OLSType.Normalize;
		int		num_segms = 0;
		
		if (args.length == 4) {
			String	opt = args[0];
			String	num_segms_s = args[1];
			
			if (!opt.equals("-ns")) {
				usage();
				System.exit(0);
			}
			
			try {
			num_segms = Integer.parseInt(num_segms_s);
			} catch (NumberFormatException ex) {
				System.err.format("Error: Number segments is not an integer.\n");
				System.exit(1);
			}
			if (num_segms == 0) {
				ols_type = OLSType.Plain;
			} else if (num_segms >= 2) {
				ols_type = OLSType.NormalizeMS;
			} else {
				System.err.format("Error: Number segments must be 0 or at least 2.\n");
				System.exit(1);
			}
			
			offset = 2;
		} else if (args.length != 2) {
			usage();
			System.exit(0);
		}
		
		String	num_points_s = args[0+offset];
		int		num_points = 0;
		
		try {
		num_points = Integer.parseInt(num_points_s);
		} catch (NumberFormatException ex) {
			System.err.format("Error: Number data points is not an integer.\n");
			System.exit(1);
		}
		if (num_points < 0 || num_points == 1) {
			System.err.format("Error: Number data points must be 0 or "
			+ "at least 2.\n");
			System.exit(1);
		}
		
		String					time_series_file = args[1+offset];
		double[]				time_series = null;
        Pair<Double, Double>	trend_line = null;
        
        try {
        time_series = GetTimeSeries.getTimeSeries(time_series_file);
        switch (ols_type) {
        case NormalizeMS:
        	trend_line = IFSNormalizedOLSTrend
        		.getNormalizedOLSTrendMS(time_series, num_segms);
        	break;
        case Plain:
        	trend_line = IFSNormalizedOLSTrend.getOLSTrend(time_series);
        	break;
        default:
        	trend_line = IFSNormalizedOLSTrend
        		.getNormalizedOLSTrend(time_series);
        	break;
        }
        } catch (IFSException ex) {
            System.err.format("Error: %s\n", ex.getMessage());
            System.exit(1);
        }
        System.out.format("\n");
        System.out.format("Slope      : %8.5f\n", trend_line.getSecond());
        System.out.format("Y-Intercept: %8.5f\n", trend_line.getFirst());
        System.out.format("\n");
	}

/*******************/
/* Private Classes */
/*******************/

/**
 * Enum defining the various supported OLS types.
 */
private enum OLSType {
	Normalize,
	NormalizeMS,
	Plain
}
	
/*******************/
/* Private Methods */
/*******************/
	
	/**
	 * Computing the forward mean of all the points remaining after reducing
	 * the point set by the number of segments. Number of segments must be at
	 * least 2.
	 * 
	 * @param series Series to operate on.
	 * @param num_segms Number of segment means to compute.
	 * 
	 * @return Segment means.
	 * 
	 * @throws IFSException if there a specification error.
	 */
	private static double[] getMeanSmooth(
		double[]	series,
		int			num_segms
	)  throws IFSException {
		if (series == null) {
			throw new IFSException("Mean smooth processing does not support "
			+ "null series.");
		}
		if (num_segms <= 1) {
			throw new IFSException("Number of segments must be greater than 1.");
		}
		
		int			num_series = series.length;
		int			num_vals = num_series-num_segms+1;
		
		if (num_vals <= 1) {
			throw new IFSException(String.format("Number of segments '%d' "
			+ "produces a value width that is greater than the number of "
			+ "values '%d'.", num_segms, num_vals));
		}
		
		double[]	means = new double[num_segms];
		double		mean = 0.0;
		
		for (int i = 0; i < num_segms; i++) {
			mean = 0.0;
			for (int j = 0; j < num_vals; j++) {
				mean += series[i+j];
			}
			mean /= num_vals;
			means[i] = mean;
		}
		
		return means;
	}

	/**
	 * Print usage information.
	 */
	private static void usage() {
		System.out.print(
  "\n"
+ "Fit a normalized OLS trend line to time series data and print the slope\n"
+ "and y-intercept.\n"
+ "\n"
+ "Usage: com.aol.ifs.soa.common.IFSNormalizedOLSTrend\n"
+ "          [-ns <number of segments>]\n"
+ "          <# data points to use>\n"
+ "          <time series file>\n"
+ "\n"
+ "-ns <number of segments> -- Optional argument indicating that the trend line\n"
+ "   should be computed from the specified number of segment points. Each segment\n"
+ "   point is derived by computing the forward mean of all the points remaining\n"
+ "   after reducing the point set by the number of segments. Number of segments\n"
+ "   must be at least 2. If the number of segments is 0, an OLS (not normalized)\n"
+ "   trend line is computed.\n"
+ "\n"
+ "# data points to use -- If value is 0, all data is used. If value is greater\n"
+ "   than 0, that many of the most recent time series numbers are processed.\n"
+ "   If the value is greater than the number of time series values, an error\n"
+ "   is printed. If value is not 0, it must be at least 2.\n"
+ "\n"
+ "time series file -- Path to file containing time series data. There is one\n"
+ "   number per line from least recent to most recent.\n"
+ "\n"
		);
	}

}
