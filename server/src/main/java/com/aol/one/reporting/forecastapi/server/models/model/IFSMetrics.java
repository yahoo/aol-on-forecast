/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.models.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class implementing metrics for actuals versus forecasts.
 */
public final class IFSMetrics {

	/**
	 * Compute the Akaike Information Criterion (AIC) adjusted for ordinary
	 * least squares (OLS) (and having common factor removed for efficiency).
	 * 
	 * @param num_parms Number of parameters used to compute the forecast.
	 * @param actuals Actual series of values.
	 * @param forecasts Forecasts for the series.
	 * 
	 * @return Adjusted AIC.
	 * 
	 * @throws IFSException if insufficient data provided or data lengths
	 *    do not match.
	 */
	public static double getAdjAIC(
		int			num_parms,
		double[]	actuals,
		double[] 	forecasts
	) throws IFSException {
		if (num_parms < 0) {
			throw new IFSException(5);
		} else if (actuals == null || actuals.length < 1) {
	    	throw new IFSException(6, "Adjusted AIC");
		} else if (forecasts == null || forecasts.length < 1) {
	    	throw new IFSException(7, "Adjusted AIC");
		} else if (actuals.length != forecasts.length) {
	    	throw new IFSException(8, "Adjusted AIC");
		}
		
		double	aic = 2*num_parms;
		double 	rss = 0.0;
		
		for (int i = 0; i < actuals.length; i++) {
			rss += (forecasts[i]-actuals[i])*(forecasts[i]-actuals[i]);
		}
		aic += actuals.length*Math.log(rss/actuals.length);
		return(aic);
	}
	
	/**
	 * Compute the mean absolute percentage error (MAPE) of forecasts
	 * versus actuals.
	 * 
	 * @param actuals Actual series of values.
	 * @param forecasts Forecasts for the series.
	 * 
	 * @return Mean absolute percentage error.
	 * 
	 * @throws IFSException if insufficient data provided or data lengths
	 *    do not match.
	 */
	public static double getMAPE(
		double[]	actuals,
		double[] 	forecasts
	) throws IFSException {
		if (actuals == null || actuals.length == 0) {
			throw new IFSException(6, "MAPE");
		} else if (forecasts == null || forecasts.length == 0) {
			throw new IFSException(7, "MAPE");
		} else if (actuals.length != forecasts.length) {
			throw new IFSException(8, "MAPE");
		}
		
		List<Double>	apes = new ArrayList<Double>();
		double			ape = 0.0;
		
		for (int i = 0; i < actuals.length; i++) {
			if (actuals[i] != 0.0) {
				ape = 100.0*Math.abs(forecasts[i]-actuals[i])/Math.abs(actuals[i]);
				apes.add(ape);
			}
		}
		
		if (apes.size() == 0) {
			return(Double.POSITIVE_INFINITY);
		} else {
			double	sum_ape = 0.0;
			
			for (double apev : apes) {
				sum_ape += apev;
			}
			return(sum_ape/(double)apes.size());
		}
	}
	
	/**
	 * Compute the mean absolute scaled error (MASE) of forecasts
	 * versus actuals.
	 * 
	 * @param actuals Actual series of values.
	 * @param forecasts Forecasts for the series.
	 * 
	 * @return Mean absolute scaled error.
	 * 
	 * @throws IFSException if insufficient data provided or data lengths
	 *    do not match.
	 */
	public static double getMASE(
		double[]	actuals,
		double[] 	forecasts
	) throws IFSException {
		if (actuals == null || actuals.length == 0) {
			throw new IFSException(6, "MASE");
		} else if (forecasts == null || forecasts.length == 0) {
			throw new IFSException(7, "MASE");
		} else if (actuals.length != forecasts.length) {
			throw new IFSException(8, "MASE");
		} else if (actuals.length < 2) {
			throw new IFSException(74, "MASE");
		}
		
		double		scale = 0.0;
		
		for (int i = 1; i < actuals.length; i++) {
			scale += Math.abs(actuals[i]-actuals[i-1]);
		}
		scale /= (double)(actuals.length-1);
		if (IFSStatistics.isZero(scale)) {
			scale = 1.0;
		}
		
		double		sae = 0.0;
		
		for (int i = 1; i < actuals.length; i++) {
			sae += Math.abs(forecasts[i]-actuals[i])/scale;
		}
		
		return sae/(double)actuals.length;
	}
	
	/**
	 * Compute the median absolute percentage error (MedAPE) of forecasts
	 * versus actuals.
	 * 
	 * @param actuals Actual series of values.
	 * @param forecasts Forecasts for the series.
	 * 
	 * @return Median absolute percentage error.
	 * 
	 * @throws IFSException if insufficient data provided or data lengths
	 *    do not match.
	 */
	public static double getMedAPE(
		double[]	actuals,
		double[] 	forecasts
	) throws IFSException {
		if (actuals == null || actuals.length == 0) {
			throw new IFSException(6, "MedAPE");
		} else if (forecasts == null || forecasts.length == 0) {
			throw new IFSException(7, "MedAPE");
		} else if (actuals.length != forecasts.length) {
			throw new IFSException(8, "MedAPE");
		}
		
		List<Double>	apes = new ArrayList<Double>();
		double			ape = 0.0;
		
		for (int i = 0; i < actuals.length; i++) {
			if (actuals[i] != 0.0) {
				ape = 100.0*Math.abs(forecasts[i]-actuals[i])/Math.abs(actuals[i]);
				apes.add(ape);
			}
		}
		
		if (apes.size() == 0) {
			return(Double.POSITIVE_INFINITY);
		}
		
		Collections.sort(apes);
		
		if (apes.size()%2 == 1) {
			int		idx = apes.size()/2;
			
			return(apes.get(idx));
		} else {
			int		idx2 = apes.size()/2;
			int		idx1 = idx2-1;
			
			return((apes.get(idx1)+apes.get(idx2))/2.0);
		}
	}
	
	/**
	 * Compute the root mean squared error (RMSE) of forecasts versus actuals.
	 * 
	 * @param actuals Actual series of values.
	 * @param forecasts Forecasts for the series.
	 * 
	 * @return Root mean squared error.
	 * 
	 * @throws IFSException if insufficient data provided or data lengths
	 *    do not match.
	 */
	public static double getRMSE(
		double[]	actuals,
		double[] 	forecasts
	) throws IFSException {
		if (actuals == null || actuals.length == 0) {
			throw new IFSException(6, "RMSE");
		} else if (forecasts == null || forecasts.length == 0) {
			throw new IFSException(7, "RMSE");
		} else if (actuals.length != forecasts.length) {
			throw new IFSException(8, "RMSE");
		}
		
		double		sq_resid = 0.0;
		
		for (int i = 0; i < actuals.length; i++) {
			sq_resid += (actuals[i]-forecasts[i])*(actuals[i]-forecasts[i]);
		}
		sq_resid /= (double)actuals.length;
		return(Math.sqrt(sq_resid));
	}
	
	/**
	 * Compute the symmetric mean absolute percentage error (SMAPE) of
	 * forecasts versus actuals.
	 * 
	 * @param actuals Actual series of values.
	 * @param forecasts Forecasts for the series.
	 * 
	 * @return Symmetric mean absolute percentage error.
	 * 
	 * @throws IFSException if insufficient data provided or data lengths
	 *    do not match.
	 */
	public static double getSMAPE(
		double[]	actuals,
		double[] 	forecasts
	) throws IFSException {
		if (actuals == null || actuals.length == 0) {
			throw new IFSException(6, "SMAPE");
		} else if (forecasts == null || forecasts.length == 0) {
			throw new IFSException(7, "SMAPE");
		} else if (actuals.length != forecasts.length) {
			throw new IFSException(8, "SMAPE");
		}
		
		List<Double>	sapes = new ArrayList<Double>();
		double			sape = 0.0;
		
		for (int i = 0; i < actuals.length; i++) {
			if (actuals[i] != 0.0 || forecasts[i] != 0.0) {
				sape = 200.0*Math.abs(actuals[i]-forecasts[i])
					/(actuals[i]+forecasts[i]);
				sapes.add(sape);
			}
		}
		
		if (sapes.size() == 0) {
			return(Double.POSITIVE_INFINITY);
		} else {
			double	sum_sape = 0.0;
			
			for (double sapev : sapes) {
				sum_sape += sapev;
			}
			return(sum_sape/(double)sapes.size());
		}
	}
	
	/**
	 * Compute the total absolute percentage error (TotPE) of forecasts
	 * versus actuals.
	 * 
	 * @param actuals Actual series of values.
	 * @param forecasts Forecasts for the series.
	 * 
	 * @return Total absolute percentage error.
	 * 
	 * @throws IFSException if insufficient data provided or data lengths
	 *    do not match.
	 */
	public static double getTotAPE(
		double[]	actuals,
		double[] 	forecasts
	) throws IFSException {
		if (actuals == null || actuals.length == 0) {
			throw new IFSException(6, "TotAPE");
		} else if (forecasts == null || forecasts.length == 0) {
			throw new IFSException(7, "TotAPE");
		} else if (actuals.length != forecasts.length) {
			throw new IFSException(8, "TotAPE");
		}
		
		double			tot_a = 0.0;
		double			tot_f = 0.0;
		
		for (int i = 0; i < actuals.length; i++) {
			tot_a += actuals[i];
			tot_f += forecasts[i];
		}
		
		if (tot_a == 0) {
			return(Double.POSITIVE_INFINITY);
		} else {
			return(100.0*Math.abs(tot_f-tot_a)/tot_a);
		}
	}
}
