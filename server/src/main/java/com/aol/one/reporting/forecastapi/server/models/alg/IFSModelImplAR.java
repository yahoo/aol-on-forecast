/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.models.alg;

import java.util.ArrayList;
import java.util.List;

import com.aol.one.reporting.forecastapi.server.models.model.IFSException;
import com.aol.one.reporting.forecastapi.server.models.model.IFSMetrics;
import com.aol.one.reporting.forecastapi.server.models.model.IFSModel;
import com.aol.one.reporting.forecastapi.server.models.model.IFSParameterValue;
import com.aol.one.reporting.forecastapi.server.models.model.IFSStatistics;
import com.aol.one.reporting.forecastapi.server.models.model.IFSUsageDescription;

/**
 * Class implementing auto-regressive forecasting model. The maximum lag
 * for which a coefficient is produced is min(n-1, 10*log10(n)) where n is
 * the number of series points. The model supports the following specific
 * parameters:
 * 
 * aic -- Should minimum AIC be used to determine maximum lag coefficient?
 *    If the value is greater than 1, a lag of min(n-1, aic) is used.
 * center -- Should the forecast be centered?
 * demean -- Should a mean be estimated for calibration?
 * 
 * If there are less than 2 points in the series a random walk forecast is
 * produced. 
 */
public final class IFSModelImplAR extends IFSModel {
	public static final String					ModelName = "model_ar";
	private static final IFSUsageDescription	UsageDescription
	= new IFSUsageDescription(
  "Auto-regressive forecast model implementation.\n",
  "Implements the auto-regressive forecast model up to a maximum lag\n"
+ "of min(n-1, 10*log10(n)) where n is the number of series values.\n",
  "\n"
+ "aic=<0|1|>1> -- 0 indicates the lag is min(n-1, 10log10(n)) where n is the\n"
+ "                number of series values. 1 causes the lag that minimizes the\n"
+ "                AIC in the interval [1, min(n-1, 10log10(n))] to be used. A\n"
+ "                value greater than 1 indicates the lag min(n-1, aic) is to\n"
+ "                be used. The default is 1.\n"
+ "\n"
+ "center=<0|1> -- 0 indicates forecast is derived using previous forecasts.\n"
+ "                1 indicates the most recent history is recycled to derive\n"
+ "                forecasts. The default is 0.\n"
+ "\n"
+ "demean=<0|1> -- 0 indicates no mean is estimated for calibration.\n"
+ "                1 indicates a mean is estimated. The default is 0.\n"
	);
	
	private static final int				MinSeriesLength = 2;
	
	private int								IsAIC = 1;
	private boolean							IsCenter = false;
	private boolean							Demean = false;

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
			for (int i = 0; i < nf; i++) {
				forecasts[i] = series[n-1];
			}
			return "rw";
		}
		
		int			max_lags = (int)Math.max(1, Math.min(10.0*Math.log10(n), n-1));
		double		mean = (Demean) ? IFSStatistics.getMean(series) : 0.0;
		double[]	acf = IFSStatistics.getACF(series, Demean, 1, max_lags);
		double[]	arc = null;
		double[]	fcsts = null;
		
		if (IsAIC == 0) {
			arc = IFSStatistics.getARCoefficients(acf);
		} else if (IsAIC == 1) {
			double[]	aic_acf = null;
			double[]	aic_arc = null;
			double		aic_val = 0.0;
			double		min_aic_val = 0.0;
			int			aic_nf = n-max_lags;
			double[]	aic_series = new double[aic_nf];

			System.arraycopy(series, max_lags, aic_series, 0, aic_nf);
			for (int i = 1; i <= max_lags; i++) {
				aic_acf = new double[i];
				System.arraycopy(acf, 0, aic_acf, 0, i);
				aic_arc = IFSStatistics.getARCoefficients(aic_acf);
				fcsts = getForecastsAR(aic_nf, max_lags, series, mean, aic_arc);
				aic_val = IFSMetrics.getAdjAIC(i, aic_series, fcsts);
				if (i == 1 || aic_val < min_aic_val) {
					min_aic_val = aic_val;
					arc = aic_arc;
				}
			}
		} else {
			max_lags = Math.min(n-1, IsAIC);
			acf = IFSStatistics.getACF(series, Demean, 1, max_lags);
			arc = IFSStatistics.getARCoefficients(acf);
		}
		
		fcsts = getForecastsAR(nf, n, series, mean, arc);
		System.arraycopy(fcsts, 0, forecasts, 0, nf);
		
		String	calib_info = String.format("ar::mean:%.3f", mean);
		
		if (arc != null) {
			for (int i = 0; i < arc.length; i++) {
				calib_info = String.format("%s,ar%d:%.3f",
					calib_info, i+1, arc[i]);
			}
		}
		
		return calib_info;
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
		List<IFSParameterValue>	parameters
	) throws IFSException {
		int		is_aic = 1;
		boolean is_center = false;
		boolean	demean = false;
		
		if (parameters != null && parameters.size() > 0) {
			for (IFSParameterValue parameter : parameters) {
				if (parameter.getParameter().equals("aic")) {
					int		value = 0;
					
					try {
					value = Integer.parseInt(parameter.getValue());
					} catch (NumberFormatException ex) {
						throw new IFSException(17, getModelName(), "aic");
					}
					if (value >= 0) {
						is_aic = value;
					} else {
						throw new IFSException(75, getModelName(), "aic");
					}
				} else if (parameter.getParameter().equals("center")) {
					int		value = 0;
					
					try {
					value = Integer.parseInt(parameter.getValue());
					} catch (NumberFormatException ex) {
						throw new IFSException(17, getModelName(), "center");
					}
					if (value == 0) {
						is_center = false;
					} else if (value == 1) {
						is_center = true;
					} else {
						throw new IFSException(17, getModelName(), "center");
					}
				} else if (parameter.getParameter().equals("demean")) {
					int		value = 0;
					
					try {
					value = Integer.parseInt(parameter.getValue());
					} catch (NumberFormatException ex) {
						throw new IFSException(17, getModelName(), "demean");
					}
					if (value == 0) {
						demean = false;
					} else if (value == 1) {
						demean = true;
					} else {
						throw new IFSException(17, getModelName(), "demean");
					}
				} else {
					throw new IFSException(18, getModelName(),
						parameter.getParameter());
				}
			}
		}
		
		IsAIC = is_aic;
		IsCenter = is_center;
		Demean = demean;
	}
	
/*******************/
/* Private Methods */
/*******************/
	
	/**
	 * Given a time series and a set of auto-regressive coefficients, compute
	 * the requested number of forecasts.
	 * 
	 * @param num_forecasts Number of forecasts to produce.
	 * @param base_pos First series position corresponding to a forecast.
	 *    If there is no correspondence, the series position is 1 point
	 *    past the last series point.
	 * @param series Time series.
	 * @param mean Time series mean. Can be 0 if the series is not demeaned.
	 * @param arc Auto-regressive coefficients corresponding to the number
	 *    of series lags to use.
	 *    
	 * @return Requested number of forecasts.
	 * 
	 * @throws IFSException if the base position is outside the acceptable
	 *    series range.
	 */
	private double[] getForecastsAR(
		int			num_forecasts,
		int			base_pos,
		double[]	series,
		double		mean,
		double[]	arc
	) throws IFSException {
		if (base_pos < 0 || base_pos > series.length) {
			throw new IFSException(19, base_pos);
		}
		
		double[]	fcsts = new double[num_forecasts];
		int			ns = series.length;
		int			nc = arc.length;
		int			si = 0;
		double		sv = 0.0;
		
		if (IsCenter) {
			List<Integer>	svl = new ArrayList<Integer>();
			
			for (int j = 0; j < nc; j++) {
				si = base_pos-1-j;
				svl.add(si);
			}
			for (int i = 0; i < num_forecasts; i++) {
				fcsts[i] = mean;
				for (int j = 0; j < nc; j++) {
					si = svl.get(j);
					sv = series[si];
					fcsts[i] += arc[j]*(sv-mean);
				}
				si = svl.remove(0);
				svl.add(si);
			}
		} else {
			for (int i = 0; i < num_forecasts; i++) {
				fcsts[i] = mean;
				for (int j = 0; j < nc; j++) {
					si = base_pos+i-1-j;
					if (si >= ns) {
						sv = fcsts[si-ns];
					} else {
						sv = series[si];
					}
					fcsts[i] += arc[j]*(sv-mean);
				}
			}
		}
		return(fcsts);
	}
}
