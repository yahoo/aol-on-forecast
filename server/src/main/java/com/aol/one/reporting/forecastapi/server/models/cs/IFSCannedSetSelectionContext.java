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
 * This class implements a canned set selection context. The context defines
 * the candidate canned sets, the series data, the competition hold back size,
 * a context id, and any nominal values that should be considered in making
 * the canned set selection.
 */
public final class IFSCannedSetSelectionContext {
	private SortedSet<IFSCannedSet>	CannedSetCandidates;
	private int						ID;
	private Integer					NumHoldback;
	private Integer					ProfitCenter;
	private double[]				Series;
	private double[]				SeriesCalibration;
	private double[]				SeriesHoldback;
	
	/**
	 * Default constructor.
	 */
	public IFSCannedSetSelectionContext() {
		CannedSetCandidates = new TreeSet<IFSCannedSet>();
		ID = 0;
		NumHoldback = null;
		ProfitCenter = null;
		Series = null;
		SeriesCalibration = null;
		SeriesHoldback = null;
	}
	
	/**
	 * Fetch the canned set candidates.
	 * 
	 * @return The canned set candidates.
	 * 
	 * @throws IFSException Thrown if canned set candidates have not been
	 *    previously specified.
	 */
	public SortedSet<IFSCannedSet> getCannedSetCandidates() throws IFSException {
		if (CannedSetCandidates.size() == 0)
			throw new IFSException(56, ID);
		return CannedSetCandidates;
	}
	
	/**
	 * Fetch context id.
	 * 
	 * @return Context id.
	 */
	public int getID() {
		return ID;
	}
	
	/**
	 * Fetch the hold back size.
	 * 
	 * @return The hold back size.
	 * 
	 * @throws IFSException Thrown if a hold back size has not been previously
	 *    specified.
	 */
	public int getNumHoldback() throws IFSException {
		if (NumHoldback == null)
			throw new IFSException(54, ID);
		return NumHoldback;
	}
	
	/**
	 * Fetch the profit center.
	 * 
	 * @return The profit center.
	 * 
	 * @throws IFSException Thrown if a profit center has not been previously
	 *    specified.
	 */
	public int getProfitCenter() throws IFSException {
		if (ProfitCenter == null)
			throw new IFSException(52, ID);
		return ProfitCenter;
	}
	
	/**
	 * Fetch the context series.
	 * 
	 * @return The context series.
	 * 
	 * @throws IFSException Thrown if a series has not been previously
	 *    specified.
	 */
	public double[] getSeries() throws IFSException {
		if (Series == null)
			throw new IFSException(51, ID);
		return Series;
	}
	
	/**
	 * Fetch the calibration series.
	 * 
	 * @return The calibration series.
	 * 
	 * @throws IFSException Thrown if a series has not been previously
	 *    specified.
	 */
	public double[] getSeriesCalibration() throws IFSException {
		if (SeriesCalibration != null)
			return SeriesCalibration;
		getSeries();
		return null;
	}
	
	/**
	 * Fetch the hold back series.
	 * 
	 * @return The hold back series.
	 * 
	 * @throws IFSException Thrown if a series has not been previously
	 *    specified.
	 */
	public double[] getSeriesHoldback() throws IFSException {
		if (SeriesHoldback != null)
			return SeriesHoldback;
		getSeries();
		return null;
	}
	
	/**
	 * Fetch the specified number of points off the end of the series.
	 * 
	 * @param num_points Number of points to take off the series end.
	 * 
	 * @return The specified number of end points or null if the series
	 *    does not have the requested number of points.
	 * 
	 * @throws IFSException Thrown if a series has not been previously
	 *    specified.
	 */
	public double[] getSeriesLast(
		int		num_points
	) throws IFSException {
		double[]	series = getSeries();
		double[]	last_points = null;
		int			last_idx = 0;
		
		if (series.length < num_points)
			return null;
		
		last_idx = series.length-num_points;
		last_points = new double[num_points];
		System.arraycopy(Series, last_idx, last_points, 0, num_points);

		return last_points;
	}
	
	/**
	 * Set the candidates to use in selecting a candidate canned set. Note
	 * that only one canned set with the same name is remembered.
	 * 
	 * @param canned_set_candidates Canned set candidates to use.
	 * 
	 * @throws IFSException Thrown if candidate list is null or empty.
	 */
	public void setCannedSetCandidates(
		List<IFSCannedSet>	canned_set_candidates
	) throws IFSException {
		if (canned_set_candidates == null || canned_set_candidates.size() == 0)
			throw new IFSException(57, ID);
		CannedSetCandidates.clear();
		for (IFSCannedSet canned_set : canned_set_candidates)
			CannedSetCandidates.add(canned_set);
	}
	
	/**
	 * Set context id.
	 * 
	 * @param id Context id.
	 */
	public void setID(
		int		id
	) {
		ID = id;
	}
	
	/**
	 * Set the profit center to use in selecting a candidate canned set.
	 * 
	 * @param profit_center Profit center to use.
	 * 
	 * @throws IFSException Thrown if profit center is less than one.
	 */
	public void setProfitCenter(
		int			profit_center
	) throws IFSException {
		if (profit_center < 1)
			throw new IFSException(53, ID);
		ProfitCenter = profit_center;
	}
	
	/**
	 * Set the series to use in selecting a candidate canned set. If a single
	 * point is provided the point is duplicated to make at least 2 points.
	 * Also the ideal number to holdback is specified. Internally this number
	 * may be adjusted. Use the {@link getNumHoldback()} method to determine
	 * the value actually used.
	 * 
	 * @param series Time series to use.
	 * @param num_holdback Hold back size to use.
	 * 
	 * @throws IFSException Thrown if series is null or less than 1 point or
	 *    if hold back size is less than one.
	 */
	public void setSeries(
		double[]	series,
		int			num_holdback
	) throws IFSException {
		if (series == null)
			throw new IFSException(49, ID);
		else if (series.length < 1)
			throw new IFSException(50, ID);
		else if (series.length == 1) {
			Series = new double[2];
			Series[0] = series[0];
			Series[1] = series[0];
		} else {
			Series = new double[series.length];
			System.arraycopy(series, 0, Series, 0, series.length);
		}

		if (num_holdback < 1)
			throw new IFSException(55, ID);
		else if (num_holdback >= Series.length/2)
			NumHoldback = Series.length/2;
		else
			NumHoldback = num_holdback;
		
		int			num_calibration = 0;
		
		num_calibration = Series.length-NumHoldback;
		SeriesCalibration = new double[num_calibration];
		System.arraycopy(Series, 0, SeriesCalibration, 0, num_calibration);
		
		SeriesHoldback = new double[NumHoldback];
		System.arraycopy(Series, num_calibration, SeriesHoldback, 0, NumHoldback);
	}
}
