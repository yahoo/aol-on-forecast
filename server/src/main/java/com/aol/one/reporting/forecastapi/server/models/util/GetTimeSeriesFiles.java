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
import java.util.List;

import com.aol.one.reporting.forecastapi.server.models.model.IFSException;


/**
 * Class implementing methods for fetching time series from a file of time
 * series file paths.
 */
public final class GetTimeSeriesFiles {
	
	/**
	 * Fetch a file of time series files. Each time series is returned in an
	 * array of time series vectors. The time series vectors are added to the
	 * array in the order in which their file paths are listed.
	 * 
	 * @param ts_files File containing time series file paths.
	 * 
	 * @return An array of time series arrays.
	 * 
	 * @throws IFSException Thrown if an error is encountered reading the
	 *    values.
	 */
	public static double[][] getTimeSeriesFiles(
		String		ts_files
	) throws IFSException {
        BufferedReader  fin = null;
        List<double[]>	tss_l = new ArrayList<double[]>();
        String			line = null;
        String			ts_file = null;
        
        try {
        	
		fin = new BufferedReader(new InputStreamReader(
			new FileInputStream(ts_files)));
        while ((line = fin.readLine()) != null) {
        	ts_file = line.trim();
        	tss_l.add(GetTimeSeries.getTimeSeries(ts_file));
        }
        fin.close();
			
        } catch (SecurityException ex) {
        	throw new IFSException("File access error occurred. "
        	+ ex.getMessage());
        } catch (FileNotFoundException ex) {
        	throw new IFSException("File read error occurred. "
        	+ ex.getMessage());
        } catch (IOException ex) {
        	throw new IFSException("Unexpected error occurred in reading "
        	+ "time series. "
        	+ ex.getMessage());
        }
        
        return tss_l.toArray(new double[tss_l.size()][]);
	}
}
