/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.models.cs;

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import com.aol.one.reporting.forecastapi.server.models.model.IFSException;
import com.aol.one.reporting.forecastapi.server.models.model.IFSMetrics;
import com.aol.one.reporting.forecastapi.server.models.model.IFSModel;
import com.aol.one.reporting.forecastapi.server.models.model.IFSModelFactory;

/**
 * Class implementing canned set selection. Given a canned set context
 * and a canned set list, the canned set that produces the canned set
 * closest to the actual value holdback is selected.
 */
public final class IFSCannedSetCompetition {
	private static ForkJoinPool		ParallelPool = null;
	
	/**
	 * Run a competition over a list of canned sets and select the one with
	 * the smallest RMSE for the context hold back period. Note that a list
	 * of one canned set still has the hold back forecast executed as a form
	 * of validation.
	 * 
	 * @param context Canned set selection context.
	 * @param canned_set_list List of canned sets.
	 * 
	 * @return Selected canned set.
	 * 
	 * @throws IFSException thrown if a forecast error occurs or there are
	 *    no canned sets in the list.
	 */
	public static IFSCannedSet competeCannedSets(
		IFSCannedSetSelectionContext	context,
		List<IFSCannedSet>				canned_set_list
	) throws IFSException {
		if (canned_set_list == null || canned_set_list.isEmpty())
			throw new IFSException(69, context.getID());
		
		if (ParallelPool == null)
			ParallelPool = new ForkJoinPool();
		
		return ParallelPool.invoke(
			new ParallelCompeteCannedSets(context, canned_set_list)).getCannedSet();
	}

/*******************/
/* Private Classes */
/*******************/

/**
 * This class contains competition results and provides min and max methods
 * for comparing results.
 */
private static final class CannedSetResult {
	private final IFSCannedSet	CannedSet;
	private final double		RMSE;
	
	/**
	 * Fully specified constructor.
	 * 
	 * @param canned_set Resulting canned set.
	 * @param rmse Resulting root mean squared error.
	 */
	public CannedSetResult(
		IFSCannedSet	canned_set,
		double			rmse
	) {
		CannedSet = canned_set;
		RMSE = rmse;
	}
	
	/**
	 * Default constructor inaccessible.
	 */
	@SuppressWarnings("unused")
	private CannedSetResult() {
		CannedSet = null;
		RMSE = 0.0;
	}
	
	/**
	 * Fetch canned set.
	 * 
	 * @return Canned set.
	 */
	public IFSCannedSet getCannedSet() {
		return CannedSet;
	}
	
	/**
	 * Fetch RMSE (root mean squared error).
	 * 
	 * @return RMSE.
	 */
	public double getRMSE() {
		return RMSE;
	}
	
	/**
	 * Compare the RMSEs of two canned set results and return the canned
	 * set result with the greater RMSE. If the results are equal, the first
	 * canned set is returned.
	 * 
	 * @param first First canned set result.
	 * @param second Second canned set result.
	 * 
	 * @return Canned set with the greater RMSE.
	 */
	@SuppressWarnings("unused")
	public static CannedSetResult max(
		CannedSetResult	first,
		CannedSetResult	second
	) {
		if (first.getRMSE() >= second.getRMSE())
			return first;
		else
			return second;
	}
	
	/**
	 * Compare the RMSEs of two canned set results and return the canned
	 * set result with the least RMSE. If the results are equal, the first
	 * canned set is returned.
	 * 
	 * @param first First canned set result.
	 * @param second Second canned set result.
	 * 
	 * @return Canned set with the least RMSE.
	 */
	public static CannedSetResult min(
		CannedSetResult	first,
		CannedSetResult	second
	) {
		if (first.getRMSE() <= second.getRMSE())
			return first;
		else
			return second;
	}
}

/**
 * Run a parallel competition over a list of canned sets and select the one
 * with the smallest RMSE for the context hold back period. Note that a list
 * of one canned set still has the hold back forecast executed as a form
 * of validation.
 * 
 * 
 * @return Selected canned set.
 * 
 * @throws IFSException thrown if a forecast error occurs or there are
 *    no canned sets in the list.
 */
private static final class ParallelCompeteCannedSets
	extends RecursiveTask<CannedSetResult> {
	private static final long					serialVersionUID = 1L;
	private final IFSCannedSetSelectionContext	Context;
	private final List<IFSCannedSet>			CannedSetList;
	
	/**
	 * Fully specified constructor.
	 * 
	 * @param context Canned set selection context.
	 * @param canned_set_list List of canned sets.
	 */
	public ParallelCompeteCannedSets(
		IFSCannedSetSelectionContext	context,
		List<IFSCannedSet>				canned_set_list
	) {
		Context = context;
		CannedSetList = canned_set_list;
	}
	
	/**
	 * Default constructor inaccessible.
	 */
	@SuppressWarnings("unused")
	private ParallelCompeteCannedSets() {
		Context = null;
		CannedSetList = null;
	}
	
	/**
	 * Run canned set competition.
	 * 
	 * @return Canned set that minimized the RMSE.
	 */
	@Override
	protected CannedSetResult compute() {
		if (CannedSetList.size() <= 1)
			try {
			return execCannedSet(Context, CannedSetList.get(0));
			}
			catch (IFSException ex) {
				throw new RuntimeException(ex.getMessage());
			}
		
		int		half_index = CannedSetList.size()/2;
			
		ParallelCompeteCannedSets	ccs01
			= new ParallelCompeteCannedSets(Context,
			CannedSetList.subList(0, half_index));
		
		ccs01.fork();
			
		ParallelCompeteCannedSets	ccs02
			= new ParallelCompeteCannedSets(Context,
			CannedSetList.subList(half_index, CannedSetList.size()));
				
		return CannedSetResult.min(ccs02.compute(), ccs01.join());
	}
	
	/**
	 * Forecast a context using the designed canned set into the context
	 * hold back period and compute the resulting RMSE.
	 * 
	 * @param context Canned set selection context.
	 * @param canned_set Canned set to execute.
	 * 
	 * @return Canned set result.
	 * 
	 * @throws IFSException Thrown if there is an error in executing the
	 *    canned set.
	 */
	private static CannedSetResult execCannedSet(
		IFSCannedSetSelectionContext	context,
		IFSCannedSet					canned_set
	) throws IFSException {
        IFSModel	model = null;
        double[]	forecasts = null;
		double		rmse = Double.POSITIVE_INFINITY;
        
		try {
        model = IFSModelFactory.create(canned_set.getParameterSpec().getModel());
        IFSModelFactory.setup(model, context.getSeriesCalibration(),
        	canned_set.getParameterSpec().getParameterValues());
        model.generateForecasts(context.getNumHoldback());
        forecasts = model.getForecasts();
        rmse = IFSMetrics.getRMSE(context.getSeriesHoldback(), forecasts);
// 		System.out.printf("Canned Set: %s RMSE: %f\n", canned_set.getName(), rmse);
		}
		catch (IFSException ex) {
			throw new IFSException("Canned set id '"
			+ canned_set.getName()
			+ "' encountered the following error: "
			+ ex.getMessage());
		}

		return new CannedSetResult(canned_set, rmse);
	}
}
}
