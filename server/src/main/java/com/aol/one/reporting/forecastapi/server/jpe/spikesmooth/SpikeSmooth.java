/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.jpe.spikesmooth;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Class that detects and smoothes time series spikes. Spikes are determined
 * as 3-sigma from moving average. Spikes are smoothed by using moving average
 * instead of spike value. Moving average transitions based on shifts in the
 * series. The spike clipping window (i.e. how long a spike can persist without
 * considering it a series shift) can be specified, but the default is 3 points.
 * 
 * @author Copyright &copy; 2010 John Eldreth All rights reserved.
 */
public final class SpikeSmooth {
	private static final int		ClipFactor = 4;
	private static final int		ClippingWindow = 3;
	private static final double		DevBoundary = 3;
	private static final int[][][]	States = new int[][][] {
//      spike < clip   spike >= clip   everything-else   EOF
/* 0 */ {{ 1, 2},      {-1, 6},        { 0, 4},          {-1, 1}},
/* 1 */ {{ 1, 2},      { 0, 5},        { 0, 7},          {-1, 3}}
	};

	/**
	 * Spike smoothing application. Time series can be read from file or
	 * from standard input. The spike clipping window can be optionally
	 * specified. Also the output can be a side by side comparison of the
	 * original series with the smoothed series or just the smoothed series.
	 * 
	 * @param args The various program arguments (required and optional). Refer
	 *    to usage method for more information.
	 */
	public static void main(
		String[]	args
	) {
		if (args.length != 2 && args.length != 3) {
			usage();
			System.exit(1);
		}
		
        ArrayList<Double>       time_series = null;
        double                  value = 0.0;
        BufferedReader          in = null;
        String                  line = null;

        try {
        time_series = new ArrayList<Double>();
        in = getFile(args[0]);
        while ((line = in.readLine()) != null) {
            try {
            value = Double.parseDouble(line);
            time_series.add(value);
            }
            catch (NumberFormatException ex) {
                System.err.println("Time series value '"
                +   line
                +   "' not a number. "
                +   ex.getMessage());
                System.exit(1);
            }
        }
        }
        catch (SecurityException ex) {
            System.err.println("File access error occurred. "
            +   ex.getMessage());
            System.exit(1);
        }
        catch (FileNotFoundException ex) {
            System.err.println("File read error occurred. "
            +   ex.getMessage());
            System.exit(1);
        }
        catch (IOException ex) {
            System.err.println("Unexpected error occurred in reading "
            +   "time series. "
            +   ex.getMessage());
            System.exit(1);
        }
        
        boolean					is_compare = false;
        
        if (args[1].equals("compare"))
        	is_compare = true;
        else if (!args[1].equals("result")) {
            System.err.println("The second argument must be either 'compare' "
            + "or 'result'.");
            System.exit(1);
        }
        
        int						clipping_window = ClippingWindow;
        
        if (args.length == 3) {
        	try {
        	clipping_window = Integer.parseInt(args[2]);
            }
            catch (NumberFormatException ex) {
                System.err.println("Unexpected clipping window specification. "
                +	ex.getMessage());
                System.exit(1);
            }
            if (clipping_window < ClippingWindow) {
                System.err.println("Clipping window must be greater than or "
                +	"equal to "
                +	ClippingWindow
                +	".");
                System.exit(1);
            }
        }
        

        double[]                ts = new double[time_series.size()];
        double[]				smoothed_ts = null;
        int                     i = 0;

        for (Double ts_value : time_series)
            ts[i++] = ts_value.doubleValue();
		smoothed_ts = smoothSpike(ts, clipping_window);
		
		if (is_compare) {
	        System.out.printf("\n");
	        System.out.printf("       Series         Results      \n");
	        System.out.printf("       -------------  -------------\n");
	        for (i = 0; i < smoothed_ts.length; i++)
	            System.out.printf("%5d. %,13.3f  %,13.3f\n", i+1,
	            	ts[i], smoothed_ts[i]);
		} else
	        for (double val_ts : smoothed_ts)
	            System.out.printf("%f\n", val_ts);
		
		System.exit(0);
	}
	
	/**
	 * Smooth the spikes in a time series.
	 * 
	 * @param series Time series to smooth.
	 * 
	 * @return Series with smoothed spikes.
	 */
	public static double[] smoothSpike(
		double[]	series
	) {
		return smoothSpike(series, ClippingWindow);
	}
	
	/**
	 * Smooth the spikes in a time series. This implements two passes over
	 * the time series, forward and reverse.
	 * 
	 * @param series Time series to smooth.
	 * @param clipping_window How long a spike can persist before considering
	 *    it a change in the moving average.
	 * 
	 * @return Series with smoothed spikes.
	 */
	public static double[] smoothSpike(
		double[]	series,
		int			clipping_window
	) {
		double[]	new_series = null;
		
		new_series = smoothSpikeReal(series, clipping_window);
		new_series = revSeries(new_series);
		new_series = smoothSpikeReal(new_series, clipping_window);
		new_series = revSeries(new_series);
		return new_series;
	}
	
/*******************/
/* Private Methods */
/*******************/
	
	private SpikeSmooth() {} // Public constructor disabled

    /**
     * Set up a buffered reader for a time series file or standard input.
     *
     * @param file_name Time series file or -- for standard input.
     *
     * @throws FileNotFoundException
     * @throws SecurityException
     */
    private static BufferedReader getFile(
        String      file_name
    ) throws FileNotFoundException, SecurityException {
        BufferedReader  in = null;

        if (file_name.equals("--")) {
            in = new BufferedReader(new InputStreamReader(System.in));
            return in;
        }

        in = new BufferedReader(new InputStreamReader(
                new FileInputStream(file_name)));
        return in;
    }
    
    /**
     * Compute the mean and standard deviation of a series based on series
     * positions in a linked list.
     * 
     * @param series Series for which to compute mean and standard deviation.
     * @param pos  Series entries to use to compute mean and standard deviation.
     * @param mean Resulting mean.
     * @param sd Resulting standard deviation.
     * 
     * @return Mean and standard deviation container object.
     */
    private static MeanAndStdDev getMeanAndStdDev(
    	double[]			series,
    	LinkedList<Integer>	pos
    ) {
		if (series == null || series.length < 2
		|| pos == null || pos.size() < 2)
			return null;
		
		double		mean = 0.0;
		double		sd = 0.0;
    	double		sum_diff = 0.0;
    	double		n = (double)pos.size();
    	
		for (Integer idx : pos)
			mean += series[idx];
		mean /= n;
		
		for (Integer idx : pos)
			sum_diff += (series[idx]-mean)*(series[idx]-mean);
		sum_diff /= (n-1);
		sd = (double)Math.sqrt(sum_diff);
		
		return new MeanAndStdDev(mean, sd);
    }
   
    /**
     * Reverse a time series.
     * 
     * @param series Time series to reverse.
     * 
     * @return Reversed series.
     */
    private static double[] revSeries(
    	double[]	series
    ) {
    	if (series == null)
    		return null;
    	
    	double[]	rev_series = new double[series.length];
    	
    	for (int i = 0; i < series.length; i++)
    		rev_series[i] = series[series.length-i-1];
    	return rev_series;
    }
	
	/**
	 * Smooth the spikes in a time series. This method does the bulk of the
	 * work.
	 * 
	 * @param series Time series to smooth.
	 * @param clipping_window How long a spike can persist before considering
	 *    it a change in the moving average.
	 * 
	 * @return Series with smoothed spikes.
	 */
	private static double[] smoothSpikeReal(
		double[]	series,
		int			clipping_window
	) {
		if (clipping_window < ClippingWindow
		|| series == null
		|| series.length <= (clipping_window+1))
			return series;
		
		LinkedList<Integer>	cur_window = new LinkedList<Integer>();
		MeanAndStdDev		cur_u_s = null;
		double				cur_u = 0.0;
		double				cur_s = 0.0;
		LinkedList<Integer>	clip_window = new LinkedList<Integer>();
		
		for(int i = 0; i < (clipping_window+1); i++)
			cur_window.add(i);
		cur_u_s = getMeanAndStdDev(series, cur_window);
		cur_u = cur_u_s.getMean();
		cur_s = cur_u_s.getStdDev();
		
		int					i = clipping_window+1;
		int					cur_state = 0;
		int					input = -1;
		int					action = -1;
		double[]			smooth_series = new double[series.length];
		
		System.arraycopy(series, 0, smooth_series, 0, series.length);
		while (cur_state >= 0) {
			if (i >= series.length)
				input = 3;
			else if (((series[i] > cur_u+DevBoundary*cur_s)
			| (series[i] < cur_u-DevBoundary*cur_s))
			&& (clip_window.size() < clipping_window))
				input = 0;
			else if (((series[i] > cur_u+DevBoundary*cur_s)
			| (series[i] < cur_u-DevBoundary*cur_s))
			&& (clip_window.size() >= clipping_window))
				input = 1;
			else
				input = 2;

			action = States[cur_state][input][1];
			switch(action) {
			case 2:
				clip_window.add(i);
				break;
			case 3:
				for (int j : clip_window)
					smooth_series[j] = cur_u;
				clip_window.clear();
				break;
			case 4:
				cur_window.add(i);
				if (cur_window.size() > ClipFactor*clipping_window)
					cur_window.remove();
				cur_u_s = getMeanAndStdDev(series, cur_window);
				cur_u = cur_u_s.getMean();
				cur_s = cur_u_s.getStdDev();
				break;
			case 5:
				cur_window.clear();
				for (int j = i-clipping_window; j <= i; j++)
					cur_window.add(j);
				cur_u_s = getMeanAndStdDev(series, cur_window);
				cur_u = cur_u_s.getMean();
				cur_s = cur_u_s.getStdDev();
				clip_window.clear();
				break;
			case 6:
				return null;
			case 7:
				for (int j : clip_window)
					smooth_series[j] = cur_u;
				clip_window.clear();
				cur_window.add(i);
				cur_u_s = getMeanAndStdDev(series, cur_window);
				cur_u = cur_u_s.getMean();
				cur_s = cur_u_s.getStdDev();
				break;
			default:
				break;
			}

			cur_state = States[cur_state][input][0];
			i++;
		}
		
		return smooth_series;
	}

	/**
	 * Usage information.
	 */
	private static void usage() {
		System.out.print(
  "SpikeSmooth -- smooth the spikes in a time series.\n"
+ "\n"
+ "usage: java com.aol.one.reporting.forecastapi.server.jpe.spikesmooth.SpikeSmooth <-- | file name>\n"
+ "          [compare | result] [<clipping window size>]\n"
+ "\n"
+ "Data is read from standard input if the file name is '--'. Otherwise data\n"
+ "is read from the specified file name. The input must have one time series\n"
+ "value per line.\n"
+ "\n"
+ "[compare | result] -- If 'compare' is specified, the original series is\n"
+ "   printed along side the smoothed result. If 'result' is specified, only\n"
+ "   the smoothed series is printed in way that can be redirected into\n"
+ "   processes.\n"
+ "\n"
+ "[<clipping window size>] -- Can optionally specify the size of the spike\n"
+ "   clipping window which determines how long a spike can persist before\n"
+ "   considering it a change in the moving average. The default is 3.\n"
+ "\n"
		);
	}
	
/*******************/
/* Private Classes */
/*******************/

/**
 * Container class for mean and standard deviation;
 */
private static final class MeanAndStdDev {
	private double		Mean;
	private double		StdDev;
		
	@SuppressWarnings("unused")
	private MeanAndStdDev() {} // Disabled
		
	/**
	 * Package mean and standard deviation.
	 * 
	 * @param mean Mean.
	 * @param std_dev Standard deviation.
	 */
	public MeanAndStdDev(
		double		mean,
		double		std_dev
	) {
		Mean = mean;
		StdDev = std_dev;
	}
		
	/**
	 * Return mean.
	 * 
	 * @return Mean.
	 */
	public double getMean() {
		return Mean;
	}

	/**
	 * Return standard deviation.
	 * 
	 * @return Standard deviation.
	 */
	public double getStdDev() {
		return StdDev;
	}
}
}
