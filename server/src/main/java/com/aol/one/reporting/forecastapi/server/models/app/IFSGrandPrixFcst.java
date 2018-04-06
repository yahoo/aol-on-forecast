/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.models.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.aol.one.reporting.forecastapi.server.models.cs.GetCannedSetCandidates;
import com.aol.one.reporting.forecastapi.server.models.cs.GetCannedSetDefinitions;
import com.aol.one.reporting.forecastapi.server.models.cs.IFSCannedSet;
import com.aol.one.reporting.forecastapi.server.models.cs.IFSCannedSetSelection;
import com.aol.one.reporting.forecastapi.server.models.cs.IFSCannedSetSelectionConstraints;
import com.aol.one.reporting.forecastapi.server.models.cs.IFSCannedSetSelectionContext;
import com.aol.one.reporting.forecastapi.server.models.model.IFSException;
import com.aol.one.reporting.forecastapi.server.models.model.IFSModel;
import com.aol.one.reporting.forecastapi.server.models.model.IFSModelFactory;
import com.aol.one.reporting.forecastapi.server.models.util.GetTimeSeriesFiles;
import com.aol.one.reporting.forecastapi.server.models.util.Timer;

/**
 * Class implementing canned set selection followed by time series forecast
 * using the selected canned set for a collection of files containing time
 * series. A total timing is printed along with a summary of each time series
 * forecast.
 */
public final class IFSGrandPrixFcst {

	/**
	 * Main program for running a collection of canned set selection based
	 * forecasts. Arguments are a file containing canned set definitions, file
	 * containing canned set candidate list, file of time series files, number
	 * of selection hold back values, and number of forecasts. Printed is
	 * timing and forecast summaries.
	 * 
	 * @param args Command arguments.
	 */
	public static void main(
		String[]	args
	) {
		if (args.length != 5) {
			usage();
			System.exit(0);
		}
		
		String								cs_defn_file = args[0];
		String								cs_cand_file = args[1];
		String								ts_files = args[2];
		String								num_hbv_s = args[3];
		String								num_fcst_s = args[4];
		Map<String, IFSCannedSet>			cs_defn = null;
		List<IFSCannedSet>					cs_cand = null;
		double[][]							tss = null;
		IFSCannedSetSelectionConstraints	cnstrnts = null;
		IFSCannedSetSelectionContext		cntxt = null;
		List<Integer>						dpcs = null;
		int									num_hbv = 0;
		int									num_fcst = 0;
		Timer								timer_fcst = new Timer();
		Timer								timer_tot = new Timer();
		IFSCannedSet						cs_select = null;
        IFSModel							model = null;
        double[][]							fcsts = null;
        double[]							fcst_timings = null;
        String[]							fcst_csids = null;
        double								fcst_sum = 0.0;
		
		try {
		cs_defn = GetCannedSetDefinitions.getCannedSetDefinitions(cs_defn_file);
		cs_cand = GetCannedSetCandidates.getCannedSetCandidates(cs_cand_file, cs_defn);
		tss = GetTimeSeriesFiles.getTimeSeriesFiles(ts_files);
		dpcs = new ArrayList<Integer>();
		
		try {
		num_hbv = Integer.parseInt(num_hbv_s);
		} catch (NumberFormatException ex) {
			throw new IFSException(String.format("Number hold back values '%s' "
			+ "is not an integer.", num_hbv_s));
		}
		if (num_hbv < 2) {
			throw new IFSException(String.format("Number hold back values %d "
			+ "is not at least 2.", num_hbv));
		}
		
		try {
		num_fcst = Integer.parseInt(num_fcst_s);
		} catch (NumberFormatException ex) {
			throw new IFSException(String.format("Number forecast values '%s' "
			+ "is not an integer.", num_fcst_s));
		}
		if (num_fcst < 1) {
			throw new IFSException(String.format("Number forecast values %d "
			+ "is not at least 1.", num_fcst));
		}
		fcsts = new double[tss.length][];
		fcst_timings = new double[fcsts.length];
		fcst_csids = new String[fcsts.length];
		
		cnstrnts = new IFSCannedSetSelectionConstraints();
		cnstrnts.setCannedSetDecline(cs_defn.get("AR-NONE-NONE"));
		cnstrnts.setCannedSetNoneNew(cs_defn.get("AVG-NONE-28-NEW"));
		cnstrnts.setCannedSetWeekNew(cs_defn.get("REG-NONE-ADD-AUTO-NEW"));
	    cnstrnts.setProfitCentersDecline(dpcs);
		
	    cntxt = new IFSCannedSetSelectionContext();
	    cntxt.setID(1);
	    cntxt.setProfitCenter(1);
	    cntxt.setCannedSetCandidates(cs_cand);
	    
        timer_tot.start();
        for (int i = 0; i < fcsts.length; i++) {
        	timer_fcst.start();
    	    cntxt.setSeries(tss[i], num_hbv);
        	cs_select = IFSCannedSetSelection.selectCannedSet(cnstrnts, cntxt);
        	model = IFSModelFactory.create(cs_select.getParameterSpec().getModel());
        	IFSModelFactory.setup(model, tss[i],
        			cs_select.getParameterSpec().getParameterValues());
        	model.generateForecasts(num_fcst);
        	fcsts[i] = model.getForecasts();
        	timer_fcst.stop();
        	fcst_timings[i] = timer_fcst.getTimeMilliSeconds();
        	fcst_csids[i] = cs_select.getName();
        }
        timer_tot.stop();
	    
		} catch (IFSException ex) {
			System.err.format("** Error ** %s\n", ex.getMessage());
			System.exit(1);
		}
		
        System.out.format("\n");
	    System.out.printf("Timing Total (ms):  %.3f\n",
	    	timer_tot.getTimeMilliSeconds());
        System.out.printf("Forecasts        :\n");
        for (int i = 0; i < fcsts.length; i++) {
        	fcst_sum = 0.0;
        	for (int j = 0; j < fcsts[i].length; j++)
        		fcst_sum += fcsts[i][j];
        	System.out.printf("%5d.  %-25.25s  %14d  %10.3f\n",
        		i+1, fcst_csids[i], Math.round(fcst_sum), fcst_timings[i]);
        }
		
		System.exit(0);
	}

/*******************/
/* Private Methods */
/*******************/
	
	/**
	 * Prints usage for the main program.
	 */
	private static void usage() {
		System.out.print(
  "\n"
+ "Perform a collection of time series forecasts based on a canned set selection\n"
+ "as defined by a canned set definitions file, a candidate canned set file, a\n"
+ "file of time series file paths, number of selection hold back values, and a\n"
+ "number of forecasts.\n"
+ "\n"
+ "Usage: IFSGrandPrixFcst\n"
+ "          <canned set definitions file>\n"
+ "          <candidate canned set file>\n"
+ "          <file of time series files>\n"
+ "          <number of selection hold back values>\n"
+ "          <number of forecasts>\n"
+ "\n"
+ "canned set definitions file -- Path to file containing canned set\n"
+ "   definitions. Each line in the file defines a parameter and its value\n"
+ "   for a specific canned set. Following are some example lines.\n"
+ "\n"
+ "   AR-NONE-NONE     model         model_ar\n"
+ "   AR-NONE-NONE     ndays_back    z.10\n"
+ "   AR-NONE-NONE     spike_filter  14\n"
+ "   AVG-NONE-14      model         model_movavg\n"
+ "   AVG-NONE-14      ndays_back    z.10\n"
+ "   AVG-NONE-14      spike_filter  14\n"
+ "   AVG-NONE-14      window        14\n"
+ "   AVG-NONE-14-NEW  model         model_movavg\n"
+ "   AVG-NONE-14-NEW  spike_filter  3\n"
+ "   AVG-NONE-14-NEW  window        14\n"
+ "\n"
+ "   Note that each canned set must have a special parameter 'model'\n"
+ "   indicating the model to be used.\n"
+ "\n"
+ "   The following canned sets must be defined as they are built into the\n"
+ "   selection logic: AR-NONE-NONE,AVG-NONE-14-NEW,EXP-NONE-ADD-WEEK-NEW.\n"
+ "\n"
+ "candidate canned set file -- This is a list of candidate canned sets. Each\n"
+ "   candidate name must exist in the canned set definitions file. Also note\n"
+ "   there are built-in canned sets used by the selection logic as described\n"
+ "   under the canned set definitions file. An example list of candidate\n"
+ "   canned sets is:\n"
+ "\n"
+ "   EXP-DA-ADD-AUTO\n"
+ "   EXP-DA-NONE\n"
+ "   EXP-DM-MULT-YEAR\n"
+ "   EXP-NONE-ADD-AUTO\n"
+ "   REG-NONE-CONST2-AUTO\n"
+ "   REG-NONE-PHASE2-WEEK-YEAR\n"
+ "\n"
+ "file of time series files -- This is a file containing file paths to the time\n"
+ "   series to evaluate in determining the canned set selection. Each line in\n"
+ "   the file contains the file path to a time series file. Each line in the\n"
+ "   time series file contains the next value in the series ordered from least\n"
+ "   recent to most recent value. An example of series values is:\n"
+ "\n"
+ "   15010251\n"
+ "   14208450\n"
+ "   13122392\n"
+ "   13726736\n"
+ "   15134212\n"
+ "   15275474\n"
+ "   15218248\n"
+ "\n"
+ "number of selection hold back values -- Number of values to hold back from\n"
+ "   the end of the time series. A forecast is made from the values preceding\n"
+ "   those for the same length. The root mean squared error (RMSE) is computed\n"
+ "   for the pairs and the canned set with the minimum RMSE is selected. An\n"
+ "   example value for this value is 30.\n"
+ "\n"
+ "number of forecasts -- Number of forecasts to produce.\n"
+ "\n"
		);
	}
}
