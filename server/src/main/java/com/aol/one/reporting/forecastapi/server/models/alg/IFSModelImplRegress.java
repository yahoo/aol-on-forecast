/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.models.alg;

import java.util.List;

import com.aol.one.reporting.forecastapi.server.models.model.IFSComputation;
import com.aol.one.reporting.forecastapi.server.models.model.IFSException;
import com.aol.one.reporting.forecastapi.server.models.model.IFSModel;
import com.aol.one.reporting.forecastapi.server.models.model.IFSParameterValue;
import com.aol.one.reporting.forecastapi.server.models.model.IFSStatistics;
import com.aol.one.reporting.forecastapi.server.models.model.IFSUsageDescription;

/**
 * Class implementing a regression forecasting model. This model is intended
 * to use ordinary least squares to capture the trend and seasonality of a
 * time series (and not be concerned as much about "stationary" residuals).
 * Since trend and seasonality typically comprise most of the time series
 * signal, residuals are not the focus. The model supports the following
 * specific parameters:
 * 
 * cycle2=<integer>
 *    <eq 0>      -- Value if there is no 2cd phase seasonal component. (default)
 *    <gt 1>      -- # points in the 2cd phase seasonal cycle. Value
 *                   must be an integer greater than 1.
 * polynomial_degree=<integer>
 *    <eq 0>      -- No trend. (default)
 *    <gt 0>      -- Degree of polynomial to use as trend
 *                   line. The value must be an integer between
 *                   1 and 3 inclusive.
 * seasonality_type=<seasonality type>
 *    none        -- No seasonal component. (default)
 *    add         -- Additive seasonal element.
 *    const1      -- Constant seasonal variation according to
 *                   sin(2*pi*t/cycle)+cos(2*pi*t/cycle).
 *    const2      -- Constant seasonal variation according to
 *                   sin(2*pi*t/cycle)+cos(2*pi*t/cycle)
 *                   +sin(4*pi*t/cycle)+cos(4*pi*t/cycle).
 *    const3      -- Constant 2-phase seasonal variation according to
 *                   sin(2*pi*t/cycle)+cos(2*pi*t/cycle)
 *                   +sin(2*pi*t/cycle2)+cos(2*pi*t/cycle2).
 *    incrs1      -- Increasing seasonal variation according to
 *                   sin(2*pi*t/cycle)+cos(2*pi*t/cycle)
 *                   +t*sin(2*pi*t/cycle)+t*cos(2*pi*t/cycle).
 *    incrs2      -- Increasing seasonal variation according to
 *                   sin(2*pi*t/cycle)+cos(2*pi*t/cycle)
 *                   +t*sin(2*pi*t/cycle)+t*cos(2*pi*t/cycle)
 *                   +sin(4*pi*t/cycle)+cos(4*pi*t/cycle)
 *                   +t*sin(4*pi*t/cycle)+t*cos(4*pi*t/cycle).
 *    incrs3      -- Increasing 2-phase seasonal variation according to
 *                   sin(2*pi*t/cycle)+cos(2*pi*t/cycle)
 *                   +t*sin(2*pi*t/cycle)+t*cos(2*pi*t/cycle)
 *                   +sin(2*pi*t/cycle2)+cos(2*pi*t/cycle2)
 *                   +t*sin(2*pi*t/cycle2)+t*cos(2*pi*t/cycle2).
 * 
 * Note that data length must be greater than or equal to 2 times the number
 * of parameters or the sample mean is used.
 */
public final class IFSModelImplRegress extends IFSModel {
	public static final Integer					MaxPolyDegree = 3;
	public static final String					ModelName = "model_regress";
	private static final IFSUsageDescription	UsageDescription
	= new IFSUsageDescription(
  "Regression forecast model implementation.\n",
  "Implements the regression forecast model which focuses on capturing\n"
+ "the trend and seasonal components of a time series believing those to\n"
+ "comprise most of the series signal. If the series length is not at least\n"
+ "2 times the number of parameters, the sample mean is used.\n",
  "\n"
+ "cycle2=<integer>\n"
+ "   <eq 0> -- Value if there is no 2cd phase seasonal component. (default)\n"
+ "   <gt 1> -- # points in the 2cd phase seasonal cycle. Value must be an\n"
+ "             integer greater than 1.\n"
+ "\n"
+ "polynomial_degree=<integer>\n"
+ "   <eq 0> -- No trend. (default)\n"
+ "   <gt 0> -- Degree of polynomial to use as trend line. The value must be\n"
+ "             an integer between 1 and 3 inclusive.\n"
+ "\n"
+ "seasonality_type=<seasonality type>\n"
+ "   none   -- No seasonal component. (default)\n"
+ "   add    -- Additive seasonal element.\n"
+ "   const1 -- Constant seasonal variation according to\n"
+ "             sin(2*pi*t/cycle)+cos(2*pi*t/cycle).\n"
+ "   const2 -- Constant seasonal variation according to\n"
+ "              sin(2*pi*t/cycle)+cos(2*pi*t/cycle)\n"
+ "             +sin(4*pi*t/cycle)+cos(4*pi*t/cycle).\n"
+ "   const3 -- Constant 2-phase seasonal variation according to\n"
+ "              sin(2*pi*t/cycle)+cos(2*pi*t/cycle)\n"
+ "             +sin(2*pi*t/cycle2)+cos(2*pi*t/cycle2).\n"
+ "   incrs1 -- Increasing seasonal variation according to\n"
+ "              sin(2*pi*t/cycle)+cos(2*pi*t/cycle)\n"
+ "             +t*sin(2*pi*t/cycle)+t*cos(2*pi*t/cycle).\n"
+ "   incrs2 -- Increasing seasonal variation according to\n"
+ "              sin(2*pi*t/cycle)+cos(2*pi*t/cycle)\n"
+ "             +t*sin(2*pi*t/cycle)+t*cos(2*pi*t/cycle)\n"
+ "             +sin(4*pi*t/cycle)+cos(4*pi*t/cycle)\n"
+ "             +t*sin(4*pi*t/cycle)+t*cos(4*pi*t/cycle).\n"
+ "   incrs3 -- Increasing 2-phase seasonal variation according to\n"
+ "              sin(2*pi*t/cycle)+cos(2*pi*t/cycle)\n"
+ "             +t*sin(2*pi*t/cycle)+t*cos(2*pi*t/cycle)\n"
+ "             +sin(2*pi*t/cycle2)+cos(2*pi*t/cycle2)\n"
+ "             +t*sin(2*pi*t/cycle2)+t*cos(2*pi*t/cycle2).\n"
	);
	
	private enum							Seasonality {
		None, Add, Const1, Const2, Const3, Incrs1, Incrs2, Incrs3
	};
	private int								Cycle2 = 0;
	private int								PolyDegree = 0;
	private Seasonality						Season = Seasonality.None;

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
		
		// Automatically determine seasonal cycle if it was requested.
		
		if (cycle <= 1) {
			Cycle2 = 0;
			Season = Seasonality.None;
		}
		
		// Verify the series length is at least 2 times the number of
		// parameters. If it isn't compute the sample mean and return that
		// as the forecast. Also if the number of parameters is 0, return
		// the sample mean.
		
		int			num_parms = PolyDegree;
		
		switch(Season) {
		case Add:
			num_parms += cycle-1;
			break;
		case Const1:
			num_parms += 2;
			break;
		case Const2:
			num_parms += 4;
			break;
		case Const3:
			num_parms += 4;
			break;
		case Incrs1:
			num_parms += 4;
			break;
		case Incrs2:
			num_parms += 8;
			break;
		case Incrs3:
			num_parms += 8;
			break;
		case None:
		default:
			break;
		}
		if (num_parms == 0 || num_parms > 2*n) {
			double	mean = IFSStatistics.getMean(series);
			
			for (int i = 0; i < nf; i++) {
				forecasts[i] = mean;
			}
			return String.format("avg::mean:%f", mean);
		}
		
		// Allocate and fill out training vectors. The Y component is the
		// series values and the X component is the parameters corresponding
		// to each Y value. We also generate the forecast parameters as part
		// of the X component while we're at it.
		
		int				nv = n+nf;
		double[][]		tx = new double[nv][];
		int				k = 0;
		double			t = 0;
		
		for (int i = 0; i < nv; i++) {
			tx[i] = new double[num_parms];
			k = 0;
			
			// Add in polynomial parameters
			
			t = 1.0;
			for (int j = 0; j < PolyDegree; j++) {
				t *= i+1;
				tx[i][k++] = t;
			}
			
			// Add in seasonality parameters
			
			switch(Season) {
			case Add:
				for (int j = 0; j < (cycle-1); j++) {
					tx[i][k+j] = 0;
				}
				if (i%cycle > 0) {
					tx[i][k+(i%cycle-1)] = 1;
				}
				break;
			case Const1:
				tx[i][k++] = Math.sin(2.0*Math.PI*(double)(i+1)/(double)cycle);
				tx[i][k++] = Math.cos(2.0*Math.PI*(double)(i+1)/(double)cycle);
				break;
			case Const2:
				tx[i][k++] = Math.sin(2.0*Math.PI*(double)(i+1)/(double)cycle);
				tx[i][k++] = Math.cos(2.0*Math.PI*(double)(i+1)/(double)cycle);
				tx[i][k++] = Math.sin(4.0*Math.PI*(double)(i+1)/(double)cycle);
				tx[i][k++] = Math.cos(4.0*Math.PI*(double)(i+1)/(double)cycle);
				break;
			case Const3:
				tx[i][k++] = Math.sin(2.0*Math.PI*(double)(i+1)/(double)cycle);
				tx[i][k++] = Math.cos(2.0*Math.PI*(double)(i+1)/(double)cycle);
				tx[i][k++] = Math.sin(2.0*Math.PI*(double)(i+1)/(double)Cycle2);
				tx[i][k++] = Math.cos(2.0*Math.PI*(double)(i+1)/(double)Cycle2);
				break;
			case Incrs1:
				tx[i][k++] = Math.sin(2.0*Math.PI*(double)(i+1)/(double)cycle);
				tx[i][k++] = Math.cos(2.0*Math.PI*(double)(i+1)/(double)cycle);
				tx[i][k++] = (double)(i+1)*Math.sin(2.0*Math.PI*(double)(i+1)/(double)cycle);
				tx[i][k++] = (double)(i+1)*Math.cos(2.0*Math.PI*(double)(i+1)/(double)cycle);
				break;
			case Incrs2:
				tx[i][k++] = Math.sin(2.0*Math.PI*(double)(i+1)/(double)cycle);
				tx[i][k++] = Math.cos(2.0*Math.PI*(double)(i+1)/(double)cycle);
				tx[i][k++] = (double)(i+1)*Math.sin(2.0*Math.PI*(double)(i+1)/(double)cycle);
				tx[i][k++] = (double)(i+1)*Math.cos(2.0*Math.PI*(double)(i+1)/(double)cycle);
				tx[i][k++] = Math.sin(4.0*Math.PI*(double)(i+1)/(double)cycle);
				tx[i][k++] = Math.cos(4.0*Math.PI*(double)(i+1)/(double)cycle);
				tx[i][k++] = (double)(i+1)*Math.sin(4.0*Math.PI*(double)(i+1)/(double)cycle);
				tx[i][k++] = (double)(i+1)*Math.cos(4.0*Math.PI*(double)(i+1)/(double)cycle);
				break;
			case Incrs3:
				tx[i][k++] = Math.sin(2.0*Math.PI*(double)(i+1)/(double)cycle);
				tx[i][k++] = Math.cos(2.0*Math.PI*(double)(i+1)/(double)cycle);
				tx[i][k++] = (double)(i+1)*Math.sin(2.0*Math.PI*(double)(i+1)/(double)cycle);
				tx[i][k++] = (double)(i+1)*Math.cos(2.0*Math.PI*(double)(i+1)/(double)cycle);
				tx[i][k++] = Math.sin(2.0*Math.PI*(double)(i+1)/(double)Cycle2);
				tx[i][k++] = Math.cos(2.0*Math.PI*(double)(i+1)/(double)Cycle2);
				tx[i][k++] = (double)(i+1)*Math.sin(2.0*Math.PI*(double)(i+1)/(double)Cycle2);
				tx[i][k++] = (double)(i+1)*Math.cos(2.0*Math.PI*(double)(i+1)/(double)Cycle2);
				break;
			case None:
			default:
				break;
			}
		}
		
		// Compute regression coefficients. If an error occurs, reflect it
		// in calibration string. But return series mean as the forecast.
		
		double[]		rc = null;
		
		try {
		rc = getRegressionCoefficients(series, tx);
		} catch (IFSException ex) {
			double	mean = IFSStatistics.getMean(series);
			
			for (int i = 0; i < nf; i++) {
				forecasts[i] = mean;
			}
			return String.format("reg.err:%s::mean:%f", ex.getCode(), mean);
		}
		
		// Generate forecasts by multiplying coefficients by the forecast
		// training vectors noting the first coefficient is the regression
		// constant.
		
		for (int i = 0; i < nf; i++) {
			forecasts[i] = rc[0];
			for (int j = 0; j < num_parms; j++) {
				forecasts[i] += tx[n+i][j]*rc[j+1];
			}
		}
		
		String	calib_info = String.format("regress::season:%s,poly:%d,"
				+ "cycle:%d,cycle2:%d,const:%.3f", Season.toString(),
				PolyDegree, cycle, Cycle2, rc[0]);
		
		for (int i = 1; i < rc.length; i++) {
			calib_info = String.format("%s,c%d:%.3f", calib_info, i, rc[i]);
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
		int			cycle2 = 0;
		int			poly_degree = 0;
		Seasonality	season = Seasonality.None;
		
		if (parameters != null && parameters.size() > 0) {
			for (IFSParameterValue parameter : parameters) {
				if (parameter.getParameter().equals("cycle2")) {
					try {
					cycle2 = Integer.parseInt(parameter.getValue());
					} catch (NumberFormatException ex) {
						throw new IFSException(20, getModelName(), "cycle2");
					}
					if (cycle2 < 0 || cycle2 == 1) {
						throw new IFSException(21, getModelName(), "cycle2");
					}
				} else if (parameter.getParameter().equals("polynomial_degree")) {
					try {
					poly_degree = Integer.parseInt(parameter.getValue());
					} catch (NumberFormatException ex) {
						throw new IFSException(20, getModelName(), "poly_degree");
					}
					if (poly_degree < 0 || poly_degree > MaxPolyDegree) {
						throw new IFSException(26, getModelName(),
							"poly_degree", MaxPolyDegree);
					}
				} else if (parameter.getParameter().equals("seasonality_type")) {
					if (parameter.getValue().equals("none")) {
						season = Seasonality.None;
					} else if (parameter.getValue().equals("add")) {
						season = Seasonality.Add;
					} else if (parameter.getValue().equals("const1")) {
						season = Seasonality.Const1;
					} else if (parameter.getValue().equals("const2")) {
						season = Seasonality.Const2;
					} else if (parameter.getValue().equals("const3")) {
						season = Seasonality.Const3;
					} else if (parameter.getValue().equals("incrs1")) {
						season = Seasonality.Incrs1;
					} else if (parameter.getValue().equals("incrs2")) {
						season = Seasonality.Incrs2;
					} else if (parameter.getValue().equals("incrs3")) {
						season = Seasonality.Incrs3;
					} else {
						throw new IFSException(22, getModelName(),
							"seasonality", parameter.getValue());
					}
				} else {
					throw new IFSException(23, getModelName(),
						parameter.getParameter());
				}
			}
			if ((season == Seasonality.Const3 || season == Seasonality.Incrs3)
			&& cycle2 == 0) {
				throw new IFSException(27, getModelName(), season);
			}
		}
		
		Cycle2 = cycle2;
		PolyDegree = poly_degree;
		Season = season;
	}
	
/*******************/
/* Private Methods */
/*******************/
	
	/**
	 * Compute regression coefficients for a set of training vectors. The
	 * independent variable component is allowed to contain more vectors
	 * than there are corresponding dependent variable values. This is
	 * because it is often the case that generating forecast vectors at the
	 * same time as calibration vectors is the most convenient.
	 * 
	 * @param y Dependent variable values. There must be at least 1.
	 * @param x Independent variable values. There must be at least as many
	 *    rows as there are dependent variable values (and can be more).
	 *    The first rows correspond to the dependent variable values. And there
	 *    must be at least one column in the independent variable rows and
	 *    all rows must have the same number of columns.
	 * 
	 * @return The computed regression coefficients. The first value is a 
	 *    constant while the remaining values correspond to the independent
	 *    variables.
	 *    
	 * @throws IFSException if the y and x parameters do not have the proper
	 *    dimensions or something goes wrong with the computation.
	 */
	private double[] getRegressionCoefficients(
		double[]	y,
		double[][]	x
	) throws IFSException {
		if (x == null || x.length == 0) {
			throw new IFSException(28, getModelName());
		} else if (y == null || y.length == 0) {
			throw new IFSException(29, getModelName());
		} else if (x.length < y.length) {
			throw new IFSException(30, getModelName());
		}
		
		// Set up multi-linear regression matrix equation Ax = b.
		
		int			ny = y.length;
		int			np = x[0].length;
		int			ntx = ((np+1)*(np+2))/2;
		double[]	tx = new double[ntx];
		int			i = 0;
		int			j = 0;
		int			k = 0;;
		int			l = 0;
		int			p = np+1;
		double[][]	m = new double[p][];
		
		for (i = 0; i < p; i++) {
			m[i] = new double[p+1];
		}
		
		tx[0] = ny;
		for (i = 1; i <= np; i++) {
			tx[i] = 0.0;
			for (j = 0; j < ny; j++) {
				tx[i] += x[j][i-1];
			}
		}
		for (j = 0; j < np; j++) {
			for (k = j; k < np; k++, i++) {
				tx[i] = 0.0;
				for (l = 0; l < ny; l++) {
					tx[i] += x[l][j]*x[l][k];
				}
			}
		}

		m[0][p] = 0.0;
		for (j = 0; j < ny; j++) {
			m[0][p] += y[j];
		}
		for (i = 1; i < p; i++) {
			m[i][p] = 0.0;
			for (j = 0; j < ny; j++) {
				m[i][p] += x[j][i-1]*y[j];
			}
		}
		
		for (k = i = 0; i < p; i++) {
			l = i;
			for (j = 0; j < i; j++) {
				m[i][j] = tx[l];
				l += np-j;
			}
			for (j = i; j < p; j++, k++) {
				m[i][j] = tx[k];
			}
		}
		
		return(IFSComputation.getLinearEqnSoln(m));
	}
}
