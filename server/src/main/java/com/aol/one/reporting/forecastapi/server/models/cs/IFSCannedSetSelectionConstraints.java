/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.models.cs;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.aol.one.reporting.forecastapi.server.models.model.IFSException;

/**
 * Class encapsulating the various constraints that are used to bound
 * the canned set selection process.
 */
public final class IFSCannedSetSelectionConstraints {
	private static final double	ACFWeekLB = 0.4;
	private static final double	ACFYearLB = 0.3;
	
	private static final int	LagWeek = 7;
	private static final int	LagYear = 364;
	
	private static final int	NumPointsDeclineLB = 120;
	private static final int	NumPointsNewUB = 56;
	private static final int	NumPointsYearLB = 730;
	
	private static final double	SlopeDeclineUB = -0.002;
	
	private static final int	SpikeFilterWindow = 14;
	
	private IFSCannedSet		CannedSetDecline;
	private IFSCannedSet		CannedSetNoneNew;
	private IFSCannedSet		CannedSetWeekNew;
	
	private SortedSet<Integer>	ProfitCentersDecline;
	
	/**
	 * Default constructor.
	 */
	public IFSCannedSetSelectionConstraints() {
		ProfitCentersDecline = new TreeSet<Integer>();
		CannedSetDecline = null;
		CannedSetNoneNew = null;
		CannedSetWeekNew = null;
	}
	
	/**
	 * Fetch the auto-correlation coefficient lower bound for detecting
	 * the weekly cycle.
	 * 
	 * @return Weekly cycle auto-correlation coefficient.
	 */
	public double getACFWeekLB() {
		return ACFWeekLB;
	}
	
	/**
	 * Fetch the auto-correlation coefficient lower bound for detecting
	 * the annual cycle.
	 * 
	 * @return Annual cycle auto-correlation coefficient.
	 */
	public double getACFYearLB() {
		return ACFYearLB;
	}
	
	/**
	 * Fetch canned set to use for contexts that indicate a trend decline.
	 * 
	 * @return Decline canned set.
	 * 
	 * @throws IFSException thrown if declining canned set is not specified.
	 */
	public IFSCannedSet getCannedSetDecline() throws IFSException {
		if (CannedSetDecline == null)
			throw new IFSException(58);
		return CannedSetDecline;
	}
	
	/**
	 * Fetch canned set to use for new contexts that have no cycle.
	 * 
	 * @return No cycle canned set.
	 * 
	 * @throws IFSException thrown if non-seasonal canned set for new contexts
	 *    is not specified.
	 */
	public IFSCannedSet getCannedSetNoneNew() throws IFSException {
		if (CannedSetNoneNew == null)
			throw new IFSException(59);
		return CannedSetNoneNew;
	}
	
	/**
	 * Fetch canned set to use for new contexts that have weekly cycle.
	 * 
	 * @return Weekly cycle canned set.
	 * 
	 * @throws IFSException thrown if weekly canned set for new contexts
	 *    is not specified.
	 */
	public IFSCannedSet getCannedSetWeekNew() throws IFSException {
		if (CannedSetWeekNew == null)
			throw new IFSException(60);
		return CannedSetWeekNew;
	}
	
	/**
	 * Fetch the lag for detecting the weekly cycle.
	 * 
	 * @return Weekly cycle lag.
	 */
	public int getLagWeek() {
		return LagWeek;
	}
	
	/**
	 * Fetch the lag for detecting the annual cycle.
	 * 
	 * @return Annual cycle lag.
	 */
	public int getLagYear() {
		return LagYear;
	}
	
	/**
	 * Fetch the lower bound for decline context number of points.
	 * 
	 * @return Lower bound on decline context number of points.
	 */
	public int getNumPointsDeclineLB() {
		return NumPointsDeclineLB;
	}
	
	/**
	 * Fetch the upper bound for new context number of points.
	 * 
	 * @return Upper bound on new context number of points.
	 */
	public int getNumPointsNewUB() {
		return NumPointsNewUB;
	}
	
	/**
	 * Fetch the lower bound for annual cycle context number of points.
	 * 
	 * @return Lower bound on annual cycle context number of points.
	 */
	public int getNumPointsYearLB() {
		return NumPointsYearLB;
	}
	
	/**
	 * Fetch set of declining profit centers to consider.
	 * 
	 * @return Declining profit center set.
	 */
	public SortedSet<Integer> getProfitCentersDecline() {
		return ProfitCentersDecline;
	}
	
	/**
	 * Fetch the trend slope upper bound for detecting a decline.
	 * 
	 * @return Slope upper bound.
	 */
	public double getSlopeDeclineUB() {
		return SlopeDeclineUB;
	}
	
	/**
	 * Fetch the spike filter window.
	 * 
	 * @return Spike filter window.
	 */
	public int getSpikeFilterWindow() {
		return SpikeFilterWindow;
	}
	
	/**
	 * Set canned set to use for contexts that indicate a trend decline.
	 * 
	 * @param canned_set_decline Decline canned set.
	 * 
	 * @throws IFSException thrown if declining canned set is null.
	 */
	public void setCannedSetDecline(
		IFSCannedSet	canned_set_decline
	) throws IFSException {
		if (canned_set_decline == null)
			throw new IFSException(61);
		CannedSetDecline = canned_set_decline;
	}
	
	/**
	 * Set canned set to use for new contexts that have no cycle.
	 * 
	 * @param canned_set_none_new No cycle canned set.
	 * 
	 * @throws IFSException thrown if non-seasonal canned set for new 
	 *    contexts is null.
	 */
	public void setCannedSetNoneNew(
		IFSCannedSet	canned_set_none_new
	) throws IFSException {
		if (canned_set_none_new == null)
			throw new IFSException(62);
		CannedSetNoneNew = canned_set_none_new;
	}
	
	/**
	 * Set canned set to use for new contexts that have weekly cycle.
	 * 
	 * @param canned_set_week_new Weekly cycle canned set.
	 * 
	 * @throws IFSException thrown if weekly canned set for new 
	 *    contexts is null.
	 */
	public void setCannedSetWeekNew(
		IFSCannedSet	canned_set_week_new
	) throws IFSException {
		if (canned_set_week_new == null)
			throw new IFSException(63);
		CannedSetWeekNew = canned_set_week_new;
	}
	
	/**
	 * Replace the set of declining profit centers. If null, the current set
	 * is cleared. Otherwise the unique profit center ids are saved.
	 * 
	 * @param profit_centers_decline List of declining profit centers.
	 */
	public void setProfitCentersDecline(
		List<Integer>	profit_centers_decline
	) {
		ProfitCentersDecline.clear();
		if (profit_centers_decline == null)
			return;
		for (Integer profit_center : profit_centers_decline)
			ProfitCentersDecline.add(profit_center);
	}
}
