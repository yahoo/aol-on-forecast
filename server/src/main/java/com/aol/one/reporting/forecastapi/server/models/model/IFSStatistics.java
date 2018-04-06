/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.models.model;

import java.util.Arrays;

/**
 * Class implementing commonly used statistics.
 */
public final class IFSStatistics {
	
	/**
	 * Compute ACF for designated lag range. The returned array, size of the
	 * designated lag range, has the first coefficient at index 0 and the last
	 * at array size minus 1. If there is insufficient data to compute the
	 * coefficient for a lag, the value will be 0.0. Also note that a null
	 * time series yields a null result. It is optional as to whether the
	 * covariances are about the sample mean or not.
	 * 
	 * @param ts Time series to evaluate.
	 * @param demean If the value is true, the covariances are about the sample
	 *    mean. If the value is false, they are not.
	 * @param beg_lag Beginning lag.
	 * @param end_lag Ending lag.
	 * 
	 * @return Auto-correlation coefficients for the specified lag range.
	 * 
	 * @throws IFSException if no values are specified.
	 */
	public static double[] getACF(
		double[]	ts,
		boolean		demean,
		int			beg_lag,
		int			end_lag
	) throws IFSException {
		if (ts == null || ts.length < 1) {
			throw new IFSException(43);
		}
		
		int			range_size = end_lag - beg_lag + 1;
		double[]	coefficients = new double[range_size];
		double		mean = (demean) ? getMean(ts) : 0.0;
		double		sum0 = 0.0;
		
		for (double value : ts) {
			sum0 += (value-mean)*(value-mean);
		}
        if (IFSStatistics.isZero(sum0)) {
        	for (int i = 0; i < coefficients.length; i++) {
        		coefficients[i] = 0.0;
        	}
        } else
        	for (int i = beg_lag; i <= end_lag; i++) {
        		double	sum = 0.0;
        		for (int j = 0; j < ts.length-i; j++) {
        			sum += (ts[j]-mean)*(ts[j+i]-mean);
        		}
        		coefficients[i-beg_lag] = sum / sum0;
        	}

		return coefficients;
	}
	
	/**
	 * Compute auto-regressive (regression) coefficients from a series
	 * of auto-correlation coefficients.
	 * 
	 * @param acf Auto-correlation coefficients.
	 * 
	 * @return Corresponding auto-regressive coefficients.
	 * 
	 * @throws IFSException if insufficient number of auto-correlation
	 *    coefficients have been specified or an unexpected computation
	 *    error occurs.
	 */
	public static double[] getARCoefficients(
		double[]	acf
	) throws IFSException {
		if (acf == null || acf.length < 1) {
			throw new IFSException(44);
		}
		
		int			n = acf.length;
		
		if (n == 1) {
			return acf;
		}
		
		double[][]	matrix = new double[n][n+1];
		double[]	arc = null;
		int			i;
		int			j;
			
		for (i = 0; i < n; i++) {
			for (j = 0; j < n; j++) {
				if (j > i) {
					matrix[i][j] = acf[j-i-1];
				} else if (i == j) {
					matrix[i][j] = 1.0;
				} else {
					matrix[i][j] = acf[i-j-1];
				}
			}
			matrix[i][j] = acf[i];
		}
		arc = IFSComputation.getLinearEqnSoln(matrix);
		return arc;
	}

	/**
	 * Compute the series maximum.
	 * 
	 * @param series Values for which to compute the maximum.
	 * 
	 * @return Series maximum.
	 */
	public static double getMax(
		double[]	series
	) {
//	    return Arrays.stream(series).max().orElse(0);
	    
	    if (series.length <= 0) {
	    	return 0.0;
	    } else {
	    	double	max = series[0];
	    	
	    	for (int i = 1; i < series.length; i++) {
	    		if (series[i] > max) {
	    			max = series[i];
	    		}
	    	}
	    	return max;
	    }
	}

	/**
	 * Compute the sample mean.
	 * 
	 * @param series Values for which to compute the mean.
	 * 
	 * @return Sample mean.
	 */
	public static double getMean(
		double[]	series
	) {
	    double		mean = 0.0;
	        	
	    for (double value : series) {
	    	mean += value;
	    }
	    mean /= series.length;
	    return mean;
	}

	/**
	 * Compute the median.
	 * 
	 * @param values Values for which to compute the median.
	 * 
	 * @return Median.
	 */
	public static double getMedian(
		double[]	values
	) {
		if (values.length == 1) {
	    	return values[0];
	    }
	    		
	    double[]	m_values = Arrays.copyOf(values, values.length);
	    int			idx = m_values.length/2;
	    
	    Arrays.sort(m_values);
	    
	    if (m_values.length%2 == 0) {
	    	return (m_values[idx-1]+m_values[idx])/2.0;
	    } else {
	    	return m_values[idx];
	    }
	}

	/**
	 * Compute the series minimum.
	 * 
	 * @param series Values for which to compute the minimum.
	 * 
	 * @return Series minimum.
	 */
	public static double getMin(
		double[]	series
	) {
//	    return Arrays.stream(series).min().orElse(0);
	    
	    if (series.length <= 0) {
	    	return 0.0;
	    } else {
	    	double	min = series[0];
	    	
	    	for (int i = 1; i < series.length; i++) {
	    		if (series[i] < min) {
	    			min = series[i];
	    		}
	    	}
	    	return min;
	    }
	}
	
	/**
	 * Compute the sample standard deviation.
	 * 
	 * @param series Values for which to compute the standard deviation.
	 * 
	 * @return Sample standard deviation.
	 * 
	 * @throws IFSException if insufficient values are specified.
	 */
	public static double getStdDev(
		double[]	series
	) throws IFSException {
		if (series == null || series.length < 2) {
	    	throw new IFSException(46);
		}
		
		double		mean = getMean(series);
		double		sd = 0.0;
    	double		sum_diff = 0.0;

		for (double value : series) {
			sum_diff += (value-mean)*(value-mean);
		}
		sum_diff /= (series.length-1);
		sd = Math.sqrt(sum_diff);
    	return sd;
	}
	
	/**
	 * Determine if double value is undefined.
	 * 
	 * @param value Double to check.
	 * 
	 * @return True if value is undefined; false otherwise.
	 */
	public static boolean isUndef(
		double	value
	) {
		return Math.abs(value) == Double.POSITIVE_INFINITY
			|| value == Double.NaN;
	}
	
	/**
	 * Determine if double value is close enough to zero.
	 * 
	 * @param value Double to check.
	 * 
	 * @return True if value is "zero"; false otherwise.
	 */
	public static boolean isZero(
		double	value
	) {
		return Math.abs(value) <= Double.MIN_VALUE;
	}
}
