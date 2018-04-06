/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.models.alg;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.aol.one.reporting.forecastapi.server.models.model.IFSException;
import com.aol.one.reporting.forecastapi.server.models.model.IFSMetrics;
import com.aol.one.reporting.forecastapi.server.models.model.IFSModel;
import com.aol.one.reporting.forecastapi.server.models.model.IFSModelFactory;
import com.aol.one.reporting.forecastapi.server.models.model.IFSNormalizedOLSTrend;
import com.aol.one.reporting.forecastapi.server.models.model.IFSParameterValue;
import com.aol.one.reporting.forecastapi.server.models.model.IFSStatistics;
import com.aol.one.reporting.forecastapi.server.models.model.IFSUsageDescription;
import com.aol.one.reporting.forecastapi.server.jpe.gw.GW;
import com.aol.one.reporting.forecastapi.server.jpe.gw.GWException;
import com.aol.one.reporting.forecastapi.server.jpe.gw.GWInterface;

/**
 * Class implementing various ARIMA instances. The supported ARIMA instances
 * are:
 * 
 *  1. (0,1,1)
 *  2. (0,1,1)(0,1,1)s
 *  3. (0,1,2)(0,1,1)s
 *  4. (0,2,2)(0,1,1)s
 *  5. (2,1,0)(1,0,0)s
 *  6. (2,1,0)(0,1,1)s
 *  7. (2,1,2)(0,1,1)s
 *  8. (3,1,0)
 *  9. (1,1,1)(1,1,1)s
 * 10. (1,0,0)
 * 11. (1,0,0)(1,0,0)s
 * 12. (1,0,0)(2,0,0)s
 * 13. (2,0,0)
 * 14. (2,0,0)(1,0,0)s
 * 15. (3,0,0)
 * 16. (3,0,0)(1,0,0)s
 * 17. (0,0,1)
 * 18. (0,0,1)(0,0,1)s
 * 19. (0,0,2)
 * 20. (0,0,2)(0,0,1)s
 * 21. (0,0,3)
 * 22. (0,0,3)(0,0,1)s
 * 23. (1,0,1)
 * 24. (1,0,1)(1,0,1)s
 * 25. (2,0,2)
 * 26. (2,0,2)(1,0,1)s
 * 27. (3,0,3)
 * 28. (3,0,3)(1,0,1)s
 * 
 * The model supports the following specific parameters:
 * 
 * arima=<arima specification>
 *    <arima specification> -- One of the supported ARIMA specifications
 *       (e.g. (0,1,1)(0,1,1)s).
 * center -- Should the forecast be centered?
 *
 */
public final class IFSModelImplARIMA extends IFSModel {
	public static final String					ModelName = "model_arima";
	private static final IFSUsageDescription	UsageDescription
	= new IFSUsageDescription(
  "ARIMA forecast model implementation.\n",
  "Implements various ARIMA instances.\n",
  "\n"
+ "arima=<arima specification>\n"
+ "   <arima specification> -- One of the following:\n"
+ "   (0,0,0)\n"
+ "   (0,0,1)\n"
+ "   (0,0,2)\n"
+ "   (0,0,3)\n"
+ "   (0,1,1) (Default)\n"
+ "   (0,1,2)\n"
+ "   (0,2,2)\n"
+ "   (1,0,0)\n"
+ "   (1,0,1)\n"
+ "   (1,1,1)\n"
+ "   (2,0,0)\n"
+ "   (2,0,2)\n"
+ "   (2,1,0)\n"
+ "   (2,1,2)\n"
+ "   (3,0,0)\n"
+ "   (3,0,3)\n"
+ "   (3,1,0)\n"
+ "   (0,0,0)(0,0,1)s\n"
+ "   (0,0,0)(0,0,2)s\n"
+ "   (0,0,0)(0,0,3)s\n"
+ "   (0,0,0)(1,0,0)s\n"
+ "   (0,0,0)(2,0,0)s\n"
+ "   (0,0,0)(3,0,0)s\n"
+ "   (0,0,0)(1,0,1)s\n"
+ "   (0,0,0)(2,0,2)s\n"
+ "   (0,0,0)(3,0,3)s\n"
+ "   (0,0,1)(0,0,1)s\n"
+ "   (0,0,2)(0,0,1)s\n"
+ "   (0,0,3)(0,0,1)s\n"
+ "   (0,1,1)(0,1,1)s\n"
+ "   (0,1,2)(0,1,1)s\n"
+ "   (0,2,2)(0,1,1)s\n"
+ "   (1,0,0)(1,0,0)s\n"
+ "   (1,0,0)(2,0,0)s\n"
+ "   (1,0,1)(1,0,1)s\n"
+ "   (1,1,1)(1,1,1)s\n"
+ "   (2,0,0)(1,0,0)s\n"
+ "   (2,0,2)(1,0,1)s\n"
+ "   (2,1,0)(0,1,1)s\n"
+ "   (2,1,0)(1,0,0)s\n"
+ "   (2,1,2)(0,1,1)s\n"
+ "   (3,0,0)(1,0,0)s\n"
+ "   (3,0,3)(1,0,1)s\n"
+ "\n"
+ "center=<0|1> -- 0 indicates forecast is derived using previous forecasts.\n"
+ "                1 indicates the most recent history is recycled to derive\n"
+ "                forecasts. The default is 0.\n"
	);
	
	private static final Map<String, ArimaData>	ArimaMap;
	
	static {
		ArimaMap = new HashMap<String, ArimaData>();
			
		ArimaMap.put("(0,0,0)",
			new ArimaData("(0,0,0)", false, "(0,0,0)", Arima_0_0_0.class));
		ArimaMap.put("(0,0,1)",
			new ArimaData("(0,0,1)", false, "(0,0,1)", Arima_0_0_1.class));
		ArimaMap.put("(0,0,2)",
			new ArimaData("(0,0,2)", false, "(0,0,2)", Arima_0_0_2.class));
		ArimaMap.put("(0,0,3)",
			new ArimaData("(0,0,3)", false, "(0,0,3)", Arima_0_0_3.class));
		ArimaMap.put("(0,1,1)",
			new ArimaData("(0,1,1)", false, "(0,1,1)", Arima_0_1_1.class));
		ArimaMap.put("(0,1,2)",
			new ArimaData("(0,1,2)", false, "(0,1,2)", Arima_0_1_2.class));
		ArimaMap.put("(0,2,2)",
			new ArimaData("(0,2,2)", false, "(0,2,2)", Arima_0_2_2.class));
		ArimaMap.put("(1,0,0)",
			new ArimaData("(1,0,0)", false, "(1,0,0)", Arima_1_0_0.class));
		ArimaMap.put("(1,0,1)",
			new ArimaData("(1,0,1)", false, "(1,0,1)", Arima_1_0_1.class));
		ArimaMap.put("(1,1,1)",
			new ArimaData("(1,1,1)", false, "(1,1,1)", Arima_1_1_1.class));
		ArimaMap.put("(2,0,0)",
			new ArimaData("(2,0,0)", false, "(2,0,0)", Arima_2_0_0.class));
		ArimaMap.put("(2,0,2)",
			new ArimaData("(2,0,2)", false, "(2,0,2)", Arima_2_0_2.class));
		ArimaMap.put("(2,1,0)",
			new ArimaData("(2,1,0)", false, "(2,1,0)", Arima_2_1_0.class));
		ArimaMap.put("(2,1,2)",
			new ArimaData("(2,1,2)", false, "(2,1,2)", Arima_2_1_2.class));
		ArimaMap.put("(3,0,0)",
			new ArimaData("(3,0,0)", false, "(3,0,0)", Arima_3_0_0.class));
		ArimaMap.put("(3,0,3)",
			new ArimaData("(3,0,3)", false, "(3,0,3)", Arima_3_0_3.class));
		ArimaMap.put("(3,1,0)",
			new ArimaData("(3,1,0)", false, "(3,1,0)", Arima_3_1_0.class));
		ArimaMap.put("(0,0,0)(0,0,1)s",
			new ArimaData("(0,0,0)(0,0,1)s", true, "(0,0,1)", Arima_s_0_0_0__0_0_1.class));
		ArimaMap.put("(0,0,0)(0,0,2)s",
			new ArimaData("(0,0,0)(0,0,2)s", true, "(0,0,2)", Arima_s_0_0_0__0_0_2.class));
		ArimaMap.put("(0,0,0)(0,0,3)s",
			new ArimaData("(0,0,0)(0,0,3)s", true, "(0,0,3)", Arima_s_0_0_0__0_0_3.class));
		ArimaMap.put("(0,0,0)(1,0,0)s",
			new ArimaData("(0,0,0)(1,0,0)s", true, "(1,0,0)", Arima_s_0_0_0__1_0_0.class));
		ArimaMap.put("(0,0,0)(2,0,0)s",
			new ArimaData("(0,0,0)(2,0,0)s", true, "(2,0,0)", Arima_s_0_0_0__2_0_0.class));
		ArimaMap.put("(0,0,0)(3,0,0)s",
			new ArimaData("(0,0,0)(3,0,0)s", true, "(3,0,0)", Arima_s_0_0_0__3_0_0.class));
		ArimaMap.put("(0,0,0)(1,0,1)s",
			new ArimaData("(0,0,0)(1,0,1)s", true, "(1,0,1)", Arima_s_0_0_0__1_0_1.class));
		ArimaMap.put("(0,0,0)(2,0,2)s",
			new ArimaData("(0,0,0)(2,0,2)s", true, "(2,0,2)", Arima_s_0_0_0__2_0_2.class));
		ArimaMap.put("(0,0,0)(3,0,3)s",
			new ArimaData("(0,0,0)(3,0,3)s", true, "(3,0,3)", Arima_s_0_0_0__3_0_3.class));
		ArimaMap.put("(0,0,1)(0,0,1)s",
			new ArimaData("(0,0,1)(0,0,1)s", true, "(0,0,1)", Arima_s_0_0_1__0_0_1.class));
		ArimaMap.put("(0,0,2)(0,0,1)s",
			new ArimaData("(0,0,2)(0,0,1)s", true, "(0,0,2)", Arima_s_0_0_2__0_0_1.class));
		ArimaMap.put("(0,0,3)(0,0,1)s",
			new ArimaData("(0,0,3)(0,0,1)s", true, "(0,0,3)", Arima_s_0_0_3__0_0_1.class));
		ArimaMap.put("(0,1,1)(0,1,1)s",
			new ArimaData("(0,1,1)(0,1,1)s", true, "(0,1,1)", Arima_s_0_1_1__0_1_1.class));
		ArimaMap.put("(0,1,2)(0,1,1)s",
			new ArimaData("(0,1,2)(0,1,1)s", true, "(0,1,2)", Arima_s_0_1_2__0_1_1.class));
		ArimaMap.put("(0,2,2)(0,1,1)s",
			new ArimaData("(0,2,2)(0,1,1)s", true, "(0,2,2)", Arima_s_0_2_2__0_1_1.class));
		ArimaMap.put("(1,0,0)(1,0,0)s",
			new ArimaData("(1,0,0)(1,0,0)s", true, "(1,0,0)", Arima_s_1_0_0__1_0_0.class));
		ArimaMap.put("(1,0,0)(2,0,0)s",
			new ArimaData("(1,0,0)(2,0,0)s", true, "(1,0,0)", Arima_s_1_0_0__2_0_0.class));
		ArimaMap.put("(1,0,1)(1,0,1)s",
			new ArimaData("(1,0,1)(1,0,1)s", true, "(1,0,1)", Arima_s_1_0_1__1_0_1.class));
		ArimaMap.put("(1,1,1)(1,1,1)s",
			new ArimaData("(1,1,1)(1,1,1)s", true, "(1,1,1)", Arima_s_1_1_1__1_1_1.class));
		ArimaMap.put("(2,0,0)(1,0,0)s",
			new ArimaData("(2,0,0)(1,0,0)s", true, "(2,0,0)", Arima_s_2_0_0__1_0_0.class));
		ArimaMap.put("(2,0,2)(1,0,1)s",
			new ArimaData("(2,0,2)(1,0,1)s", true, "(2,0,2)", Arima_s_2_0_2__1_0_1.class));
		ArimaMap.put("(2,1,0)(0,1,1)s",
			new ArimaData("(2,1,0)(0,1,1)s", true, "(2,1,0)", Arima_s_2_1_0__0_1_1.class));
		ArimaMap.put("(2,1,0)(1,0,0)s",
			new ArimaData("(2,1,0)(1,0,0)s", true, "(2,1,0)", Arima_s_2_1_0__1_0_0.class));
		ArimaMap.put("(2,1,2)(0,1,1)s",
			new ArimaData("(2,1,2)(0,1,1)s", true, "(2,1,2)", Arima_s_2_1_2__0_1_1.class));
		ArimaMap.put("(3,0,0)(1,0,0)s",
			new ArimaData("(3,0,0)(1,0,0)s", true, "(3,0,0)", Arima_s_3_0_0__1_0_0.class));
		ArimaMap.put("(3,0,3)(1,0,1)s",
			new ArimaData("(3,0,3)(1,0,1)s", true, "(3,0,3)", Arima_s_3_0_3__1_0_1.class));
	}
	
	private String								ArimaSpec = "(0,1,1)";
	private boolean								IsCenter = false;

	/* (non-Javadoc)
	 * @see com.aol.ifs.soa.common.IFSModel#execModel(double[], double[], int)
	 */
	@Override
	protected String execModel(
		double[]	series,
		double[]	forecasts,
		int			cycle
	) throws IFSException {
		int				ns = series.length;
		int				nf = forecasts.length;
		String			arima_spec = ArimaSpec;
		
		// If the series length is not at least 3, we use random walk.
		
		if (ns < 3) {
			for (int i = 0; i < nf; i++) {
				forecasts[i] = series[ns-1];
			}
			return "rw";
		}
		
		ArimaData		defn = ArimaMap.get(arima_spec);
		
		// If the series length is not at least 2 times the cycle, we drop
		// back to associated non-seasonal variant.
		
		if (defn.IsSeasonal && (cycle <= 1 || 2*cycle > ns)) {
			arima_spec = defn.NonSeasonSpec;
			defn = ArimaMap.get(arima_spec);
		}
		
		
		ArimaVariant	model = null;
		
		try {
		model = (ArimaVariant)defn.ModelClass.newInstance();
		} catch (InstantiationException ex) {
			throw new IFSException(15, getModelName(), ex.getMessage());
		} catch (IllegalAccessException ex) {
			throw new IFSException(15, getModelName(), ex.getMessage());
		}
		
		double[]		forecasts_t = new double[series.length+forecasts.length];
		String			fcst_info = null;
		
		model.init(model, series, cycle, IsCenter);
		model.calibrate();
		fcst_info = model.forecast(forecasts_t);
        System.arraycopy(forecasts_t, series.length, forecasts, 0, forecasts.length);
		
		return String.format("arima::spec:%s,cycle:%d,fcst(%s)",
			ArimaSpec, cycle, fcst_info);
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
		String	arima_spec = "(0,1,1)";
		boolean is_center = false;
		
		if (parameters != null && parameters.size() > 0) {
			for (IFSParameterValue parameter : parameters) {
				if (parameter.getParameter().equals("arima")) {
					ArimaData	defn = ArimaMap.get(parameter.getValue());
					
					if (defn == null) {
						throw new IFSException(22, getModelName(),
							"arima", parameter.getValue());
					}
					arima_spec = defn.Spec;
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
				} else {
					throw new IFSException(23, getModelName(),
						parameter.getParameter());
				}
			}
		}
		
		ArimaSpec = arima_spec;
		IsCenter = is_center;
	}
    
/*******************/
/* Private Methods */
/*******************/
	
	/**
	 * Backcast the given series.
	 * 
	 * @param series Time series to backcast.
	 * @param cycle Seasonal cycle.
	 * @param num_backcasts Number backcasts to produce.
	 * 
	 * @return Backcasts.
	 */
	private static double[] backcast(
		double[]	series,
		int			cycle,
		int			num_backcasts
	) {
		double[]				rev_series = new double[series.length];
		
		for (int i = 0; i < series.length; i++) {
			rev_series[i] = series[series.length-i-1];
		}
		
        List<IFSParameterValue>	parameters = new LinkedList<IFSParameterValue>();
        IFSModel				model = null;
        double[]				backcasts = null;
        
        parameters.add(new IFSParameterValue("cycle", Integer.toString(cycle)));
        parameters.add(new IFSParameterValue("ndays_back", "0"));
        parameters.add(new IFSParameterValue("seasonality_type", "mult"));
        parameters.add(new IFSParameterValue("spike_filter", "0"));
        
        try {
        	
        model = IFSModelFactory.create("model_expsm");
        IFSModelFactory.setup(model, rev_series, parameters);
        model.generateForecasts(num_backcasts);
        backcasts = model.getForecasts();
        
        } catch (IFSException ex) {
        	System.out.format("Could not create backcast model: %s\n",
        		ex.getMessage());
        	ex.printStackTrace(System.out);
        	return null;
        }
		return backcasts;
	}
	
	/**
	 * Center forecasts based on recycling earliest values. Whether to center
	 * or not depends on whether center flag is set and whether there is a
	 * non-zero slope of the mean smoothed forecasts.
	 * 
	 * @param is_center Center forecasts?
	 * @param start_idx Where to start recycling.
	 * @param cycle Number of early values to recycle.
	 * @param forecasts Forecasts to adjust.
	 * 
	 * @return Slope of the mean smoothed forecasts if center flag is set. If
	 *    flag is not set, 0 is returned.
	 * 
	 * @throws IFSException if a centering specification error occurs.
	 */
	private static double centerForecasts(
		boolean		is_center,
		int			start_idx,
		int			cycle,
		double[]	forecasts
	) throws IFSException {
		final int	NumSegms = 10;
		
		if (!is_center) {
			return 0.0;
		}
		
		int		nv = forecasts.length-start_idx;
		
		if (nv < (NumSegms+1)) {
			return 0.0;
		}
		
		double[]	vs = new double[nv];
		double		slope = 0.0;
		
		System.arraycopy(forecasts, start_idx, vs, 0, nv);
		slope = IFSNormalizedOLSTrend
			.getNormalizedOLSTrendMS(vs, NumSegms).getSecond();
		if (IFSStatistics.isZero(slope)) {
			return 0.0;
		}
		
		int		j = 0;
		
		for (int i = start_idx+cycle; i < forecasts.length; i++) {
			forecasts[i] = forecasts[j+start_idx];
			j = (j+1)%cycle;
		}
		
		return slope;
	}
	
	/**
	 * Generate a missing value for a series for a given index.
	 * 
	 * @param idx Missing value index.
	 * @param av Where to get missing values.
	 * 
	 * @return Missing value.
	 */
	private static double genMissValue(
		int				idx,
		ArimaVariant	av
	) {
		double[]	iv = av.getIV();
				
		if (iv == null) {
			return av.getMean();
		}
		
		return iv[idx];
	}
	
    /**
     * Fetch an error term.
     *
     * @param idx Value index.
     * @param series Time series values.
     * @param forecasts Forecast values. This vector is assumed to have
     *    extra values beyond the time series to cover all value indices.
     * @param av Where to get values for negative indices.
     *
     * @return Designated error term.
     */
    private static double et(
        int         	idx,
        double[]    	series,
        double[]    	forecasts,
        ArimaVariant	av
    ) {
    	if (idx < 0) {
    		return yt(idx, series, forecasts, av)
    			-yt(idx-1, series, forecasts, av);
    	} else {
    		return yt(idx, series, forecasts, av)-forecasts[idx];
    	}
    }
	
    /**
     * Fetch time series value if index is in time series range. Otherwise,
     * a previous forecast value is returned. Negative indices return missing
     * values.
     *
     * @param idx Value index.
     * @param series Time series values.
     * @param forecasts Forecast values. This vector is assumed to have
     *    extra values beyond the time series to cover all value indices.
     * @param av Where to get values for negative indices.
     *
     * @return The time series value if the value index is in that range.
     *    Values beyond that range are taken from the forecast vector.
     *    Negative indices return missing values. 
     */
    private static double yt(
        int         	idx,
        double[]    	series,
        double[]    	forecasts,
        ArimaVariant	av
    ) {
    	if (idx < 0) {
    		return genMissValue(Math.abs(idx)-1, av);
    	} else if (0 <= idx && idx < series.length) {
            return series[idx];
    	} else {
            return forecasts[idx];
    	}
    }

/*******************/
/* Private Classes */
/*******************/

/**
 * Class encapsulating an Arima variant specification.
 */
private static final class ArimaData {
	public final String		Spec;
	public final boolean	IsSeasonal;
	public final String		NonSeasonSpec;
	@SuppressWarnings("rawtypes")
	public final Class		ModelClass;
	
	/**
	 * Default constructor.
	 */
	@SuppressWarnings("unused")
	public ArimaData() {
		Spec = null;
		IsSeasonal = false;
		NonSeasonSpec = null;
		ModelClass = null;
	}
	
	/**
	 * Fully specified constructor.
	 * 
	 * @param spec Arima variant specification.
	 * @param is_seasonal Is Arima variant seasonal?
	 * @param non_season_spec Arima variant to use if series has no cycle.
	 * @param model_class Arima variant class to use.
	 */
	public ArimaData(
		String		spec,
		boolean		is_seasonal,
		String		non_season_spec,
		@SuppressWarnings("rawtypes")
		Class		model_class
	) {
		Spec = spec;
		IsSeasonal = is_seasonal;
		NonSeasonSpec = non_season_spec;
		ModelClass = model_class;
	}
}

/**
 * Abstract class for implementing ARIMA variants.
 */
private static abstract class ArimaVariant implements GWInterface {
	protected static final double	CoeffMin = -3.0;
	protected static final double	CoeffMax = 3.0;
	
	private static final double[]	Steps = {0.1, 0.01, 0.001};

	protected boolean				IsCenter;
	protected int					Cycle;
	protected double[]				Series;
	protected GWInterface			Model;
	protected double				Mean;
	protected double[]				IV;
	
	/**
	 * Default constructor.
	 */
	public ArimaVariant() {
		reset();
	}
	
    /**
     * Fetch the number of coefficient significant digits.
     * 
     * @return The number of coefficient significant digits.
     */
	public final void calibrate() {
		try {
		new GW(Model, false, System.out);
		} catch (GWException ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Generate forecasts and return string representing forecast parameter
	 * info.
	 * 
	 * @param forecasts Where to put forecasts. Array length determines how
	 *    many forecasts to generate. Must be long enough to include forecasts
	 *    for series in addition to future predictions (if any).
	 * 
	 * @return Forecast parameter info string.
	 */
	public abstract String forecast(
		double[]	forecasts
	);
	
	/**
	 * Fetch initial values.
	 * 
	 * @return Initial values.
	 */
	public final double[] getIV() {
		return IV;
	}
	
	/**
	 * Fetch series mean.
	 * 
	 * @returnSeries mean.
	 */
	public final double getMean() {
		return Mean;
	}
	
    /**
     * Fetch the number of coefficient significant digits.
     * 
     * @return The number of coefficient significant digits.
     */
	public final int getNumLevels() {
		return Steps.length;
	}
    
    /**
     * Fetch coefficient precision depending on current optimization level.
     * 
     * @param level Current optimization level.
     * @param weight_idx N/A.
     * 
     * @return Coefficient precision.
     */
	public final double getStepSize(
		int		level,
		int		weight_idx
	) {
		return Steps[level];
	}

	public final double getWeightLowerBound(
		int			weight_idx
	) {
		return CoeffMin;
	}

	public final double getWeightUpperBound(
		int			weight_idx
	) {
		return CoeffMax;
	}
	
	/**
	 * Initialize model.
	 * 
	 * @param model GW model to use.
	 * @param series Historical time series to access.
	 * @param forecasts Where to put forecasts.
	 * @param cycle Seasonal cycle.
	 * @param is_center Center forecasts?
	 */
	public final void init(
		GWInterface	model,
		double[]	series, 
		int			cycle,
		boolean		is_center
	) {
		Model = model;
		Series = series;
		Cycle = cycle;
		IsCenter = is_center;
		Mean = IFSStatistics.getMean(Series);
		IV = null;
	}

    /**
     * Print trace information.
     * 
     * @param trace_out Where to print trace info if trace is enabled.
     * @param iteration Optimization iteration. Can be 0 on initialization.
     * @param level Optimization accuracy level (0-based).
     * @param coeff_idx Coefficient index. Can be -1 when no specific
     *    coefficient is being adjusted.
     * @param coeffs Coefficient vector.
     * @param rating Coefficient vector rating.
     */
    public final void printTrace(
		PrintStream		trace_out,
		int				iteration,
		int				level,
		int				coeff_idx,
		double[]		coeffs,
		double			rating
	) {
		if (iteration < 1) {
			trace_out.printf("Iter   Level  Idx    Rating          ");
			for (int i = 0; i < coeffs.length; i++) {
				trace_out.printf("  C%d    ", i+1);
			}
			trace_out.printf("\n");
		}
		trace_out.printf("%5d  %5d  %5d  %,14.2f",
			iteration, level, coeff_idx, rating);
		for (int i = 0; i < coeffs.length; i++) {
			trace_out.printf("  %6.3f", coeffs[i]);
		}
		trace_out.printf("\n");
	}

    /**
     * Reset variant object state.
     */
    public final void reset() {
		Model = null;
		Cycle = 0;
		Series = null;
		Mean = 0.0;
		IV = null;
    }
	
	/**
	 * Produce an initial value string representation.
	 * 
	 * @return Initial value string representation.
	 */
	public String toStringIV() {
		String		svals = "";
		
		for (int i = 0; i < IV.length; i++) {
			svals = String.format("%s%siv%d:%.3f",
				svals, (i == 0) ? "" : ",", i+1, IV[i]);
		}
		
		return svals;
	}
}

/**
 * Class implementing ARIMA(0,0,0).
 */
private static final class Arima_0_0_0 extends ArimaVariant {
	double		AIC = 0.0;
	
	@SuppressWarnings("unused")
	public Arima_0_0_0() {}

	public int getNumWeights() {
		return 0;
	}

	public double getRating(
		double[]	coefficients
	) {
        double[]	fitfcsts = new double[Series.length];

		forecast(fitfcsts);
        try {
		return AIC = IFSMetrics.getAdjAIC(getNumWeights(), Series, fitfcsts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			return Double.MAX_VALUE;
		}
	}

	public String forecast(
		double[]	forecasts
	) {
		double		u = getMean();
		
		forecasts[0] = Series[0];
        for (int i = 1; i < forecasts.length; i++) {
        	forecasts[i] = u;
        }
        
        double	slope = 0.0;
        boolean	iserror = false;
        
        try {
        slope = centerForecasts(IsCenter, Series.length, 1, forecasts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			iserror = true;
		}
        
        return String.format("variant:%s,iscenter:%s,slope:%.6f,aic:%.3f,"
        + "u:%.3f",
        	"(0,0,0)", iserror ? "error" : IsCenter, slope, AIC,
        	u);
	}
}

/**
 * Class implementing ARIMA(0,0,1).
 */
private static final class Arima_0_0_1 extends ArimaVariant {
	double		AIC = 0.0;
	double		V1 = 0.0;
	
	@SuppressWarnings("unused")
	public Arima_0_0_1() {}

	public int getNumWeights() {
		return 1;
	}

	public double getRating(
		double[]	coefficients
	) {
        double[]	fitfcsts = new double[Series.length];

		V1 = coefficients[0];
		forecast(fitfcsts);
        try {
		return AIC = IFSMetrics.getAdjAIC(getNumWeights(), Series, fitfcsts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			return Double.MAX_VALUE;
		}
	}

	public String forecast(
		double[]	forecasts
	) {
		double		u = getMean();
		
		forecasts[0] = Series[0];
        for (int i = 1; i < forecasts.length; i++) {
        	forecasts[i] = u
        		- V1*et(i-1, Series, forecasts, this);
        }
        
        double	slope = 0.0;
        boolean	iserror = false;
        
        try {
        slope = centerForecasts(IsCenter, Series.length, 1, forecasts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			iserror = true;
		}
        
        return String.format("variant:%s,iscenter:%s,slope:%.6f,aic:%.3f,"
        + "u:%.3f,v1:%.3f",
        	"(0,0,1)", iserror ? "error" : IsCenter, slope, AIC,
        	u, V1);
	}
}

/**
 * Class implementing ARIMA(0,0,2).
 */
private static final class Arima_0_0_2 extends ArimaVariant {
	double		AIC = 0.0;
	double		V1 = 0.0;
	double		V2 = 0.0;
	
	@SuppressWarnings("unused")
	public Arima_0_0_2() {}

	public int getNumWeights() {
		return 2;
	}

	public double getRating(
		double[]	coefficients
	) {
        double[]	fitfcsts = new double[Series.length];

		V1 = coefficients[0];
		V2 = coefficients[1];
		forecast(fitfcsts);
        try {
		return AIC = IFSMetrics.getAdjAIC(getNumWeights(), Series, fitfcsts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			return Double.MAX_VALUE;
		}
	}

	public String forecast(
		double[]	forecasts
	) {
		double		u = getMean();
		
		forecasts[0] = Series[0];
        for (int i = 1; i < forecasts.length; i++) {
        	forecasts[i] = u
        		- V1*et(i-1, Series, forecasts, this)
        		- V2*et(i-2, Series, forecasts, this);
        }
        
        double	slope = 0.0;
        boolean	iserror = false;
        
        try {
        slope = centerForecasts(IsCenter, Series.length, 2, forecasts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			iserror = true;
		}
        
        return String.format("variant:%s,iscenter:%s,slope:%.6f,aic:%.3f,"
        + "u:%.3f,v1:%.3f,v2:%.3f",
        	"(0,0,2)", iserror ? "error" : IsCenter, slope, AIC,
        	u, V1, V2);
	}
}

/**
 * Class implementing ARIMA(0,0,3).
 */
private static final class Arima_0_0_3 extends ArimaVariant {
	double		AIC = 0.0;
	double		V1 = 0.0;
	double		V2 = 0.0;
	double		V3 = 0.0;
	
	@SuppressWarnings("unused")
	public Arima_0_0_3() {}

	public int getNumWeights() {
		return 3;
	}

	public double getRating(
		double[]	coefficients
	) {
        double[]	fitfcsts = new double[Series.length];

		V1 = coefficients[0];
		V2 = coefficients[1];
		V3 = coefficients[2];
		forecast(fitfcsts);
        try {
		return AIC = IFSMetrics.getAdjAIC(getNumWeights(), Series, fitfcsts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			return Double.MAX_VALUE;
		}
	}

	public String forecast(
		double[]	forecasts
	) {
		double		u = getMean();
		
		forecasts[0] = Series[0];
        for (int i = 1; i < forecasts.length; i++) {
        	forecasts[i] = u
        		- V1*et(i-1, Series, forecasts, this)
        		- V2*et(i-2, Series, forecasts, this)
        		- V3*et(i-3, Series, forecasts, this);
        }
        
        double	slope = 0.0;
        boolean	iserror = false;
        
        try {
        slope = centerForecasts(IsCenter, Series.length, 3, forecasts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			iserror = true;
		}
        
        return String.format("variant:%s,iscenter:%s,slope:%.6f,aic:%.3f,"
        + "u:%.3f,v1:%.3f,v2:%.3f,v3:%.3f",
        	"(0,0,3)", iserror ? "error" : IsCenter, slope, AIC,
        	u, V1, V2, V3);
	}
}

/**
 * Class implementing ARIMA(0,1,1).
 */
private static final class Arima_0_1_1 extends ArimaVariant {
	double		AIC = 0.0;
	double		V1 = 0.0;
	final int	NIV = 1;
	
	@SuppressWarnings("unused")
	public Arima_0_1_1() {}

	public int getNumWeights() {
		return 1;
	}

	public double getRating(
		double[]	coefficients
	) {
        double[]	fitfcsts = new double[Series.length];

		V1 = coefficients[0];
		if (IV == null) {
			IV = backcast(Series, Cycle, NIV);
		}
		forecast(fitfcsts);
        try {
		return AIC = IFSMetrics.getAdjAIC(getNumWeights(), Series, fitfcsts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			return Double.MAX_VALUE;
		}
	}

	public String forecast(
		double[]	forecasts
	) {
		forecasts[0] = Series[0];
        for (int i = 1; i < forecasts.length; i++) {
        	forecasts[i] = yt(i-1, Series, forecasts, this)
                -V1*et(i-1, Series, forecasts, this);
        }
        
        double	slope = 0.0;
        boolean	iserror = false;
        
        try {
        slope = centerForecasts(IsCenter, Series.length, 1, forecasts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			iserror = true;
		}
        
        return String.format("variant:%s,iscenter:%s,slope:%.6f,aic:%.3f,"
        + "v1:%.3f,%s",
        	"(0,1,1)", iserror ? "error" : IsCenter, slope, AIC,
        	V1, toStringIV());
	}
}

/**
 * Class implementing ARIMA(0,1,2).
 */
private static final class Arima_0_1_2 extends ArimaVariant {
	double		AIC = 0.0;
	double		V1 = 0.0;
	double		V2 = 0.0;
	final int	NIV = 3;
	
	@SuppressWarnings("unused")
	public Arima_0_1_2() {}

	public int getNumWeights() {
		return 2;
	}

	public double getRating(
		double[]	coefficients
	) {
        double[]	fitfcsts = new double[Series.length];

		V1 = coefficients[0];
		V2 = coefficients[1];
		if (IV == null) {
			IV = backcast(Series, Cycle, NIV);
		}
		forecast(fitfcsts);
        try {
		return AIC = IFSMetrics.getAdjAIC(getNumWeights(), Series, fitfcsts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			return Double.MAX_VALUE;
		}
	}

	public String forecast(
		double[]	forecasts
	) {
		forecasts[0] = Series[0];
        for (int i = 1; i < forecasts.length; i++) {
        	forecasts[i] = yt(i-1, Series, forecasts, this)
                -V1*et(i-1, Series, forecasts, this)
                -V2*et(i-2, Series, forecasts, this);
        }
        
        double	slope = 0.0;
        boolean	iserror = false;
        
        try {
        slope = centerForecasts(IsCenter, Series.length, 2, forecasts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			iserror = true;
		}
        
        return String.format("variant:%s,iscenter:%s,slope:%.6f,aic:%.3f,"
        + "v1:%.3f,v2:%.3f,%s",
        	"(0,1,2)", iserror ? "error" : IsCenter, slope, AIC,
        	V1, V2, toStringIV());
	}
}

/**
 * Class implementing ARIMA(0,2,2).
 */
private static final class Arima_0_2_2 extends ArimaVariant {
	double		AIC = 0.0;
	double		V1 = 0.0;
	double		V2 = 0.0;
	final int	NIV = 3;
	
	@SuppressWarnings("unused")
	public Arima_0_2_2() {}

	public int getNumWeights() {
		return 2;
	}

	public double getRating(
		double[]	coefficients
	) {
        double[]	fitfcsts = new double[Series.length];

		V1 = coefficients[0];
		V2 = coefficients[1];
		if (IV == null) {
			IV = backcast(Series, Cycle, NIV);
		}
		forecast(fitfcsts);
        try {
        return AIC = IFSMetrics.getAdjAIC(getNumWeights(), Series, fitfcsts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			return Double.MAX_VALUE;
		}
	}

	public String forecast(
		double[]	forecasts
	) {
		forecasts[0] = Series[0];
        for (int i = 1; i < forecasts.length; i++) {
        	forecasts[i] = 2.0*yt(i-1, Series, forecasts, this)
        		-yt(i-2, Series, forecasts, this)
                -V1*et(i-1, Series, forecasts, this)
                -V2*et(i-2, Series, forecasts, this);
        }
        
        double	slope = 0.0;
        boolean	iserror = false;
        
        try {
        slope = centerForecasts(IsCenter, Series.length, 2, forecasts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			iserror = true;
		}
        
        return String.format("variant:%s,iscenter:%s,slope:%.6f,aic:%.3f,"
        + "v1:%.3f,v2:%.3f,%s",
        	"(0,2,2)", iserror ? "error" : IsCenter, slope, AIC,
        	V1, V2, toStringIV());
	}
}

/**
 * Class implementing ARIMA(1,0,0).
 */
private static final class Arima_1_0_0 extends ArimaVariant {
	double		AIC = 0.0;
	double		U1 = 0.0;
	
	@SuppressWarnings("unused")
	public Arima_1_0_0() {}

	public int getNumWeights() {
		return 1;
	}

	public double getRating(
		double[]	coefficients
	) {
        double[]	fitfcsts = new double[Series.length];

		U1 = coefficients[0];
		forecast(fitfcsts);
        try {
		return AIC = IFSMetrics.getAdjAIC(getNumWeights(), Series, fitfcsts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			return Double.MAX_VALUE;
		}
	}

	public String forecast(
		double[]	forecasts
	) {
		double		u = getMean();
		
		forecasts[0] = Series[0];
        for (int i = 1; i < forecasts.length; i++) {
        	forecasts[i] = u
        		+ U1*(yt(i-1, Series, forecasts, this)-u);
        }
        
        double	slope = 0.0;
        boolean	iserror = false;
        
        try {
        slope = centerForecasts(IsCenter, Series.length, 1, forecasts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			iserror = true;
		}
        
        return String.format("variant:%s,iscenter:%s,slope:%.6f,aic:%.3f,"
        + "u:%.3f,u1:%.3f",
        	"(1,0,0)", iserror ? "error" : IsCenter, slope, AIC,
        	u, U1);
	}
}

/**
 * Class implementing ARIMA(1,0,1).
 */
private static final class Arima_1_0_1 extends ArimaVariant {
	double		AIC = 0.0;
	double		U1 = 0.0;
	double		V1 = 0.0;
	
	@SuppressWarnings("unused")
	public Arima_1_0_1() {}

	public int getNumWeights() {
		return 2;
	}

	public double getRating(
		double[]	coefficients
	) {
        double[]	fitfcsts = new double[Series.length];

		U1 = coefficients[0];
		V1 = coefficients[1];
		forecast(fitfcsts);
        try {
		return AIC = IFSMetrics.getAdjAIC(getNumWeights(), Series, fitfcsts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			return Double.MAX_VALUE;
		}
	}

	public String forecast(
		double[]	forecasts
	) {
		double		u = getMean();
		
		forecasts[0] = Series[0];
        for (int i = 1; i < forecasts.length; i++) {
        	forecasts[i] = u
            	+ U1*(yt(i-1, Series, forecasts, this)-u)
            	- V1*et(i-1, Series, forecasts, this);
        }
        
        double	slope = 0.0;
        boolean	iserror = false;
        
        try {
        slope = centerForecasts(IsCenter, Series.length, 1, forecasts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			iserror = true;
		}
        
        return String.format("variant:%s,iscenter:%s,slope:%.6f,aic:%.3f,"
        + "u:%.3f,u1:%.3f,v1:%.3f",
        	"(1,0,1)", iserror ? "error" : IsCenter, slope, AIC,
        	u, U1, V1);
	}
}

/**
 * Class implementing ARIMA(1,1,1).
 */
private static final class Arima_1_1_1 extends ArimaVariant {
	double		AIC = 0.0;
	double		U1 = 0.0;
	double		V1 = 0.0;
	final int	NIV = 2;
	
	@SuppressWarnings("unused")
	public Arima_1_1_1() {}

	public int getNumWeights() {
		return 2;
	}

	public double getRating(
		double[]	coefficients
	) {
        double[]	fitfcsts = new double[Series.length];

		U1 = coefficients[0];
		V1 = coefficients[1];
		if (IV == null) {
			IV = backcast(Series, Cycle, NIV);
		}
		forecast(fitfcsts);
        try {
		return AIC = IFSMetrics.getAdjAIC(getNumWeights(), Series, fitfcsts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			return Double.MAX_VALUE;
		}
	}

	public String forecast(
		double[]	forecasts
	) {
		forecasts[0] = Series[0];
        for (int i = 1; i < forecasts.length; i++) {
        	forecasts[i] = yt(i-1, Series, forecasts, this)
            	+ U1*(yt(i-1, Series, forecasts, this)-yt(i-2, Series, forecasts, this))
            	- V1*et(i-1, Series, forecasts, this);
        }
        
        double	slope = 0.0;
        boolean	iserror = false;
        
        try {
        slope = centerForecasts(IsCenter, Series.length, 2, forecasts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			iserror = true;
		}
        
        return String.format("variant:%s,iscenter:%s,slope:%.6f,aic:%.3f,"
        + "u1:%.3f,v1:%.3f,%s",
        	"(1,1,1)", iserror ? "error" : IsCenter, slope, AIC,
        	U1, V1, toStringIV());
	}
}

/**
 * Class implementing ARIMA(2,0,0).
 */
private static final class Arima_2_0_0 extends ArimaVariant {
	double		AIC = 0.0;
	double		U1 = 0.0;
	double		U2 = 0.0;
	
	@SuppressWarnings("unused")
	public Arima_2_0_0() {}

	public int getNumWeights() {
		return 2;
	}

	public double getRating(
		double[]	coefficients
	) {
        double[]	fitfcsts = new double[Series.length];

		U1 = coefficients[0];
		U2 = coefficients[1];
		forecast(fitfcsts);
        try {
		return AIC = IFSMetrics.getAdjAIC(getNumWeights(), Series, fitfcsts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			return Double.MAX_VALUE;
		}
	}

	public String forecast(
		double[]	forecasts
	) {
		double		u = getMean();
		
		forecasts[0] = Series[0];
        for (int i = 1; i < forecasts.length; i++) {
        	forecasts[i] = u
        		+ U1*(yt(i-1, Series, forecasts, this)-u)
    			+ U2*(yt(i-2, Series, forecasts, this)-u);
        }
        
        double	slope = 0.0;
        boolean	iserror = false;
        
        try {
        slope = centerForecasts(IsCenter, Series.length, 2, forecasts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			iserror = true;
		}
        
        return String.format("variant:%s,iscenter:%s,slope:%.6f,aic:%.3f,"
        + "u:%.3f,u1:%.3f,u2:%.3f",
        	"(2,0,0)", iserror ? "error" : IsCenter, slope, AIC,
        	u, U1, U2);
	}
}

/**
 * Class implementing ARIMA(2,0,2).
 */
private static final class Arima_2_0_2 extends ArimaVariant {
	double		AIC = 0.0;
	double		U1 = 0.0;
	double		U2 = 0.0;
	double		V1 = 0.0;
	double		V2 = 0.0;
	
	@SuppressWarnings("unused")
	public Arima_2_0_2() {}

	public int getNumWeights() {
		return 4;
	}

	public double getRating(
		double[]	coefficients
	) {
        double[]	fitfcsts = new double[Series.length];

		U1 = coefficients[0];
		U2 = coefficients[1];
		V1 = coefficients[2];
		V2 = coefficients[3];
		forecast(fitfcsts);
        try {
		return AIC = IFSMetrics.getAdjAIC(getNumWeights(), Series, fitfcsts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			return Double.MAX_VALUE;
		}
	}

	public String forecast(
		double[]	forecasts
	) {
		double		u = getMean();
		
		forecasts[0] = Series[0];
        for (int i = 1; i < forecasts.length; i++) {
        	forecasts[i] = u
        		+ U1*(yt(i-1, Series, forecasts, this)-u)
        		+ U2*(yt(i-2, Series, forecasts, this)-u)
                - V1*et(i-1, Series, forecasts, this)
                - V2*et(i-2, Series, forecasts, this);
        }
        
        double	slope = 0.0;
        boolean	iserror = false;
        
        try {
        slope = centerForecasts(IsCenter, Series.length, 2, forecasts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			iserror = true;
		}
        
        return String.format("variant:%s,iscenter:%s,slope:%.6f,aic:%.3f,"
        + "u:%.3f,u1:%.3f,u2:%.3f,v1:%.3f,v2:%.3f",
        	"(2,0,2)", iserror ? "error" : IsCenter, slope, AIC,
        	u, U1, U2, V1, V2);
	}
}

/**
 * Class implementing ARIMA(2,1,0).
 */
private static final class Arima_2_1_0 extends ArimaVariant {
	double		AIC = 0.0;
	double		U1 = 0.0;
	double		U2 = 0.0;
	final int	NIV = 3;
	
	@SuppressWarnings("unused")
	public Arima_2_1_0() {}

	public int getNumWeights() {
		return 2;
	}

	public double getRating(
		double[]	coefficients
	) {
        double[]	fitfcsts = new double[Series.length];

		U1 = coefficients[0];
		U2 = coefficients[1];
		if (IV == null) {
			IV = backcast(Series, Cycle, NIV);
		}
		forecast(fitfcsts);
        try {
		return AIC = IFSMetrics.getAdjAIC(getNumWeights(), Series, fitfcsts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			return Double.MAX_VALUE;
		}
	}

	public String forecast(
		double[]	forecasts
	) {
		forecasts[0] = Series[0];
        for (int i = 1; i < forecasts.length; i++) {
        	forecasts[i] = yt(i-1, Series, forecasts, this)
        		+ U1*(yt(i-1, Series, forecasts, this)-yt(i-2, Series, forecasts, this))
        		+ U2*(yt(i-2, Series, forecasts, this)-yt(i-3, Series, forecasts, this));
        }
        
        double	slope = 0.0;
        boolean	iserror = false;
        
        try {
        slope = centerForecasts(IsCenter, Series.length, 3, forecasts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			iserror = true;
		}
        
        return String.format("variant:%s,iscenter:%s,slope:%.6f,aic:%.3f,"
        + "u1:%.3f,u2:%.3f,%s",
        	"(2,1,0)", iserror ? "error" : IsCenter, slope, AIC,
        	U1, U2, toStringIV());
	}
}

/**
 * Class implementing ARIMA(2,1,2).
 */
private static final class Arima_2_1_2 extends ArimaVariant {
	double		AIC = 0.0;
	double		U1 = 0.0;
	double		U2 = 0.0;
	double		V1 = 0.0;
	double		V2 = 0.0;
	final int	NIV = 3;
	
	@SuppressWarnings("unused")
	public Arima_2_1_2() {}

	public int getNumWeights() {
		return 4;
	}

	public double getRating(
		double[]	coefficients
	) {
        double[]	fitfcsts = new double[Series.length];

		U1 = coefficients[0];
		U2 = coefficients[1];
		V1 = coefficients[2];
		V2 = coefficients[3];
		if (IV == null) {
			IV = backcast(Series, Cycle, NIV);
		}
		forecast(fitfcsts);
        try {
		return AIC = IFSMetrics.getAdjAIC(getNumWeights(), Series, fitfcsts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			return Double.MAX_VALUE;
		}
	}

	public String forecast(
		double[]	forecasts
	) {
		forecasts[0] = Series[0];
        for (int i = 1; i < forecasts.length; i++) {
        	forecasts[i] = yt(i-1, Series, forecasts, this)
        		+ U1*(yt(i-1, Series, forecasts, this)-yt(i-2, Series, forecasts, this))
        		+ U2*(yt(i-2, Series, forecasts, this)-yt(i-3, Series, forecasts, this))
                - V1*et(i-1, Series, forecasts, this)
                - V2*et(i-2, Series, forecasts, this);
        }
        
        double	slope = 0.0;
        boolean	iserror = false;
        
        try {
        slope = centerForecasts(IsCenter, Series.length, 3, forecasts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			iserror = true;
		}
        
        return String.format("variant:%s,iscenter:%s,slope:%.6f,aic:%.3f,"
        + "u1:%.3f,u2:%.3f,v1:%.3f,v2:%.3f,%s",
        	"(2,1,2)", iserror ? "error" : IsCenter, slope, AIC,
        	U1, U2, V1, V2, toStringIV());
	}
}

/**
 * Class implementing ARIMA(3,0,0).
 */
private static final class Arima_3_0_0 extends ArimaVariant {
	double		AIC = 0.0;
	double		U1 = 0.0;
	double		U2 = 0.0;
	double		U3 = 0.0;
	
	@SuppressWarnings("unused")
	public Arima_3_0_0() {}

	public int getNumWeights() {
		return 3;
	}

	public double getRating(
		double[]	coefficients
	) {
        double[]	fitfcsts = new double[Series.length];

		U1 = coefficients[0];
		U2 = coefficients[1];
		U3 = coefficients[2];
		forecast(fitfcsts);
        try {
		return AIC = IFSMetrics.getAdjAIC(getNumWeights(), Series, fitfcsts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			return Double.MAX_VALUE;
		}
	}

	public String forecast(
		double[]	forecasts
	) {
		double		u = getMean();
		
		forecasts[0] = Series[0];
        for (int i = 1; i < forecasts.length; i++) {
        	forecasts[i] = u
        		+ U1*(yt(i-1, Series, forecasts, this)-u)
    			+ U2*(yt(i-2, Series, forecasts, this)-u)
				+ U3*(yt(i-3, Series, forecasts, this)-u);
        }
        
        double	slope = 0.0;
        boolean	iserror = false;
        
        try {
        slope = centerForecasts(IsCenter, Series.length, 3, forecasts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			iserror = true;
		}
        
        return String.format("variant:%s,iscenter:%s,slope:%.6f,aic:%.3f,"
        + "u:%.3f,u1:%.3f,u2:%.3f,u3:%.3f",
        	"(3,0,0)", iserror ? "error" : IsCenter, slope, AIC,
        	u, U1, U2, U3);
	}
}

/**
 * Class implementing ARIMA(3,0,3).
 */
private static final class Arima_3_0_3 extends ArimaVariant {
	double		AIC = 0.0;
	double		U1 = 0.0;
	double		U2 = 0.0;
	double		U3 = 0.0;
	double		V1 = 0.0;
	double		V2 = 0.0;
	double		V3 = 0.0;
	
	@SuppressWarnings("unused")
	public Arima_3_0_3() {}

	public int getNumWeights() {
		return 6;
	}

	public double getRating(
		double[]	coefficients
	) {
        double[]	fitfcsts = new double[Series.length];

		U1 = coefficients[0];
		U2 = coefficients[1];
		U3 = coefficients[2];
		V1 = coefficients[3];
		V2 = coefficients[4];
		V3 = coefficients[5];
		forecast(fitfcsts);
        try {
		return AIC = IFSMetrics.getAdjAIC(getNumWeights(), Series, fitfcsts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			return Double.MAX_VALUE;
		}
	}

	public String forecast(
		double[]	forecasts
	) {
		double		u = getMean();
		
		forecasts[0] = Series[0];
        for (int i = 1; i < forecasts.length; i++) {
        	forecasts[i] = u
        		+ U1*(yt(i-1, Series, forecasts, this)-u)
        		+ U2*(yt(i-2, Series, forecasts, this)-u)
        		+ U3*(yt(i-3, Series, forecasts, this)-u)
        		- V1*et(i-1, Series, forecasts, this)
        		- V2*et(i-2, Series, forecasts, this)
        		- V3*et(i-3, Series, forecasts, this);
        }
        
        double	slope = 0.0;
        boolean	iserror = false;
        
        try {
        slope = centerForecasts(IsCenter, Series.length, 3, forecasts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			iserror = true;
		}
        
        return String.format("variant:%s,iscenter:%s,slope:%.6f,aic:%.3f,"
        + "u:%.3f,u1:%.3f,u2:%.3f,u3:%.3f,v1:%.3f,v2:%.3f,v3:%.3f",
        	"(3,0,3)", iserror ? "error" : IsCenter, slope, AIC,
        	u, U1, U2, U3, V1, V2, V3);
	}
}

/**
 * Class implementing ARIMA(3,1,0).
 */
private static final class Arima_3_1_0 extends ArimaVariant {
	double		AIC = 0.0;
	double		U1 = 0.0;
	double		U2 = 0.0;
	double		U3 = 0.0;
	final int	NIV = 4;
	
	@SuppressWarnings("unused")
	public Arima_3_1_0() {}

	public int getNumWeights() {
		return 3;
	}

	public double getRating(
		double[]	coefficients
	) {
        double[]	fitfcsts = new double[Series.length];

		U1 = coefficients[0];
		U2 = coefficients[1];
		U3 = coefficients[2];
		if (IV == null) {
			IV = backcast(Series, Cycle, NIV);
		}
		forecast(fitfcsts);
        try {
		return AIC = IFSMetrics.getAdjAIC(getNumWeights(), Series, fitfcsts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			return Double.MAX_VALUE;
		}
	}

	public String forecast(
		double[]	forecasts
	) {
		forecasts[0] = Series[0];
        for (int i = 1; i < forecasts.length; i++) {
        	forecasts[i] = yt(i-1, Series, forecasts, this)
        		+ U1*(yt(i-1, Series, forecasts, this)-yt(i-2, Series, forecasts, this))
    			+ U2*(yt(i-2, Series, forecasts, this)-yt(i-3, Series, forecasts, this))
				+ U3*(yt(i-3, Series, forecasts, this)-yt(i-4, Series, forecasts, this));
        }
        
        double	slope = 0.0;
        boolean	iserror = false;
        
        try {
        slope = centerForecasts(IsCenter, Series.length, 4, forecasts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			iserror = true;
		}
        
        return String.format("variant:%s,iscenter:%s,slope:%.6f,aic:%.3f,"
        + "u1:%.3f,u2:%.3f,u3:%.3f,%s",
        	"(3,1,0)", iserror ? "error" : IsCenter, slope, AIC,
        	U1, U2, U3, toStringIV());
	}
}

/**
 * Class implementing ARIMA(0,0,0)(0,0,1)s.
 */
private static final class Arima_s_0_0_0__0_0_1 extends ArimaVariant {
	double		AIC = 0.0;
	double		X1 = 0.0;
	
	@SuppressWarnings("unused")
	public Arima_s_0_0_0__0_0_1() {}

	public int getNumWeights() {
		return 1;
	}

	public double getRating(
		double[]	coefficients
	) {
        double[]	fitfcsts = new double[Series.length];

		X1 = coefficients[0];
		forecast(fitfcsts);
        try {
		return AIC = IFSMetrics.getAdjAIC(getNumWeights(), Series, fitfcsts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			return Double.MAX_VALUE;
		}
	}

	public String forecast(
		double[]	forecasts
	) {
		double		u = getMean();
		
		forecasts[0] = Series[0];
        for (int i = 1; i < forecasts.length; i++) {
        	forecasts[i] = u
        		- X1*et(i-Cycle, Series, forecasts, this);
        }
        
        double	slope = 0.0;
        boolean	iserror = false;
        
        try {
        slope = centerForecasts(IsCenter, Series.length, Cycle, forecasts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			iserror = true;
		}
        
        return String.format("variant:%s,iscenter:%s,slope:%.6f,aic:%.3f,"
        + "u:%.3f,x1:%.3f",
        	"(0,0,0)(0,0,1)s", iserror ? "error" : IsCenter, slope, AIC,
        	u, X1);
	}
}

/**
 * Class implementing ARIMA(0,0,0)(0,0,2)s.
 */
private static final class Arima_s_0_0_0__0_0_2 extends ArimaVariant {
	double		AIC = 0.0;
	double		X1 = 0.0;
	double		X2 = 0.0;
	
	@SuppressWarnings("unused")
	public Arima_s_0_0_0__0_0_2() {}

	public int getNumWeights() {
		return 2;
	}

	public double getRating(
		double[]	coefficients
	) {
        double[]	fitfcsts = new double[Series.length];

		X1 = coefficients[0];
		X2 = coefficients[1];
		forecast(fitfcsts);
        try {
		return AIC = IFSMetrics.getAdjAIC(getNumWeights(), Series, fitfcsts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			return Double.MAX_VALUE;
		}
	}

	public String forecast(
		double[]	forecasts
	) {
		double		u = getMean();
		
		forecasts[0] = Series[0];
        for (int i = 1; i < forecasts.length; i++) {
        	forecasts[i] = u
        		- X1*et(i-Cycle, Series, forecasts, this)
    			- X2*et(i-2*Cycle, Series, forecasts, this);
        }
        
        double	slope = 0.0;
        boolean	iserror = false;
        
        try {
        slope = centerForecasts(IsCenter, Series.length, Cycle, forecasts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			iserror = true;
		}
        
        return String.format("variant:%s,iscenter:%s,slope:%.6f,aic:%.3f,"
        + "u:%.3f,x1:%.3f,x2:%.3f",
        	"(0,0,0)(0,0,2)s", iserror ? "error" : IsCenter, slope, AIC,
        	u, X1, X2);
	}
}

/**
 * Class implementing ARIMA(0,0,0)(0,0,3)s.
 */
private static final class Arima_s_0_0_0__0_0_3 extends ArimaVariant {
	double		AIC = 0.0;
	double		X1 = 0.0;
	double		X2 = 0.0;
	double		X3 = 0.0;
	
	@SuppressWarnings("unused")
	public Arima_s_0_0_0__0_0_3() {}

	public int getNumWeights() {
		return 3;
	}

	public double getRating(
		double[]	coefficients
	) {
        double[]	fitfcsts = new double[Series.length];

		X1 = coefficients[0];
		X2 = coefficients[1];
		X3 = coefficients[2];
		forecast(fitfcsts);
        try {
		return AIC = IFSMetrics.getAdjAIC(getNumWeights(), Series, fitfcsts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			return Double.MAX_VALUE;
		}
	}

	public String forecast(
		double[]	forecasts
	) {
		double		u = getMean();
		
		forecasts[0] = Series[0];
        for (int i = 1; i < forecasts.length; i++) {
        	forecasts[i] = u
        		- X1*et(i-Cycle, Series, forecasts, this)
    			- X2*et(i-2*Cycle, Series, forecasts, this)
				- X3*et(i-3*Cycle, Series, forecasts, this);
        }
        
        double	slope = 0.0;
        boolean	iserror = false;
        
        try {
        slope = centerForecasts(IsCenter, Series.length, Cycle, forecasts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			iserror = true;
		}
        
        return String.format("variant:%s,iscenter:%s,slope:%.6f,aic:%.3f,"
        + "u:%.3f,x1:%.3f,x2:%.3f,x3:%.3f",
        	"(0,0,0)(0,0,3)s", iserror ? "error" : IsCenter, slope, AIC,
        	u, X1, X2, X3);
	}
}

/**
 * Class implementing ARIMA(0,0,0)(1,0,0)s.
 */
private static final class Arima_s_0_0_0__1_0_0 extends ArimaVariant {
	double		AIC = 0.0;
	double		W1 = 0.0;
	
	@SuppressWarnings("unused")
	public Arima_s_0_0_0__1_0_0() {}

	public int getNumWeights() {
		return 1;
	}

	public double getRating(
		double[]	coefficients
	) {
        double[]	fitfcsts = new double[Series.length];

		W1 = coefficients[0];
		forecast(fitfcsts);
        try {
		return AIC = IFSMetrics.getAdjAIC(getNumWeights(), Series, fitfcsts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			return Double.MAX_VALUE;
		}
	}

	public String forecast(
		double[]	forecasts
	) {
		double		u = getMean();
		
		forecasts[0] = Series[0];
        for (int i = 1; i < forecasts.length; i++) {
        	forecasts[i] = u
        		+ W1*(yt(i-Cycle, Series, forecasts, this)-u);
        }
        
        double	slope = 0.0;
        boolean	iserror = false;
        
        try {
        slope = centerForecasts(IsCenter, Series.length, Cycle, forecasts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			iserror = true;
		}
        
        return String.format("variant:%s,iscenter:%s,slope:%.6f,aic:%.3f,"
        + "u:%.3f,w1:%.3f",
        	"(0,0,0)(1,0,0)s", iserror ? "error" : IsCenter, slope, AIC,
        	u, W1);
	}
}

/**
 * Class implementing ARIMA(0,0,0)(2,0,0)s.
 */
private static final class Arima_s_0_0_0__2_0_0 extends ArimaVariant {
	double		AIC = 0.0;
	double		W1 = 0.0;
	double		W2 = 0.0;
	
	@SuppressWarnings("unused")
	public Arima_s_0_0_0__2_0_0() {}

	public int getNumWeights() {
		return 2;
	}

	public double getRating(
		double[]	coefficients
	) {
        double[]	fitfcsts = new double[Series.length];

		W1 = coefficients[0];
		W2 = coefficients[1];
		forecast(fitfcsts);
        try {
		return AIC = IFSMetrics.getAdjAIC(getNumWeights(), Series, fitfcsts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			return Double.MAX_VALUE;
		}
	}

	public String forecast(
		double[]	forecasts
	) {
		double		u = getMean();
		
		forecasts[0] = Series[0];
        for (int i = 1; i < forecasts.length; i++) {
        	forecasts[i] = u
        		+ W1*(yt(i-Cycle, Series, forecasts, this)-u)
    			+ W2*(yt(i-2*Cycle, Series, forecasts, this)-u);
        }
        
        double	slope = 0.0;
        boolean	iserror = false;
        
        try {
        slope = centerForecasts(IsCenter, Series.length, Cycle, forecasts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			iserror = true;
		}
        
        return String.format("variant:%s,iscenter:%s,slope:%.6f,aic:%.3f,"
        + "u:%.3f,w1:%.3f,w2:%.3f",
        	"(0,0,0)(2,0,0)s", iserror ? "error" : IsCenter, slope, AIC,
        	u, W1, W2);
	}
}

/**
 * Class implementing ARIMA(0,0,0)(3,0,0)s.
 */
private static final class Arima_s_0_0_0__3_0_0 extends ArimaVariant {
	double		AIC = 0.0;
	double		W1 = 0.0;
	double		W2 = 0.0;
	double		W3 = 0.0;
	
	@SuppressWarnings("unused")
	public Arima_s_0_0_0__3_0_0() {}

	public int getNumWeights() {
		return 3;
	}

	public double getRating(
		double[]	coefficients
	) {
        double[]	fitfcsts = new double[Series.length];

		W1 = coefficients[0];
		W2 = coefficients[1];
		W3 = coefficients[2];
		forecast(fitfcsts);
        try {
		return AIC = IFSMetrics.getAdjAIC(getNumWeights(), Series, fitfcsts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			return Double.MAX_VALUE;
		}
	}

	public String forecast(
		double[]	forecasts
	) {
		double		u = getMean();
		
		forecasts[0] = Series[0];
        for (int i = 1; i < forecasts.length; i++) {
        	forecasts[i] = u
        		+ W1*(yt(i-Cycle, Series, forecasts, this)-u)
    			+ W2*(yt(i-2*Cycle, Series, forecasts, this)-u)
    			+ W3*(yt(i-3*Cycle, Series, forecasts, this)-u);
        }
        
        double	slope = 0.0;
        boolean	iserror = false;
        
        try {
        slope = centerForecasts(IsCenter, Series.length, Cycle, forecasts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			iserror = true;
		}
        
        return String.format("variant:%s,iscenter:%s,slope:%.6f,aic:%.3f,"
        + "u:%.3f,w1:%.3f,w2:%.3f,w3:%.3f",
        	"(0,0,0)(3,0,0)s", iserror ? "error" : IsCenter, slope, AIC,
        	u, W1, W2, W3);
	}
}

/**
 * Class implementing ARIMA(0,0,0)(1,0,1)s.
 */
private static final class Arima_s_0_0_0__1_0_1 extends ArimaVariant {
	double		AIC = 0.0;
	double		W1 = 0.0;
	double		X1 = 0.0;
	
	@SuppressWarnings("unused")
	public Arima_s_0_0_0__1_0_1() {}

	public int getNumWeights() {
		return 2;
	}

	public double getRating(
		double[]	coefficients
	) {
        double[]	fitfcsts = new double[Series.length];

		W1 = coefficients[0];
		X1 = coefficients[1];
		forecast(fitfcsts);
        try {
		return AIC = IFSMetrics.getAdjAIC(getNumWeights(), Series, fitfcsts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			return Double.MAX_VALUE;
		}
	}

	public String forecast(
		double[]	forecasts
	) {
		double		u = getMean();
		
		forecasts[0] = Series[0];
        for (int i = 1; i < forecasts.length; i++) {
        	forecasts[i] = u
        		+ W1*(yt(i-Cycle, Series, forecasts, this)-u)
        		- X1*et(i-Cycle, Series, forecasts, this);
        }
        
        double	slope = 0.0;
        boolean	iserror = false;
        
        try {
        slope = centerForecasts(IsCenter, Series.length, Cycle, forecasts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			iserror = true;
		}
        
        return String.format("variant:%s,iscenter:%s,slope:%.6f,aic:%.3f,"
        + "u:%.3f,w1:%.3f,x1:%.3f",
        	"(0,0,0)(1,0,1)s", iserror ? "error" : IsCenter, slope, AIC,
        	u, W1, X1);
	}
}

/**
 * Class implementing ARIMA(0,0,0)(2,0,2)s.
 */
private static final class Arima_s_0_0_0__2_0_2 extends ArimaVariant {
	double		AIC = 0.0;
	double		W1 = 0.0;
	double		W2 = 0.0;
	double		X1 = 0.0;
	double		X2 = 0.0;
	
	@SuppressWarnings("unused")
	public Arima_s_0_0_0__2_0_2() {}

	public int getNumWeights() {
		return 4;
	}

	public double getRating(
		double[]	coefficients
	) {
        double[]	fitfcsts = new double[Series.length];

		W1 = coefficients[0];
		W2 = coefficients[1];
		X1 = coefficients[2];
		X2 = coefficients[3];
		forecast(fitfcsts);
        try {
		return AIC = IFSMetrics.getAdjAIC(getNumWeights(), Series, fitfcsts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			return Double.MAX_VALUE;
		}
	}

	public String forecast(
		double[]	forecasts
	) {
		double		u = getMean();
		
		forecasts[0] = Series[0];
        for (int i = 1; i < forecasts.length; i++) {
        	forecasts[i] = u
        		+ W1*(yt(i-Cycle, Series, forecasts, this)-u)
        		+ W2*(yt(i-2*Cycle, Series, forecasts, this)-u)
        		- X1*et(i-Cycle, Series, forecasts, this)
    			- X2*et(i-2*Cycle, Series, forecasts, this);
        }
        
        double	slope = 0.0;
        boolean	iserror = false;
        
        try {
        slope = centerForecasts(IsCenter, Series.length, Cycle, forecasts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			iserror = true;
		}
        
        return String.format("variant:%s,iscenter:%s,slope:%.6f,aic:%.3f,"
        + "u:%.3f,w1:%.3f,w2:%.3f,x1:%.3f,x2:%.3f",
        	"(0,0,0)(2,0,2)s", iserror ? "error" : IsCenter, slope, AIC,
        	u, W1, W2, X1, X2);
	}
}

/**
 * Class implementing ARIMA(0,0,0)(3,0,3)s.
 */
private static final class Arima_s_0_0_0__3_0_3 extends ArimaVariant {
	double		AIC = 0.0;
	double		W1 = 0.0;
	double		W2 = 0.0;
	double		W3 = 0.0;
	double		X1 = 0.0;
	double		X2 = 0.0;
	double		X3 = 0.0;
	
	@SuppressWarnings("unused")
	public Arima_s_0_0_0__3_0_3() {}

	public int getNumWeights() {
		return 6;
	}

	public double getRating(
		double[]	coefficients
	) {
        double[]	fitfcsts = new double[Series.length];

		W1 = coefficients[0];
		W2 = coefficients[1];
		W3 = coefficients[2];
		X1 = coefficients[3];
		X2 = coefficients[4];
		X3 = coefficients[5];
		forecast(fitfcsts);
        try {
		return AIC = IFSMetrics.getAdjAIC(getNumWeights(), Series, fitfcsts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			return Double.MAX_VALUE;
		}
	}

	public String forecast(
		double[]	forecasts
	) {
		double		u = getMean();
		
		forecasts[0] = Series[0];
        for (int i = 1; i < forecasts.length; i++) {
        	forecasts[i] = u
        		+ W1*(yt(i-Cycle, Series, forecasts, this)-u)
        		+ W2*(yt(i-2*Cycle, Series, forecasts, this)-u)
        		+ W3*(yt(i-3*Cycle, Series, forecasts, this)-u)
        		- X1*et(i-Cycle, Series, forecasts, this)
    			- X2*et(i-2*Cycle, Series, forecasts, this)
    			- X3*et(i-3*Cycle, Series, forecasts, this);
        }
        
        double	slope = 0.0;
        boolean	iserror = false;
        
        try {
        slope = centerForecasts(IsCenter, Series.length, Cycle, forecasts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			iserror = true;
		}
        
        return String.format("variant:%s,iscenter:%s,slope:%.6f,aic:%.3f,"
        + "u:%.3f,w1:%.3f,w2:%.3f,w3:%.3f,x1:%.3f,x2:%.3f,x3:%.3f",
        	"(0,0,0)(3,0,3)s", iserror ? "error" : IsCenter, slope, AIC,
        	u, W1, W2, W3, X1, X2, X3);
	}
}

/**
 * Class implementing ARIMA(0,0,1)(0,0,1)s.
 */
private static final class Arima_s_0_0_1__0_0_1 extends ArimaVariant {
	double		AIC = 0.0;
	double		V1 = 0.0;
	double		X1 = 0.0;
	
	@SuppressWarnings("unused")
	public Arima_s_0_0_1__0_0_1() {}

	public int getNumWeights() {
		return 2;
	}

	public double getRating(
		double[]	coefficients
	) {
        double[]	fitfcsts = new double[Series.length];

		V1 = coefficients[0];
		X1 = coefficients[1];
		forecast(fitfcsts);
        try {
		return AIC = IFSMetrics.getAdjAIC(getNumWeights(), Series, fitfcsts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			return Double.MAX_VALUE;
		}
	}

	public String forecast(
		double[]	forecasts
	) {
		double		u = getMean();
		
		forecasts[0] = Series[0];
        for (int i = 1; i < forecasts.length; i++) {
        	forecasts[i] = u
        		- V1*et(i-1, Series, forecasts, this)
        		- X1*et(i-Cycle, Series, forecasts, this)
        		+ V1*X1*et(i-Cycle-1, Series, forecasts, this);
        }
        
        double	slope = 0.0;
        boolean	iserror = false;
        
        try {
        slope = centerForecasts(IsCenter, Series.length, Cycle, forecasts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			iserror = true;
		}
        
        return String.format("variant:%s,iscenter:%s,slope:%.6f,aic:%.3f,"
        + "u:%.3f,v1:%.3f,x1:%.3f",
        	"(0,0,1)(0,0,1)s", iserror ? "error" : IsCenter, slope, AIC,
        	u, V1, X1);
	}
}

/**
 * Class implementing ARIMA(0,0,2)(0,0,1)s.
 */
private static final class Arima_s_0_0_2__0_0_1 extends ArimaVariant {
	double		AIC = 0.0;
	double		V1 = 0.0;
	double		V2 = 0.0;
	double		X1 = 0.0;
	
	@SuppressWarnings("unused")
	public Arima_s_0_0_2__0_0_1() {}

	public int getNumWeights() {
		return 3;
	}

	public double getRating(
		double[]	coefficients
	) {
        double[]	fitfcsts = new double[Series.length];

		V1 = coefficients[0];
		V2 = coefficients[1];
		X1 = coefficients[2];
		forecast(fitfcsts);
        try {
		return AIC = IFSMetrics.getAdjAIC(getNumWeights(), Series, fitfcsts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			return Double.MAX_VALUE;
		}
	}

	public String forecast(
		double[]	forecasts
	) {
		double		u = getMean();
		
		forecasts[0] = Series[0];
        for (int i = 1; i < forecasts.length; i++) {
        	forecasts[i] = u
        		- V1*et(i-1, Series, forecasts, this)
        		- V2*et(i-2, Series, forecasts, this)
        		- X1*et(i-Cycle, Series, forecasts, this)
        		+ V1*X1*et(i-Cycle-1, Series, forecasts, this)
        		+ V2*X1*et(i-Cycle-2, Series, forecasts, this);
        }
        
        double	slope = 0.0;
        boolean	iserror = false;
        
        try {
        slope = centerForecasts(IsCenter, Series.length, Cycle, forecasts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			iserror = true;
		}
        
        return String.format("variant:%s,iscenter:%s,slope:%.6f,aic:%.3f,"
        + "u:%.3f,v1:%.3f,v2:%.3f,x1:%.3f",
        	"(0,0,2)(0,0,1)s", iserror ? "error" : IsCenter, slope, AIC,
        	u, V1, V2, X1);
	}
}

/**
 * Class implementing ARIMA(0,0,3)(0,0,1)s.
 */
private static final class Arima_s_0_0_3__0_0_1 extends ArimaVariant {
	double		AIC = 0.0;
	double		V1 = 0.0;
	double		V2 = 0.0;
	double		V3 = 0.0;
	double		X1 = 0.0;
	
	@SuppressWarnings("unused")
	public Arima_s_0_0_3__0_0_1() {}

	public int getNumWeights() {
		return 4;
	}

	public double getRating(
		double[]	coefficients
	) {
        double[]	fitfcsts = new double[Series.length];

		V1 = coefficients[0];
		V2 = coefficients[1];
		V3 = coefficients[2];
		X1 = coefficients[3];
		forecast(fitfcsts);
        try {
		return AIC = IFSMetrics.getAdjAIC(getNumWeights(), Series, fitfcsts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			return Double.MAX_VALUE;
		}
	}

	public String forecast(
		double[]	forecasts
	) {
		double		u = getMean();
		
		forecasts[0] = Series[0];
        for (int i = 1; i < forecasts.length; i++) {
        	forecasts[i] = u
        		- V1*et(i-1, Series, forecasts, this)
        		- V2*et(i-2, Series, forecasts, this)
        		- V3*et(i-3, Series, forecasts, this)
        		- X1*et(i-Cycle, Series, forecasts, this)
        		+ V1*X1*et(i-Cycle-1, Series, forecasts, this)
        		+ V2*X1*et(i-Cycle-2, Series, forecasts, this)
        		+ V3*X1*et(i-Cycle-3, Series, forecasts, this);
        }
        
        double	slope = 0.0;
        boolean	iserror = false;
        
        try {
        slope = centerForecasts(IsCenter, Series.length, Cycle, forecasts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			iserror = true;
		}
        
        return String.format("variant:%s,iscenter:%s,slope:%.6f,aic:%.3f,"
        + "u:%.3f,v1:%.3f,v2:%.3f,v3:%.3f,x1:%.3f",
        	"(0,0,3)(0,0,1)s", iserror ? "error" : IsCenter, slope, AIC,
        	u, V1, V2, V3, X1);
	}
}

/**
 * Class implementing ARIMA(0,1,1)(0,1,1)s.
 */
private static final class Arima_s_0_1_1__0_1_1 extends ArimaVariant {
	double		AIC = 0.0;
	double		V1 = 0.0;
	double		X1 = 0.0;
	
	@SuppressWarnings("unused")
	public Arima_s_0_1_1__0_1_1() {}

	public int getNumWeights() {
		return 2;
	}

	public double getRating(
		double[]	coefficients
	) {
        double[]	fitfcsts = new double[Series.length];

		V1 = coefficients[0];
		X1 = coefficients[1];
		if (IV == null) {
			IV = backcast(Series, Cycle, Cycle+2);
		}
		forecast(fitfcsts);
        try {
		return AIC = IFSMetrics.getAdjAIC(getNumWeights(), Series, fitfcsts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			return Double.MAX_VALUE;
		}
	}

	public String forecast(
		double[]	forecasts
	) {
		forecasts[0] = Series[0];
        for (int i = 1; i < forecasts.length; i++) {
        	forecasts[i] = yt(i-1, Series, forecasts, this)
        		+ yt(i-Cycle, Series, forecasts, this)
        		- yt(i-Cycle-1, Series, forecasts, this)
                - V1*et(i-1, Series, forecasts, this)
        		- X1*et(i-Cycle, Series, forecasts, this)
        		+ V1*X1*et(i-Cycle-1, Series, forecasts, this);
        }
        
        double	slope = 0.0;
        boolean	iserror = false;
        
        try {
        slope = centerForecasts(IsCenter, Series.length, Cycle, forecasts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			iserror = true;
		}
        
        return String.format("variant:%s,iscenter:%s,slope:%.6f,aic:%.3f,"
    	+ "v1:%.3f,x1:%.3f,%s",
            "(0,1,1)(0,1,1)s", iserror ? "error" : IsCenter, slope, AIC,
            V1, X1, toStringIV());
	}
}

/**
 * Class implementing ARIMA(0,1,2)(0,1,1)s.
 */
private static final class Arima_s_0_1_2__0_1_1 extends ArimaVariant {
	double		AIC = 0.0;
	double		V1 = 0.0;
	double		V2 = 0.0;
	double		X1 = 0.0;
	
	@SuppressWarnings("unused")
	public Arima_s_0_1_2__0_1_1() {}

	public int getNumWeights() {
		return 3;
	}

	public double getRating(
		double[]	coefficients
	) {
        double[]	fitfcsts = new double[Series.length];

		V1 = coefficients[0];
		V2 = coefficients[1];
		X1 = coefficients[2];
		if (IV == null) {
			IV = backcast(Series, Cycle, Cycle+3);
		}
		forecast(fitfcsts);
        try {
		return AIC = IFSMetrics.getAdjAIC(getNumWeights(), Series, fitfcsts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			return Double.MAX_VALUE;
		}
	}

	public String forecast(
		double[]	forecasts
	) {
		forecasts[0] = Series[0];
        for (int i = 1; i < forecasts.length; i++) {
        	forecasts[i] = yt(i-1, Series, forecasts, this)
        		+ yt(i-Cycle, Series, forecasts, this)
        		- yt(i-Cycle-1, Series, forecasts, this)
                - V1*et(i-1, Series, forecasts, this)
                - V2*et(i-2, Series, forecasts, this)
        		- X1*et(i-Cycle, Series, forecasts, this)
        		+ V1*X1*et(i-Cycle-1, Series, forecasts, this)
        		+ V2*X1*et(i-Cycle-2, Series, forecasts, this);
        }
        
        double	slope = 0.0;
        boolean	iserror = false;
        
        try {
        slope = centerForecasts(IsCenter, Series.length, Cycle, forecasts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			iserror = true;
		}
        
        return String.format("variant:%s,iscenter:%s,slope:%.6f,aic:%.3f,"
    	+ "v1:%.3f,v2:%.3f,x1:%.3f,%s",
            "(0,1,2)(0,1,1)s", iserror ? "error" : IsCenter, slope, AIC,
            V1, V2, X1, toStringIV());
	}
}

/**
 * Class implementing ARIMA(0,2,2)(0,1,1)s.
 */
private static final class Arima_s_0_2_2__0_1_1 extends ArimaVariant {
	double		AIC = 0.0;
	double		V1 = 0.0;
	double		V2 = 0.0;
	double		X1 = 0.0;
	
	@SuppressWarnings("unused")
	public Arima_s_0_2_2__0_1_1() {}

	public int getNumWeights() {
		return 3;
	}

	public double getRating(
		double[]	coefficients
	) {
        double[]	fitfcsts = new double[Series.length];

		V1 = coefficients[0];
		V2 = coefficients[1];
		X1 = coefficients[2];
		if (IV == null) {
			IV = backcast(Series, Cycle, Cycle+3);
		}
		forecast(fitfcsts);
        try {
		return AIC = IFSMetrics.getAdjAIC(getNumWeights(), Series, fitfcsts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			return Double.MAX_VALUE;
		}
	}

	public String forecast(
		double[]	forecasts
	) {
		forecasts[0] = Series[0];
        for (int i = 1; i < forecasts.length; i++) {
        	forecasts[i] = 2.0*yt(i-1, Series, forecasts, this)
        		- yt(i-2, Series, forecasts, this)
        		+ yt(i-Cycle, Series, forecasts, this)
        		- 2.0*yt(i-Cycle-1, Series, forecasts, this)
        		+ yt(i-Cycle-2, Series, forecasts, this)
                - V1*et(i-1, Series, forecasts, this)
                - V2*et(i-2, Series, forecasts, this)
        		- X1*et(i-Cycle, Series, forecasts, this)
        		+ V1*X1*et(i-Cycle-1, Series, forecasts, this)
        		+ V2*X1*et(i-Cycle-2, Series, forecasts, this);
        }
        
        double	slope = 0.0;
        boolean	iserror = false;
        
        try {
        slope = centerForecasts(IsCenter, Series.length, Cycle, forecasts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			iserror = true;
		}
        
        return String.format("variant:%s,iscenter:%s,slope:%.6f,aic:%.3f,"
    	+ "v1:%.3f,v2:%.3f,x1:%.3f,%s",
            "(0,2,2)(0,1,1)s", iserror ? "error" : IsCenter, slope, AIC,
            V1, V2, X1, toStringIV());
	}
}

/**
 * Class implementing ARIMA(1,0,0)(1,0,0)s.
 */
private static final class Arima_s_1_0_0__1_0_0 extends ArimaVariant {
	double		AIC = 0.0;
	double		U1 = 0.0;
	double		W1 = 0.0;
	
	@SuppressWarnings("unused")
	public Arima_s_1_0_0__1_0_0() {}

	public int getNumWeights() {
		return 2;
	}

	public double getRating(
		double[]	coefficients
	) {
        double[]	fitfcsts = new double[Series.length];

		U1 = coefficients[0];
		W1 = coefficients[1];
		forecast(fitfcsts);
        try {
		return AIC = IFSMetrics.getAdjAIC(getNumWeights(), Series, fitfcsts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			return Double.MAX_VALUE;
		}
	}

	public String forecast(
		double[]	forecasts
	) {
		double		u = getMean();
		
		forecasts[0] = Series[0];
        for (int i = 1; i < forecasts.length; i++) {
        	forecasts[i] = u
        		+ U1*(yt(i-1, Series, forecasts, this)-u)
        		+ W1*(yt(i-Cycle, Series, forecasts, this)-u)
        		- U1*W1*(yt(i-Cycle-1, Series, forecasts, this)-u);
        }
        
        double	slope = 0.0;
        boolean	iserror = false;
        
        try {
        slope = centerForecasts(IsCenter, Series.length, Cycle, forecasts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			iserror = true;
		}
        
        return String.format("variant:%s,iscenter:%s,slope:%.6f,aic:%.3f,"
        + "u:%.3f,u1:%.3f,w1:%.3f",
        	"(1,0,0)(1,0,0)s", iserror ? "error" : IsCenter, slope, AIC,
        	u, U1, W1);
	}
}

/**
 * Class implementing ARIMA(1,0,0)(2,0,0)s.
 */
private static final class Arima_s_1_0_0__2_0_0 extends ArimaVariant {
	double		AIC = 0.0;
	double		U1 = 0.0;
	double		W1 = 0.0;
	double		W2 = 0.0;
	
	@SuppressWarnings("unused")
	public Arima_s_1_0_0__2_0_0() {}

	public int getNumWeights() {
		return 3;
	}

	public double getRating(
		double[]	coefficients
	) {
        double[]	fitfcsts = new double[Series.length];

		U1 = coefficients[0];
		W1 = coefficients[1];
		W2 = coefficients[2];
		forecast(fitfcsts);
        try {
		return AIC = IFSMetrics.getAdjAIC(getNumWeights(), Series, fitfcsts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			return Double.MAX_VALUE;
		}
	}

	public String forecast(
		double[]	forecasts
	) {
		double		u = getMean();
		
		forecasts[0] = Series[0];
        for (int i = 1; i < forecasts.length; i++) {
        	forecasts[i] = u
        		+ U1*(yt(i-1, Series, forecasts, this)-u)
        		+ W1*(yt(i-Cycle, Series, forecasts, this)-u)
        		- U1*W1*(yt(i-Cycle-1, Series, forecasts, this)-u)
        		+ W2*(yt(i-2*Cycle, Series, forecasts, this)-u)
        		- U1*W2*(yt(i-2*Cycle-1, Series, forecasts, this)-u);
        }
        
        double	slope = 0.0;
        boolean	iserror = false;
        
        try {
        slope = centerForecasts(IsCenter, Series.length, Cycle, forecasts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			iserror = true;
		}
        
        return String.format("variant:%s,iscenter:%s,slope:%.6f,aic:%.3f,"
        + "u:%.3f,u1:%.3f,w1:%.3f,w2:%.3f",
        	"(1,0,0)(2,0,0)s", iserror ? "error" : IsCenter, slope, AIC,
        	u, U1, W1, W2);
	}
}

/**
 * Class implementing ARIMA(1,0,1)(1,0,1)s.
 */
private static final class Arima_s_1_0_1__1_0_1 extends ArimaVariant {
	double		AIC = 0.0;
	double		U1 = 0.0;
	double		V1 = 0.0;
	double		W1 = 0.0;
	double		X1 = 0.0;
	
	@SuppressWarnings("unused")
	public Arima_s_1_0_1__1_0_1() {}

	public int getNumWeights() {
		return 4;
	}

	public double getRating(
		double[]	coefficients
	) {
        double[]	fitfcsts = new double[Series.length];

		U1 = coefficients[0];
		V1 = coefficients[1];
		W1 = coefficients[2];
		X1 = coefficients[3];
		forecast(fitfcsts);
        try {
		return AIC = IFSMetrics.getAdjAIC(getNumWeights(), Series, fitfcsts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			return Double.MAX_VALUE;
		}
	}

	public String forecast(
		double[]	forecasts
	) {
		double		u = getMean();
		
		forecasts[0] = Series[0];
        for (int i = 1; i < forecasts.length; i++) {
        	forecasts[i] = u
        		+ U1*(yt(i-1, Series, forecasts, this)-u)
        		+ W1*(yt(i-Cycle, Series, forecasts, this)-u)
        		- U1*W1*(yt(i-Cycle-1, Series, forecasts, this)-u)
                - V1*et(i-1, Series, forecasts, this)
            	- X1*et(i-Cycle, Series, forecasts, this)
            	+ V1*X1*et(i-Cycle-1, Series, forecasts, this);
        }
        
        double	slope = 0.0;
        boolean	iserror = false;
        
        try {
        slope = centerForecasts(IsCenter, Series.length, Cycle, forecasts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			iserror = true;
		}
        
        return String.format("variant:%s,iscenter:%s,slope:%.6f,aic:%.3f,"
        + "u:%.3f,u1:%.3f,w1:%.3f,v1:%.3f,x1:%.3f",
        	"(1,0,1)(1,0,1)s", iserror ? "error" : IsCenter, slope, AIC,
        	u, U1, W1, V1, X1);
	}
}

/**
 * Class implementing ARIMA(1,1,1)(1,1,1)s.
 */
private static final class Arima_s_1_1_1__1_1_1 extends ArimaVariant {
	double		AIC = 0.0;
	double		U1 = 0.0;
	double		V1 = 0.0;
	double		W1 = 0.0;
	double		X1 = 0.0;
	
	@SuppressWarnings("unused")
	public Arima_s_1_1_1__1_1_1() {}

	public int getNumWeights() {
		return 4;
	}

	public double getRating(
		double[]	coefficients
	) {
        double[]	fitfcsts = new double[Series.length];

		U1 = coefficients[0];
		V1 = coefficients[1];
		W1 = coefficients[2];
		X1 = coefficients[3];
		if (IV == null) {
			IV = backcast(Series, Cycle, 2*Cycle+2);
		}
		forecast(fitfcsts);
        try {
		return AIC = IFSMetrics.getAdjAIC(getNumWeights(), Series, fitfcsts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			return Double.MAX_VALUE;
		}
	}

	public String forecast(
		double[]	forecasts
	) {
		forecasts[0] = Series[0];
        for (int i = 1; i < forecasts.length; i++) {
        	forecasts[i] = yt(i-1, Series, forecasts, this)
        		+ yt(i-Cycle, Series, forecasts, this)
        		- yt(i-Cycle-1, Series, forecasts, this)
        		+ U1*(yt(i-1, Series, forecasts, this)
        			- yt(i-2, Series, forecasts, this)
                	- yt(i-Cycle-1, Series, forecasts, this)
                	+ yt(i-Cycle-2, Series, forecasts, this))
        		+ W1*(yt(i-Cycle, Series, forecasts, this)
            		- yt(i-Cycle-1, Series, forecasts, this)
                    - yt(i-2*Cycle, Series, forecasts, this)
                    + yt(i-2*Cycle-1, Series, forecasts, this))
        		- U1*W1*(yt(i-Cycle-1, Series, forecasts, this)
                	- yt(i-Cycle-2, Series, forecasts, this)
                    - yt(i-2*Cycle-1, Series, forecasts, this)
                    + yt(i-2*Cycle-2, Series, forecasts, this))
                - V1*et(i-1, Series, forecasts, this)
            	- X1*et(i-Cycle, Series, forecasts, this)
            	+ V1*X1*et(i-Cycle-1, Series, forecasts, this);
        }
        
        double	slope = 0.0;
        boolean	iserror = false;
        
        try {
        slope = centerForecasts(IsCenter, Series.length, Cycle, forecasts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			iserror = true;
		}
        
        return String.format("variant:%s,iscenter:%s,slope:%.6f,aic:%.3f,"
        + "u1:%.3f,w1:%.3f,v1:%.3f,x1:%.3f,%s",
        	"(1,1,1)(1,1,1)s", iserror ? "error" : IsCenter, slope, AIC,
        	U1, W1, V1, X1, toStringIV());
	}
}

/**
 * Class implementing ARIMA(2,0,0)(1,0,0)s.
 */
private static final class Arima_s_2_0_0__1_0_0 extends ArimaVariant {
	double		AIC = 0.0;
	double		U1 = 0.0;
	double		U2 = 0.0;
	double		W1 = 0.0;
	
	@SuppressWarnings("unused")
	public Arima_s_2_0_0__1_0_0() {}

	public int getNumWeights() {
		return 3;
	}

	public double getRating(
		double[]	coefficients
	) {
        double[]	fitfcsts = new double[Series.length];

		U1 = coefficients[0];
		U2 = coefficients[1];
		W1 = coefficients[2];
		forecast(fitfcsts);
        try {
		return AIC = IFSMetrics.getAdjAIC(getNumWeights(), Series, fitfcsts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			return Double.MAX_VALUE;
		}
	}

	public String forecast(
		double[]	forecasts
	) {
		double		u = getMean();
		
		forecasts[0] = Series[0];
        for (int i = 1; i < forecasts.length; i++) {
        	forecasts[i] = u
        		+ U1*(yt(i-1, Series, forecasts, this)-u)
        		+ U2*(yt(i-2, Series, forecasts, this)-u)
        		+ W1*(yt(i-Cycle, Series, forecasts, this)-u)
        		- U1*W1*(yt(i-Cycle-1, Series, forecasts, this)-u)
    			- U2*W1*(yt(i-Cycle-2, Series, forecasts, this)-u);
        }
        
        double	slope = 0.0;
        boolean	iserror = false;
        
        try {
        slope = centerForecasts(IsCenter, Series.length, Cycle, forecasts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			iserror = true;
		}
        
        return String.format("variant:%s,iscenter:%s,slope:%.6f,aic:%.3f,"
        + "u:%.3f,u1:%.3f,u2:%.3f,w1:%.3f",
        	"(2,0,0)(1,0,0)s", iserror ? "error" : IsCenter, slope, AIC,
        	u, U1, U2, W1);
	}
}

/**
 * Class implementing ARIMA(2,0,2)(1,0,1)s.
 */
private static final class Arima_s_2_0_2__1_0_1 extends ArimaVariant {
	double		AIC = 0.0;
	double		U1 = 0.0;
	double		U2 = 0.0;
	double		V1 = 0.0;
	double		V2 = 0.0;
	double		W1 = 0.0;
	double		X1 = 0.0;
	
	@SuppressWarnings("unused")
	public Arima_s_2_0_2__1_0_1() {}

	public int getNumWeights() {
		return 6;
	}

	public double getRating(
		double[]	coefficients
	) {
        double[]	fitfcsts = new double[Series.length];

		U1 = coefficients[0];
		U2 = coefficients[1];
		V1 = coefficients[2];
		V2 = coefficients[3];
		W1 = coefficients[4];
		X1 = coefficients[5];
		forecast(fitfcsts);
        try {
		return AIC = IFSMetrics.getAdjAIC(getNumWeights(), Series, fitfcsts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			return Double.MAX_VALUE;
		}
	}

	public String forecast(
		double[]	forecasts
	) {
		double		u = getMean();
		
		forecasts[0] = Series[0];
        for (int i = 1; i < forecasts.length; i++) {
        	forecasts[i] = u
        		+ U1*(yt(i-1, Series, forecasts, this)-u)
        		+ U2*(yt(i-2, Series, forecasts, this)-u)
        		+ W1*(yt(i-Cycle, Series, forecasts, this)-u)
        		- U1*W1*(yt(i-Cycle-1, Series, forecasts, this)-u)
        		- U2*W1*(yt(i-Cycle-2, Series, forecasts, this)-u)
                - V1*et(i-1, Series, forecasts, this)
                - V2*et(i-2, Series, forecasts, this)
            	- X1*et(i-Cycle, Series, forecasts, this)
            	+ V1*X1*et(i-Cycle-1, Series, forecasts, this)
            	+ V2*X1*et(i-Cycle-2, Series, forecasts, this);
        }
        
        double	slope = 0.0;
        boolean	iserror = false;
        
        try {
        slope = centerForecasts(IsCenter, Series.length, Cycle, forecasts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			iserror = true;
		}
        
        return String.format("variant:%s,iscenter:%s,slope:%.6f,aic:%.3f,"
        + "u:%.3f,u1:%.3f,u2:%.3f,w1:%.3f,v1:%.3f,v2:%.3f,x1:%.3f",
        	"(2,0,2)(1,0,1)s", iserror ? "error" : IsCenter, slope, AIC,
        	u, U1, U2, W1, V1, V2, X1);
	}
}

/**
 * Class implementing ARIMA(2,1,0)(0,1,1)s.
 */
private static final class Arima_s_2_1_0__0_1_1 extends ArimaVariant {
	double		AIC = 0.0;
	double		U1 = 0.0;
	double		U2 = 0.0;
	double		X1 = 0.0;
	
	@SuppressWarnings("unused")
	public Arima_s_2_1_0__0_1_1() {}

	public int getNumWeights() {
		return 3;
	}

	public double getRating(
		double[]	coefficients
	) {
        double[]	fitfcsts = new double[Series.length];

		U1 = coefficients[0];
		U2 = coefficients[1];
		X1 = coefficients[2];
		if (IV == null) {
			IV = backcast(Series, Cycle, Cycle+3);
		}
		forecast(fitfcsts);
        try {
		return AIC = IFSMetrics.getAdjAIC(getNumWeights(), Series, fitfcsts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			return Double.MAX_VALUE;
		}
	}

	public String forecast(
		double[]	forecasts
	) {
		forecasts[0] = Series[0];
        for (int i = 1; i < forecasts.length; i++) {
        	forecasts[i] = yt(i-1, Series, forecasts, this)
            	+ yt(i-Cycle, Series, forecasts, this)
            	- yt(i-Cycle-1, Series, forecasts, this)
        		+ U1*(yt(i-1, Series, forecasts, this)
        			- yt(i-2, Series, forecasts, this)
                    - yt(i-Cycle-1, Series, forecasts, this)
                    + yt(i-Cycle-2, Series, forecasts, this))
        		+ U2*(yt(i-2, Series, forecasts, this)
            		- yt(i-3, Series, forecasts, this)
                    - yt(i-Cycle-2, Series, forecasts, this))
        		- X1*et(i-Cycle, Series, forecasts, this);
        }
        
        double	slope = 0.0;
        boolean	iserror = false;
        
        try {
        slope = centerForecasts(IsCenter, Series.length, Cycle, forecasts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			iserror = true;
		}
        
        return String.format("variant:%s,iscenter:%s,slope:%.6f,aic:%.3f,"
        + "u1:%.3f,u2:%.3f,x1:%.3f,%s",
        	"(2,1,0)(0,1,1)s", iserror ? "error" : IsCenter, slope, AIC,
        	U1, U2, X1, toStringIV());
	}
}

/**
 * Class implementing ARIMA(2,1,0)(1,0,0)s.
 */
private static final class Arima_s_2_1_0__1_0_0 extends ArimaVariant {
	double		AIC = 0.0;
	double		U1 = 0.0;
	double		U2 = 0.0;
	double		W1 = 0.0;
	
	@SuppressWarnings("unused")
	public Arima_s_2_1_0__1_0_0() {}

	public int getNumWeights() {
		return 3;
	}

	public double getRating(
		double[]	coefficients
	) {
        double[]	fitfcsts = new double[Series.length];

		U1 = coefficients[0];
		U2 = coefficients[1];
		W1 = coefficients[2];
		if (IV == null) {
			IV = backcast(Series, Cycle, Cycle+3);
		}
		forecast(fitfcsts);
        try {
		return AIC = IFSMetrics.getAdjAIC(getNumWeights(), Series, fitfcsts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			return Double.MAX_VALUE;
		}
	}

	public String forecast(
		double[]	forecasts
	) {
		forecasts[0] = Series[0];
        for (int i = 1; i < forecasts.length; i++) {
        	forecasts[i] = yt(i-1, Series, forecasts, this)
        		+ U1*(yt(i-1, Series, forecasts, this)-yt(i-2, Series, forecasts, this))
        		+ U2*(yt(i-2, Series, forecasts, this)-yt(i-3, Series, forecasts, this))
        		+ W1*(yt(i-Cycle, Series, forecasts, this)-yt(i-Cycle-1, Series, forecasts, this))
        		- U1*W1*(yt(i-Cycle-1, Series, forecasts, this)-yt(i-Cycle-2, Series, forecasts, this))
    			- U2*W1*(yt(i-Cycle-2, Series, forecasts, this)-yt(i-Cycle-3, Series, forecasts, this));
        }
        
        double	slope = 0.0;
        boolean	iserror = false;
        
        try {
        slope = centerForecasts(IsCenter, Series.length, Cycle, forecasts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			iserror = true;
		}
        
        return String.format("variant:%s,iscenter:%s,slope:%.6f,aic:%.3f,"
        + "u1:%.3f,u2:%.3f,w1:%.3f,%s",
        	"(2,1,0)(1,0,0)s", iserror ? "error" : IsCenter, slope, AIC,
        	U1, U2, W1, toStringIV());
	}
}

/**
 * Class implementing ARIMA(2,1,2)(0,1,1)s.
 */
private static final class Arima_s_2_1_2__0_1_1 extends ArimaVariant {
	double		AIC = 0.0;
	double		U1 = 0.0;
	double		U2 = 0.0;
	double		V1 = 0.0;
	double		V2 = 0.0;
	double		X1 = 0.0;
	
	@SuppressWarnings("unused")
	public Arima_s_2_1_2__0_1_1() {}

	public int getNumWeights() {
		return 5;
	}

	public double getRating(
		double[]	coefficients
	) {
        double[]	fitfcsts = new double[Series.length];

		U1 = coefficients[0];
		U2 = coefficients[1];
		V1 = coefficients[2];
		V2 = coefficients[3];
		X1 = coefficients[4];
		if (IV == null) {
			IV = backcast(Series, Cycle, Cycle+3);
		}
		forecast(fitfcsts);
        try {
		return AIC = IFSMetrics.getAdjAIC(getNumWeights(), Series, fitfcsts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			return Double.MAX_VALUE;
		}
	}

	public String forecast(
		double[]	forecasts
	) {
		forecasts[0] = Series[0];
        for (int i = 1; i < forecasts.length; i++) {
        	forecasts[i] = yt(i-1, Series, forecasts, this)
            	+ yt(i-Cycle, Series, forecasts, this)
            	- yt(i-Cycle-1, Series, forecasts, this)
        		+ U1*(yt(i-1, Series, forecasts, this)
        			- yt(i-2, Series, forecasts, this)
                    - yt(i-Cycle-1, Series, forecasts, this)
                    + yt(i-Cycle-2, Series, forecasts, this))
        		+ U2*(yt(i-2, Series, forecasts, this)
            		- yt(i-3, Series, forecasts, this)
                    - yt(i-Cycle-2, Series, forecasts, this)
                    + yt(i-Cycle-3, Series, forecasts, this))
        		- V1*et(i-1, Series, forecasts, this)
        		- V2*et(i-2, Series, forecasts, this)
        		- X1*et(i-Cycle, Series, forecasts, this)
        		+ V1*X1*et(i-Cycle-1, Series, forecasts, this)
        		+ V2*X1*et(i-Cycle-2, Series, forecasts, this);
        }
        
        double	slope = 0.0;
        boolean	iserror = false;
        
        try {
        slope = centerForecasts(IsCenter, Series.length, Cycle, forecasts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			iserror = true;
		}
        
        return String.format("variant:%s,iscenter:%s,slope:%.6f,aic:%.3f,"
        + "u1:%.3f,u2:%.3f,v1:%.3f,v2:%.3f,x1:%.3f,%s",
        	"(2,1,2)(0,1,1)s", iserror ? "error" : IsCenter, slope, AIC,
        	U1, U2, V1, V2, X1, toStringIV());
	}
}

/**
 * Class implementing ARIMA(3,0,0)(1,0,0)s.
 */
private static final class Arima_s_3_0_0__1_0_0 extends ArimaVariant {
	double		AIC = 0.0;
	double		U1 = 0.0;
	double		U2 = 0.0;
	double		U3 = 0.0;
	double		W1 = 0.0;
	
	@SuppressWarnings("unused")
	public Arima_s_3_0_0__1_0_0() {}

	public int getNumWeights() {
		return 4;
	}

	public double getRating(
		double[]	coefficients
	) {
        double[]	fitfcsts = new double[Series.length];

		U1 = coefficients[0];
		U2 = coefficients[1];
		U3 = coefficients[2];
		W1 = coefficients[3];
		forecast(fitfcsts);
        try {
		return AIC = IFSMetrics.getAdjAIC(getNumWeights(), Series, fitfcsts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			return Double.MAX_VALUE;
		}
	}

	public String forecast(
		double[]	forecasts
	) {
		double		u = getMean();
		
		forecasts[0] = Series[0];
        for (int i = 1; i < forecasts.length; i++) {
        	forecasts[i] = u
        		+ U1*(yt(i-1, Series, forecasts, this)-u)
        		+ U2*(yt(i-2, Series, forecasts, this)-u)
        		+ U3*(yt(i-3, Series, forecasts, this)-u)
        		+ W1*(yt(i-Cycle, Series, forecasts, this)-u)
        		- U1*W1*(yt(i-Cycle-1, Series, forecasts, this)-u)
    			- U2*W1*(yt(i-Cycle-2, Series, forecasts, this)-u)
				- U3*W1*(yt(i-Cycle-3, Series, forecasts, this)-u);
        }
        
        double	slope = 0.0;
        boolean	iserror = false;
        
        try {
        slope = centerForecasts(IsCenter, Series.length, Cycle, forecasts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			iserror = true;
		}
        
        return String.format("variant:%s,iscenter:%s,slope:%.6f,aic:%.3f,"
        + "u:%.3f,u1:%.3f,u2:%.3f,u3:%.3f,w1:%.3f",
        	"(3,0,0)(1,0,0)s", iserror ? "error" : IsCenter, slope, AIC,
        	u, U1, U2, U3, W1);
	}
}

/**
 * Class implementing ARIMA(3,0,3)(1,0,1)s.
 */
private static final class Arima_s_3_0_3__1_0_1 extends ArimaVariant {
	double		AIC = 0.0;
	double		U1 = 0.0;
	double		U2 = 0.0;
	double		U3 = 0.0;
	double		V1 = 0.0;
	double		V2 = 0.0;
	double		V3 = 0.0;
	double		W1 = 0.0;
	double		X1 = 0.0;
	
	@SuppressWarnings("unused")
	public Arima_s_3_0_3__1_0_1() {}

	public int getNumWeights() {
		return 8;
	}

	public double getRating(
		double[]	coefficients
	) {
        double[]	fitfcsts = new double[Series.length];

		U1 = coefficients[0];
		U2 = coefficients[1];
		U3 = coefficients[2];
		V1 = coefficients[3];
		V2 = coefficients[4];
		V3 = coefficients[5];
		W1 = coefficients[6];
		X1 = coefficients[7];
		forecast(fitfcsts);
        try {
		return AIC = IFSMetrics.getAdjAIC(getNumWeights(), Series, fitfcsts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			return Double.MAX_VALUE;
		}
	}

	public String forecast(
		double[]	forecasts
	) {
		double		u = getMean();
		
		forecasts[0] = Series[0];
        for (int i = 1; i < forecasts.length; i++) {
        	forecasts[i] = u
            	+ U1*(yt(i-1, Series, forecasts, this)-u)
            	+ U2*(yt(i-2, Series, forecasts, this)-u)
            	+ U3*(yt(i-3, Series, forecasts, this)-u)
            	+ W1*(yt(i-Cycle, Series, forecasts, this)-u)
            	- U1*W1*(yt(i-Cycle-1, Series, forecasts, this)-u)
            	- U2*W1*(yt(i-Cycle-2, Series, forecasts, this)-u)
            	- U3*W1*(yt(i-Cycle-3, Series, forecasts, this)-u)
            	- V1*et(i-1, Series, forecasts, this)
            	- V2*et(i-2, Series, forecasts, this)
            	- V3*et(i-3, Series, forecasts, this)
            	- X1*et(i-Cycle, Series, forecasts, this)
            	+ V1*X1*et(i-Cycle-1, Series, forecasts, this)
            	+ V2*X1*et(i-Cycle-2, Series, forecasts, this)
            	+ V3*X1*et(i-Cycle-3, Series, forecasts, this);
        }
        
        double	slope = 0.0;
        boolean	iserror = false;
        
        try {
        slope = centerForecasts(IsCenter, Series.length, Cycle, forecasts);
		} catch (IFSException ex) {
			ex.printStackTrace();
			iserror = true;
		}
        
        return String.format("variant:%s,iscenter:%s,slope:%.6f,aic:%.3f,"
        + "u:%.3f,u1:%.3f,u2:%.3f,u3:%.3f,w1:%.3f,v1:%.3f,v2:%.3f,v3:%.3f,"
        + "x1:%.3f",
        	"(3,0,3)(1,0,1)s", iserror ? "error" : IsCenter, slope, AIC,
        	u, U1, U2, U3, W1, V1, V2, V3, X1);
	}
}
}
