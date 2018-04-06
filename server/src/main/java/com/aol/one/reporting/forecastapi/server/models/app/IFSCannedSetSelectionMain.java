/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.models.app;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import com.aol.one.reporting.forecastapi.server.models.cs.GetCannedSetDefinitions;
import com.aol.one.reporting.forecastapi.server.models.cs.IFSCannedSet;
import com.aol.one.reporting.forecastapi.server.models.cs.IFSCannedSetSelection;
import com.aol.one.reporting.forecastapi.server.models.cs.IFSCannedSetSelectionConstraints;
import com.aol.one.reporting.forecastapi.server.models.cs.IFSCannedSetSelectionContext;
import com.aol.one.reporting.forecastapi.server.models.model.IFSException;
import com.aol.one.reporting.forecastapi.server.models.util.GetTimeSeries;
import com.aol.one.reporting.forecastapi.server.models.util.Timer;

/**
 * Class implementing canned set selection command line driver. Given canned
 * set constraints and context, the appropriate canned set is selected and
 * printed.
 */
public final class IFSCannedSetSelectionMain {
	
	/**
	 * Main program for running canned set selection stand alone. Argument
	 * is a file containing a canned set selection scenario in properties
	 * format. Printed is the scenario definition followed by the selected
	 * canned set.
	 * 
	 * @param args Name of canned set selection scenario file.
	 */
	public static void main(
		String[]	args
	) {
		if (args.length != 1) {
			usage();
			System.exit(0);
		}
		
		ScenarioConfig						settings = null;
		Map<String, IFSCannedSet>			canned_set_defns = null;
		List<Integer>						declining_profit_centers = null;
		IFSCannedSetSelectionConstraints	constraints = null;
		IFSCannedSetSelectionContext		context = null;
		double[]							time_series = null;
		List<IFSCannedSet>					candidates = null;
		IFSCannedSet						canned_set_selection = null;
		Timer								timer = new Timer();
		
		try {
	    	
		// Fetch settings

		settings = new ScenarioConfig(args[0]);
	    System.out.println("");
	    System.out.print(settings.toString());
	    
	    // Fetch canned set definitions
	    
	    canned_set_defns = GetCannedSetDefinitions.getCannedSetDefinitions(
	    	settings.getFileCannedSetDefinitions());
	    
	    // Set up constraint canned sets
	    
	    constraints = new IFSCannedSetSelectionConstraints();
	    constraints.setCannedSetDecline(canned_set_defns.get("AR-NONE-NONE"));
	    constraints.setCannedSetNoneNew(canned_set_defns.get("AVG-NONE-28-NEW"));
	    constraints.setCannedSetWeekNew(canned_set_defns.get("REG-NONE-ADD-AUTO-NEW"));
	    
	    // Fetch and set declining profit centers
	    
	    declining_profit_centers = getDecliningProfitCenters(
	    	settings.getFileDecliningProfitCenters());
	    constraints.setProfitCentersDecline(declining_profit_centers);
	    
	    // Fetch time series and set up selection context
	    
	    context = new IFSCannedSetSelectionContext();
	    time_series = GetTimeSeries.getTimeSeries(settings.getFileTimeSeries());
	    context.setSeries(time_series, settings.getValueNumHoldback());
	    context.setID(settings.getValueID());
	    context.setProfitCenter(settings.getValueProfitCenter());
	    
	    // Fetch the candidate canned sets and add them to the selection
	    // context
	    
	    candidates = getCannedSetCandidates(settings.getValueCannedSetCandidates(),
	    	canned_set_defns);
	    context.setCannedSetCandidates(candidates);
	    
	    // Select the canned set to forecast the context
	    
        timer.start();
	    canned_set_selection = IFSCannedSetSelection.selectCannedSet(constraints, context);
        timer.stop();
	    
	    // Print the selection
	    
        System.out.printf("\n");
	    System.out.printf("Canned Set Selection:          %s\n",
	    	canned_set_selection.getName());
	    System.out.printf("Timing (ms):                   %,11.3f\n",
	    	timer.getTimeMilliSeconds());
	    System.out.printf("\n");
	    
		} catch (IFSException ex) {
	        System.err.println(ex.getMessage());
	        System.exit(1);
		}
		System.exit(0);
	}

/*******************/
/* Private Methods */
/*******************/
	
	/**
	 * Fetch the canned set candidates. A string containing a comma separated
	 * list of canned set ids is compared against a collection of canned set
	 * definitions to arrive at a list of candidate canned sets. The list of
	 * canned set ids has the following format:
	 * 
	 * <canned set id>, ... ,<canned set id>
	 * 
	 * @param canned_set_candidates Canned set ids to look up.
	 * @param canned_set_definitions Collection of canned sets to reference.
	 * 
	 * @return List of candidate canned sets.
	 * 
	 * @throws IFSException Thrown if canned set ids have an unexpected format
	 *    or any of the canned set ids cannot be found in the canned set
	 *    collection.
	 */
	private static List<IFSCannedSet> getCannedSetCandidates(
		String						canned_set_candidates,
		Map<String, IFSCannedSet>	canned_set_definitions
	) throws IFSException {
		List<IFSCannedSet>		candidates = new ArrayList<>();
		StringTokenizer			tokens = new StringTokenizer(canned_set_candidates, ";");
		String					canned_set_id = null;
		IFSCannedSet			canned_set = null;
		
		while (tokens.hasMoreTokens()) {
			canned_set_id = tokens.nextToken().trim();
			canned_set = canned_set_definitions.get(canned_set_id);
			if (canned_set == null) {
				throw new IFSException("Candidate canned set id '"
				+ canned_set_id
				+ "' is not a canned set definition.");
			}
			candidates.add(canned_set);
		}
       
		return candidates;
	}
	
	/**
	 * Fetch the declining profit center ids from a specified file. The file
	 * may contain no profit center ids, but, if it does, there must be one
	 * profit center id per line in the following format:
	 * 
	 * <profit center id>
	 * 
	 * @param declining_profit_centers_file File containing declining profit
	 *    centers.
	 * 
	 * @return Declining profit center list which could be empty.
	 * 
	 * @throws IFSException Thrown if file has an unexpected format.
	 */
	private static List<Integer> getDecliningProfitCenters(
		String declining_profit_centers_file
	) throws IFSException {
        String			line = null;
        List<Integer>	profit_centers = null;
        StringTokenizer	tokens = null;
        int				line_num = 0;
        int				profit_center = 0;
        
        try  (BufferedReader in = new BufferedReader(new InputStreamReader(
        	new FileInputStream(declining_profit_centers_file)))) {
        profit_centers = new ArrayList<Integer>();
        while ((line = in.readLine()) != null) {
        	line_num++;
        	tokens = new StringTokenizer(line, " ");
        	if (tokens.countTokens() != 1) {
        		throw new IFSException("Declining profit centers file '"
        		+ declining_profit_centers_file
        		+ "' has the wrong format at line "
        		+ line_num
        		+ ".");
        	}
        	profit_center = Integer.parseInt(tokens.nextToken());
        	if (profit_center < 1) {
            	throw new IFSException("Declining profit center in '"
                + declining_profit_centers_file
                + "' at line "
                + line_num
                + " was less than 1.");
        	}
        	profit_centers.add(profit_center);
        }
        } catch (FileNotFoundException ex) {
        	throw new IFSException("Could not read '"
        	+ declining_profit_centers_file
        	+ "': "
        	+ ex.getMessage());
        } catch (SecurityException ex) {
        	throw new IFSException("Could not read '"
            + declining_profit_centers_file
            + "': "
            + ex.getMessage());
        } catch (IOException ex) {
        	throw new IFSException("Could not read '"
            + declining_profit_centers_file
            + "': "
            + ex.getMessage());
        } catch (NumberFormatException ex) {
        	throw new IFSException("Declining profit center in '"
            + declining_profit_centers_file
            + "' at line "
            + line_num
            + " had an unexpected format.");
        }
        
		return profit_centers;
	}
	
	/**
	 * Prints usage for the main program.
	 */
	private static void usage() {
		System.out.print(
  "\n"
+ "Perform a canned set selection as defined by a canned set selection\n"
+ "scenario. A scenario definition consists of a set of canned set definitions,\n"
+ "a set of declining profit centers, a specific profit center to check,\n"
+ "the number of data points to hold back in comparing canned sets, a list\n"
+ "of candidate canned set names, and time series data values.\n"
+ "\n"
+ "Usage: IFSCannedSetSelection <canned set selection scenario file>\n"
+ "\n"
+ "canned set selection scenario file -- File containing the following\n"
+ "   properties:\n"
+ "\n"
+ "1. canned.set.definitions.file -- Path to file containing canned set\n"
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
+ "2. declining.profit.centers.file -- Path to file containing declining\n"
+ "   profit centers; one per line. Following are some example lines.\n"
+ "\n"
+ "   11\n"
+ "   89\n"
+ "   17\n"
+ "   7\n"
+ "\n"
+ "3. series.file -- This is a file containing the time series to evaluate\n"
+ "   in determining the canned set selection. Each line in the file contains\n"
+ "   the next value in the series ordered from least recent to most recent\n"
+ "   value. An example of series values is:\n"
+ "\n"
+ "   15010251\n"
+ "   14208450\n"
+ "   13122392\n"
+ "   13726736\n"
+ "   15134212\n"
+ "   15275474\n"
+ "   15218248\n"
+ "\n"
+ "4. id -- The integer identifier to use for the selection context. This\n"
+ "   value is used in error messages.\n"
+ "\n"
+ "5. num.hold.back -- Number of values to hold back from the end of the\n"
+ "   time series. A forecast is made from the values preceding those for\n"
+ "   the same length. The root mean squared error (RMSE) is computed for\n"
+ "   the pairs and the canned set with the minimum RMSE is selected. Note\n"
+ "   the list of canned sets compared is determined by domain specific\n"
+ "   knowledge and is documented elsewhere. An example value for this value\n"
+ "   is 30.\n"
+ "\n"
+ "6. profit.center -- This is the profit center associated with the time\n"
+ "   time series. It is compared with the list of declining profit centers\n"
+ "   and is used in determining which canned sets will be compared with\n"
+ "   each other.\n"
+ "\n"
+ "7. canned.set.candidates -- This is a semi-colon separated list of candidate\n"
+ "   canned sets. Each candidate name must exist in the canned set definitions\n"
+ "   file. Also note there are built-in canned sets used by the selection\n"
+ "   logic as described under the canned set definitions file. An example\n"
+ "   list of candidate canned sets is:\n"
+ "\n"
+ "   EXP-DA-ADD-AUTO;EXP-DA-NONE;EXP-DM-MULT-YEAR;EXP-NONE-ADD-AUTO;\n"
+ "   REG-NONE-CONST2-AUTO;REG-NONE-PHASE2-WEEK-YEAR\n"
+ "\n"
		);
	}

/*******************/
/* Private Classes */
/*******************/

/**
 * Class that sets up the canned set selection scenario defined in the
 * scenario file.
 */
private static final class ScenarioConfig {
	private String			FileCannedSetDefinitions;
	private String			FileDecliningProfitCenters;
	private String			FileTimeSeries;
	private String			ValueCannedSetCandidates;
	private int				ValueID;
	private int				ValueNumHoldback;
	private int				ValueProfitCenter;
    private String          ConfigFile = null;
    private Properties      Settings = null;
    
    /**
     * Read a selection scenario properties file and make settings available
     * for access.
     * 
     * @param config_file Selection scenario properties file.
     * 
     * @throws IFSException Thrown if the properties file cannot be read,
     *    a property is missing, or a property is incorrectly specified.
     */
    public ScenarioConfig(
        String  config_file
    ) throws IFSException {
        ConfigFile = config_file;
        Settings = new Properties();
        
        Reader      settings_in = null;
        
        try {
        settings_in = new FileReader(ConfigFile);
        Settings.load(settings_in);
        } catch (FileNotFoundException ex) {
            throw new IFSException("Config file '"
            + config_file
            + "' encountered an error: "
            + ex.getMessage());
        } catch (IOException ex) {
            throw new IFSException("Config file '"
            + config_file
            + "' encountered an error: "
            + ex.getMessage());
        }
        
        FileCannedSetDefinitions = getSettingString("canned.set.definitions.file");
        FileDecliningProfitCenters = getSettingString("declining.profit.centers.file");
        FileTimeSeries = getSettingString("series.file");
        
        ValueCannedSetCandidates = getSettingString("canned.set.candidates");
        
        ValueID = getSettingInteger("id", 1, null);
        ValueNumHoldback = getSettingInteger("num.hold.back", 1, null);
        ValueProfitCenter = getSettingInteger("profit.center", 1, null);
       
        Settings = null;
    }
	
	/**
	 * Fetch canned set definitions file name.
	 * 
	 * @return Canned set definitions file name.
	 */
	public String getFileCannedSetDefinitions() {
		return FileCannedSetDefinitions;
	}
	
	/**
	 * Fetch declining profit centers file name.
	 * 
	 * @return Declining profit centers file name.
	 */
	public String getFileDecliningProfitCenters() {
		return FileDecliningProfitCenters;
	}
	
	/**
	 * Fetch time series file name.
	 * 
	 * @return Time series file name.
	 */
	public String getFileTimeSeries() {
		return FileTimeSeries;
	}

	/**
	 * Fetch canned set candidates.
	 * 
	 * @return Canned set candidates.
	 */
	public String getValueCannedSetCandidates() {
		return ValueCannedSetCandidates;
	}

	/**
	 * Fetch context id.
	 * 
	 * @return Context id.
	 */
	public int getValueID() {
		return ValueID;
	}

	/**
	 * Fetch number data points to hold back.
	 * 
	 * @return Number of data points to hold back.
	 */
	public int getValueNumHoldback() {
		return ValueNumHoldback;
	}

	/**
	 * Fetch context profit center.
	 * 
	 * @return Context profit center.
	 */
	public int getValueProfitCenter() {
		return ValueProfitCenter;
	}
    
    /**
     * @return Display settings values.
     */
    public String toString() {
       return "Settings::\n"
       + "canned.set.definitions.file:   " + FileCannedSetDefinitions + "\n"
       + "declining.profit.centers.file: " + FileDecliningProfitCenters + "\n"
       + "series.file:                   " + FileTimeSeries + "\n"
       + "id:                            " + ValueID + "\n"
       + "num.hold.back:                 " + ValueNumHoldback + "\n"
       + "profit.center:                 " + ValueProfitCenter + "\n"
       + "canned.set.candidates:         " + ValueCannedSetCandidates + "\n";
    }

/*******************/
/* Private Methods */
/*******************/

    /**
     * Default constructor inaccessible.
     */
    @SuppressWarnings("unused")
    private ScenarioConfig() {}
    
    /**
     * Fetch a scenario config setting from the properties list.
     * 
     * @param key Setting name.
     * 
     * @return Setting value.
     * 
     * @throws IFSException Thrown if setting not found.
     */
    private String getSetting(
        String  key
    ) throws IFSException {
        String  value = null;
        
        if ((value = Settings.getProperty(key)) == null) {
            throw new IFSException("Config file '"
                + ConfigFile
                + "' did not specify the property: "
                + key);
        }

        return value;
    }
    
    /**
     * Fetch a scenario config integer setting from the properties list.
     * 
     * @param key Setting name.
     * @param lower Lower bound for value. If null, no lower bound is specified.
     * @param upper Upper bound for value. If null, no upper bound is specified.
     * 
     * @return Setting integer value.
     * 
     * @throws IFSException Thrown if setting not found, is not an integer,
     *    or does not match range.
     */
    private int getSettingInteger(
        String  key,
        Integer lower,
        Integer upper
    ) throws IFSException {
        String  value = getSetting(key);
        int     ivalue = 0;
        
        try {
        ivalue = Integer.parseInt(value);  
        } catch (NumberFormatException ex) {
            throw new IFSException("Config file '"
            + ConfigFile
            + "' did not specify an integer for property: "
            + key);
        }
        
        if (lower != null && ivalue < lower) {
            throw new IFSException("Config file '"
            + ConfigFile
            + "' integer property '"
            + key
            + "' is less than the lower bound '"
            + lower
            + "'.");
        }
        
        if (upper != null && ivalue > upper) {
            throw new IFSException("Config file '"
            + ConfigFile
            + "' integer property '"
            + key
            + "' is greater than the upper bound '"
            + upper
            + "'.");
        }

        return ivalue;
    }
    
    /**
     * Fetch a scenario config string setting from the properties list.
     * 
     * @param key Setting name.
     * 
     * @return Setting value.
     * 
     * @throws IFSException Thrown if setting not found or the value is empty.
     */
    private String getSettingString(
        String  key
    ) throws IFSException {
        String  value = getSetting(key);
        
        if (value.equals("")) {
            throw new IFSException("Config file '"
            + ConfigFile
            + "' string property '"
            + key
            + "' has no value.");
        }

        return value;
    }
}
}
