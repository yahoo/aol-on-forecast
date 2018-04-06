/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.models.model;

import java.util.LinkedList;
import java.util.TreeMap;

/**
 * Thrown when IFS related exceptional events occur.
 *
 */
public final class IFSException extends Exception {
	private static final long				serialVersionUID = 1L;
	private static TreeMap<Integer, String>	Messages
		= new TreeMap<Integer, String>();
	private String							Code = null;
    
	// Initialize message map.
	
	static {
		Messages.put(
		  1, "Number of rows must be at least 2 when solving linear equations.");
		Messages.put(
		  2, "Row %d does not have %d columns. The matrix must be Nx(N+1) to "
		  +	"solve the linear equations.");
		Messages.put(
		  3, "Could not solve linear equations. Encountered a situation with "
		  + "all 0 values in the triangular column %d.");
		Messages.put(
		  4, "Could not solve linear equations. Encountered a situation with "
		  + "a 0 in the last A[N,N] element.");
		Messages.put(
		  5, "Number of parameters must be at least 0 when computing the "
		  + "adjusted AIC.");
		Messages.put(
		  6, "%s expects the number of actuals to be greater than 0.");
		Messages.put(
		  7, "%s expects the number of forecasts to be greater than 0.");
		Messages.put(
		  8, "%s expects the number of actuals and the number of forecasts "
		  +	"to be the same.");
		Messages.put(
		  9, "%s requires the number of forecasts to be at least 1.");
		Messages.put(
		 10, "%s requires the series length to be at least 1.");
		Messages.put(
		 11, "%s requires forecasts to be first generated before they can "
		 +	"be retrieved.");
		Messages.put(
		 12, "%s requires the series length to be at least 1.");
		Messages.put(
		 13, "Forecast model creation requires a non-empty model name.");
		Messages.put(
		 14, "Cannot create forecast model '%s'. That model does not exist.");
		Messages.put(
		 15, "Cannot create forecast model '%s'. %s");
		Messages.put(
		 16, "Cannot setup a null forecast model.");
		Messages.put(
		 17, "%s %s parameter only accepts the values of 0 or 1.");
		Messages.put(
		 18, "%s encountered an attempt to set the unknown parameter '%s'.");
		Messages.put(
		 19, "AR forecast generation encountered a situation where the base "
		 +	"series position was outside the expected range. The value was "
		 +	"'%d'.");
		Messages.put(
		 20, "%s %s parameter is not an integer.");
		Messages.put(
		 21, "%s %s parameter must be 0 or greater than 1.");
		Messages.put(
		 22, "%s encountered an unknown %s type '%s'.");
		Messages.put(
		 23, "%s encountered an attempt to set the unknown parameter '%s'.");
		Messages.put(
		 24, "%s encountered an attempt to specify a seasonal cycle '%s' "
		 +	"with a cycle length of 0.");
		Messages.put(
		 25, "%s %s parameter only accepts integer values greater than 0.");
		Messages.put(
		 26, "%s %s parameter must be at least 0 and less than or equal to %d.");
		Messages.put(
		 27, "%s encountered an attempt to specify a 2-phase seasonal cycle "
		 +	"'%s' with a cycle2 length of 0.");
		Messages.put(
		 28, "%s encountered situation where no independent variable rows "
		 + "were specified when computing regression coefficients.");
		Messages.put(
		 29, "%s encountered situation where no independent variable rows "
		 + "were specified when computing regression coefficients.");
		Messages.put(
		 30, "%s enountered situation where independent variable values had "
		 + "fewer rows than dependent variable values when computing "
		 + "regression coefficients.");
		Messages.put(
		 31, "Time series reshaping requires at least 1 data point.");
		Messages.put(
		 32, "No reshaping specification provided.");
		Messages.put(
		 33, "Time series reshaping encountered invalid fractional "
		 + "specification.");
		Messages.put(
		 34, "Time series reshaping fraction cannot be 0.0.");
		Messages.put(
		 35, "Time series reshaping fraction must be greater than -1.0.");
		Messages.put(
		 36, "Time series reshaping fraction must be less than or equal to 1.0.");
		Messages.put(
		 37, "Time series reshaping encountered invalid initial zero removal "
		 + "specification.");
		Messages.put(
		 38, "Time series reshaping initial zero removal fraction must be "
		 + "greater than 0.0 and less than 1.0.");
		Messages.put(
		 39, "Time series reshaping encountered invalid integer specification.");
		Messages.put(
		 40, "Spike filtering encountered a clipping window value that is "
		 + "not an integer.");
		Messages.put(
		 41, "Spike filtering encountered an invalid clipping window value.");
		Messages.put(
		 42, "Spike filtering encountered an unexpected error.");
		Messages.put(
		 43, "No series data provided for computing the ACF.");
		Messages.put(
		 44, "At least 1 auto-correlation coefficient must be specified to "
		 + "compute corresponding auto-regressive coefficients.");
		Messages.put(
		 45, "No series data provided for computing the sample mean.");
		Messages.put(
		 46, "Insufficient series data provided for computing the sample "
		 + "standard deviation. At least 2 points are required.");
		Messages.put(
		 47, "Transformation type cannot be null or empty.");
		Messages.put(
		 48, "%s is an unknown transformation type.");
		Messages.put(
		 49, "Context id %d encountered attempt to set null time series.");
		Messages.put(
		 50, "Context id %d encountered attempt to use empty time series.");
		Messages.put(
		 51, "Context id %d has no time series specified.");
		Messages.put(
		 52, "Context id %d has no profit center specified.");
		Messages.put(
		 53, "Context id %d encountered attempt to set profit center less than one.");
		Messages.put(
		 54, "Context id %d has no hold back size specified.");
		Messages.put(
		 55, "Context id %d encountered attempt to set hold back size to less than one.");
		Messages.put(
		 56, "Context id %d has no candidate canned sets specified.");
		Messages.put(
		 57, "Context id %d encountered attempt to set candidate canned sets to null or empty.");
		Messages.put(
		 58, "No declining canned set has been specified.");
		Messages.put(
		 59, "No non-seasonal canned set for new contexts has been specified.");
		Messages.put(
		 60, "No weekly canned set for new contexts has been specified.");
		Messages.put(
		 61, "Declining canned set cannot be null.");
		Messages.put(
		 62, "Non-seasonal canned set for new contexts cannot be null.");
		Messages.put(
		 63, "Seasonal canned set for new contexts cannot be null.");
		Messages.put(
		 64, "Canned set selection constraints cannot be null.");
		Messages.put(
		 65, "Canned set selection context cannot be null.");
		Messages.put(
		 66, "Context id %d had no yearly canned sets as candidates.");
		Messages.put(
		 67, "Context id %d had no weekly canned sets as candidates.");
		Messages.put(
		 68, "Context id %d had no non-weekly and non-yearly canned sets as candidates.");
		Messages.put(
		 69, "Context id %d had no canned sets in competition.");
		Messages.put(
		 70, "%s %s parameter only accepts integer values greater than 0 "
		 + "or equal to -1.");
		Messages.put(
		 71, "%s %s parameter must be -1, 0, or greater than 1.");
		Messages.put(
		 72, "Seasonal cycle is not an integer.");
		Messages.put(
		 73, "Seasonal cycle can only be -1 or greater than or equal to 0.");
		Messages.put(
		 74, "%s expects the number of actuals to be at least 2.");
		Messages.put(
		 75, "%s %s parameter only accepts values of 0, 1, or greater than 1.");
	}
	
    /**
     * Default IFSException.
     */
    public IFSException() {
        super("An IFS exception occurred.");
    }

    /**
     * IFSException with a specified message.
     */
    public IFSException(
        String      message
    ) {
        super(message);
    }
    
    /**
     * Define IFS exception with code and message.
     * 
     * @param code IFS exception code to use.
     * @param message IFS exception message.
     */
    public IFSException(
    	String		code,
    	String		message
    ) {
    	super(message);
    	Code = code;
    }
     
    /**
     * Set exception message and code based on exception index and arguments
     * to fill in message slots.
     * 
     * @param index Exception code index value.
     * @param arguments Values to put into message slots.
     */
    public IFSException(
    	int			index,
    	Object...arguments
    ) {
    	super(String.format(Messages.get(index), arguments));
    	Code = String.format("IFS%03d", index);
    }
    
    public static String[] getAllCodes() {
    	LinkedList<String>	codes = new LinkedList<String>();
    	
    	for (Integer index : Messages.keySet())
    		codes.add(String.format("IFS%03d -- %s",
    			index, Messages.get(index)));
    	
    	return(codes.toArray(new String[codes.size()]));
    }
    
    /**
     * Fetch exception code (if one is set).
     * 
     * @return Exception code (can be null).
     */
    public String getCode() {
    	return(Code);
    }
    
    public static String getMessage(
    	int		index
    ) {
    	return(Messages.get(index));
    }

}
