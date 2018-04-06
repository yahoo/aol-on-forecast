/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.models.cs;

import java.util.ArrayList;
import java.util.List;

import com.aol.one.reporting.forecastapi.server.models.model.IFSDetectSeasonalCycle;
import com.aol.one.reporting.forecastapi.server.models.model.IFSException;
import com.aol.one.reporting.forecastapi.server.models.model.IFSSpikeFilter;
import com.aol.one.reporting.forecastapi.server.models.model.IFSStatistics;

/**
 * Class implementing canned set selection. Given canned set constraints
 * and context, the appropriate canned set is selected.
 */
public final class IFSCannedSetSelection {

	/**
	 * Given constraints and context, select a canned set.
	 * 
	 * @param constraints Canned set selection constraints.
	 * @param context Canned set selection context.
	 * 
	 * @return Selected canned set.
	 * 
	 * @throws IFSException thrown if constraints or context are improperly
	 *    specified.
	 */
	public static IFSCannedSet selectCannedSet(
		IFSCannedSetSelectionConstraints	constraints,
		IFSCannedSetSelectionContext		context
	) throws IFSException {
		if (constraints == null)
			throw new IFSException(64);
		if (context == null)
			throw new IFSException(65);
		
		List<IFSCannedSet>		canned_set_list = new ArrayList<IFSCannedSet>();
		
		if (context.getSeries().length <= constraints.getNumPointsNewUB()) {
			canned_set_list.add(constraints.getCannedSetNoneNew());
			canned_set_list.add(constraints.getCannedSetWeekNew());
		} else if (constraints.getProfitCentersDecline().contains(context.getProfitCenter())) {
			canned_set_list.add(constraints.getCannedSetDecline());
		} else if (context.getSeries().length > constraints.getNumPointsYearLB()
		&& round(IFSStatistics.getACF(context.getSeriesLast(constraints.getNumPointsYearLB()),
			true, constraints.getLagYear(), constraints.getLagYear())[0])
			>= constraints.getACFYearLB()) {
			for (IFSCannedSet canned_set : context.getCannedSetCandidates())
				if (canned_set.getName().toLowerCase().indexOf("-year") >= 0)
					canned_set_list.add(canned_set);
			if (canned_set_list.isEmpty())
				throw new IFSException(66, context.getID());
		} else if (IFSDetectSeasonalCycle.getSeasonalCycle(
			IFSSpikeFilter.getSpikeFilteredSeries(context.getSeries(),
			constraints.getSpikeFilterWindow())) > 1) {
			for (IFSCannedSet canned_set : context.getCannedSetCandidates())
				if (canned_set.getName().toLowerCase().endsWith("-auto"))
					canned_set_list.add(canned_set);
			if (canned_set_list.isEmpty())
				throw new IFSException(67, context.getID());
		} else {
			for (IFSCannedSet canned_set : context.getCannedSetCandidates())
				if (!canned_set.getName().toLowerCase().endsWith("-auto")
				&& !canned_set.getName().toLowerCase().endsWith("-year"))
					canned_set_list.add(canned_set);
			if (canned_set_list.isEmpty())
				throw new IFSException(68, context.getID());
		}
		
		return IFSCannedSetCompetition.competeCannedSets(context, canned_set_list);
	}

/*******************/
/* Private Methods */
/*******************/
	
	/**
	 * Round value to nearest 10^-2 value.
	 * 
	 * @param value Value to round.
	 * 
	 * @return Value rounded to 10^-2.
	 */
	private static double round(
		double	value
	) {
		return (double)Math.round(100.0*value) / 100.0;
	}
}
