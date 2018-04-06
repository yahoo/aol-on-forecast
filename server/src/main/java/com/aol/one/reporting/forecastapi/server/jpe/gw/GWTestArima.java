/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.jpe.gw;

import java.io.*;
import java.util.*;

/**
 * Class for testing GW implementation. It tests derivation of a the following
 * ARIMA model given a time series:
 *
 * If the seasonal cycle is 0 or 1:
 *
 * ARIMA(0,1,1):
 *    Y(t)' = Y(t-1)-c(1)e(t-1)
 *
 * If the seasonal cycle is greater than 1:
 *
 * ARIMA(0,1,1)(0,1,1)cycle:
 *    Y(t)' = Y(t-1)+Y(t-cycle)-Y(t-cycle-1)-c(1)e(t-1)-c(2)e(t-cycle)
 *            +c(1)c(2)e(t-cycle-1)
 *
 * c(1) and c(2) coefficients are derived that minimize the RMSE for the
 * 1-step ahead forecast.
 * 
 * @author Copyright &copy; 2012 John Eldreth All rights reserved.
 */
public final class GWTestArima implements GWInterface {
    private static final double		CoefficientLow = -1.0;
    private static final double		CoefficientHigh = 1.0;
    private static final double[]	CoefficientStepSizes = {0.1, 0.01, 0.001};
    private static final int		NumCoefficientsCycle = 2;
    private static final int		NumCoefficientsNoCycle = 1;
    private double[]				TimeSeries = null;
    private double[]        		FitForecasts = null;
    private int             		Cycle = 0;
    private double[]        		Coefficients = null;
    private double          		RMSE = 0.0;
    private double          		MAPE = 0.0;
    private double          		AIC = 0.0;
    private long            		FitTime = 0;
    private int						NumIterations = 0;

    /**
     * Construct a GWTestArima object which fits the test ARIMA model to the
     * time series data. The coefficients for the fit model are derived. 
     * 
     * @param time_series The time series to fit with the ARIMA model.
     * @param cycle       Seasonal cycle. If value is 0 or 1, no seasonal terms
     *                    will be used.
     * @param is_trace    Whether walk is to be traced (true) or not (false).
     * 
     * @throws GWException If there are problems with the parameter values.
     */
    public GWTestArima(
        double[]    time_series,
        int         cycle,
        boolean		is_trace
    ) throws GWException {
        if (time_series == null || time_series.length < 2)
            throw new GWException("A time series must have at least 2 "
            +   "elements.");
        if (cycle < 0)
            throw new GWException("The seasonal cycle must be greater than "
            +   "or equal to 0.");
        
        TimeSeries = time_series;
        Cycle = cycle;
        FitForecasts = new double[TimeSeries.length];

        long        stime = System.currentTimeMillis();
        GW          gw = new GW(this, is_trace, System.out);

        NumIterations = gw.getNumIterations();

        Coefficients = gw.getOptWeights();
        execARIMA(Cycle, Coefficients, TimeSeries, FitForecasts);
        execAIC(Coefficients.length);
        execMAPE();
        execRMSE();

        long        etime = System.currentTimeMillis();

        FitTime = etime-stime;
    }
    
    /**
     * Fetch AIC of the fit ARIMA model.
     * 
     * @return AIC
     */
    public double getAIC() {
        return AIC;
    }
    
    /**
     * Fetch coefficients of the fit ARIMA model.
     * 
     * @return Coefficients used to fit the ARIMA model.
     */
    public double[] getCoefficients() {
        return Coefficients;
    }

    /**
     * Fetch seasonal cycle.
     *
     * @return Seasonal cycle.
     */
    public int getCycle() {
        return Cycle;
    }

    /**
     * Fetch forecasts generated in model calibration.
     *
     * @return Model calibration forecasts.
     */
    public double[] getFitForecasts() {
        return FitForecasts;
    }

    /**
     * Fetch time to calibrate model.
     *
     * @return Model calibration time (ms).
     */
    public long getFitTime() {
        return FitTime;
    }

    /**
     * Generate requested forecasts.
     *
     * @return Generated forecasts.
     *
     * @throws GWException If number of forecasts is negative.
     */
    public double[] getForecasts(
        int         n_forecasts
    ) throws GWException {
        if (n_forecasts < 0)
            throw new GWException("Number of forecasts must be greater than "
            +   "or equal to 0.");

        if (n_forecasts == 0)
            return new double[0];

        double[]    forecasts = new double[TimeSeries.length+n_forecasts];

        execARIMA(Cycle, Coefficients, TimeSeries, forecasts);

        double[]    ret_forecasts = new double[n_forecasts];

        System.arraycopy(forecasts, TimeSeries.length,
            ret_forecasts, 0, n_forecasts);

        return(ret_forecasts);
    }
    
    /**
     * Fetch MAPE of the fit ARIMA model.
     * 
     * @return MAPE
     */
    public double getMAPE() {
        return MAPE;
    }

    /**
     * Fetch the number of iterations actually executed to find the
     * optimal value.
     *
     * @return Number of iterations executed.
     */
    public int getNumIterations() {
        return NumIterations;
    }

    /**
     * Fetch the number of coefficient significant digits.
     * 
     * @return The number of coefficient significant digits.
     */
	@Override
	public int getNumLevels() {
		return CoefficientStepSizes.length;
	}


    /**
     * Fetch the number of coefficients. Depends on whether a cycle or not/.
     * 
     * @return The number of coefficients.
     */
	@Override
	public int getNumWeights() {
		if (Cycle > 1)
			return NumCoefficientsCycle;
		else
			return NumCoefficientsNoCycle;
	}

    /**
     * Rate the given coefficients with respect to how well it fits the ARIMA
     * model to the data.
     * 
     * @param coefficients ARIMA coefficients to rate.
     *
     * @return Coefficient rating.
     */
    public double getRating(
    	double[]	coefficients
    ) {
        execARIMA(Cycle, coefficients, TimeSeries, FitForecasts);
        execAIC(coefficients.length);
        return AIC;
    }
    
    /**
     * Fetch RMSE of the fit ARIMA model.
     * 
     * @return RMSE
     */
    public double getRMSE() {
        return RMSE;
    }
    
    /**
     * Fetch coefficient precision depending on current optimization level.
     * 
     * @param level Current optimization level.
     * @param weight_idx N/A.
     * 
     * @return Coefficient precision.
     * 
     */
	@Override
	public double getStepSize(
		int		level,
		int		weight_idx
	) {
		return CoefficientStepSizes[level];
	}

    /**
     * Fetch original time series.
     *
     * @return Time series.
     */
    public double[] getTimeSeries() {
        return TimeSeries;
    }

    /**
     * Fetch coefficient lower bound.
     *
     * @return Coefficient lower bound.
     */
	@Override
	public double getWeightLowerBound(
		int			weight_idx
	) {
		return CoefficientLow;
	}

    /**
     * Fetch coefficient upper bound.
     *
     * @return Coefficient upper bound.
     */
	@Override
	public double getWeightUpperBound(
		int			weight_idx
	) {
		return CoefficientHigh;
	}

    /**
     * Execute ARIMA test described in class definition.
     *
     * @param args -- A file name containing the time series to test or 2
     *                dashes indicating standard input. Also calibration and
     *                forecasting parameters are specified.
     */
    public static void main(
        String[]    args
    ) {
        if (args.length != 4) {
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

        int                     cycle = 0;

        try {
        cycle = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException ex) {
            System.err.println("Unexpected seasonal cycle specification. "
            +   ex.getMessage());
            System.exit(1);
        }

        int                     n_forecasts = 0;

        try {
        n_forecasts = Integer.parseInt(args[2]);
        }
        catch (NumberFormatException ex) {
            System.err.println("Unexpected number of forecasts specification. "
            +   ex.getMessage());
            System.exit(1);
        }

        double[]                ts = new double[time_series.size()];
        int                     i = 0;

        for (Double ts_value : time_series)
            ts[i++] = ts_value.doubleValue();
		
		String					s_is_trace = args[3].toLowerCase();
		Boolean					is_trace = null;
		
		if (!s_is_trace.equals("true") && !s_is_trace.equals("false")) {
			System.err.printf("**Error** Is-Trace argument must be 'true' "
			+ "or 'false'.\n");
			System.exit(1);
		}
		is_trace = Boolean.parseBoolean(s_is_trace);

        GWTestArima             gwts = null;
        double[]                gfcsts = null;
        try {
        gwts = new GWTestArima(ts, cycle, is_trace);
        gfcsts = gwts.getForecasts(n_forecasts);
        }
        catch (GWException ex){
            System.err.println("Unexpected error occurred in trying to "
            +   "fit ARIMA to time series. "
            +   ex.getMessage());
            System.exit(1);
        }

        double[]                tss = gwts.getTimeSeries();
        double[]                fcsts = gwts.getFitForecasts();

        System.out.printf("\n");
        System.out.printf("       Time Series     Fit Forecasts\n");
        System.out.printf("       --------------  --------------\n");
        for (i = 0; i < tss.length; i++)
            System.out.printf("%5d. %14.2f  %14.2f\n", i+1,
                tss[i], fcsts[i]);

        System.out.printf("\n");
        System.out.printf("       Coefficients\n");
        System.out.printf("       ------------\n");
        i = 0;
        for (Double cf_value : gwts.getCoefficients())
            System.out.printf("%5d. %12.4f\n", 1+i++, cf_value);

        System.out.printf("\n");
        System.out.printf("Seasonal Cycle  : %14d\n", gwts.getCycle());
        System.out.printf("Act Number Iters: %14d\n", gwts.getNumIterations());
        System.out.printf("AIC             : %14.2f\n", gwts.getAIC());
        System.out.printf("RMSE            : %14.2f\n", gwts.getRMSE());
        System.out.printf("MAPE            : %14.2f\n", gwts.getMAPE());
        System.out.printf("Fit Time        : %14d\n", gwts.getFitTime());

        System.out.printf("\n");
        System.out.printf("       Forecasts    \n");
        System.out.printf("       --------------\n");
        for (i = 0; i < gfcsts.length; i++)
            System.out.printf("%5d. %14.2f\n", i+1, gfcsts[i]);

        System.exit(0);
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
	@Override
    public void printTrace(
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
    
/*******************/
/* Private Methods */
/*******************/

    /**
     * Default constructor hidden.
     */
    @SuppressWarnings("unused")
	private GWTestArima() {}

    /**
     * Generate forecasts for a time series using the specified coefficients.
     *
     * @param cycle         Seasonal cycle.
     * @param coefficients  Coefficient values to use.
     * @param time_series   Time series.
     * @param forecasts     Generated forecasts.
     */
    private static void execARIMA(
        int         cycle,
        double[]    coefficients,
        double[]    time_series,
        double[]    forecasts
    ) {
        forecasts[0] = 0.0;

        /* If the seasonal cycle is 0 or 1:
         *
         * ARIMA(0,1,1):
         *    Y(t)' = Y(t-1)-c(1)e(t-1)
         */
        if (cycle <= 1) {
            for (int i = 1; i < forecasts.length; i++)
                forecasts[i] = getTimeSeriesValue(i-1, time_series, forecasts)
                    -coefficients[0]*(getTimeSeriesValue(i-1, time_series,
                    forecasts)-forecasts[i-1]);
            return;
        }

        /* If the seasonal cycle is greater than 1:
         *
         * ARIMA(0,1,1)(0,1,1)cycle:
         *    Y(t)' = Y(t-1)+Y(t-cycle)-Y(t-cycle-1)-c(1)e(t-1)-c(2)e(t-cycle)
         *            +c(1)c(2)e(t-cycle-1)
         * 
         * Rewrite equation as:
         *    Y(t)' = Term(1) + Term(2) + Term(3)
         *    where:
         *       Term(1) = Y(t-1)-c(1)e(t-1)
         *       Term(2) = if (t <= cycle) then 0.0
         *                 else Y(t-cycle)-c(2)e(t-cycle)
         *       Term(3) = if (t <= (cycle+1)) then 0.0
         *                 else -Y(t-cycle-1)+c(1)c(2)e(t-cycle-1)
         */
        double      t1 = 0.0;
        double      t2 = 0.0;
        double      t3 = 0.0;

        for (int i = 1; i < forecasts.length; i++) {
            t1 = getTimeSeriesValue(i-1, time_series, forecasts)
                -coefficients[0]*(getTimeSeriesValue(i-1, time_series,
                forecasts)-forecasts[i-1]);
            if (i < cycle)
                t2 = 0.0;
            else
                t2 = getTimeSeriesValue(i-cycle, time_series, forecasts)
                    -coefficients[1]*(getTimeSeriesValue(i-cycle, time_series,
                    forecasts)-forecasts[i-cycle]);
            if (i < (cycle+1))
                t3 = 0.0;
            else
                t3 = -getTimeSeriesValue(i-cycle-1, time_series, forecasts)
                    +coefficients[0]*coefficients[1]
                    *(getTimeSeriesValue(i-cycle-1, time_series, forecasts)
                    -forecasts[i-cycle-1]);
            forecasts[i] = t1 + t2 + t3;
        }
    }

    /**
     * Generate Akaike Information Criterion (AIC) for calibration forecasts
     * against time series.
     * 
     * @param num_parms Number of estimated parameters.
     */
    private void execAIC(
    	int		num_parms
    ) {
    	double  rss = 0.0;

    	AIC = 2*num_parms;
    	for (int i = 0; i < TimeSeries.length; i++) {
    		rss += (FitForecasts[i]-TimeSeries[i])*(FitForecasts[i]-TimeSeries[i]);
    	}
    	AIC += TimeSeries.length*Math.log(rss/TimeSeries.length);
    }

    /**
     * Generate mean absolute percentage error for calibration forecasts
     * against time series.
     */
    private void execMAPE() {
        double      mape = 0.0;
        double      delta = 0.0;
        double      count = 0;
        int         i;

        for (i = 0; i < TimeSeries.length; i++)
                if (Math.abs(TimeSeries[i]) > Double.MIN_VALUE) {
                    delta = Math.abs(TimeSeries[i]-FitForecasts[i]);
                    mape += Math.abs(delta/TimeSeries[i]);
                    count++;
                }
        if (count <= 0)
            MAPE = 0.0;
        else
            MAPE = 100.0*mape/(double)count;
    }

    /**
     * Generate root mean squared error for calibration forecasts
     * against time series.
     */
    private void execRMSE() {
        double      sumsq = 0.0;
        double      delta = 0.0;
        int         i;

        for (i = 0; i < TimeSeries.length; i++) {
            delta = TimeSeries[i]-FitForecasts[i];
            sumsq += delta*delta;
        }
        RMSE = Math.sqrt(sumsq/(double)TimeSeries.length);
    }

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
            return(in);
        }

        in = new BufferedReader(new InputStreamReader(
                new FileInputStream(file_name)));
        return(in);
    }

    /**
     * Fetch time series value if index is in time series range. Otherwise,
     * a previous forecast value is returned.
     *
     * @param idx         Value index.
     * @param time_series Time series values.
     * @param forecasts   Forecast values. This vector is assumed to have
     *                    extra values beyond the time series to cover all
     *                    value indices.
     *
     * @return            The time series value if the value index is in that
     *                    range. Values beyond that range are taken from the
     *                    forecast vector.
     */
    private static double getTimeSeriesValue(
        int         idx,
        double[]    time_series,
        double[]    forecasts
    ) {
        if (0 <= idx && idx < time_series.length)
            return(time_series[idx]);
        else
            return(forecasts[idx]);
    }

    /**
     * Print main program usage information.
     */
    private static void usage() {
        System.err.print(
            "GWTestArima -- Test grid walk algorithm on the following ARIMA time\n"
        +   "   series equation:\n"
        +   "\n"
        +   "If the seasonal cycle is 0 or 1:\n"
        +   "\n"
        +   "ARIMA(0,1,1):\n"
        +   "   Y(t)' = Y(t-1)-c(1)e(t-1)\n"
        +   "\n"
        +   "If the seasonal cycle is greater than 1:\n"
        +   "\n"
        +   "ARIMA(0,1,1)(0,1,1)cycle:\n"
        +   "   Y(t)' = Y(t-1)+Y(t-cycle)-Y(t-cycle-1)-c(1)e(t-1)-c(2)e(t-cycle)\n"
        +   "           +c(1)c(2)e(t-cycle-1)\n"
        +   "\n"
        +   "usage: java GWTestArima <-- | file name> <seasonal cycle>\n"
        +   "            <number of forecasts> <is trace>\n"
        +	"\n"
        +	"<is trace> -- 'true' if a calibration trace is desired. 'false'\n"
        +	"otherwise.\n"
        +   "\n"
        +   "Data is read from standard input if the file name is '--'.\n"
        +   "Otherwise data is read from the specified file name. The input\n"
        +   "must have one time series value per line.\n"
        +   "\n"
        +   "Output consists of the time series followed by the coefficients\n"
        +   "that minimize sum of squares of errors. Next output are the\n"
        +   "forecasts. RMSE, MAPE, etc values are also printed.\n"
        );
    }
}
