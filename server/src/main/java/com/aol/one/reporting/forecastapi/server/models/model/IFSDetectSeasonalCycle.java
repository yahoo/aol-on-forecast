/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.models.model;

import com.aol.one.reporting.forecastapi.server.models.util.GetTimeSeries;
import com.aol.one.reporting.forecastapi.server.models.util.PrimeNumbers;


/**
 * This class supports auto-detection of the seasonal cycle of a time series.
 * Essentially this is based on on the auto-correlation of a first order
 * series difference. The largest prime factor of the lag with the largest
 * significant auto-correlation value is returned. If no lag has a significant
 * auto-correlation value, the cycle is set to 1.
 * 
 * An alternative method is provided that returns the lag with the largest
 * significant auto-correlation value. In this case, if no lag has a significant
 * auto-correlation value, the cycle is set to 1.
 * 
 * The maximum seasonal cycle considered is no more than 10 times the log10
 * value of the number of series points or half the series length, whichever
 * is smaller.
 */
public final class IFSDetectSeasonalCycle {
	private static final double	MinACF = 0.3;
	private static final int	MinLag = 2;
	private static final int	MinSeriesLen = 10;
	
	/**
	 * Fetch the seasonal cycle for a time series (if any). If a seasonal
	 * cycle greater than 1 cannot be detected, 1 is returned.
	 * 
	 * @param series Time series.
	 * 
	 * @return If no seasonal cycle, 1 is returned. Otherwise a value greater
	 *    than 1 is returned. This value is based on the largest prime factor
	 *    of the largest significant auto-correlation value. The idea here is
	 *    to avoid large cycles that are actually composed of smaller cycles.
	 *
	 * @throws IFSException Thrown if a null or empty series is specified.
	 */
	public static int getSeasonalCycle(
		double[]	series
	) throws IFSException {
		if (series == null || series.length == 0)
			throw new IFSException("Seasonal cycle detection cannot operate "
			+ "on a null or empty time series.");
		else if (series.length < MinSeriesLen)
			return 1;
		
		int			max_cycle = (int)Math.min(
			Math.floor(10.0*Math.log10(series.length)),
			Math.floor(series.length/2.0));
		double[]	std = new double[series.length];
		
		for (int i = 1; i < series.length; i++)
			std[i] = series[i]-series[i-1];
		
		double[]	acf = IFSStatistics.getACF(std, false, MinLag, max_cycle);
		double		max_acf = acf[0];
		int			max_lag = 0;
		
		for (int i = 1; i < acf.length; i++)
			if (acf[i] > max_acf) {
				max_acf = acf[i];
				max_lag = i;
			}
		if (max_acf >= MinACF) {
			int[]	factors = PrimeNumbers.getPrimeFactors(max_lag+MinLag);

			max_lag = factors[0];
			for (int i = 1; i < factors.length; i++)
				if (factors[i] > max_lag)
					max_lag = factors[i];
			return max_lag;
		} else
			return 1;
	}
	
	/**
	 * Fetch the seasonal cycle for a time series (if any). If a seasonal
	 * cycle greater than 1 cannot be detected, 1 is returned.
	 * 
	 * @param series Time series.
	 * 
	 * @return If no seasonal cycle, 1 is returned. Otherwise a value greater
	 *    than 1 is returned. This value is based on the largest significant
	 *    auto-correlation value. The idea here is to completely trust the
	 *    auto-correlation function as the best cycle detector.
	 *
	 * @throws IFSException Thrown if a null or empty series is specified.
	 */
	public static int getSeasonalCycleACF(
		double[]	series
	) throws IFSException {
		if (series == null || series.length == 0)
			throw new IFSException("Seasonal cycle detection cannot operate "
			+ "on a null or empty time series.");
		else if (series.length < MinSeriesLen)
			return 1;
		
		int			max_cycle = (int)Math.min(
			Math.floor(10.0*Math.log10(series.length)),
			Math.floor(series.length/2.0));
		double[]	std = new double[series.length];
		
		for (int i = 1; i < series.length; i++)
			std[i] = series[i]-series[i-1];
		
		double[]	acf = IFSStatistics.getACF(std, false, MinLag, max_cycle);
		double		max_acf = acf[0];
		int			max_lag = 0;
		
		for (int i = 1; i < acf.length; i++)
			if (acf[i] > max_acf) {
				max_acf = acf[i];
				max_lag = i;
			}
		if (max_acf >= MinACF)
			return max_lag+MinLag;
		else
			return 1;
	}

	/**
	 * Print the seasonal cycle of a given series. The series is contained in
	 * a file or taken from standard input.
	 * 
	 * @param args File or standard input indication.
	 */
	public static void main(
		String[]	args
	) {
		String		ts_file = null;
		boolean		is_acf = false;
		
		if (args.length != 1 && args.length != 2) {
			usage();
			System.exit(0);
		} else if (args.length == 2) {
			if (!args[0].equals("-t")) {
				usage();
				System.exit(0);
			}
			is_acf = true;
			ts_file = args[1];
		} else
			ts_file = args[0];
		
		// Fetch the time series from a file or standard input.
		
        double[]	ts = null;
        int			cycle = 0;

        try {
        ts = GetTimeSeries.getTimeSeries(ts_file);
        cycle = is_acf ? getSeasonalCycleACF(ts) : getSeasonalCycle(ts);
        System.out.printf("%d\n", cycle);
        }
        catch (IFSException ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        }
        
        System.exit(0);
	}

/*******************/
/* Private Methods */
/*******************/
	
	/**
	 * Print usage information.
	 */
	private static void usage() {
		System.out.print(
  "\n"
+ "Determine the seasonal cycle for a time series. The seasonal cycle is printed\n"
+ "on standard output. Error messages are printed on standard error.\n"
+ "\n"
+ "usage: java com.aol.ifs.soa.common.IFSDetectSeasonalCycle [-t] <time series file | -->\n"
+ "\n"
+ "-t -- Compute the seasonal cycle by choosing the largest significant auto-\n"
+ "   correlation value. If this option is not specified, the largest prime\n"
+ "   factor of the largest significant auto-correlation value is printed. The\n"
+ "   prime factor is used to avoid selecting large cycles that are really\n"
+ "   composed of smaller cycles. If there are no significant auto-correlation\n"
+ "   values, the value 1 is printed.\n"
+ "\n"
+ "<time series file | --> -- Specify where to get time series data. It can\n"
+ "   either be a file or standard input. Use '--' to specify standard input.\n"
+ "   Time series data has a single number on each line.\n"
		);
	}
}
