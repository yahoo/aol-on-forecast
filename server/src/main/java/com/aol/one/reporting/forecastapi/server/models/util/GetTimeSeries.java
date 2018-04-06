/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.models.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.aol.one.reporting.forecastapi.server.models.model.IFSException;


/**
 * Class supporting the reading of a time series from a file or standard
 * input. If a "--" is provided for the file name, the time series is read
 * from standard input. There is expected one value of the time series per
 * line in the file. Also the ordering is from least recent to most recent
 * value.
 */
public final class GetTimeSeries {
	
	/**
	 * Fetch a time series from a file or standard input. The series is
	 * returned in a vector unless an error is encountered.
	 * 
	 * @param input The file path or "--" if the values are from standard input.
	 * 
	 * @return A vector containing the time series.
	 * 
	 * @throws IFSException Thrown if an error is encountered reading the
	 *    values.
	 */
	public static double[] getTimeSeries(
		String		input
	) throws IFSException {
		
		// Fetch the time series from a file or standard input.
		
        ArrayList<Double>       time_series = null;
        double                  value = 0.0;
        BufferedReader          in = null;
        String                  line = null;
        double[]                ts = null;
        int                     i = 0;

        try {
        time_series = new ArrayList<Double>();
        in = getFile(input);
        while ((line = in.readLine()) != null) {
            try {
            value = Double.parseDouble(line);
            time_series.add(value);
            }
            catch (NumberFormatException ex) {
                throw new IFSException("Time series value '"
                +   line
                +   "' not a number. "
                +   ex.getMessage());
            }
        }
        if (!input.equals("--"))
        	in.close();
        }
        catch (SecurityException ex) {
        	throw new IFSException("File access error occurred. "
            +   ex.getMessage());
        }
        catch (FileNotFoundException ex) {
        	throw new IFSException("File read error occurred. "
            +   ex.getMessage());
        }
        catch (IOException ex) {
        	throw new IFSException("Unexpected error occurred in reading "
            +   "time series. "
            +   ex.getMessage());
        }
        i = 0;
        ts = new double[time_series.size()];
        for (Double ts_value : time_series)
            ts[i++] = ts_value.doubleValue();
        
        return ts;
	}

/*******************/
/* Private Methods */
/*******************/

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
	
}
