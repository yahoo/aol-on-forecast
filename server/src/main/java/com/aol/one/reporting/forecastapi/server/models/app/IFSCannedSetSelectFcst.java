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
import com.aol.one.reporting.forecastapi.server.models.util.GetTimeSeries;
import com.aol.one.reporting.forecastapi.server.models.util.Timer;

/**
 * Class implementing canned set selection followed by time series forecast
 * using the selected canned set.
 */
public final class IFSCannedSetSelectFcst {

	/**
	 * Main program for running canned set selection based forecast. Arguments
	 * are a file containing canned set definitions, file containing canned set
	 * candidate list, time series file, whether time series is in a declining
	 * profit center, number of selection hold back values, and number of
	 * forecasts. Printed is timing, canned set selection, and forecasts.
	 * 
	 * @param args Command arguments.
	 */
	public static void main(
		String[]	args
	) {
		if (args.length != 6) {
			usage();
			System.exit(0);
		}
		
		String								cs_defn_file = args[0];
		String								cs_cand_file = args[1];
		String								ts_file = args[2];
		String								dpc_flag_s = args[3];
		String								num_hbv_s = args[4];
		String								num_fcst_s = args[5];
		Map<String, IFSCannedSet>			cs_defn = null;
		List<IFSCannedSet>					cs_cand = null;
		double[]							ts = null;
		IFSCannedSetSelectionConstraints	cnstrnts = null;
		IFSCannedSetSelectionContext		cntxt = null;
		List<Integer>						dpcs = null;
		boolean								dpc_flag = false;
		int									num_hbv = 0;
		int									num_fcst = 0;
		Timer								timer = new Timer();
		IFSCannedSet						cs_select = null;
        IFSModel							model = null;
        double[]							fcsts = null;
		
		try {
		cs_defn = GetCannedSetDefinitions.getCannedSetDefinitions(cs_defn_file);
		cs_cand = GetCannedSetCandidates.getCannedSetCandidates(cs_cand_file, cs_defn);
		ts = GetTimeSeries.getTimeSeries(ts_file);
		
		dpc_flag = Boolean.parseBoolean(dpc_flag_s);
		dpcs = new ArrayList<Integer>();
		if (dpc_flag)
			dpcs.add(1);
		
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
		
		cnstrnts = new IFSCannedSetSelectionConstraints();
		cnstrnts.setCannedSetDecline(cs_defn.get("AR-NONE-NONE"));
		cnstrnts.setCannedSetNoneNew(cs_defn.get("AVG-NONE-28-NEW"));
		cnstrnts.setCannedSetWeekNew(cs_defn.get("REG-NONE-ADD-AUTO-NEW"));
	    cnstrnts.setProfitCentersDecline(dpcs);
		
	    cntxt = new IFSCannedSetSelectionContext();
	    cntxt.setSeries(ts, num_hbv);
	    cntxt.setID(1);
	    cntxt.setProfitCenter(1);
	    cntxt.setCannedSetCandidates(cs_cand);
	    
        timer.start();
	    cs_select = IFSCannedSetSelection.selectCannedSet(cnstrnts, cntxt);
        model = IFSModelFactory.create(cs_select.getParameterSpec().getModel());
        IFSModelFactory.setup(model, ts,
        	cs_select.getParameterSpec().getParameterValues());
        model.generateForecasts(num_fcst);
        fcsts = model.getForecasts();
        timer.stop();
	    
		} catch (IFSException ex) {
			System.err.format("** Error ** %s\n", ex.getMessage());
			System.exit(1);
		}
		
        System.out.format("\n");
	    System.out.format("Canned Set Selection:  %s\n", cs_select.getName());
	    System.out.printf("Timing (ms)         :  %.3f\n",
	    	timer.getTimeMilliSeconds());
        System.out.printf("Forecasts           :\n");
        for (int i = 0; i < fcsts.length; i++)
        	System.out.printf("%4d.  %10d\n", i+1, Math.round(fcsts[i]));
		
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
+ "Perform a time series forecast based on canned set selection as defined by\n"
+ "a canned set definitions file, a candidate canned set file, a time series\n"
+ "file, a declining profit center flag, number of selection hold back values,\n"
+ "and a number of forecasts.\n"
+ "\n"
+ "Usage: IFSCannedSetSelectFcst\n"
+ "          <canned set definitions file>\n"
+ "          <candidate canned set file>\n"
+ "          <time series file | -->\n"
+ "          <declining profit center flag>\n"
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
+ "time series file -- This is a file containing the time series to evaluate\n"
+ "   in determining the canned set selection. Each line in the file contains\n"
+ "   the next value in the series ordered from least recent to most recent\n"
+ "   value. A '--' indicates reading the time series from stdin. An example\n"
+ "   of series values is:\n"
+ "\n"
+ "   15010251\n"
+ "   14208450\n"
+ "   13122392\n"
+ "   13726736\n"
+ "   15134212\n"
+ "   15275474\n"
+ "   15218248\n"
+ "\n"
+ "declining profit centers flag -- A 'true' indicates the time series is from\n"
+ "   a declining profit center. A 'false' indicates the time series is not from\n"
+ "   a declining profitc center.\n"
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
