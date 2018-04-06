/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.models.alg;

import java.io.PrintStream;
import java.util.List;

import com.aol.one.reporting.forecastapi.server.models.model.IFSException;
import com.aol.one.reporting.forecastapi.server.models.model.IFSModel;
import com.aol.one.reporting.forecastapi.server.models.model.IFSParameterValue;
import com.aol.one.reporting.forecastapi.server.models.model.IFSStatistics;
import com.aol.one.reporting.forecastapi.server.models.model.IFSUsageDescription;
import com.aol.one.reporting.forecastapi.server.jpe.gw.GW;
import com.aol.one.reporting.forecastapi.server.jpe.gw.GWException;
import com.aol.one.reporting.forecastapi.server.jpe.gw.GWInterface;


/**
 * Class implementing the standard exponential smoothing variations. Currently
 * there are 15 variations with 5 trends and 3 seasonal variations for each
 * trend. The supported trends are: none, linear, exponential, damped additive,
 * and damped multiplicative. The supported seasonal variations are: none,
 * additive, and multiplicative. The model supports the following specific
 * parameters:
 * 
 * seasonality_type=<seasonality type>
 *    none -- No seasonal adjustment.
 *    add  -- Additive seasonal adjustment.
 *    mult -- Multiplicative seasonal adjustment.
 * trend_type=<trend type>
 *    none        -- No trend adjustment.
 *    linear      -- Linear trend adjustment.
 *    exponential -- Exponential trend adjustment.
 *    dampedadd   -- Damped additive trend adjustment.
 *    dampedmult  -- Damped multiplicative trend adjustment.
 */
public final class IFSModelImplExpSm extends IFSModel {
	public static final String					ModelName = "model_expsm";
	private static final IFSUsageDescription	UsageDescription
	= new IFSUsageDescription(
  "Exponential smoothing forecast model implementation.\n",
  "Implements the standard exponential smoothing variations. Currently\n"
+ "there are 15 variations with 5 trends and 3 seasonal variations for each\n"
+ "trend. The supported trends are: none, linear, exponential, damped additive,\n"
+ "and damped multiplicative. The supported seasonal variations are: none,\n"
+ "additive, and multiplicative.\n",
  "\n"
+ "seasonality_type=<seasonality type>\n"
+ "   none -- No seasonal adjustment.\n"
+ "   add  -- Additive seasonal adjustment.\n"
+ "   mult -- Multiplicative seasonal adjustment.\n"
+ "\n"
+ "trend_type=<trend type>\n"
+ "   none        -- No trend adjustment.\n"
+ "   linear      -- Linear trend adjustment.\n"
+ "   exponential -- Exponential trend adjustment.\n"
+ "   dampedadd   -- Damped additive trend adjustment.\n"
+ "   dampedmult  -- Damped multiplicative trend adjustment.\n"
	);
	
	private static enum			SeasonalType {
		None, Additive, Multiplicative
	};
	private static enum			TrendType {
		None, Additive, Multiplicative, DampedAdditive, DampedMultiplicative
	};
	
	private SeasonalType		Seasonality = SeasonalType.None;
	private TrendType			Trend = TrendType.None;

	/* (non-Javadoc)
	 * @see com.aol.ifs.soa.common.IFSModel#execModel(double[], double[])
	 */
	@Override
	protected String execModel(
		double[]	series,
		double[]	forecasts,
		int			cycle
	) throws IFSException {
		int				ns = series.length;
		int				nf = forecasts.length;
		SeasonalType	seasonality = Seasonality;
		TrendType		trend = Trend;
		
		// If the series length is not at least 3, we use random walk.
		
		if (ns < 3) {
			for (int i = 0; i < nf; i++)
				forecasts[i] = series[ns-1];
			return "rw";
		}
		
		// If the series length is not at least 2 times the cycle, we drop
		// back to no seasonal adjustment.
		
		if (seasonality != SeasonalType.None && (cycle <= 1 || 2*cycle > ns))
			seasonality = SeasonalType.None;
		
		// If the model has multiplicative trend or seasonality, the series
		// cannot contain zeroes. If it does we drop back to the additive
		// trend or seasonality or both.
		
		if (trend == TrendType.DampedMultiplicative
		|| trend == TrendType.Multiplicative
		|| seasonality == SeasonalType.Multiplicative) {
            boolean         is_zero = false;

            for (int i = 0; i < ns; i++)
                if (IFSStatistics.isZero(series[i])) {
                    is_zero = true;
                    break;
                }
            if (is_zero) {
                if (trend == TrendType.Multiplicative)
                    trend = TrendType.Additive;
                else if (trend == TrendType.DampedMultiplicative)
                    trend = TrendType.DampedAdditive;
                if (seasonality == SeasonalType.Multiplicative)
                    seasonality = SeasonalType.Additive;
            }
		}
		
		// Execute the exponential smoothing variation appropriate for the
		// trend and seasonal adjustments.
		
		ExpSmVariant	model = null;
		
		switch (trend) {
		case None:
			switch (seasonality) {
			case None:
				model = new ExpSmNoneNone(series, forecasts);
				break;
			case Additive:
				model = new ExpSmNoneAdditive(series, forecasts, cycle);
				break;
			case Multiplicative:
				model = new ExpSmNoneMultiplicative(series, forecasts, cycle);
				break;
			}
			break;
		case Additive:
			switch (seasonality) {
			case None:
				model = new ExpSmAdditiveNone(series, forecasts);
				break;
			case Additive:
				model = new ExpSmAdditiveAdditive(series, forecasts, cycle);
				break;
			case Multiplicative:
				model = new ExpSmAdditiveMultiplicative(series, forecasts, cycle);
				break;
			}
			break;
		case Multiplicative:
			switch (seasonality) {
			case None:
				model = new ExpSmMultiplicativeNone(series, forecasts);
				break;
			case Additive:
				model = new ExpSmMultiplicativeAdditive(series, forecasts, cycle);
				break;
			case Multiplicative:
				model = new ExpSmMultiplicativeMultiplicative(series,
					forecasts, cycle);
				break;
			}
			break;
		case DampedAdditive:
			switch (seasonality) {
			case None:
				model = new ExpSmDampedAdditiveNone(series, forecasts);
				break;
			case Additive:
				model = new ExpSmDampedAdditiveAdditive(series, forecasts, cycle);
				break;
			case Multiplicative:
				model = new ExpSmDampedAdditiveMultiplicative(series,
					forecasts, cycle);
				break;
			}
			break;
		case DampedMultiplicative:
			switch (seasonality) {
			case None:
				model = new ExpSmDampedMultiplicativeNone(series, forecasts);
				break;
			case Additive:
				model = new ExpSmDampedMultiplicativeAdditive(series,
					forecasts, cycle);
				break;
			case Multiplicative:
				model = new ExpSmDampedMultiplicativeMultiplicative(series,
					forecasts, cycle);
				break;
			}
			break;
		}
		
		String	calib_info = null;
		String	fcst_info = null;
		
		model.init();
		calib_info = model.calibrate();
		fcst_info = model.forecast();
		
		return String.format("exp::trend:%s,season:%s,calib(trend:%s,season:%s,%s),fcst(%s)",
			Trend.toString(), Seasonality.toString(), trend.toString(), seasonality.toString(),
			calib_info, fcst_info);
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
		SeasonalType	seasonality = SeasonalType.None;
		TrendType		trend = TrendType.None;
		
		if (parameters != null && parameters.size() > 0) {
			for (IFSParameterValue parameter : parameters)
				if (parameter.getParameter().equals("seasonality_type")) {
					if (parameter.getValue().equals("none"))
						seasonality = SeasonalType.None;
					else if (parameter.getValue().equals("add"))
						seasonality = SeasonalType.Additive;
					else if (parameter.getValue().equals("mult"))
						seasonality = SeasonalType.Multiplicative;
					else
						throw new IFSException(22, getModelName(),
							"seasonality", parameter.getValue());
				} else if (parameter.getParameter().equals("trend_type")) {
					if (parameter.getValue().equals("none"))
						trend = TrendType.None;
					else if (parameter.getValue().equals("linear"))
						trend = TrendType.Additive;
					else if (parameter.getValue().equals("exponential"))
						trend = TrendType.Multiplicative;
					else if (parameter.getValue().equals("dampedadd"))
						trend = TrendType.DampedAdditive;
					else if (parameter.getValue().equals("dampedmult"))
						trend = TrendType.DampedMultiplicative;
					else
						throw new IFSException(22, getModelName(),
							"trend", parameter.getValue());
				} else
					throw new IFSException(23, getModelName(),
						parameter.getParameter());
		}
		
		Seasonality = seasonality;
		Trend = trend;
	}

/*******************/
/* Private Methods */
/*******************/
	
	/**
	 * Return -1.0 if value < 0.0, 0.0 if value is 0.0, or 1.0 if value > 0.0.
	 * 
	 * @param value Value to test.
	 * 
	 * @return Value sign.
	 */
	private static double signum(
		double	value
	) {
		if (value < 0.0)
			return(-1.0);
		else if (value == 0.0)
			return(0.0);
		else
			return(1.0);
	}

/*******************/
/* Private Classes */
/*******************/

/**
 * Abstract class for implementing exponential smoothing variants.
 */
private static abstract class ExpSmVariant implements GWInterface {
	protected static final double	AlphaMin = 0.0;
	protected static final double	AlphaMax = 1.0;
	protected static final double	BetaMin = 0.0;
	protected static final double	BetaMax = 1.0;
	protected static final double	GammaMin = 0.0;
	protected static final double	GammaMax = 1.0;
	protected static final double	PhiMin = 0.0;
	protected static final double	PhiMax = 0.9;
	
	private static final double[]	Steps = {0.1, 0.01, 0.001};

	protected final int				Cycle;
	protected final double[]		Forecasts;
	protected final double[]		Series;
	protected ExpSmVariant			Model;
	
	/**
	 * Default constructor inaccessible.
	 */
	private ExpSmVariant() {
		Cycle = 0;
		Series = null;
		Forecasts = null;
	}
	
	/**
	 * Fully specified constructor for calibration and forecast.
	 * 
	 * @param series Historical time series to access.
	 * @param forecasts Where to put forecasts.
	 * @param cycle Seasonal cycle.
	 */
	public ExpSmVariant(
		double[]	series, 
		double[]	forecasts,
		int			cycle
	) {
		Series = series;
		Forecasts = forecasts;
		Cycle = cycle;
	}
	
    /**
     * Fetch the number of coefficient significant digits.
     * 
     * @return String representation of calibration info.
     */
	public final String calibrate() {
		try {
		new GW(Model, false, System.out);
		} catch (GWException ex) {
			ex.printStackTrace();
		}
		
		return Model.getCalibInfo();
	}
	
	/**
	 * Generate forecasts and return string representing forecast parameter
	 * info.
	 * 
	 * @return Forecast parameter info string.
	 */
	public abstract String forecast();
	
	/**
	 * Fetch model calibration info.
	 * 
	 * @return Model calibration info.
	 */
	protected abstract String getCalibInfo();
	
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
	public double getStepSize(
		int		level,
		int		weight_idx
	) {
		return Steps[level];
	}
	
	/**
	 * Initialize model.
	 */
	public abstract void init();

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
			for (int i = 0; i < coeffs.length; i++)
				trace_out.printf("  C%d    ", i+1);
			trace_out.printf("\n");
		}
		trace_out.printf("%5d  %5d  %5d  %,14.2f",
			iteration, level, coeff_idx, rating);
		for (int i = 0; i < coeffs.length; i++)
			trace_out.printf("  %6.3f", coeffs[i]);
		trace_out.printf("\n");
	}
}

/**
 * Class for implementing the additive trend additive seasonal
 * adjustment variation.
 */
private static final class ExpSmAdditiveAdditive extends ExpSmVariant {
	double		Level = 0.0;
	double		Level0 = 0.0;
	double		Slope = 0.0;
	double		Slope0 = 0.0;
	double[]	Season = null;
	double[]	Season0 = null;
	String		CalibInfo = "";
	
	@SuppressWarnings("unused")
	private ExpSmAdditiveAdditive() {}
	
	public ExpSmAdditiveAdditive(
		double[]	series,
		double[]	forecasts,
		int			cycle
	) {
		super(series, forecasts, cycle);
		Model = this;
		Season = new double[Cycle];
		Season0 = new double[Cycle];
	}

	public String forecast() {
		for (int i = 0; i < Forecasts.length; i++) {
			Forecasts[i] = Level+(i+1)*Slope+Season[(Series.length+i)%Cycle];
			if (IFSStatistics.isUndef(Forecasts[i]))
				Forecasts[i] = 0.0;
		}
		
		String	fcst_info = String.format("level:%.3f,slope:%.3f,cycle:%d",
				Level, Slope, Cycle);
		
		for (int i = 0; i < Season.length; i++) {
			fcst_info = String.format("%s,season%d:%.7f", fcst_info,
				i+1, Season[i]);
		}
		
		return fcst_info;
	}

	@Override
	public String getCalibInfo() {
		return CalibInfo;
	}

	public int getNumWeights() {
		return 3;
	}

	public double getRating(
		double[]	coefficients
	) {
		double		alpha = coefficients[0];
		double		beta = coefficients[1];
		double		gamma = coefficients[2];
		double		rmse = 0.0;
		double		fcst = 0.0;
		double		fcst_delta  = 0.0;
		double		level_p = 0.0;
		
		Level = Level0;
		Slope = Slope0;
		for (int i = 0; i < Cycle; i++)
			Season[i] = Season0[i];
		for (int i = 0; i < Series.length; i++) {
			fcst = Level+Slope+Season[i%Cycle];
			if (IFSStatistics.isUndef(fcst))
				fcst = 0.0;
			fcst_delta = Series[i]-fcst;
			rmse += fcst_delta*fcst_delta;
			level_p = Level;
			Level = alpha*(Series[i]-Season[i%Cycle])+(1.0-alpha)*(Level+Slope);
			Slope = beta*(Level-level_p)+(1.0-beta)*Slope;
			Season[i%Cycle] = gamma*(Series[i]-Level)+(1.0-gamma)*Season[i%Cycle];
		}
		rmse = Math.sqrt(rmse/Series.length);

		CalibInfo = String.format("rmse:%.3f,alpha:%.3f,beta:%.3f,gamma:%.3f",
			rmse, alpha, beta, gamma);
		
		return rmse;
	}

	public double getWeightLowerBound(
		int			weight_idx
	) {
		switch (weight_idx) {
		case 0:
			return AlphaMin;
		case 1:
			return BetaMin;
		default:
			return GammaMin;
		}
	}

	public double getWeightUpperBound(
		int			weight_idx
	) {
		switch (weight_idx) {
		case 0:
			return AlphaMax;
		case 1:
			return BetaMax;
		default:
			return GammaMax;
		}
	}

	public void init() {
		for (int i = 0; i < Cycle; i++) {
			Level0 += Series[i];
			Slope0 += (Series[Cycle+i]-Series[i])/Cycle;
		}
		Level0 /= Cycle;
		Slope0 /= Cycle;

		for (int i = 0; i < Cycle; i++)
			Season0[i] = Series[i]-Level0;
	}
}

/**
 * Class for implementing the additive trend multiplicative seasonal
 * adjustment variation.
 */
private static final class ExpSmAdditiveMultiplicative extends ExpSmVariant {
	double		Level = 0.0;
	double		Level0 = 0.0;
	double		Slope = 0.0;
	double		Slope0 = 0.0;
	double[]	Season = null;
	double[]	Season0 = null;
	String		CalibInfo = "";
	
	@SuppressWarnings("unused")
	private ExpSmAdditiveMultiplicative() {}
	
	public ExpSmAdditiveMultiplicative(
		double[]	series,
		double[]	forecasts,
		int			cycle
	) {
		super(series, forecasts, cycle);
		Model = this;
		Season = new double[Cycle];
		Season0 = new double[Cycle];
	}

	public String forecast() {
		for (int i = 0; i < Forecasts.length; i++) {
			Forecasts[i] = (Level+(i+1)*Slope)*Season[(Series.length+i)%Cycle];
			if (IFSStatistics.isUndef(Forecasts[i]))
				Forecasts[i] = 0.0;
		}
		
		String	fcst_info = String.format("level:%.3f,slope:%.3f,cycle:%d",
				Level, Slope, Cycle);
		
		for (int i = 0; i < Season.length; i++) {
			fcst_info = String.format("%s,season%d:%.7f", fcst_info,
				i+1, Season[i]);
		}
		
		return fcst_info;
	}

	@Override
	public String getCalibInfo() {
		return CalibInfo;
	}

	public int getNumWeights() {
		return 3;
	}

	public double getRating(
		double[]	coefficients
	) {
		double		alpha = coefficients[0];
		double		beta = coefficients[1];
		double		gamma = coefficients[2];
		double		rmse = 0.0;
		double		fcst = 0.0;
		double		fcst_delta  = 0.0;
		double		level_p = 0.0;
		
		Level = Level0;
		Slope = Slope0;
		for (int i = 0; i < Cycle; i++)
			Season[i] = Season0[i];
		for (int i = 0; i < Series.length; i++) {
			fcst = (Level+Slope)*Season[i%Cycle];
			if (IFSStatistics.isUndef(fcst))
				fcst = 0.0;
			fcst_delta = Series[i]-fcst;
			rmse += fcst_delta*fcst_delta;
			level_p = Level;
			Level = alpha*(Series[i]/Season[i%Cycle])+(1.0-alpha)*(Level+Slope);
			Slope = beta*(Level-level_p)+(1.0-beta)*Slope;
			Season[i%Cycle] = gamma*(Series[i]/Level)+(1.0-gamma)*Season[i%Cycle];
		}
		rmse = Math.sqrt(rmse/Series.length);

		CalibInfo = String.format("rmse:%.3f,alpha:%.3f,beta:%.3f,gamma:%.3f",
			rmse, alpha, beta, gamma);
		
		return rmse;
	}

	public double getWeightLowerBound(
		int			weight_idx
	) {
		switch (weight_idx) {
		case 0:
			return AlphaMin;
		case 1:
			return BetaMin;
		default:
			return GammaMin;
		}
	}

	public double getWeightUpperBound(
		int			weight_idx
	) {
		switch (weight_idx) {
		case 0:
			return AlphaMax;
		case 1:
			return BetaMax;
		default:
			return GammaMax;
		}
	}

	public void init() {
		int		nreps = Series.length/Cycle;
		int		csize = nreps*Cycle;
		int		ssize = (nreps-1)*Cycle;
       
		for (int i = 0; i < csize; i++)
			Level0 += Series[i];
		Level0 /= csize;
		for (int i = 0; i < ssize; i++)
			Slope0 += (Series[Cycle+i]-Series[i])/Cycle;
		Slope0 /= ssize;
        if (IFSStatistics.isZero(Level0))
        	Level0 = 1.0;

		for (int i = 0; i < csize; i++)
			Season0[i%Cycle] += Series[i]/Level0;
		for (int i = 0; i < Cycle; i++)
			Season0[i] /= Level0;
	}
}

/**
 * Class for implementing the additive trend no seasonal adjustment
 * variation.
 */
private static final class ExpSmAdditiveNone extends ExpSmVariant {
	double		Level = 0.0;
	double		Level0 = 0.0;
	double		Slope = 0.0;
	double		Slope0 = 0.0;
	String		CalibInfo = "";
	
	@SuppressWarnings("unused")
	private ExpSmAdditiveNone() {}
	
	public ExpSmAdditiveNone(
		double[]	series,
		double[]	forecasts
	) {
		super(series, forecasts, 0);
		Model = this;
	}

	public String forecast() {
		for (int i = 0; i < Forecasts.length; i++) {
			Forecasts[i] = Level+(i+1)*Slope;
			if (IFSStatistics.isUndef(Forecasts[i]))
				Forecasts[i] = 0.0;
		}
		
		String	fcst_info = String.format("level:%.3f,slope:%.3f", Level, Slope);
		
		return fcst_info;
	}

	@Override
	public String getCalibInfo() {
		return CalibInfo;
	}

	public int getNumWeights() {
		return 2;
	}

	public double getRating(
		double[]	coefficients
	) {
		double		alpha = coefficients[0];
		double		beta = coefficients[1];
		double		rmse = 0.0;
		double		fcst = 0.0;
		double		fcst_delta  = 0.0;
		double		level_p = 0.0;
		
		Level = Level0;
		Slope = Slope0;
		for (int i = 0; i < Series.length; i++) {
			fcst = Level+Slope;
			if (IFSStatistics.isUndef(fcst))
				fcst = 0.0;
			fcst_delta = Series[i]-fcst;
			rmse += fcst_delta*fcst_delta;
			level_p = Level;
			Level = alpha*Series[i]+(1.0-alpha)*(Level+Slope);
			Slope = beta*(Level-level_p)+(1.0-beta)*Slope;
		}
		rmse = Math.sqrt(rmse/Series.length);

		CalibInfo = String.format("rmse:%.3f,alpha:%.3f,beta:%.3f",
			rmse, alpha, beta);
		
		return rmse;
	}

	public double getWeightLowerBound(
		int			weight_idx
	) {
		switch (weight_idx) {
		case 0:
			return AlphaMin;
		default:
			return BetaMin;
		}
	}

	public double getWeightUpperBound(
		int			weight_idx
	) {
		switch (weight_idx) {
		case 0:
			return AlphaMax;
		default:
			return BetaMax;
		}
	}

	public void init() {
		Level0 = Series[0];
		Slope0 = Series[1]-Series[0];
	}
}

/**
 * Class for implementing the damped additive trend additive seasonal
 * adjustment variation.
 */
private static final class ExpSmDampedAdditiveAdditive extends ExpSmVariant {
	double		Level = 0.0;
	double		Level0 = 0.0;
	double		Slope = 0.0;
	double		Slope0 = 0.0;
	double[]	Season = null;
	double[]	Season0 = null;
	double		Phi = 0.0;
	String		CalibInfo = "";
	
	@SuppressWarnings("unused")
	private ExpSmDampedAdditiveAdditive() {}
	
	public ExpSmDampedAdditiveAdditive(
		double[]	series,
		double[]	forecasts,
		int			cycle
	) {
		super(series, forecasts, cycle);
		Model = this;
		Season = new double[Cycle];
		Season0 = new double[Cycle];
	}

	public String forecast() {
		double		h = 0.0;

		for (int i = 0; i < Forecasts.length; i++) {
			h += Math.pow(Phi, i+1);
			Forecasts[i] = Level+h*Slope+Season[(Series.length+i)%Cycle];
			if (IFSStatistics.isUndef(Forecasts[i]))
				Forecasts[i] = 0.0;
		}
		
		String	fcst_info = String.format("level:%.3f,phi:%.3f,slope:%.3f,cycle:%d",
				Level, Phi, Slope, Cycle);
		
		for (int i = 0; i < Season.length; i++) {
			fcst_info = String.format("%s,season%d:%.7f", fcst_info,
				i+1, Season[i]);
		}
		
		return fcst_info;
	}

	@Override
	public String getCalibInfo() {
		return CalibInfo;
	}

	public int getNumWeights() {
		return 4;
	}

	public double getRating(
		double[]	coefficients
	) {
		double		alpha = coefficients[0];
		double		beta = coefficients[1];
		double		gamma = coefficients[2];
		double		rmse = 0.0;
		double		fcst = 0.0;
		double		fcst_delta  = 0.0;
		double		level_p = 0.0;

		Phi = coefficients[3];
		
		Level = Level0;
		Slope = Slope0;
		for (int i = 0; i < Cycle; i++)
			Season[i] = Season0[i];
		for (int i = 0; i < Series.length; i++) {
			fcst = Level+Slope+Season[i%Cycle];
			if (IFSStatistics.isUndef(fcst))
				fcst = 0.0;
			fcst_delta = Series[i]-fcst;
			rmse += fcst_delta*fcst_delta;
			level_p = Level;
			Level = alpha*(Series[i]-Season[i%Cycle])+(1.0-alpha)*(Level+Phi*Slope);
			Slope = beta*(Level-level_p)+(1.0-beta)*Phi*Slope;
			Season[i%Cycle] = gamma*(Series[i]-Level)+(1.0-gamma)*Season[i%Cycle];
		}
		rmse = Math.sqrt(rmse/Series.length);
		
		CalibInfo = String.format("rmse:%.3f,alpha:%.3f,beta:%.3f,gamma:%.3f,phi:%.3f",
			rmse, alpha, beta, gamma, Phi);
		
		return rmse;
	}

	public double getWeightLowerBound(
		int			weight_idx
	) {
		switch (weight_idx) {
		case 0:
			return AlphaMin;
		case 1:
			return BetaMin;
		case 2:
			return GammaMin;
		default:
			return PhiMin;
		}
	}

	public double getWeightUpperBound(
		int			weight_idx
	) {
		switch (weight_idx) {
		case 0:
			return AlphaMax;
		case 1:
			return BetaMax;
		case 2:
			return GammaMax;
		default:
			return PhiMax;
		}
	}

	public void init() {
		for (int i = 0; i < Cycle; i++) {
			Level0 += Series[i];
			Slope0 += (Series[Cycle+i]-Series[i])/Cycle;
		}
		Level0 /= Cycle;
		Slope0 /= Cycle;

		for (int i = 0; i < Cycle; i++)
			Season0[i] = Series[i]-Level0;
	}
}

/**
 * Class for implementing the damped additive trend multiplicative seasonal
 * adjustment variation.
 */
private static final class ExpSmDampedAdditiveMultiplicative extends ExpSmVariant {
	double		Level = 0.0;
	double		Level0 = 0.0;
	double		Slope = 0.0;
	double		Slope0 = 0.0;
	double[]	Season = null;
	double[]	Season0 = null;
	double		Phi = 0.0;
	String		CalibInfo = "";
	
	@SuppressWarnings("unused")
	private ExpSmDampedAdditiveMultiplicative() {}
	
	public ExpSmDampedAdditiveMultiplicative(
		double[]	series,
		double[]	forecasts,
		int			cycle
	) {
		super(series, forecasts, cycle);
		Model = this;
		Season = new double[Cycle];
		Season0 = new double[Cycle];
	}

	public String forecast() {
		double		h = 0.0;

		for (int i = 0; i < Forecasts.length; i++) {
			h += Math.pow(Phi, i+1);
			Forecasts[i] = (Level+h*Slope)*Season[(Series.length+i)%Cycle];
			if (IFSStatistics.isUndef(Forecasts[i]))
				Forecasts[i] = 0.0;
		}
		
		String	fcst_info = String.format("level:%.3f,phi:%.3f,slope:%.3f,cycle:%d",
				Level, Phi, Slope, Cycle);
		
		for (int i = 0; i < Season.length; i++) {
			fcst_info = String.format("%s,season%d:%.7f", fcst_info,
				i+1, Season[i]);
		}
		
		return fcst_info;
	}

	@Override
	public String getCalibInfo() {
		return CalibInfo;
	}

	public int getNumWeights() {
		return 4;
	}

	public double getRating(
		double[]	coefficients
	) {
		double		alpha = coefficients[0];
		double		beta = coefficients[1];
		double		gamma = coefficients[2];
		double		rmse = 0.0;
		double		fcst = 0.0;
		double		fcst_delta  = 0.0;
		double		level_p = 0.0;

		Phi = coefficients[3];
		
		Level = Level0;
		Slope = Slope0;
		for (int i = 0; i < Cycle; i++)
			Season[i] = Season0[i];
		for (int i = 0; i < Series.length; i++) {
			fcst = (Level+Slope)*Season[i%Cycle];
			if (IFSStatistics.isUndef(fcst))
				fcst = 0.0;
			fcst_delta = Series[i]-fcst;
			rmse += fcst_delta*fcst_delta;
			level_p = Level;
			Level = alpha*(Series[i]/Season[i%Cycle])+(1.0-alpha)*(Level+Phi*Slope);
			Slope = beta*(Level-level_p)+(1.0-beta)*Phi*Slope;
			Season[i%Cycle] = gamma*(Series[i]/Level)+(1.0-gamma)*Season[i%Cycle];
		}
		rmse = Math.sqrt(rmse/Series.length);
		
		CalibInfo = String.format("rmse:%.3f,alpha:%.3f,beta:%.3f,gamma:%.3f,phi:%.3f",
			rmse, alpha, beta, gamma, Phi);
		
		return rmse;
	}

	public double getWeightLowerBound(
		int			weight_idx
	) {
		switch (weight_idx) {
		case 0:
			return AlphaMin;
		case 1:
			return BetaMin;
		case 2:
			return GammaMin;
		default:
			return PhiMin;
		}
	}

	public double getWeightUpperBound(
		int			weight_idx
	) {
		switch (weight_idx) {
		case 0:
			return AlphaMax;
		case 1:
			return BetaMax;
		case 2:
			return GammaMax;
		default:
			return PhiMax;
		}
	}

	public void init() {
		int		nreps = Series.length/Cycle;
		int		csize = nreps*Cycle;
		int		ssize = (nreps-1)*Cycle;
       
		for (int i = 0; i < csize; i++)
			Level0 += Series[i];
		Level0 /= csize;
		for (int i = 0; i < ssize; i++)
			Slope0 += (Series[Cycle+i]-Series[i])/Cycle;
		Slope0 /= ssize;
        if (IFSStatistics.isZero(Level0))
        	Level0 = 1.0;

		for (int i = 0; i < csize; i++)
			Season0[i%Cycle] += Series[i]/Level0;
		for (int i = 0; i < Cycle; i++)
			Season0[i] /= Level0;
	}
}

/**
 * Class for implementing the damped additive trend no seasonal adjustment
 * variation.
 */
private static final class ExpSmDampedAdditiveNone extends ExpSmVariant {
	double		Level = 0.0;
	double		Level0 = 0.0;
	double		Slope = 0.0;
	double		Slope0 = 0.0;
	double		Phi = 0.0;
	String		CalibInfo = "";
	
	@SuppressWarnings("unused")
	private ExpSmDampedAdditiveNone() {}
	
	public ExpSmDampedAdditiveNone(
		double[]	series,
		double[]	forecasts
	) {
		super(series, forecasts, 0);
		Model = this;
	}

	public String forecast() {
		double		h = 0.0;

		for (int i = 0; i < Forecasts.length; i++) {
			h += Math.pow(Phi, i+1);
			Forecasts[i] = Level+h*Slope;
			if (IFSStatistics.isUndef(Forecasts[i]))
				Forecasts[i] = 0.0;
		}
		
		String	fcst_info = String.format("level:%.3f,phi:%.3f,slope:%.3f",
				Level, Phi, Slope);
		
		return fcst_info;
	}

	@Override
	public String getCalibInfo() {
		return CalibInfo;
	}

	public int getNumWeights() {
		return 3;
	}

	public double getRating(
		double[]	coefficients
	) {
		double		alpha = coefficients[0];
		double		beta = coefficients[1];
		double		rmse = 0.0;
		double		fcst = 0.0;
		double		fcst_delta  = 0.0;
		double		level_p = 0.0;

		Phi = coefficients[2];
		
		Level = Level0;
		Slope = Slope0;
		for (int i = 0; i < Series.length; i++) {
			fcst = Level+Slope;
			if (IFSStatistics.isUndef(fcst))
				fcst = 0.0;
			fcst_delta = Series[i]-fcst;
			rmse += fcst_delta*fcst_delta;
			level_p = Level;
			Level = alpha*Series[i]+(1.0-alpha)*(Level+Phi*Slope);
			Slope = beta*(Level-level_p)+(1.0-beta)*Phi*Slope;
		}
		rmse = Math.sqrt(rmse/Series.length);
		
		CalibInfo = String.format("rmse:%.3f,alpha:%.3f,beta:%.3f,phi:%.3f",
			rmse, alpha, beta, Phi);
		
		return rmse;
	}

	public double getWeightLowerBound(
		int			weight_idx
	) {
		switch (weight_idx) {
		case 0:
			return AlphaMin;
		case 1:
			return BetaMin;
		default:
			return PhiMin;
		}
	}

	public double getWeightUpperBound(
		int			weight_idx
	) {
		switch (weight_idx) {
		case 0:
			return AlphaMax;
		case 1:
			return BetaMax;
		default:
			return PhiMax;
		}
	}

	public void init() {
		Level0 = Series[0];
		Slope0 = Series[1]-Series[0];
	}
}

/**
 * Class for implementing the damped multiplicative trend additive seasonal
 * adjustment variation.
 */
private static final class ExpSmDampedMultiplicativeAdditive extends ExpSmVariant {
	double		Level = 0.0;
	double		Level0 = 0.0;
	double		Slope = 0.0;
	double		Slope0 = 0.0;
	double[]	Season = null;
	double[]	Season0 = null;
	double		Phi = 0.0;
	String		CalibInfo = "";
	
	@SuppressWarnings("unused")
	private ExpSmDampedMultiplicativeAdditive() {}
	
	public ExpSmDampedMultiplicativeAdditive(
		double[]	series,
		double[]	forecasts,
		int			cycle
	) {
		super(series, forecasts, cycle);
		Model = this;
		Season = new double[Cycle];
		Season0 = new double[Cycle];
	}

	public String forecast() {
		double		h = 0.0;

		for (int i = 0; i < Forecasts.length; i++) {
			h += Math.pow(Phi, i+1);
			Forecasts[i] = (Level*signum(Slope)*Math.pow(Math.abs(Slope),h))
				+Season[(Series.length+i)%Cycle];
			if (IFSStatistics.isUndef(Forecasts[i]))
				Forecasts[i] = 0.0;
		}
		
		String	fcst_info = String.format("level:%.3f,phi:%.3f,slope:%.3f,cycle:%d",
				Level, Phi, Slope, Cycle);
		
		for (int i = 0; i < Season.length; i++) {
			fcst_info = String.format("%s,season%d:%.7f", fcst_info,
				i+1, Season[i]);
		}
		
		return fcst_info;
	}

	@Override
	public String getCalibInfo() {
		return CalibInfo;
	}

	public int getNumWeights() {
		return 4;
	}

	public double getRating(
		double[]	coefficients
	) {
		double		alpha = coefficients[0];
		double		beta = coefficients[1];
		double		gamma = coefficients[2];
		double		rmse = 0.0;
		double		fcst = 0.0;
		double		fcst_delta  = 0.0;
		double		level_p = 0.0;

		Phi = coefficients[3];
		
		Level = Level0;
		Slope = Slope0;
		for (int i = 0; i < Cycle; i++)
			Season[i] = Season0[i];
		for (int i = 0; i < Series.length; i++) {
			fcst = Level*Slope+Season[i%Cycle];
			if (IFSStatistics.isUndef(fcst))
				fcst = 0.0;
			fcst_delta = Series[i]-fcst;
			rmse += fcst_delta*fcst_delta;
			level_p = Level;
			Level = alpha*(Series[i]-Season[i%Cycle])+(1.0-alpha)
				*(Level*signum(Slope)*Math.pow(Math.abs(Slope),Phi));
			Slope = beta*(Level/level_p)+(1.0-beta)
				*signum(Slope)*Math.pow(Math.abs(Slope),Phi);
			Season[i%Cycle] = gamma*(Series[i]-Level)+(1.0-gamma)*Season[i%Cycle];
		}
		rmse = Math.sqrt(rmse/Series.length);
		
		CalibInfo = String.format("rmse:%.3f,alpha:%.3f,beta:%.3f,gamma:%.3f,phi:%.3f",
			rmse, alpha, beta, gamma, Phi);
		
		return rmse;
	}

	public double getWeightLowerBound(
		int			weight_idx
	) {
		switch (weight_idx) {
		case 0:
			return AlphaMin;
		case 1:
			return BetaMin;
		case 2:
			return GammaMin;
		default:
			return PhiMin;
		}
	}

	public double getWeightUpperBound(
		int			weight_idx
	) {
		switch (weight_idx) {
		case 0:
			return AlphaMax;
		case 1:
			return BetaMax;
		case 2:
			return GammaMax;
		default:
			return PhiMax;
		}
	}

	public void init() {
		for (int i = 0; i < Cycle; i++) {
			Level0 += Series[i];
			Slope0 += Series[Cycle+i]/Series[i];
		}
		Level0 /= Cycle;
		Slope0 /= Cycle;

		for (int i = 0; i < Cycle; i++)
			Season0[i] = Series[i]-Level0;
	}
}

/**
 * Class for implementing the damped multiplicative trend multiplicative seasonal
 * adjustment variation.
 */
private static final class ExpSmDampedMultiplicativeMultiplicative extends ExpSmVariant {
	double		Level = 0.0;
	double		Level0 = 0.0;
	double		Slope = 0.0;
	double		Slope0 = 0.0;
	double[]	Season = null;
	double[]	Season0 = null;
	double		Phi = 0.0;
	String		CalibInfo = "";
	
	@SuppressWarnings("unused")
	private ExpSmDampedMultiplicativeMultiplicative() {}
	
	public ExpSmDampedMultiplicativeMultiplicative(
		double[]	series,
		double[]	forecasts,
		int			cycle
	) {
		super(series, forecasts, cycle);
		Model = this;
		Season = new double[Cycle];
		Season0 = new double[Cycle];
	}

	public String forecast() {
		double		h = 0.0;

		for (int i = 0; i < Forecasts.length; i++) {
			h += Math.pow(Phi, i+1);
			Forecasts[i] = Level*signum(Slope)*Math.pow(Math.abs(Slope),h)
				*Season[(Series.length+i)%Cycle];
			if (IFSStatistics.isUndef(Forecasts[i]))
				Forecasts[i] = 0.0;
		}
		
		String	fcst_info = String.format("level:%.3f,phi:%.3f,slope:%.3f,cycle:%d",
				Level, Phi, Slope, Cycle);
		
		for (int i = 0; i < Season.length; i++) {
			fcst_info = String.format("%s,season%d:%.7f", fcst_info,
				i+1, Season[i]);
		}
		
		return fcst_info;
	}

	@Override
	public String getCalibInfo() {
		return CalibInfo;
	}

	public int getNumWeights() {
		return 4;
	}

	public double getRating(
		double[]	coefficients
	) {
		double		alpha = coefficients[0];
		double		beta = coefficients[1];
		double		gamma = coefficients[2];
		double		rmse = 0.0;
		double		fcst = 0.0;
		double		fcst_delta  = 0.0;
		double		level_p = 0.0;

		Phi = coefficients[3];
		
		Level = Level0;
		Slope = Slope0;
		for (int i = 0; i < Cycle; i++)
			Season[i] = Season0[i];
		for (int i = 0; i < Series.length; i++) {
			fcst = Level*Slope*Season[i%Cycle];
			if (IFSStatistics.isUndef(fcst))
				fcst = 0.0;
			fcst_delta = Series[i]-fcst;
			rmse += fcst_delta*fcst_delta;
			level_p = Level;
			Level = alpha*(Series[i]/Season[i%Cycle])+(1.0-alpha)
				*(Level*signum(Slope)*Math.pow(Math.abs(Slope),Phi));
			Slope = beta*(Level/level_p)+(1.0-beta)
				*signum(Slope)*Math.pow(Math.abs(Slope),Phi);
			Season[i%Cycle] = gamma*(Series[i]/Level)+(1.0-gamma)*Season[i%Cycle];
		}
		rmse = Math.sqrt(rmse/Series.length);
		
		CalibInfo = String.format("rmse:%.3f,alpha:%.3f,beta:%.3f,gamma:%.3f,phi:%.3f",
			rmse, alpha, beta, gamma, Phi);
		
		return rmse;
	}

	public double getWeightLowerBound(
		int			weight_idx
	) {
		switch (weight_idx) {
		case 0:
			return AlphaMin;
		case 1:
			return BetaMin;
		case 2:
			return GammaMin;
		default:
			return PhiMin;
		}
	}

	public double getWeightUpperBound(
		int			weight_idx
	) {
		switch (weight_idx) {
		case 0:
			return AlphaMax;
		case 1:
			return BetaMax;
		case 2:
			return GammaMax;
		default:
			return PhiMax;
		}
	}

	public void init() {
        int     nreps = Series.length/Cycle;
        int     csize = nreps*Cycle;
        int     ssize = (nreps-1)*Cycle;

        for (int i = 0; i < csize; i++)
            Level0 += Series[i];
        Level0 /= csize;
        for (int i = 0; i < ssize; i++)
            Slope0 += Series[Cycle+i]/Series[i];
        Slope0 /= ssize;
        if (IFSStatistics.isZero(Level0))
        	Level0 = 1.0;

        for (int i = 0; i < csize; i++)
            Season0[i%Cycle] += Series[i]/Level0;
        for (int i = 0; i < Cycle; i++)
            Season0[i] /= Level0;
	}
}

/**
 * Class for implementing the damped multiplicative trend no seasonal adjustment
 * variation.
 */
private static final class ExpSmDampedMultiplicativeNone extends ExpSmVariant {
	double		Level = 0.0;
	double		Level0 = 0.0;
	double		Slope = 0.0;
	double		Slope0 = 0.0;
	double		Phi = 0.0;
	String		CalibInfo = "";
	
	@SuppressWarnings("unused")
	private ExpSmDampedMultiplicativeNone() {}
	
	public ExpSmDampedMultiplicativeNone(
		double[]	series,
		double[]	forecasts
	) {
		super(series, forecasts, 0);
		Model = this;
	}

	public String forecast() {
		double		h = 0.0;

		for (int i = 0; i < Forecasts.length; i++) {
			h += Math.pow(Phi, i+1);
			Forecasts[i] = Level*signum(Slope)*Math.pow(Math.abs(Slope), h);
			if (IFSStatistics.isUndef(Forecasts[i]))
				Forecasts[i] = 0.0;
		}
		
		String	fcst_info = String.format("level:%.3f,phi:%.3f,slope:%.3f",
				Level, Phi, Slope);
		
		return fcst_info;
	}

	@Override
	public String getCalibInfo() {
		return CalibInfo;
	}

	public int getNumWeights() {
		return 3;
	}

	public double getRating(
		double[]	coefficients
	) {
		double		alpha = coefficients[0];
		double		beta = coefficients[1];
		double		rmse = 0.0;
		double		fcst = 0.0;
		double		fcst_delta  = 0.0;
		double		level_p = 0.0;

		Phi = coefficients[2];
		
		Level = Level0;
		Slope = Slope0;
		for (int i = 0; i < Series.length; i++) {
			fcst = Level*Slope;
			if (IFSStatistics.isUndef(fcst))
				fcst = 0.0;
			fcst_delta = Series[i]-fcst;
			rmse += fcst_delta*fcst_delta;
			level_p = Level;
			Level = alpha*Series[i]+(1.0-alpha)
				*(Level*signum(Slope)*Math.pow(Math.abs(Slope),Phi));
			Slope = beta*(Level/level_p)+(1.0-beta)
				*signum(Slope)*Math.pow(Math.abs(Slope),Phi);
		}
		rmse = Math.sqrt(rmse/Series.length);
		
		CalibInfo = String.format("rmse:%.3f,alpha:%.3f,beta:%.3f,phi:%.3f",
			rmse, alpha, beta, Phi);
		
		return rmse;
	}

	public double getWeightLowerBound(
		int			weight_idx
	) {
		switch (weight_idx) {
		case 0:
			return AlphaMin;
		case 1:
			return BetaMin;
		default:
			return PhiMin;
		}
	}

	public double getWeightUpperBound(
		int			weight_idx
	) {
		switch (weight_idx) {
		case 0:
			return AlphaMax;
		case 1:
			return BetaMax;
		default:
			return PhiMax;
		}
	}

	public void init() {
		Level0 = Series[0];
		Slope0 = Series[1]/Series[0];
	}
}

/**
 * Class for implementing the multiplicative trend additive seasonal
 * adjustment variation.
 */
private static final class ExpSmMultiplicativeAdditive extends ExpSmVariant {
	double		Level = 0.0;
	double		Level0 = 0.0;
	double		Slope = 0.0;
	double		Slope0 = 0.0;
	double[]	Season = null;
	double[]	Season0 = null;
	String		CalibInfo = "";
	
	@SuppressWarnings("unused")
	private ExpSmMultiplicativeAdditive() {}
	
	public ExpSmMultiplicativeAdditive(
		double[]	series,
		double[]	forecasts,
		int			cycle
	) {
		super(series, forecasts, cycle);
		Model = this;
		Season = new double[Cycle];
		Season0 = new double[Cycle];
	}

	public String forecast() {
		for (int i = 0; i < Forecasts.length; i++) {
			Forecasts[i] = Level*Math.pow(Slope, i+1)+Season[(Series.length+i)%Cycle];
			if (IFSStatistics.isUndef(Forecasts[i]))
				Forecasts[i] = 0.0;
		}
		
		String	fcst_info = String.format("level:%.3f,slope:%.3f,cycle:%d",
				Level, Slope, Cycle);
		
		for (int i = 0; i < Season.length; i++) {
			fcst_info = String.format("%s,season%d:%.7f", fcst_info,
				i+1, Season[i]);
		}
		
		return fcst_info;
	}

	@Override
	public String getCalibInfo() {
		return CalibInfo;
	}

	public int getNumWeights() {
		return 3;
	}

	public double getRating(
		double[]	coefficients
	) {
		double		alpha = coefficients[0];
		double		beta = coefficients[1];
		double		gamma = coefficients[2];
		double		rmse = 0.0;
		double		fcst = 0.0;
		double		fcst_delta  = 0.0;
		double		level_p = 0.0;
		
		Level = Level0;
		Slope = Slope0;
		for (int i = 0; i < Cycle; i++)
			Season[i] = Season0[i];
		for (int i = 0; i < Series.length; i++) {
			fcst = Level*Slope+Season[i%Cycle];
			if (IFSStatistics.isUndef(fcst))
				fcst = 0.0;
			fcst_delta = Series[i]-fcst;
			rmse += fcst_delta*fcst_delta;
			level_p = Level;
			Level = alpha*(Series[i]-Season[i%Cycle])+(1.0-alpha)*(Level*Slope);
			Slope = beta*(Level/level_p)+(1.0-beta)*Slope;
			Season[i%Cycle] = gamma*(Series[i]-Level)+(1.0-gamma)*Season[i%Cycle];
		}
		rmse = Math.sqrt(rmse/Series.length);

		CalibInfo = String.format("rmse:%.3f,alpha:%.3f,beta:%.3f,gamma:%.3f",
			rmse, alpha, beta, gamma);
		
		return rmse;
	}

	public double getWeightLowerBound(
		int			weight_idx
	) {
		switch (weight_idx) {
		case 0:
			return AlphaMin;
		case 1:
			return BetaMin;
		default:
			return GammaMin;
		}
	}

	public double getWeightUpperBound(
		int			weight_idx
	) {
		switch (weight_idx) {
		case 0:
			return AlphaMax;
		case 1:
			return BetaMax;
		default:
			return GammaMax;
		}
	}

	public void init() {
		for (int i = 0; i < Cycle; i++) {
			Level0 += Series[i];
			Slope0 += Series[Cycle+i]/Series[i];
		}
		Level0 /= Cycle;
		Slope0 /= Cycle;
        if (IFSStatistics.isZero(Level0))
        	Level0 = 1.0;

		for (int i = 0; i < Cycle; i++)
			Season0[i] = Series[i]-Level0;
	}
}

/**
 * Class for implementing the multiplicative trend multiplicative seasonal
 * adjustment variation.
 */
private static final class ExpSmMultiplicativeMultiplicative extends ExpSmVariant {
	double		Level = 0.0;
	double		Level0 = 0.0;
	double		Slope = 0.0;
	double		Slope0 = 0.0;
	double[]	Season = null;
	double[]	Season0 = null;
	String		CalibInfo = "";
	
	@SuppressWarnings("unused")
	private ExpSmMultiplicativeMultiplicative() {}
	
	public ExpSmMultiplicativeMultiplicative(
		double[]	series,
		double[]	forecasts,
		int			cycle
	) {
		super(series, forecasts, cycle);
		Model = this;
		Season = new double[Cycle];
		Season0 = new double[Cycle];
	}

	public String forecast() {
		for (int i = 0; i < Forecasts.length; i++) {
			Forecasts[i] = Level*Math.pow(Slope, i+1)*Season[(Series.length+i)%Cycle];
			if (IFSStatistics.isUndef(Forecasts[i]))
				Forecasts[i] = 0.0;
		}
		
		String	fcst_info = String.format("level:%.3f,slope:%.3f,cycle:%d",
				Level, Slope, Cycle);
		
		for (int i = 0; i < Season.length; i++) {
			fcst_info = String.format("%s,season%d:%.7f", fcst_info,
				i+1, Season[i]);
		}
		
		return fcst_info;
	}

	@Override
	public String getCalibInfo() {
		return CalibInfo;
	}

	public int getNumWeights() {
		return 3;
	}

	public double getRating(
		double[]	coefficients
	) {
		double		alpha = coefficients[0];
		double		beta = coefficients[1];
		double		gamma = coefficients[2];
		double		rmse = 0.0;
		double		fcst = 0.0;
		double		fcst_delta  = 0.0;
		double		level_p = 0.0;
		
		Level = Level0;
		Slope = Slope0;
		for (int i = 0; i < Cycle; i++)
			Season[i] = Season0[i];
		for (int i = 0; i < Series.length; i++) {
			fcst = Level*Slope*Season[i%Cycle];
			if (IFSStatistics.isUndef(fcst))
				fcst = 0.0;
			fcst_delta = Series[i]-fcst;
			rmse += fcst_delta*fcst_delta;
			level_p = Level;
			Level = alpha*(Series[i]/Season[i%Cycle])+(1.0-alpha)*(Level*Slope);
			Slope = beta*(Level/level_p)+(1.0-beta)*Slope;
			Season[i%Cycle] = gamma*(Series[i]/Level)+(1.0-gamma)*Season[i%Cycle];
		}
		rmse = Math.sqrt(rmse/Series.length);

		CalibInfo = String.format("rmse:%.3f,alpha:%.3f,beta:%.3f,gamma:%.3f",
			rmse, alpha, beta, gamma);
		
		return rmse;
	}

	public double getWeightLowerBound(
		int			weight_idx
	) {
		switch (weight_idx) {
		case 0:
			return AlphaMin;
		case 1:
			return BetaMin;
		default:
			return GammaMin;
		}
	}

	public double getWeightUpperBound(
		int			weight_idx
	) {
		switch (weight_idx) {
		case 0:
			return AlphaMax;
		case 1:
			return BetaMax;
		default:
			return GammaMax;
		}
	}

	public void init() {
        int     nreps = Series.length/Cycle;
        int     csize = nreps*Cycle;
        int     ssize = (nreps-1)*Cycle;

        for (int i = 0; i < csize; i++)
            Level0 += Series[i];
        Level0 /= csize;
        for (int i = 0; i < ssize; i++)
            Slope0 += Series[Cycle+i]/Series[i];
        Slope0 /= ssize;
        if (IFSStatistics.isZero(Level0))
        	Level0 = 1.0;

        for (int i = 0; i < csize; i++)
            Season0[i%Cycle] += Series[i]/Level0;
        for (int i = 0; i < Cycle; i++)
            Season0[i] /= Level0;
	}
}

/**
 * Class for implementing the multiplicative trend no seasonal adjustment
 * variation.
 */
private static final class ExpSmMultiplicativeNone extends ExpSmVariant {
	double		Level = 0.0;
	double		Level0 = 0.0;
	double		Slope = 0.0;
	double		Slope0 = 0.0;
	String		CalibInfo = "";
	
	@SuppressWarnings("unused")
	private ExpSmMultiplicativeNone() {}
	
	public ExpSmMultiplicativeNone(
		double[]	series,
		double[]	forecasts
	) {
		super(series, forecasts, 0);
		Model = this;
	}

	public String forecast() {
		for (int i = 0; i < Forecasts.length; i++) {
			Forecasts[i] = Level*Math.pow(Slope, i+1);
			if (IFSStatistics.isUndef(Forecasts[i]))
				Forecasts[i] = 0.0;
		}
		
		String	fcst_info = String.format("level:%.3f,slope:%.3f", Level, Slope);
		
		return fcst_info;
	}

	@Override
	public String getCalibInfo() {
		return CalibInfo;
	}

	public int getNumWeights() {
		return 2;
	}

	public double getRating(
		double[]	coefficients
	) {
		double		alpha = coefficients[0];
		double		beta = coefficients[1];
		double		rmse = 0.0;
		double		fcst = 0.0;
		double		fcst_delta  = 0.0;
		double		level_p = 0.0;
		
		Level = Level0;
		Slope = Slope0;
		for (int i = 0; i < Series.length; i++) {
			fcst = Level*Slope;
			if (IFSStatistics.isUndef(fcst))
				fcst = 0.0;
			fcst_delta = Series[i]-fcst;
			rmse += fcst_delta*fcst_delta;
			level_p = Level;
			Level = alpha*Series[i]+(1.0-alpha)*(Level*Slope);
			Slope = beta*(Level/level_p)+(1.0-beta)*Slope;
		}
		rmse = Math.sqrt(rmse/Series.length);

		CalibInfo = String.format("rmse:%.3f,alpha:%.3f,beta:%.3f",
			rmse, alpha, beta);
		
		return rmse;
	}

	public double getWeightLowerBound(
		int			weight_idx
	) {
		switch (weight_idx) {
		case 0:
			return AlphaMin;
		default:
			return BetaMin;
		}
	}

	public double getWeightUpperBound(
		int			weight_idx
	) {
		switch (weight_idx) {
		case 0:
			return AlphaMax;
		default:
			return BetaMax;
		}
	}

	public void init() {
		Level0 = Series[0];
		Slope0 = Series[1]/Series[0];
	}
}

/**
 * Class for implementing the no trend additive seasonal adjustment variation.
 */
private static final class ExpSmNoneAdditive extends ExpSmVariant {
	double		Level = 0.0;
	double		Level0 = 0.0;
	double[]	Season = null;
	double[]	Season0 = null;
	String		CalibInfo = "";
	
	@SuppressWarnings("unused")
	private ExpSmNoneAdditive() {}
	
	public ExpSmNoneAdditive(
		double[]	series,
		double[]	forecasts,
		int			cycle
	) {
		super(series, forecasts, cycle);
		Model = this;
		Season = new double[Cycle];
		Season0 = new double[Cycle];
	}

	public String forecast() {
		for (int i = 0; i < Forecasts.length; i++)
			Forecasts[i] = Level+Season[(Series.length+i)%Cycle];
		
		String	fcst_info = String.format("level:%.3f,cycle:%d", Level, Cycle);
		
		for (int i = 0; i < Season.length; i++) {
			fcst_info = String.format("%s,season%d:%.7f", fcst_info,
				i+1, Season[i]);
		}
		
		return fcst_info;
	}

	@Override
	public String getCalibInfo() {
		return CalibInfo;
	}

	public int getNumWeights() {
		return 2;
	}

	public double getRating(
		double[]	coefficients
	) {
		double		alpha = coefficients[0];
		double		gamma = coefficients[1];
		double		rmse = 0.0;
		double		fcst = 0.0;
		double		fcst_delta  = 0.0;
		
		Level = Level0;
		for (int i = 0; i < Cycle; i++)
			Season[i] = Season0[i];
		for (int i = 0; i < Series.length; i++) {
			fcst = Level+Season[i%Cycle];
			fcst_delta = Series[i]-fcst;
			rmse += fcst_delta*fcst_delta;
			Level = alpha*(Series[i]-Season[i%Cycle])+(1.0-alpha)*Level;
			Season[i%Cycle] = gamma*(Series[i]-Level)+(1.0-gamma)*Season[i%Cycle];
		}
		rmse = Math.sqrt(rmse/Series.length);

		CalibInfo = String.format("rmse:%.3f,alpha:%.3f,gamma:%.3f",
			rmse, alpha, gamma);
		
		return rmse;
	}

	public double getWeightLowerBound(
		int			weight_idx
	) {
		switch (weight_idx) {
		case 0:
			return AlphaMin;
		default:
			return GammaMin;
		}
	}

	public double getWeightUpperBound(
		int			weight_idx
	) {
		switch (weight_idx) {
		case 0:
			return AlphaMax;
		default:
			return GammaMax;
		}
	}

	public void init() {
		for (int i = 0; i < Cycle; i++)
			Level0 += Series[i];
		Level0 /= Cycle;

		for (int i = 0; i < Cycle; i++)
			Season0[i] = Series[i]-Level0;
	}
}

/**
 * Class for implementing the no trend multiplicative seasonal adjustment
 * variation.
 */
private static final class ExpSmNoneMultiplicative extends ExpSmVariant {
	double		Level = 0.0;
	double		Level0 = 0.0;
	double[]	Season = null;
	double[]	Season0 = null;
	String		CalibInfo = "";
	
	@SuppressWarnings("unused")
	private ExpSmNoneMultiplicative() {}
	
	public ExpSmNoneMultiplicative(
		double[]	series,
		double[]	forecasts,
		int			cycle
	) {
		super(series, forecasts, cycle);
		Model = this;
		Season = new double[Cycle];
		Season0 = new double[Cycle];
	}

	@Override
	public String getCalibInfo() {
		return CalibInfo;
	}

	public String forecast() {
		for (int i = 0; i < Forecasts.length; i++)
			Forecasts[i] = Level*Season[(Series.length+i)%Cycle];
		
		String	fcst_info = String.format("level:%.3f,cycle:%d", Level, Cycle);
		
		for (int i = 0; i < Season.length; i++) {
			fcst_info = String.format("%s,season%d:%.7f", fcst_info,
				i+1, Season[i]);
		}
		
		return fcst_info;
	}

	public int getNumWeights() {
		return 2;
	}

	public double getRating(
		double[]	coefficients
	) {
		double		alpha = coefficients[0];
		double		gamma = coefficients[1];
		double		rmse = 0.0;
		double		fcst = 0.0;
		double		fcst_delta  = 0.0;
		
		Level = Level0;
		for (int i = 0; i < Cycle; i++)
			Season[i] = Season0[i];
		for (int i = 0; i < Series.length; i++) {
			fcst = Level*Season[i%Cycle];
			fcst_delta = Series[i]-fcst;
			rmse += fcst_delta*fcst_delta;
			Level = alpha*(Series[i]/Season[i%Cycle])+(1.0-alpha)*Level;
			Season[i%Cycle] = gamma*(Series[i]/Level)+(1.0-gamma)*Season[i%Cycle];
		}
		rmse = Math.sqrt(rmse/Series.length);

		CalibInfo = String.format("rmse:%.3f,alpha:%.3f,gamma:%.3f",
			rmse, alpha, gamma);
		
		return rmse;
	}

	public double getWeightLowerBound(
		int			weight_idx
	) {
		switch (weight_idx) {
		case 0:
			return AlphaMin;
		default:
			return GammaMin;
		}
	}

	public double getWeightUpperBound(
		int			weight_idx
	) {
		switch (weight_idx) {
		case 0:
			return AlphaMax;
		default:
			return GammaMax;
		}
	}

	public void init() {
		int		nreps = Series.length/Cycle;
		int		csize = nreps*Cycle;
		
		for (int i = 0; i < csize; i++)
			Level0 += Series[i];
		Level0 /= csize;
        if (IFSStatistics.isZero(Level0))
        	Level0 = 1.0;

		for (int i = 0; i < csize; i++)
			Season0[i%Cycle] += Series[i];
		for (int i = 0; i < Cycle; i++)
			Season0[i] /= Level0;
	}
}

/**
 * Class for implementing the no trend no seasonal adjustment variation.
 */
private static final class ExpSmNoneNone extends ExpSmVariant {
	double		Level = 0.0;
	double		Level0 = 0.0;
	String		CalibInfo = "";
	
	@SuppressWarnings("unused")
	private ExpSmNoneNone() {}
	
	public ExpSmNoneNone(
		double[]	series,
		double[]	forecasts
	) {
		super(series, forecasts, 0);
		Model = this;
	}

	public String forecast() {
		for (int i = 0; i < Forecasts.length; i++)
			Forecasts[i] = Level;
		
		String	fcst_info = String.format("level:%.3f", Level);
		
		return fcst_info;
	}

	@Override
	public String getCalibInfo() {
		return CalibInfo;
	}

	public int getNumWeights() {
		return 1;
	}

	public double getRating(
		double[]	coefficients
	) {
		double		alpha = coefficients[0];
		double		rmse = 0.0;
		double		fcst = 0.0;
		double		fcst_delta  = 0.0;
		
		Level = Level0;
		for (int i = 0; i < Series.length; i++) {
			fcst = Level;
			fcst_delta = Series[i]-fcst;
			rmse += fcst_delta*fcst_delta;
			Level = alpha*Series[i]+(1.0-alpha)*Level;
		}
		rmse = Math.sqrt(rmse/Series.length);

		CalibInfo = String.format("rmse:%.3f,alpha:%.3f", rmse, alpha);
		
		return rmse;
	}

	public double getWeightLowerBound(
		int			weight_idx
	) {
		return AlphaMin;
	}

	public double getWeightUpperBound(
		int			weight_idx
	) {
		return AlphaMax;
	}

	public void init() {
		Level0 = Series[0];
	}
}

}
