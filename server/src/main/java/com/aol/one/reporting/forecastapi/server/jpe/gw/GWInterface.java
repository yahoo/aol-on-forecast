/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.jpe.gw;

import java.io.PrintStream;

/**
 * A class wanting to use grid walk algorithm (GW) optimization capabilities
 * must implement this interface to allow the GW to determine when optimal
 * weights have been identified.
 * 
 * @author Copyright &copy; 2012 John Eldreth All rights reserved.
 */
public interface GWInterface {
	
    /**
     * Fetch number of optimization accuracy levels.
     * 
     * @return Number of optimization accuracy levels.
     */
    public int getNumLevels();
	
    /**
     * Fetch number of weights to walk.
     * 
     * @return Number of weights.
     */
    public int getNumWeights();

    /**
     * This method is given a set of weights and it is up to this method
     * to determine its rating (noting the smaller the rating, the higher
     * the likelihood the weights are optimal).
     *
     * @param weights Weight vector to rate.
     *
     * @return Weight vector rating.
     */
    public double getRating(double[] weights);
    
    /**
     * Fetch step size for an optimization accuracy level and a weight index.
     * 
     * @param level Optimization accuracy level (0-based).
     * @param weight_idx Weight index.
     * 
     * @return Optimization step size.
     */
    public double getStepSize(int level, int weight_idx);
    
    /**
     * Fetch lower bound for a weight index.
     * 
     * @param weight_idx Weight index.
     * 
     * @return Weight lower bound.
     */
    public double getWeightLowerBound(int weight_idx);
    
    /**
     * Fetch upper bound for a weight index.
     * 
     * @param weight_idx Weight index.
     * 
     * @return Weight upper bound.
     */
    public double getWeightUpperBound(int weight_idx);
    
    /**
     * Print trace information.
     * 
     * @param trace_out Where to print trace info if trace is enabled.
     * @param iteration Optimization iteration. Can be 0 on initialization.
     * @param level Optimization accuracy level (0-based).
     * @param weight_idx Weight index. Can be -1 when no specific weight is
     *    being adjusted.
     * @param weights Weight vector.
     * @param rating Weight vector rating.
     */
    public void printTrace(PrintStream trace_out, int iteration, int level, int weight_idx,
                           double[] weights, double rating);
}
