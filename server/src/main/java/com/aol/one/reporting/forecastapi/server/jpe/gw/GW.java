/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.jpe.gw;

import java.io.PrintStream;

/**
 * Implements a grid walk algorithm (GW) for optimization purposes. To use, the
 * GW is instantiated with an object that implements the GWInterface.
 * 
 * @author Copyright &copy; 2012 John Eldreth All rights reserved.
 */
public final class GW {
    private GWInterface         GWIObject = null;
    private boolean				IsTrace = false;
    private int					NumIterations = 0;
    private double				OptRating = 0.0;
    private double[]			OptWeights = null;
    private PrintStream			TraceOut = null;

    /**
     * Instantiate a GW with the a GWInterface object. Once the GW object
     * is constructed, the optimal weights can be requested.
     *
     * @param gwi_object Object implementing the GWInterface.
     *
     * @throws GWException If there are problems with the parameter values.
     */
    public GW(
        GWInterface gwi_object
    ) throws GWException {
        if (gwi_object == null)
            throw new GWException("The GW interface object cannot be null.");

        init(gwi_object, false, null);
    }

    /**
     * Instantiate a GW with the a GWInterface object. Once the GW object
     * is constructed, the optimal weights can be requested.
     *
     * @param gwi_object Object implementing the GWInterface.
     * @param is_trace Whether walk is to be traced (true) or not (false).
     * @param trace_out Where to print trace info if trace is enabled.
     *
     * @throws GWException If there are problems with the parameter values.
     */
    public GW(
        GWInterface gwi_object,
        boolean		is_trace,
        PrintStream	trace_out
    ) throws GWException {
        if (gwi_object == null)
            throw new GWException("The GW interface object cannot be null.");

        init(gwi_object, is_trace, trace_out);
    }

    /**
     * Fetch the number of iterations actually executed to find
     * the optimal set of weights.
     * 
     * @return Number of iterations executed.
     */
    public int getNumIterations() {
        return NumIterations;
    }

    /**
     * Fetch optimal weight rating.
     *
     * @return Optimal weight rating.
     */
    public double getOptRating() {
        return OptRating;
    }

    /**
     * Fetch optimal weight values.
     *
     * @return Optimal weight values.
     */
    public double[] getOptWeights() {
        return OptWeights;
    }

/*******************/
/* Private Methods */
/*******************/

    /**
     * Default constructor not allowed.
     */
    @SuppressWarnings("unused")
	private GW() {}

    /**
     * Initialize the GW object. Then compute the optimal weights.
     *
     * @param gwi_object Object implementing the GWInterface.
     * @param is_trace Whether walk is to be traced (true) or not (false).
     * @param trace_out Where to print trace info if trace is enabled.
     *
     * @throws GWException If there are problems with the parameter values.
     */
    private void init(
        GWInterface gwi_object,
        boolean		is_trace,
        PrintStream	trace_out
    ) throws GWException {
        if (gwi_object == null)
            throw new GWException("The GW interface object cannot be null.");

        GWIObject = gwi_object;
        IsTrace = is_trace;
        TraceOut = trace_out;
        
        int			num_weights = GWIObject.getNumWeights();
        
        OptWeights = new double[num_weights];
        for (int i = 0; i < num_weights; i++)
        	OptWeights[i] = (GWIObject.getWeightUpperBound(i)
        		+GWIObject.getWeightLowerBound(i))/2.0;
        OptRating = GWIObject.getRating(OptWeights);
        if (IsTrace)
        	GWIObject.printTrace(TraceOut, NumIterations, 0, -1,
        		OptWeights, OptRating);
		
		double[]	weights_n_p = new double[num_weights];	
		double[]	weights_t = new double[num_weights];
		boolean		is_done = true;
		int			levels = GWIObject.getNumLevels();
		double		step = 0.0;
		double		min_bound = 0.0;
		double		max_bound = 0.0;
		double		rating = 0.0;
		
		for (int level = 0; level < levels; level++) {
			is_done = false;
			while (!is_done) {
				is_done = true;
				NumIterations++;
				System.arraycopy(OptWeights, 0, weights_n_p, 0, num_weights);
				for (int i = 0; i < num_weights; i++) {
					step = GWIObject.getStepSize(level, i);
					min_bound = GWIObject.getWeightLowerBound(i);
					max_bound = GWIObject.getWeightUpperBound(i);
					System.arraycopy(weights_n_p, 0, weights_t, 0, num_weights);
					weights_t[i] = Math.min(max_bound, weights_t[i]+step);
			        rating = GWIObject.getRating(weights_t);
					if (IsTrace)
						GWIObject.printTrace(TraceOut, NumIterations,
							level, i, weights_t, rating);
					if (rating < OptRating) {
						System.arraycopy(weights_t, 0, OptWeights, 0, num_weights);
						OptRating = rating;
						is_done = false;
					}
					System.arraycopy(weights_n_p, 0, weights_t, 0, num_weights);
					weights_t[i] = Math.max(min_bound, weights_t[i]-step);
			        rating = GWIObject.getRating(weights_t);
					if (IsTrace)
						GWIObject.printTrace(TraceOut, NumIterations,
							level, i, weights_t, rating);
					if (rating < OptRating) {
						System.arraycopy(weights_t, 0, OptWeights, 0, num_weights);
						OptRating = rating;
						is_done = false;
					}
				}
			}
		}
        OptRating = GWIObject.getRating(OptWeights);
    }
}
