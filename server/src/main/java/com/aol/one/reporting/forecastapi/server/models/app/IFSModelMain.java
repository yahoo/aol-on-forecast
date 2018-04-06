/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.models.app;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import com.aol.one.reporting.forecastapi.server.models.model.IFSCycle;
import com.aol.one.reporting.forecastapi.server.models.model.IFSException;
import com.aol.one.reporting.forecastapi.server.models.model.IFSModel;
import com.aol.one.reporting.forecastapi.server.models.model.IFSModelFactory;
import com.aol.one.reporting.forecastapi.server.models.model.IFSNDaysBack;
import com.aol.one.reporting.forecastapi.server.models.model.IFSParameterValue;
import com.aol.one.reporting.forecastapi.server.models.model.IFSSpikeFilter;
import com.aol.one.reporting.forecastapi.server.models.model.IFSTransformType;
import com.aol.one.reporting.forecastapi.server.models.util.GetTimeSeries;
import com.aol.one.reporting.forecastapi.server.models.util.Timer;

/**
 * Class implementing main program for executing forecast models from the
 * command line. Essentially one specifies the source of the time series
 * (i.e. a file or standard input), the number of forecasts to generate,
 * a model name, and model parameters (if any). The output is the historical
 * data followed by the forecasts. Model parameter are of the form key=value.
 */
public final class IFSModelMain {

	/**
	 * Main program for executing forecast models.
	 * 
	 * @param args Forecast data and model specification.
	 */
	public static void main(
		String[]	args
	) {
		if (args.length < 3) {
			usage();
			System.exit(0);
		}
		
		// Fetch the time series from a file or standard input.
		
        double[]                ts = null;

        try {
        ts = GetTimeSeries.getTimeSeries(args[0]);
        }
        catch (IFSException ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        }
       
        // Fetch the number of forecasts to generate.
        
        int                     num_forecasts = 0;
        try {
        num_forecasts = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException ex) {
        	System.err.println("Number of forecasts has the wrong format.");
        	System.exit(1);
        }
        if (num_forecasts < 1) {
        	System.err.println("Number of forecasts must be at least 1.");
        	System.exit(1);
        }
        
        // Hang onto the model name.
        
        String					model_name = args[2];
        
        // Fetch any model parameters that have been specified.
        
        List<IFSParameterValue>	parameters = new LinkedList<IFSParameterValue>();
        StringTokenizer			tokens = null;
        String					key = null;
        String					val = null;
        
        for (int i = 3; i < args.length; i++) {
        	tokens = new StringTokenizer(args[i], "=");
        	if (tokens.countTokens() != 2) {
        		System.err.println("Model parameter '"
        		+ args[i]
        		+ "' is not in <key>=<value> format.");
        		System.exit(1);
        	}
        	key = tokens.nextToken().trim();
        	val = tokens.nextToken().trim();
        	parameters.add(new IFSParameterValue(key, val));
        }
        
        // Manufacture the appropriate model if everything has been specified
        // correctly.
        
        IFSModel				model = null;
        double[]				forecasts = null;
        String					calib_info = null;
        Timer					timer = new Timer();
        
        try {
        model = IFSModelFactory.create(model_name);
        IFSModelFactory.setup(model, ts, parameters);
        timer.start();
        calib_info = model.generateForecasts(num_forecasts);
        forecasts = model.getForecasts();
        timer.stop();
        }
        catch (IFSException ex) {
        	System.err.println("Could not create a model: "
        	+ ex.getMessage());
        	System.exit(1);
        }
        
        // Print forecast timing.
        
        System.out.printf("\n");
        System.out.printf("Timing (ms): %.3f\n", timer.getTimeMilliSeconds());
        System.out.printf("Calib Info : %s\n", calib_info);
        
        // Print possibly reshaped history.
        
        ts = model.getSeries();
        
        System.out.printf("\n");
        System.out.printf("Time Series\n");
        for (int i = 0; i < ts.length; i++)
        	System.out.printf("%4d.  %.3f\n", i+1, ts[i]);
        
        // Print forecasts.

        System.out.printf("\n");
        System.out.printf("Forecasts\n");
        for (int i = 0; i < forecasts.length; i++)
        	System.out.printf("%4d.  %.3f\n", i+1, forecasts[i]);
        System.out.printf("\n");

		System.exit(0);
	}
	
	/**
	 * Print usage information.
	 */
	private static void usage() {
		System.out.print(
  "\n"
+ "Execute a forecast model.\n"
+ "\n"
+ "Usage: java ... com.aol.ifs.soa.common.IFSModelMain <time series file | -->\n"
+ "          <# forecasts> <model name> [<model parameter> ... <model parameter>]\n"
+ "\n"
+ "<time series file | --> -- Specify where to get time series data. It can\n"
+ "                           either be a file or standard input. Use '--' to\n"
+ "                           specify standard input. Time series data has a\n"
+ "                           single number on each line and is the historical\n"
+ "                           data used as a basis for producing forecasts.\n"
+ "\n"
+ "# forecasts             -- Number of forecasts to produce. The value must\n"
+ "                           be at least 1.\n"
+ "\n"
+ "model name              -- Name of the model to execute.\n"
+ "\n"
+ "Model parameters are optional, but can be used to customize the model\n"
+ "behavior. There are common parameters and parameters specific to each model.\n"
+ "\n"
+ "Common parameters:\n"
+ IFSCycle.usage()
+ IFSNDaysBack.usage()
+ IFSSpikeFilter.usage()
+ IFSTransformType.usage()
+ IFSModelFactory.usage()
+ "\n"
		);
	}
}
