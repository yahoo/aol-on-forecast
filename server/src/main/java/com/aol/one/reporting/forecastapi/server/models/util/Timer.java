/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.models.util;

/**
 * Timer class mainly used to time code sections. Uses system nanoTime api
 * since it is the most accurate regardless of the OS platform.
 */
public final class Timer {
	private long	TimeStart;
	private long	TimeDelta;
	private boolean	IsRunning;

	/**
	 * Default constructor.
	 */
	public Timer() {
		TimeStart = 0;
		TimeDelta = 0;
		IsRunning = false;
	}

	/**
	 * Get millisecond unit timing of where we are currently in running timer
	 * or the last completed timing.
	 * 
	 * @return Current or last timing in milliseconds.
	 */
	public double getTimeMilliSeconds() {
		if (IsRunning)
			return (System.nanoTime()-TimeStart)/1000000.0;
		else
			return TimeDelta/1000000.0;
	}

	/**
	 * Get minute unit timing of where we are currently in running timer or
	 * the last completed timing.
	 * 
	 * @return Current or last timing in minutes.
	 */
	public double getTimeMinutes() {
		if (this.IsRunning)
			return (System.nanoTime()-TimeStart)/60000000000.0;
		else
			return TimeDelta/60000000000.0;
	}

	/**
	 * Get second unit timing of where we are currently in running timer or
	 * the last completed timing.
	 * 
	 * @return Current or last timing in seconds.
	 */
	public double getTimeSeconds() {
		if (this.IsRunning)
			return (System.nanoTime()-TimeStart)/1000000000.0;
		else
			return TimeDelta/1000000000.0;
	}

	/**
	 * Start timer.
	 * 
	 * @return This timer.
	 */
	public Timer start() {
		if (IsRunning)
			return this;
		TimeStart = System.nanoTime();
		IsRunning = true;
		return this;
	}

	/**
	 * Stop timer.
	 * 
	 * @return This timer.
	 */
	public Timer stop() {
		if (IsRunning) {
			TimeDelta = System.nanoTime() - TimeStart;
			IsRunning = false;
		}
		return this;
	}
}
